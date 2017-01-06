var express = require('express');
var bodyParser = require('body-parser');
var jsonParser = bodyParser.json();
var app = express();
var paperwork = require('paperwork');


var jsonuseraccept = {
    'username': String,
    'androidID': String,
    'fcmID': String,
    'vorname': String,
    'nachname': String,
    'telefonnummer': Number,
    'status': String,
    }

var jsonuserAIDaccept ={
    'androidID': String,
    'username': String
}

var jsoncontactsaccept ={
    'contactname': String,
    'akzeptiert': String
}


//Mit Redis Server verbinden
var redis = require("redis");
var client = redis.createClient();

client.on("error", function (err) {
    console.log("Error" + err);
});


app.post('/user', jsonParser, paperwork.accept(jsonuseraccept), function (req, res) {

    var newUser = req.body; // Body beinhaltet geparstes JSON-Objekt
    
     var datasetKey = 'user:' + newUser.username;
        
    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            res.status(400).json("Der Username ist schon vergeben!");
        } else {
           client.set(datasetKey, JSON.stringify(newUser), function (err, rep) { //user in Datenbank speichern

            res.status(200).json(newUser);
        })
    
        }

    });
});

app.get('/user', jsonParser, function (req, res) {
    client.keys('user:*', function (err, rep) {
        
        if (rep.length == 0) {
            res.status(404).json([]);
            return;
        } else {
            var users = [];
            client.mget(rep, function (err, rep) {
                rep.forEach(function (val) {
                    if (val != null) {
                        users.push(JSON.parse(val));
                    }
                });
                res.status(200).json(users);
            })
        }
    })
});

//GET auf einen bestimmten User
app.get('/user/:USERNAME', jsonParser, function (req, res) {
    var datasetKey = 'user:' + req.params.USERNAME;

    client.get(datasetKey, function (err, rep) {

        if (rep) {
            res.status(200).type('json').send(rep); //liegt schon in Json vor
        } else {
            res.status(404).type('text').send('Der User mit dem Usernamen: ' + req.params.USERNAME + ' existiert nicht!');
        }
    });
});


app.delete('/user/:USERNAME', jsonParser, function (req, res) {
    var datasetKey = 'user:' + req.params.USERNAME;

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            var username;

            client.get(datasetKey, function (err, rep) {
                username = JSON.parse(rep);
            });

            client.del(datasetKey, function (err, rep) {

                res.status(200).json('User: ' + username.username +' wurde gelöscht!');
            })
        } else {
            res.status(404).json('User existiert nicht!');
        }

    });
});

app.put('/user/:USERNAME', jsonParser, paperwork.accept(jsonuseraccept), function (req, res) {

    var datasetKey = 'user:' + req.params.USERNAME;

    client.exists(datasetKey, function (err, rep) {

        if (rep == 1) {
            var newData = req.body;
            newData.username = req.params.USERNAME

            client.set(datasetKey, JSON.stringify(newData), function (err, rep) {
                res.status(200).json(newData);
            });
        } else {
            res.status(404).json('Der User existiert nicht!');
        }
    })
});


//Jeder User hat Kontakte, die im Alarmfall benachrichtigt werden.. hier werden diese gespeichert 
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//



app.post('/user/:USERNAME/contact', jsonParser, paperwork.accept(jsoncontactsaccept), function (req, res) {

    var newContact = req.body; // Body beinhaltet geparstes JSON-Objekt
    
     var datasetKey = 'c:' + req.params.USERNAME + 'contact:' + newContact.contactname;
        
    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            res.status(400).json("Der User ist schon dein Freund!");
        } else {
           client.set(datasetKey, JSON.stringify(newContact), function (err, rep) { //user in Datenbank speichern

            res.status(200).json(newContact);
        })
        }
    });
});

//GET auf einen bestimmten User
app.get('/user/:USERNAME/contact/:CONTACTNAME', jsonParser, function (req, res) {
    var datasetKey = 'c:' + req.params.USERNAME + 'contact:' + req.params.CONTACTNAME;

    client.get(datasetKey, function (err, rep) {

        if (rep) {
            res.status(200).type('json').send(rep); //liegt schon in Json vor
        } else {
            res.status(404).type('text').send('Der Contact mit dem Usernamen: ' + req.params.CONTACTNAME + 'ist nicht in der Kontaktliste!');
        }
    });
});

app.get('/user/:USERNAME/contact', jsonParser, function (req, res) {
    client.keys('c:' + req.params.USERNAME + 'contact:*', function (err, rep) {
        
        if (rep.length == 0) {
            res.status(404).json([]);
            return;
        } else {
            var users = [];
            client.mget(rep, function (err, rep) {
                rep.forEach(function (val) {
                    if (val != null) {
                        users.push(JSON.parse(val));
                    }
                });
                res.status(200).json(users);
            })
        }
    })
});

app.delete('/user/:USERNAME/contact/:CONTACTNAME', jsonParser, function (req, res) {
    var datasetKey = 'c:' + req.params.USERNAME + 'contact:' + req.params.CONTACTNAME;

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            var contactname;

            client.get(datasetKey, function (err, rep) {
                contactname = JSON.parse(rep);
            });

            client.del(datasetKey, function (err, rep) {

                res.status(200).json('Kontakt: ' + contactname.contactname +' wurde gelöscht!');
            })
        } else {
            res.status(404).json('User existiert nicht!');
        }

    });
});



//UserAndroidIDs werden gespeichert, um zu überprüfen, ob das Smartphone schon registriert wurde. 
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//





app.post('/userAID', jsonParser, paperwork.accept(jsonuserAIDaccept), function(req,res){
    
    var newUser = req.body; // Body beinhaltet geparstes JSON-Objekt
    
     var datasetKey = 'userAID:' + newUser.androidID;
        
    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            res.status(400).json("Das Smartphone ist schon registriert!");
        } else {
           client.set(datasetKey, JSON.stringify(newUser), function (err, rep) { //user in Datenbank speichern

            res.status(200).json(newUser);
        })
        }
    });
});

//GET auf einen bestimmten User
app.get('/userAID/:AID', jsonParser, function (req, res) {
    var datasetKey = 'userAID:' + req.params.AID;

    client.get(datasetKey, function (err, rep) {

        if (rep) {
            res.status(200).type('json').send(rep); //liegt schon in Json vor
        } else {
            res.status(404).type('text').send('Das Smartphone wurde noch nicht registriert!');
        }
    });
});

app.delete('/userAID/:AID', jsonParser, function (req, res) {
    var datasetKey = 'userAID:' + req.params.AID;

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            var username;

            client.get(datasetKey, function (err, rep) {
                username = JSON.parse(rep);
            });

            client.del(datasetKey, function (err, rep) {

                res.status(200).json('Die AndroidID des Users: ' + username.username +' wurde gelöscht!');
            })
        } else {
            res.status(404).json('Das Smartphone wurde noch nicht registriert!');
        }

    });
});

app.get('/userAID', jsonParser, function (req, res) {
    client.keys('userAID:*', function (err, rep) {
        
        if (rep.length == 0) {
            res.status(404).json([]);
            return;
        } else {
            var smartphones = [];
            client.mget(rep, function (err, rep) {
                rep.forEach(function (val) {
                    if (val != null) {
                        smartphones.push(JSON.parse(val));
                    }
                });
                res.status(200).json(smartphones);
            })
        }
    })
});



app.listen(1234);