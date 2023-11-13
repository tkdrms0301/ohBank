var express = require("express");
var router = express.Router();
var Model = require("../../../models/index");
var Response = require("../../Response");
var statusCodes = require("../../statusCodes");
const multer = require("multer");
var { validateUserToken } = require("../../../middlewares/validateToken");
var { encryptResponse, decryptRequest } = require("../../../middlewares/crypt");

/**
 * QnA file list route
 * This endpoint allows to view list of files of a question
 * @path                             - /api/qna/list
 * @middleware
 * @param file
 * @param qna_id
 * @return                           - Qna list
 */

// const bodyParser = require('body-parser');
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, req.body.path);
  },
  filename: function (req, file, cb) {
    // Create safe file names
    const uniqueSuffix = Date.now() + "-" + Math.round(Math.random() * 1e9);
    const santiziedFilename =
      uniqueSuffix + "-" + file.originalname.replace(/[^a-zA-Z0-9]/g, "_");
    cb(null, santiziedFilename);
  },
});

const upload = multer({
  storage: storage,
  limits: {
    fileSize: 1024 * 1024 * 5, // Limit file size to 5MB
  },
});

router.post("/", upload.single("file"), validateUserToken, (req, res) => {
  var r = new Response();
  let user_id = req.user_id;
  var filename = req.file.originalname;
  var savedname = req.file.destination + "/" + filename;

  const allowedExtensions = ["jpg", "jpeg", "png", "pdf"]; // List of allowed file extension

  const fileNameParts = req.file.originalname.split(".");
  const fileExtension = fileNameParts[fileNameParts.length - 1].toLowerCase();

  if (!allowedExtensions.includes(fileExtension)) {
    r.status = statusCodes.BAD_INPUT;
    r.data = {
      message: "invalid file extension",
    };
    return res.json(encryptResponse(r));
  }

  Model.file
    .create({
      file_name: filename,
      saved_name: savedname,
      user_id: user_id,
    })
    .then((data) => {
      r.status = statusCodes.SUCCESS;
      r.data = data;
      return res.json(encryptResponse(r));
    })
    .catch((err) => {
      r.status = statusCodes.SERVER_ERROR;
      r.data = {
        message: "server error",
      };
      return res.json(encryptResponse(r));
    });
});

module.exports = router;
