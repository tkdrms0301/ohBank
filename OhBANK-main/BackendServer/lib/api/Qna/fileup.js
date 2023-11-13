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
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
    const santiziedFilename = uniqueSuffix + '-' + file.originalname.replace(/[^a-zA-Z0-9]/g, '_');
    cb(null, santiziedFilename);
  },
});

const upload = multer({ 
  storage: storage,
  limits: {
    fileSize: 1024 * 1024 * 5, // Limit file size to 5MB
  },
  fileFilter: (req, file, cb) => {
    //Check file extension
    const allowedExtensions = ['jpg','jpeg','png','pdf']; // List of allowed file extension

    const fileExtension = file.originalname.split('.').pop().toLowerCase();
    if (!allowedExtensions.includes(fileExtension)) {
      return cb(new Error('file extension not allowed.'));
    }
    cb(null, true);
  },
});

router.post("/", upload.single("file"), validateUserToken, (req, res) => {
  var r = new Response();
  let user_id = req.user_id;

  try {
    if (!req.file) {
      throw new Error('File not provided');
    }

    const filename = req.file.originalname;
    const savedname = req.file.destination + "/" + filename;
  
  Model.file.create({
      file_name: filename,
      saved_name: savedname,
      user_id: user_id,
    }).then((data) => {
      r.status = statusCodes.SUCCESS;
      r.data = data;
      return res.json(encryptResponse(r));
    }).catch((err) => {
      r.status = statusCodes.SERVER_ERROR;
      r.data = {
        message: err.toString(),
      };
      return res.json(encryptResponse(r));
    });
  } catch (err) {
    r.status = statusCodes.CLIENT_ERROR;
    r.data = {
      message: err.toString(),
    };
    return res.json(encryptResponse(r));
  }
});

module.exports = router;