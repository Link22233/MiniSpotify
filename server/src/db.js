import { DatabaseSync } from "node:sqlite";
import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const dataDir = path.join(__dirname, "..", "data");
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });

const dbPath = process.env.SQLITE_PATH || path.join(dataDir, "minispotify.db");
export const db = new DatabaseSync(dbPath);

db.exec("PRAGMA journal_mode = WAL;");
db.exec("PRAGMA foreign_keys = ON;");

db.exec(`
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email TEXT UNIQUE,
  password_hash TEXT,
  display_name TEXT,
  google_id TEXT UNIQUE,
  spotify_id TEXT UNIQUE,
  created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS playlists (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  remote_id TEXT NOT NULL,
  name TEXT NOT NULL,
  updated_at TEXT NOT NULL DEFAULT (datetime('now')),
  UNIQUE(user_id, remote_id)
);

CREATE INDEX IF NOT EXISTS idx_playlists_user_updated
  ON playlists(user_id, updated_at DESC);

CREATE TABLE IF NOT EXISTS playlist_tracks (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  playlist_id INTEGER NOT NULL REFERENCES playlists(id) ON DELETE CASCADE,
  track_remote_id TEXT NOT NULL,
  title TEXT NOT NULL,
  artist TEXT,
  duration_ms INTEGER,
  stream_url TEXT,
  position INTEGER NOT NULL DEFAULT 0,
  UNIQUE(playlist_id, track_remote_id)
);

CREATE INDEX IF NOT EXISTS idx_tracks_playlist_position
  ON playlist_tracks(playlist_id, position);
`);

/**
 * 模拟 better-sqlite3 的 db.transaction(fn)(...args)：
 * 同步事务，失败自动 ROLLBACK。
 */
export function transaction(fn) {
  return (...args) => {
    db.exec("BEGIN IMMEDIATE");
    try {
      const out = fn(...args);
      db.exec("COMMIT");
      return out;
    } catch (e) {
      try {
        db.exec("ROLLBACK");
      } catch (_) {
        /* ignore */
      }
      throw e;
    }
  };
}

/** Batch replace tracks for a playlist inside one transaction (fast path for sync). */
export function replacePlaylistTracks(playlistId, tracks) {
  const del = db.prepare(`DELETE FROM playlist_tracks WHERE playlist_id = ?`);
  const ins = db.prepare(`
    INSERT INTO playlist_tracks (playlist_id, track_remote_id, title, artist, duration_ms, stream_url, position)
    VALUES (@playlist_id, @track_remote_id, @title, @artist, @duration_ms, @stream_url, @position)
  `);
  const run = transaction((rows) => {
    del.run(playlistId);
    let pos = 0;
    for (const t of rows) {
      ins.run({
        playlist_id: playlistId,
        track_remote_id: t.remoteId,
        title: t.title,
        artist: t.artist ?? null,
        duration_ms: t.durationMs ?? null,
        stream_url: t.streamUrl ?? null,
        position: pos++,
      });
    }
  });
  run(tracks);
}
