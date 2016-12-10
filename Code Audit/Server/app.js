var http = require('http');
var express = require('express');
var app = express();
var bodyParser = require('body-parser');
var jsonParser = bodyParser.json();
var httpServer = require('http').Server(app);
var fs = require('fs');

var cors = require('cors');
var port = 81;

//var alarm = require('./routes/alarm');
//app.use('/alarm', alarm);

var user = require('./routes/user');
app.use('/user', user);

//var liveview = require('./routes/liveview');
//app.use('/liveview', liveview);


app.get('/download', function(req, res){

  var file = fs.readFileSync(__dirname + '/Server.rar', 'binary');

  res.setHeader('Content-Length', file.length);
  res.write(file, 'binary');
  res.end();
});



httpServer.listen(port);
module.exports = app;