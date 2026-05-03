import "dotenv/config";
import express from "express";
import cors from "cors";
import helmet from "helmet";
import morgan from "morgan";
import rateLimit from "express-rate-limit";
import passport from "passport";
import authRoutes from "./routes/auth.js";
import playlistRoutes from "./routes/playlists.js";
import recoRoutes from "./routes/recommendations.js";
import "./db.js";
import { attachOAuthStrategies } from "./auth/passport.js";

const app = express();
const port = Number(process.env.PORT || 3000);

app.use(helmet());
app.use(cors({ origin: true, credentials: true }));
app.use(express.json({ limit: "2mb" }));
app.use(morgan("tiny"));
app.use(passport.initialize());

attachOAuthStrategies(app);

const limiter = rateLimit({
  windowMs: 60 * 1000,
  max: 500,
  standardHeaders: true,
  legacyHeaders: false,
});
app.use("/api/", limiter);

app.get("/health", (_req, res) => res.json({ ok: true, service: "minispotify-api" }));

app.use("/api/v1/auth", authRoutes);
app.use("/api/v1/playlists", playlistRoutes);
app.use("/api/v1/recommendations", recoRoutes);

app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: "internal_error" });
});

app.listen(port, () => {
  console.log(`Mini-Spotify API listening on http://localhost:${port}`);
});

export default app;
