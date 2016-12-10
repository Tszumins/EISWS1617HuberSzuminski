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

router.post('/', jsonParser, function(req, res){
    
    var contentType = req.get('Content-Type');

    if (contentType != "application/json") {
        res.set("Accepts", "application/json").status(415).end();
    } else {

    
    
    var alarm = req.body;
        
    db.incr('id:alarm', function(err, rep){
        
        var newAlarm = {
            'id': rep,
            'androID': alarm.androID,
            'datum': alarm.datum,
            'gpsLocation': alarm.gpsLocation
        };
        
        newAlarm.id = rep;
        console.log("Alarm bei Android ID : "+newAlarm.androID);
        
        db.set('alarm:' + newAlarm.androID, JSON.stringify(newAlarm), function(err, rep){

            res.json(newAlarm);
        });
    });
        
    // Hier Alarmversenden mit GCM
        
        var apiKey = 'AIzaSyDkeYS-xlFMX2dPGHW5ffXNDR5eugWKg40';
        var fcm = new FCM(apiKey);

        var message = {
            registration_id: 'Device registration id', // required
            collapse_key: 'Collapse key', 
            'data.key1': 'value1',
            'data.key2': 'value2'
        };

        fcm.send(message, function(err, messageId){

            if (err) {
                console.log("Something has gone wrong!");
            } else {
                console.log("Sent with message ID: ", messageId);
            }
        });

    }
});

module.exports = router;