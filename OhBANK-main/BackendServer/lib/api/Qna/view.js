var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");
const { Op, literal } = require("sequelize");

/**
 * QnA file list route
 * This endpoint allows to view list of files of a question
 * @path                             - /api/qna/list
 * @middleware
 * @param                            - qna_id
 * @return                           - Qna list
 */
router.post("/", validateUserToken, decryptRequest, (req, res) => {
  var r = new Response();
  let qna_id = req.body.qna_id;

  // SQL injection prevention
  if (!/^\d+$/.test(qna_id)) {
    r.status = statusCodes.BAD_INPUT;
    r.data = {
      message: "invalid input",
    };
    return res.json(encryptResponse(r));
  }

  Model.qna
    .findOne({
      where: {
        id: qna_id, // Sequelize operator to prevent SQL injection
        writer_id: req.user_id, // broken access control
      },
      attributes: ["title", "content", "write_at"],
    })
    .then((data) => {
      if (data == null) {
        r.status = statusCodes.BAD_INPUT;
        r.data = {
          message: "invalid input",
        };
        return res.json(encryptResponse(r));
      }

      r.status = statusCodes.SUCCESS;
      r.data = data;

      Model.file
        .findAll({
          where: {
            qna_id: qna_id,
          },
          attributes: ["id", "file_name"],
        })
        .then((file_data) => {
          r.data.dataValues.file = file_data;
          return res.json(encryptResponse(r));
        })
        .catch((err) => {
          r.status = statusCodes.BAD_INPUT;
          r.data = {
            message: "invalid input",
          };
          return res.json(encryptResponse(r));
        });
    })
    .catch((err) => {
      r.status = statusCodes.BAD_INPUT;
      r.data = {
        message: "invalid input",
      };
      return res.json(encryptResponse(r));
    });
});

module.exports = router;
