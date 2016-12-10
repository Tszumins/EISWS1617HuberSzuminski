var express = require('express');
var router = express.Router();
var app = require('../app');
var bodyParser = require('body-parser');
var jsonParser = bodyParser.json();
var redis = require('redis');
var db = redis.createClient();

//Firebased Cloud Messaging 
var FCM = require('fcm').FCM;

router.get('/', function(req, res){
    console.log("Versuch zu POSTEN!")
    res.send("Alarm Client ID + Date");
    
});

module.exports = router;