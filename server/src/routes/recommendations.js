import { Router } from "express";
import passport from "passport";

const r = Router();
r.use(passport.authenticate("jwt", { session: false }));

/** Lightweight personalized feed (placeholder: blend favorites + trending). */
r.get("/", (req, res) => {
  const userId = req.user.id;
  res.json({
    userId,
    sections: [
      {
        title: "为你推荐",
        tracks: [
          { remoteId: "rec-1", title: "示例曲目 A", artist: "Mini-Spotify", durationMs: 210000 },
          { remoteId: "rec-2", title: "示例曲目 B", artist: "Mini-Spotify", durationMs: 195000 },
        ],
      },
    ],
    generatedAt: new Date().toISOString(),
  });
});

export default r;
