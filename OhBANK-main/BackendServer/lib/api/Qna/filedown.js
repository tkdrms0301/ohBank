var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse } = require("../../../middlewares/crypt");
var FormData = require("form-data");
const Readable = require("stream").Readable;
/**
 * QnA file list route
 * This endpoint allows to view list of files of a question
 * @path                             - /api/qna/list
 * @middleware
 * @param file
 * @return                           - Qna
 */
var fs = require("fs");
const { response } = require("express");

router.get("/", async (req, res) => {
  var r = new Response();
  var filename = req.query.filename;

  try {
    // upload directory in the path is only allowed
    if (!filename.startsWith("upload/")) {
      r.status = statusCodes.BAD_REQUEST;
      r.data = {
        message: "Invalid file path.",
      };
      return res.json(encryptResponse(r));
    }

    file_data = fs.readFileSync(filename);
    s = new Readable();

    filename = filename.split("/");
    filename = filename[filename.length - 1];
    res.attachment(filename);

    s.push(file_data);
    s.push(null);
    s.pipe(res);
  } catch (err) {
    r.status = statusCodes.SERVER_ERROR;
    r.data = {
      message: err.toString(),
      stack: err.stack,
    };
    return res.json(encryptResponse(r));
  }
});

module.exports = router;
