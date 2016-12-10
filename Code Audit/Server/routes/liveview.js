var express = require('express');
var router = express.Router();
var app = require('../app');
var bodyParser = require('body-parser');
var jsonParser = bodyParser.json();

router.get('/', function(req, res){

    res.send("Liveview anzeigen ueber ID");
    
});

module.exports = router;