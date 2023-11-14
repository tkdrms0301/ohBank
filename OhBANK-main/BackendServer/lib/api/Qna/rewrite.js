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
} = require("../../../middlewares/crypt");

router.post("/", validateUserToken, decryptAuthRequest, (req, res) => {
  var r = new Response();
  var today = new Date();
  var now =
    today.getFullYear() + "-" + (today.getMonth() + 1) + "-" + today.getDate();

  if (req.body.file_id_list) {
    Model.qna
      .findOne({
        where: {
          id: req.body.qna_id,
        },
        attributes: ["writer_id"],
      })
      .then((data) => {
        if (data.dataValues.writer_id === req.user_id) {
          // writer_id validation
          Model.qna
            .update(
              {
                title: req.body.title,
                content: req.body.content,
                write_at: now,
              },
              {
                where: {
                  id: req.body.qna_id,
                  writer_id: req.user_id, // broken access control
                },
              }
            )
            .then((data) => {
              r.status = statusCodes.SUCCESS;
              r.data = data;

              for (var i = 0; i < req.body.file_id_list.length; i++) {
                Model.file.update(
                  {
                    qna_id: req.body.qna_id,
                  },
                  {
                    where: {
                      id: req.body.file_id_list[i],
                    },
                  }
                );
              }
              return res.json(encryptResponse(r));
            })
            .catch((err) => {
              r.status = statusCodes.BAD_INPUT;
              r.data = {
                message: "invalid input",
              };
              return res.json(encryptResponse(r));
            });
        } else {
          r.status = statusCodes.BAD_REQUEST;
          r.data = {
            message: "invalid input",
          };
          return res.json(encryptResponse(r));
        }
      });
  } else {
    Model.qna
      .findOne({
        where: {
          id: req.body.qna_id,
        },
        attributes: ["writer_id"],
      })
      .then((data) => {
        if (data.dataValues.writer_id === req.user_id) {
          Model.qna
            .update(
              {
                title: req.body.title,
                content: req.body.content,
                write_at: now,
              },
              {
                where: {
                  id: req.body.qna_id,
                  writer_id: req.user_id, // broken access control
                },
              }
            )
            .then((data) => {
              r.status = statusCodes.SUCCESS;
              r.data = data;
              return res.json(encryptResponse(r));
            })
            .catch((err) => {
              r.status = statusCodes.BAD_INPUT;
              r.data = {
                message: "invalid input",
              };
              return res.json(encryptResponse(r));
            });
        } else {
          r.status = statusCodes.BAD_REQUEST;
          r.data = {
            message: "invalid input",
          };
          return res.json(encryptResponse(r));
        }
      });
  }
});

module.exports = router;
