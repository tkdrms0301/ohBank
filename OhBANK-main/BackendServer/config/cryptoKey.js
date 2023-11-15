const crypto = require("crypto");

const seed = "dkstkdrmstjdtpruddldbwjd";

function generateKeyFromSeed(seed) {
  return crypto.createHash("sha256").update(seed).digest();
}

function generateIVFromSeed(seed) {
  const seedBuffer = Buffer.from(seed);
  const iv = Buffer.alloc(16);
  seedBuffer.copy(iv, 0, 0, Math.min(seedBuffer.length, iv.length));
  return iv;
}

module.exports = {
  ENC_KEY: generateKeyFromSeed(seed),
  IV: generateIVFromSeed(seed),
};