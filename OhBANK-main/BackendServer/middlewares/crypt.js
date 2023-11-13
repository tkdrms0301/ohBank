const Model = require("../models/index");
const Response = require("../lib/Response");
const statusCodes = require("../lib/statusCodes");
const jwt = require("jsonwebtoken");
const { secretKey } = require("../config/jwtTokenSecret");
const { ENC_KEY, IV } = require("../config/cryptoKey");
const crypto = require("crypto");

const decrypt = (encrypted) => {
  let decipher = crypto.createDecipheriv("aes-256-cbc", ENC_KEY, IV);
  let decrypted = decipher.update(encrypted, "base64", "utf8");
  return decrypted + decipher.final("utf8");
};

const encrypt = (val) => {
  let cipher = crypto.createCipheriv("aes-256-cbc", ENC_KEY, IV);
  let encrypted = cipher.update(val, "utf8", "base64");
  encrypted += cipher.final("base64");
  return encrypted;
};

/**
 * Encryption middleware
 * This middleware encrypts server response before forwarding to client
 * @return                           - Calls the next function on success
 */
const encryptResponse = (input) => {
  let b64 = encrypt(input);
  return {
    enc_data: b64,
  };
};

/**
 * Decryption middleware
 * This middleware decrypts user data after authorization check
 * @return                           - Calls the next function on success
 */
const decryptRequest = function (req, res, next) {
  var r = new Response();
  try {
    req.body = JSON.parse(decrypt(req.body.enc_data));
    next();
  } catch (err) {
    r.status = statusCodes.BAD_INPUT;
    r.data = err;
    return res.json(r);
  }
};

/**
 * Decryption middleware
 * This middleware decrypts user data after authorization check
 * @return                           - Calls the next function on success
 */
const decryptAuthRequest = function (req, res, next) {
  var r = new Response();
  try {
    req.body = JSON.parse(decrypt(req.body.enc_data));
    // next();
  } catch (err) {
    r.status = statusCodes.BAD_INPUT;
    r.data = err;
    return res.json(r);
  }

  const authHeader = req.headers["authorization"];
  const token = authHeader && authHeader.split(" ")[1];

  if (token == null) {
    r.status = statusCodes.NOT_AUTHORIZED;
    r.data = {
      message: "Not authorized",
    };
    return res.json(encryptResponse(r));
  }

  jwt.verify(token, secretKey, (err, data) => {
    if (err) {
      r.status = statusCodes.FORBIDDEN;
      r.data = {
        message: "Invalid token",
      };
      return res.json(encryptResponse(r));
    }

    Model.users
      .findOne({
        where: {
          username: data.username,
        },
        attributes: ["id", "username", "account_number"],
      })
      .then((data) => {
        req.account_number = data.account_number;
        req.username = data.username;
        req.user_id = data.id;
        next();
      })
      .catch((err) => {
        r.status = statusCodes.SERVER_ERROR;
        r.data = {
          message: "Invalid account",
        };
        return res.json(encryptResponse(r));
      });
  });
};

module.exports = {
  encryptResponse,
  decryptRequest,
  decryptAuthRequest,
  decrypt,
};
