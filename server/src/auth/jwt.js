import jwt from "jsonwebtoken";

const secret = process.env.JWT_SECRET || "dev-only-change-me";
const expiresIn = process.env.JWT_EXPIRES || "7d";

export function signToken(payload) {
  return jwt.sign(payload, secret, { expiresIn });
}

export function verifyToken(token) {
  return jwt.verify(token, secret);
}
