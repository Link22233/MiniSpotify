import { Router } from "express";
import bcrypt from "bcryptjs";
import passport from "passport";
import { db } from "../db.js";
import { signToken } from "../auth/jwt.js";
import { issueJwtForUser } from "../auth/passport.js";

const r = Router();

r.post("/register", (req, res) => {
  const { email, password, displayName } = req.body || {};
  if (!email || !password) return res.status(400).json({ error: "email and password required" });
  const exists = db.prepare(`SELECT id FROM users WHERE email = ?`).get(email);
  if (exists) return res.status(409).json({ error: "email already registered" });
  const hash = bcrypt.hashSync(password, 10);
  const ins = db
    .prepare(`INSERT INTO users (email, password_hash, display_name) VALUES (?, ?, ?)`)
    .run(email, hash, displayName || null);
  const user = db.prepare(`SELECT id, email, display_name FROM users WHERE id = ?`).get(ins.lastInsertRowid);
  const token = signToken({ sub: user.id, email: user.email });
  res.status(201).json({
    token,
    user: { id: user.id, email: user.email, displayName: user.display_name },
  });
});

r.post("/login", (req, res) => {
  const { email, password } = req.body || {};
  if (!email || !password) return res.status(400).json({ error: "email and password required" });
  const user = db.prepare(`SELECT * FROM users WHERE email = ?`).get(email);
  if (!user || !user.password_hash) return res.status(401).json({ error: "invalid credentials" });
  if (!bcrypt.compareSync(password, user.password_hash)) return res.status(401).json({ error: "invalid credentials" });
  const token = signToken({ sub: user.id, email: user.email });
  res.json({
    token,
    user: { id: user.id, email: user.email, displayName: user.display_name },
  });
});

r.get("/google", (req, res, next) => {
  if (!process.env.GOOGLE_CLIENT_ID) return res.status(503).json({ error: "Google OAuth not configured" });
  passport.authenticate("google", { scope: ["profile", "email"], session: false })(req, res, next);
});

r.get(
  "/google/callback",
  passport.authenticate("google", { session: false, failureRedirect: "/api/v1/auth/oauth-failure" }),
  (req, res) => {
    const token = issueJwtForUser(req.user);
    res.json({ token, user: { id: req.user.id, email: req.user.email, displayName: req.user.display_name } });
  }
);

r.get("/spotify", (req, res, next) => {
  if (!process.env.SPOTIFY_CLIENT_ID) return res.status(503).json({ error: "Spotify OAuth not configured" });
  passport.authenticate("spotify", {
    scope: ["user-read-email", "user-read-private"],
    session: false,
    showDialog: true,
  })(req, res, next);
});

r.get(
  "/spotify/callback",
  passport.authenticate("spotify", { session: false, failureRedirect: "/api/v1/auth/oauth-failure" }),
  (req, res) => {
    const token = issueJwtForUser(req.user);
    res.json({ token, user: { id: req.user.id, email: req.user.email, displayName: req.user.display_name } });
  }
);

r.get("/oauth-failure", (_req, res) => {
  res.status(401).json({ error: "oauth_failed" });
});

export default r;
