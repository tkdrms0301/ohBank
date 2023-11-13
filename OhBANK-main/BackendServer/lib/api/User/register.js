var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

// Function to check if the password meets the criteria
function isStrongPassword(password) {
  // Check if the password contains at least two types of characters
  const typesCount = ["uppercase", "lowercase", "special", "number"].filter(
    (type) => {
      if (type === "uppercase" && /[A-Z]/.test(password)) return true;
      if (type === "lowercase" && /[a-z]/.test(password)) return true;
      if (type === "special" && /[!@#$%^&*(),.?":{}|<>]/.test(password))
        return true;
      if (type === "number" && /[0-9]/.test(password)) return true;
      return false;
    }
  ).length;

  // Check if the password meets the minimum length requirement
  const isLengthValid = password.length >= 8;

  return typesCount >= 2 && isLengthValid;
}

/**
 * Registration route
 * This endpoint allows the user to register
 * Additionally this also creates a new account for this user
 * @path                             - /api/user/register
 * @middleware                       - Checks admin authorization
 * @param username                   - Username to login
 * @param password                   - Password to login
 * @return                           - Status
 */
router.post("/", decryptRequest, (req, res) => {
  var r = new Response();
  let username = req.body.username;
  let password = req.body.password;
  let account_number = Math.random() * 888888 + 111111;

  // Validate the password
  if (!isStrongPassword(password)) {
    r.status = statusCodes.BAD_INPUT;
    r.data = {
      message: "Password does not meet the criteria",
    };
    return res.json(encryptResponse(r));
  }

  Model.users
    .findAll({
      where: {
        username: username,
      },
    })
    .then((data) => {
      if (data == "") {
        Model.users
          .findOne({
            account_number: account_number,
          })
          .then((data) => {
            // Regenerates new account number if account number exists
            if (data) {
              account_number = Math.random() * 888888 + 111111;
            }

            Model.users
              .create({
                username: username,
                password: password,
                account_number: account_number,
              })
              .then(() => {
                r.status = statusCodes.SUCCESS;
                r.data = {
                  message: "Sucess",
                };
                res.json(encryptResponse(r));
              })
              .catch((err) => {
                r.status = statusCodes.SERVER_ERROR;
                r.data = {
                  message: err.toString(),
                };
                res.json(encryptResponse(r));
              });
          });
      } else {
        r.status = statusCodes.BAD_INPUT;
        r.data = {
          message: "Username already taken",
        };
        return res.json(encryptResponse(r));
      }
    })
    .catch((err) => {
      r.status = statusCodes.SERVER_ERROR;
      r.data = {
        message: err.toString(),
      };
      return res.json(encryptResponse(r));
    });
});

module.exports = router;
