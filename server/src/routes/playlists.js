import { Router } from "express";
import passport from "passport";
import { db, replacePlaylistTracks, transaction } from "../db.js";

const r = Router();

const requireUser = passport.authenticate("jwt", { session: false });

r.use(requireUser);

/** Full pull: all playlists + tracks for current user (indexed queries). */
r.get("/", (req, res) => {
  const userId = req.user.id;
  const playlists = db
    .prepare(
      `SELECT id, remote_id AS remoteId, name, updated_at AS updatedAt FROM playlists WHERE user_id = ? ORDER BY updated_at DESC`
    )
    .all(userId);

  const trackStmt = db.prepare(
    `SELECT track_remote_id AS remoteId, title, artist, duration_ms AS durationMs, stream_url AS streamUrl, position
     FROM playlist_tracks WHERE playlist_id = ? ORDER BY position ASC`
  );

  const out = playlists.map((p) => ({
    ...p,
    tracks: trackStmt.all(p.id),
  }));
  res.json({ playlists: out });
});

/**
 * Idempotent sync: upsert playlists and batch-replace tracks in transactions.
 * Designed for mobile offline-first merge (client sends authoritative snapshot or delta).
 */
r.put("/sync", (req, res) => {
  const userId = req.user.id;
  const { playlists } = req.body || {};
  if (!Array.isArray(playlists)) return res.status(400).json({ error: "playlists array required" });

  const upsertPlaylist = db.prepare(`
    INSERT INTO playlists (user_id, remote_id, name, updated_at)
    VALUES (?, ?, ?, datetime('now'))
    ON CONFLICT(user_id, remote_id) DO UPDATE SET
      name = excluded.name,
      updated_at = datetime('now')
    RETURNING id
  `);

  const tx = transaction((items) => {
    const ids = [];
    for (const pl of items) {
      if (!pl.remoteId || !pl.name) continue;
      const row = upsertPlaylist.get(userId, pl.remoteId, pl.name);
      const pid =
        row?.id ??
        db.prepare(`SELECT id FROM playlists WHERE user_id = ? AND remote_id = ?`).get(userId, pl.remoteId).id;
      const tracks = Array.isArray(pl.tracks) ? pl.tracks : [];
      replacePlaylistTracks(
        pid,
        tracks.map((t) => ({
          remoteId: t.remoteId,
          title: t.title || "Untitled",
          artist: t.artist,
          durationMs: t.durationMs,
          streamUrl: t.streamUrl,
        }))
      );
      ids.push(pid);
    }
    return ids;
  });

  try {
    tx(playlists);
    res.json({ ok: true, synced: playlists.length });
  } catch (e) {
    console.error(e);
    res.status(500).json({ error: "sync_failed" });
  }
});

export default r;
