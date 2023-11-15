var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

/**
 * QnA file list route
 * This endpoint allows to view list of files of a question
 * @path                             - /api/qna/list
 * @middleware
 * @return                           - Qna list
 */
router.post("/", validateUserToken, decryptRequest, (req, res) => {
  var r = new Response();

  Model.users
    .findOne({
      where: {
        username: req.username,
        password: req.body.password,
      },
    })
    .then((data) => {
      if (data) {
        r.status = statusCodes.SUCCESS;
        r.data = {
          message: "success",
        };
        return res.json(encryptResponse(r));
      } else {
        r.status = statusCodes.BAD_INPUT;
        r.data = {
          message: "Incorrect username or password",
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
