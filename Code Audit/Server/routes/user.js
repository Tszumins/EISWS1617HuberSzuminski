var express = require('express');
var router = express.Router();
var app = require('../app');
var bodyParser = require('body-parser');
var jsonParser = bodyParser.json();
var redis = require('redis');
var db = redis.createClient();

router.get('/', function(req, res){

    res.send("Hier ist die User Seite");
    
    
    
});

//Benutzer Anlegen wenn vorhanden nicht mehr anlegen.
//Nutzer werden eindeutig durch die Android ID benannt.
router.post('/', jsonParser, function(req, res){
    var contentType = req.get('Content-Type');
    
    if (contentType != "application/json") {
        res.set("Accepts", "application/json").status(415).end();
    } else {
    
        var user = req.body;
        
        db.exists('user:'+user.androID , function(err, reply) {
            if (reply === 1) {
                console.log('Benutzer Existiert bereits');
                res.status(409).type('text').send('Der Benutzer mit der Android ID '+req.params.id+' existiert bereits!');
                
            } else {
                db.incr('id:user', function(err, rep){
        
                    var newUser = {
                        'AndroidClientID': user.androID,
                        'FCMID' : user.FCMID,
                        'Name': user.name,
                        'Adresse': user.adresse,
                        'Status': user.status
                    };
        
                    console.log("User wird angelegt mit Android ID : "+newUser.androID);
        
                    db.set('user:' + newUser.androID, JSON.stringify(newUser), function(err, rep){
                        res.json(newUser);
                    });
                });
            }
        });
    }
});

//Benutzer anzeigen über Android ID
router.get('/:id', function(req, res){
    
    db.get('user:'+req.params.id, function(err, rep){
        
        if (rep) {
			res.status(200).type('json').send(rep);
		}
		else{
			res.status(404).type('text').send('Der Benutzer mit der Android ID '+req.params.id+' existiert nicht');
		}
	});
});

//Alarmkontakte zu einem Benutzer hinzufügen
router.post('/:id/kontakt', jsonParser, function(req, res){
    
    var contentType = req.get('Content-Type');

    if (contentType != "application/json") {
        res.set("Accepts", "application/json").status(415).end();
    } else {
    
        var kontakt = req.body;
        
        db.incr('id:kontakt'+ req.params.id , function(err, rep){
        
        var newKontakt = {
            'AndroidClientID': kontakt.androID,
            'FCMID' : kontakt.FCMID,
            'Name': kontakt.name,
            'Nummer': kontakt.nummer,
            'Status': kontakt.status
        };
        
        newKontakt.id = rep;
        
        db.set('kontakt' + req.params.id + ':' + kontakt.androID , JSON.stringify(newKontakt), function(err, rep){
            console.log('Es wurde ein Alarm Kontakt für' + req.params.id + 'angelgt.');
            res.json(newKontakt);
        });
    });
    }
});

//Alle Alarmkontakte eines Benutzer ausgeben
router.get('/:id/kontakte', function(req, res){
    
    // ins rep alle Kontakte schreiben
    db.keys('kontakt' + req.params.id + ':*', function(err, rep){

        if(rep != 0){
        console.log(rep);
        }

        else{
        res.status(404).type('text').send('Es existieren keine Alarmkontakte in der Datenbank');
        }
   
    // 
    db.mget(rep, function(err, rep) { 
            
            var kontakte = [];
            
            rep.forEach(function(val){
                kontakte.push(JSON.parse(val));
            });
            
            
            kontakte = kontakte.map(function(kontakte) {
                return { id: kontakte.id, kAndroID: kontakte.kAndroID, name: kontakte.name, nummer: kontakte.nummer
                };
            });
            
            res.json(kontakte);
        });
    });

});

//Gibt einen bestimmten Alarmkontakt eines Bestimmten User aus
//router.get('/:id/kontakt/:kid', function(req, res){    });

//Ändert die Daten eines bestimmten Alarmkontaktes eines bestimmten User
//router.put('/:id/kontakt/:kid', function(req, res){   });

//Löscht einen bestimmten Alarmkontakt eines bestimmten User
//router.delete('/:id/kontakt/:kid', function(req, res){   });


//Einen Alarm alarm einem bestimmten User zuweisen und verteilen
router.post('/:id/alarm/', jsonParser, function(req, res){
        
 var contentType = req.get('Content-Type');

    if (contentType != "application/json") {
        res.set("Accepts", "application/json").status(415).end();
    } else {
    
    var alarm = req.body;
        
    db.incr('id:alarm', function(err, rep){
        
        var newAlarm = {
            'id': rep,
            'datum': alarm.datum,
            'gpsLocation': alarm.gpsLocation,
            'status': alarm.status
        };
        
        newAlarm.id = rep;
        
        console.log("Alarm bei Android ID : "+newAlarm.androID);
        
        db.set('alarm:' + newAlarm.androID, JSON.stringify(newAlarm), function(err, rep){

            res.json(newAlarm);
            
        });
    });
        
    }
});

//Gibt alle Alarme eines bestimmten User aus
router.get('/:id/alarme/', function(req, res){
    
});

//Gibt einen bestimmten Alarm eines Bestimmten Users aus
//router.get('/:id/alarm/:aid', function(req, res){   });

//Bearbeiten oder Updaten von einem bestimmten Alarm eines Bestimmten Users
//router.put('/:id/alarm/:aid', function(req, res){  });

//Löschen eines bestimmten ALarm eines bestimmten Users
//router.delete('/:id/alarm/:aid', function(req, res){   });




module.exports = router;