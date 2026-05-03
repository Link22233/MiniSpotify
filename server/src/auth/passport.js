import passport from "passport";
import { Strategy as JwtStrategy, ExtractJwt } from "passport-jwt";
import { Strategy as GoogleStrategy } from "passport-google-oauth20";
import { Strategy as SpotifyStrategy } from "passport-spotify";
import { db } from "../db.js";
import { signToken } from "./jwt.js";

const jwtOpts = {
  jwtFromRequest: ExtractJwt.fromAuthHeaderAsBearerToken(),
  secretOrKey: process.env.JWT_SECRET || "dev-only-change-me",
};

passport.use(
  new JwtStrategy(jwtOpts, (payload, done) => {
    const row = db.prepare(`SELECT id, email, display_name FROM users WHERE id = ?`).get(payload.sub);
    if (!row) return done(null, false);
    return done(null, row);
  })
);

function upsertOAuthUser({ provider, profile }) {
  const email = profile.emails?.[0]?.value || `${provider}_${profile.id}@oauth.local`;
  const display =
    profile.displayName || profile.username || email.split("@")[0];

  if (provider === "google") {
    let u = db.prepare(`SELECT * FROM users WHERE google_id = ?`).get(profile.id);
    if (u) return u;
    u = db.prepare(`SELECT * FROM users WHERE email = ?`).get(email);
    if (u) {
      db.prepare(`UPDATE users SET google_id = ?, display_name = COALESCE(display_name, ?) WHERE id = ?`).run(
        profile.id,
        display,
        u.id
      );
      return db.prepare(`SELECT * FROM users WHERE id = ?`).get(u.id);
    }
    const r = db
      .prepare(`INSERT INTO users (email, password_hash, display_name, google_id) VALUES (?, NULL, ?, ?)`)
      .run(email, display, profile.id);
    return db.prepare(`SELECT * FROM users WHERE id = ?`).get(r.lastInsertRowid);
  }

  if (provider === "spotify") {
    let u = db.prepare(`SELECT * FROM users WHERE spotify_id = ?`).get(profile.id);
    if (u) return u;
    const spotEmail = profile._json?.email || email;
    u = db.prepare(`SELECT * FROM users WHERE email = ?`).get(spotEmail);
    if (u) {
      db.prepare(`UPDATE users SET spotify_id = ?, display_name = COALESCE(display_name, ?) WHERE id = ?`).run(
        profile.id,
        display,
        u.id
      );
      return db.prepare(`SELECT * FROM users WHERE id = ?`).get(u.id);
    }
    const r = db
      .prepare(`INSERT INTO users (email, password_hash, display_name, spotify_id) VALUES (?, NULL, ?, ?)`)
      .run(spotEmail, display, profile.id);
    return db.prepare(`SELECT * FROM users WHERE id = ?`).get(r.lastInsertRowid);
  }
  return null;
}

export function attachOAuthStrategies(app) {
  const base = process.env.PUBLIC_BASE_URL || `http://localhost:${process.env.PORT || 3000}`;

  if (process.env.GOOGLE_CLIENT_ID && process.env.GOOGLE_CLIENT_SECRET) {
    passport.use(
      new GoogleStrategy(
        {
          clientID: process.env.GOOGLE_CLIENT_ID,
          clientSecret: process.env.GOOGLE_CLIENT_SECRET,
          callbackURL: process.env.GOOGLE_CALLBACK_URL || `${base}/api/v1/auth/google/callback`,
        },
        (_accessToken, _refreshToken, profile, done) => {
          try {
            const user = upsertOAuthUser({ provider: "google", profile });
            done(null, user);
          } catch (e) {
            done(e);
          }
        }
      )
    );
  }

  if (process.env.SPOTIFY_CLIENT_ID && process.env.SPOTIFY_CLIENT_SECRET) {
    passport.use(
      new SpotifyStrategy(
        {
          clientID: process.env.SPOTIFY_CLIENT_ID,
          clientSecret: process.env.SPOTIFY_CLIENT_SECRET,
          callbackURL: process.env.SPOTIFY_CALLBACK_URL || `${base}/api/v1/auth/spotify/callback`,
        },
        (_accessToken, _refreshToken, _expires_in, profile, done) => {
          try {
            const user = upsertOAuthUser({ provider: "spotify", profile });
            done(null, user);
          } catch (e) {
            done(e);
          }
        }
      )
    );
  }
}

export function issueJwtForUser(user) {
  return signToken({ sub: user.id, email: user.email });
}

export { passport };
