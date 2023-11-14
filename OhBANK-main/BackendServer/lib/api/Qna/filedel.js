var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { validateUserToken } = require("../../../middlewares/validateToken");
var {
  encryptResponse,
  decryptRequest,
  decryptAuthRequest,
  decrypt,
} = require("../../../middlewares/crypt");
var FormData = require("form-data");
const Readable = require("stream").Readable;
/**
 * QnA file list route
 * This endpoint allows to view list of files of a question
 * @path                             - /api/qna/list
 * @middleware
 * @param file_id
 * @return                           - Qna
 */
var fs = require("fs");
const { response } = require("express");

router.post("/", decryptAuthRequest, async (req, res) => {
  var r = new Response();
  Model.file
    .findOne({
      where: {
        id: req.body.file_id,
      },
      attributes: ["file_name", "saved_name"],
    })
    .then(async (data) => {
      Model.file
        .destroy({
          where: {
            id: req.body.file_id,
          },
        })
        .then((data) => {
          r.status = statusCodes.SUCCESS;
          return res.json(encryptResponse(r));
        })
        .catch((err) => {
          r.status = statusCodes.SERVER_ERROR;
          r.data = {
            message: err.toString(),
          };
          return res.json(encryptResponse(r));
        });
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
