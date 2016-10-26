var express = require('express');
var bodyParser = require('body-parser');
var app = express();


app.get("/home", function(req, res){
    res.send("51469 Bergisch Gladbach");
});

var server = app.listen(1337, function(){
   console.log("Der Server ist laeuft.");
});