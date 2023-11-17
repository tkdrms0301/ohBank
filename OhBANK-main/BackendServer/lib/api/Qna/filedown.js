var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse } = require("../../../middlewares/crypt");
var FormData = require("form-data");
/**
 * QnA file list route
 * This endpoint allows to view list of files of a question
 * @path                             - /api/qna/list
 * @middleware
 * @param file
 * @return                           - Qna
 */
const fs = require("fs");
const path = require("path");
const { Readable } = require("stream").Readable;

router.get("/", async (req, res) => {
  const r = new Response();
  const requestedFilename = req.query.filename;

  try {
    // 파일 경로 정규화
    const filename = path.normalize(requestedFilename);

    // 정규화된 디렉토리 확인
    if (!filename.startsWith("upload" + path.sep)) {
      r.status = statusCodes.BAD_REQUEST;
      r.data = {
        message: "Invalid file path.",
      };
      return res.json(encryptResponse(r));
    }

    const fileData = fs.readFileSync(filename);
    const s = new Readable();

    // 파일 이름 추출
    const sanitizedFilename = path.basename(filename);
    res.attachment(sanitizedFilename);

    s.push(fileData);
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
