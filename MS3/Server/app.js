var express = require('express');
var bodyParser = require('body-parser');
var jsonParser = bodyParser.json();
var app = express();
var paperwork = require('paperwork');
var geocoder = require('geocoder');


var jsonuseraccept = {
    'username': String,
    'androidID': String,
    'fcmID': String,
    'vorname': String,
    'nachname': String,
    'telefonnummer': String,
    'status': String,
    'currentPLZ': paperwork.optional(Number)
}

var jsonputuseraccept = {
    'username': String,
    'androidID': String,
    'fcmID': String,
    'vorname': String,
    'nachname': String,
    'telefonnummer': String,
    'status': String,
    'currentPLZ': paperwork.optional(Number)
}

var jsonuserAIDaccept = {
    'androidID': String,
    'username': paperwork.optional(String)
}

var jsoncontactsaccept = {
    'contactname': String,
    'akzeptiert': String
}

var jsonplaceaccept = {
    'latitude': String,
    'longitude': String,
    'username': String
}

var jsonplaceuseraccept = {
    'username': String,
    'fcmID': String
}

var jsonuseralarmaccept = {
    'latitude': paperwork.optional(Number),
    'longitude': paperwork.optional(Number),
    'status': paperwork.optional(String),
    'time': String
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
                console.log(newUser);
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
                var user = {
                    users
                };

                res.status(200).json(user);
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
    var datasetKeycontacts = 'c:' + req.params.USERNAME + 'contact:';


    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {

            client.get(datasetKey, function (err, rep2) {
                var userData = JSON.parse(rep2);
                var datasetKeyAndroidID = 'userAID:' + userData.androidID;

                client.del(datasetKey, function (err, rep) {});

                client.keys(datasetKeycontacts + '*', function (err, rep) {

                    if (rep.length == 0) {

                        return;
                    } else {
                        var users = [];
                        client.mget(rep, function (err, rep) {
                            rep.forEach(function (val) {
                                if (val != null) {
                                    users.push(JSON.parse(val));
                                }
                            });
                            for (var i = 0; i < users.length; i++) {
                                client.del(datasetKeycontacts + "" + users[i].contactname, function (err, rep) {

                                });
                            }

                        })
                    }
                })
                client.del(datasetKeyAndroidID, function (err, rep) {});
                res.status(200).json('User: ' + userData.username + ' wurde gelöscht!');
            });
        } else {
            res.status(404).json('User existiert nicht!');
        }

    });
});

app.put('/user/:USERNAME', jsonParser, paperwork.accept(jsonputuseraccept), function (req, res) {
    var newData = req.body;
    var datasetKey = 'user:' + req.params.USERNAME;
    var user = "";

    if (newData.currentPLZ != null) {
        client.get(datasetKey, function (err, rep) {
            user = JSON.parse(rep);
            if (user.currentPLZ == newData.currentPLZ) {

            } else {
                var nDusername = {};
                nDusername.username = newData.username;
                nDusername.fcmID = newData.fcmID;
                client.del('placeuser:' + user.currentPLZ + 'user:' + newData.username, function (err, rep) {});

                client.set('placeuser:' + newData.currentPLZ + 'user:' + newData.username, JSON.stringify(nDusername), function (err, rep) {});
            }
        });
    }

    client.exists(datasetKey, function (err, rep) {

        if (rep == 1) {

            newData.username = req.params.USERNAME;

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

app.put('/user/:USERNAME/contact/:CONTACTNAME', jsonParser, paperwork.accept(jsoncontactsaccept), function (req, res) {
    var newData = req.body;
    var datasetKey = 'c:' + req.params.USERNAME + 'contact:' + req.params.CONTACTNAME;

    client.exists(datasetKey, function (err, rep) {

        if (rep == 1) {

            client.set(datasetKey, JSON.stringify(newData), function (err, rep) {

                res.status(200).json("Kontaktdaten wurden geändert!");
            });
        } else {
            res.status(404).json('Der User ist nicht dein Kontakt!');
        }
    })
});

//GET auf einen bestimmten User
app.get('/user/:USERNAME/contact/:CONTACTNAME', jsonParser, function (req, res) {
    var datasetKey = 'c:' + req.params.USERNAME + 'contact:' + req.params.CONTACTNAME;

    client.get(datasetKey, function (err, rep) {

        if (rep) {
            var newData = JSON.parse(rep);
            newData.url = "http://5.199.129.74:81/user/" + req.params.USERNAME + "/contact/" + req.params.CONTACTNAME;

            var user = {
                newData
            };
            res.status(200).json(user);
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
                var user = {
                    users
                };

                res.status(200).json(user);
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

                res.status(200).json('Kontakt: ' + contactname.contactname + ' wurde gelöscht!');
            })
        } else {
            res.status(404).json('User existiert nicht!');
        }

    });
});

app.post('/user/:USERNAME/alarm', jsonParser, paperwork.accept(jsonuseralarmaccept), function (req, res) {

    var newAlarm = req.body; // Body beinhaltet geparstes JSON-Objekt
    newAlarm.username = req.params.USERNAME;
    var datasetKey = 'u:' + req.params.USERNAME + 'alarm:';

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            res.status(400).json("Der User hat den Alarm schon ausgelöst!");
        } else {
            client.set(datasetKey, JSON.stringify(newAlarm), function (err, rep) { //user in Datenbank speichern

                res.status(200).json(newAlarm);
            })
        }
    });
});


app.put('/user/:USERNAME/alarm', jsonParser, paperwork.accept(jsonuseralarmaccept), function (req, res) {
    var newData = req.body;
    var datasetKey = 'u:' + req.params.USERNAME + 'alarm:';

    client.exists(datasetKey, function (err, rep) {

        if (rep == 1) {

            newData.username = req.params.USERNAME;

            client.set(datasetKey, JSON.stringify(newData), function (err, rep) {
                res.status(200).json(newData);
            });
        } else {
            res.status(404).json('Alarm wurde nicht angelegt!');
        }
    })
});

app.get('/user/:USERNAME/alarm', jsonParser, function (req, res) {
    var datasetKey = 'u:' + req.params.USERNAME + 'alarm:';

    client.get(datasetKey, function (err, rep) {

        if (rep) {
            res.status(200).type('json').send(rep); //liegt schon in Json vor
        } else {
            res.status(404).type('text').send('Der User hat keinen Alarm ausgelöst!');
        }
    });
});


app.delete('/user/:USERNAME/alarm', jsonParser, function (req, res) {
    var datasetKey = 'u:' + req.params.USERNAME + 'alarm:';

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            var username;

            client.get(datasetKey, function (err, rep) {
                username = JSON.parse(rep);
            });

            client.del(datasetKey, function (err, rep) {

                res.status(200).json('Der Alarm wurde gelöscht!');
            })
        } else {
            res.status(404).json('Es wurde kein Alarm ausgelöst!');
        }

    });
});
//UserAndroidIDs werden gespeichert, um zu überprüfen, ob das Smartphone schon registriert wurde. 
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//


app.post('/userAID', jsonParser, paperwork.accept(jsonuserAIDaccept), function (req, res) {

    var newUser = req.body; // Body beinhaltet geparstes JSON-Objekt

    var datasetKey = 'userAID:' + newUser.androidID;

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            client.get(datasetKey, function (err, rep) {
                var datajson = JSON.parse(rep);
                var responeURL = "http://5.199.129.74:81/user/" + datajson.username;

                res.status(400).json({
                    "status": 400,
                    "url": responeURL
                });
            })

        } else {
            client.set(datasetKey, JSON.stringify(newUser), function (err, rep) { //user in Datenbank speichern

                res.status(200).json(newUser.androidID);
            })
        }
    });
});

app.put('/userAID/:AID', jsonParser, paperwork.accept(jsonuserAIDaccept), function (req, res) {
    var newData = req.body;
    var datasetKey = 'userAID:' + newData.androidID;

    client.exists(datasetKey, function (err, rep) {

        if (rep == 1) {

            client.set(datasetKey, JSON.stringify(newData), function (err, rep) {
                var responseURL = "http://5.199.129.74:81/user/" + newData.username;
                res.status(200).json({
                    "status": 200,
                    "url": responseURL
                });
            });
        } else {
            res.status(404).json('Android ID wurde nicht angelegt!');
        }
    })
});

//GET auf eine bestimmte AndroidID 
app.get('/userAID/:AID', jsonParser, function (req, res) {
    var datasetKey = 'userAID:' + req.params.AID;

    client.get(datasetKey, function (err, rep) {

        if (rep) {
            var userdata = JSON.parse(rep);
            var url = "http://5.199.129.74:81/user/" + userdata.username;
            res.status(200).json(url); //liegt schon in Json vor
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

                res.status(200).json('Die AndroidID des Users: ' + username.username + ' wurde gelöscht!');
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

//Ortsgruppen werden erstellt, um Personen im Umfeld im Notfall kontaktieren zu können
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//
//---------------------------------------------------------------------------------------------//

app.post('/place', jsonParser, paperwork.accept(jsonplaceaccept), function (req, res) {

    var newPlace = req.body; // Body beinhaltet geparstes JSON-Objekt

    geocoder.reverseGeocode(newPlace.latitude, newPlace.longitude, function (err, data) {
        var address = data.results[0].formatted_address;
        var plz = "";
        var placename = "";
        var count = 0;
        var countkomma = 0;
        //Adress nach Postleitzahl filtern 
        for (var i = 0; i < address.length; i++) {
            if (count >= 2 && count < 7) {
                plz = plz + address[i];
                count++;
            }
            if (count == 1) {
                count++;
            }
            if (address[i] == ",") {
                count++;
                countkomma++;
            }
            if (countkomma == 2) {
                break;
            }
            if (count >= 9) {
                placename = placename + address[i];
                count++;
            }
            if (count >= 7 && count < 9) {
                count++;
            }
        }
        var datasetKey = 'place:' + plz;
        newPlace.plz = plz;
        newPlace.placename = placename;
        client.exists(datasetKey, function (err, rep) {
            if (rep == 1) {
                //wenn der Ort schon angelegt wurde
                  client.get('user:' + newPlace.username, function (err, data) {
                        var userData = JSON.parse(data);
                        console.log(userData.currentPLZ);
                        console.log(plz);

                        if (userData.currentPLZ == null) {
                            userData.currentPLZ = plz;
                            client.set('user:' + newPlace.username, JSON.stringify(userData), function (err2, rep2) {
                                var userPlaceData = {};
                                userPlaceData.username = newPlace.username;
                                userPlaceData.fcmID = userData.fcmID;
                                client.set('placeuser:' + plz + 'user:' + newPlace.username, JSON.stringify(userPlaceData), function (err3, rep3) {
                                    res.status(200).json(userPlaceData);
                                });
                            });
                        } else if (userData.currentPLZ == plz) {
                            //wenn die alte plz mit der neuen überein stimmt
                            res.status(200).json("Die PLZ hat sich nicht geändert!");
                        } else {
                            //wenn die alte plz nicht mit der neuen überein stimmt
                            client.del('placeuser:' + userData.currentPLZ + 'user:' + newPlace.username, function (err3, rep3) {
                                userData.currentPLZ = plz;
                                client.set('user:' + newPlace.username, JSON.stringify(userData), function (err2, rep2) {
                                    var userPlaceData = {};
                                    userPlaceData.username = newPlace.username;
                                    userPlaceData.fcmID = userData.fcmID;
                                    client.set('placeuser:' + plz + 'user:' + newPlace.username, JSON.stringify(userPlaceData), function (err3, rep3) {
                                        res.status(200).json(userPlaceData);
                                    });
                                });
                            });
                        }
                    });

            } else {
                //Der Ort wird neu angelegt 
                client.set(datasetKey, JSON.stringify(newPlace), function (err, rep) { //user in Datenbank speichern
                    client.get('user:' + newPlace.username, function (err, data) {
                        var userData = JSON.parse(data);
                        console.log(userData.currentPLZ);
                        console.log(plz);

                        if (userData.currentPLZ == null) {
                            userData.currentPLZ = plz;
                            client.set('user:' + newPlace.username, JSON.stringify(userData), function (err2, rep2) {
                                var userPlaceData = {};
                                userPlaceData.username = newPlace.username;
                                userPlaceData.fcmID = userData.fcmID;
                                client.set('placeuser:' + plz + 'user:' + newPlace.username, JSON.stringify(userPlaceData), function (err3, rep3) {
                                    res.status(200).json(userPlaceData);
                                });
                            });
                        
                        } else {
                            //wenn die alte plz nicht mit der neuen überein stimmt
                            client.del('placeuser:' + userData.currentPLZ + 'user:' + newPlace.username, function (err3, rep3) {
                                userData.currentPLZ = plz;
                                client.set('user:' + newPlace.username, JSON.stringify(userData), function (err2, rep2) {
                                    var userPlaceData = {};
                                    userPlaceData.username = newPlace.username;
                                    userPlaceData.fcmID = userData.fcmID;
                                    client.set('placeuser:' + plz + 'user:' + newPlace.username, JSON.stringify(userPlaceData), function (err3, rep3) {
                                        res.status(200).json(userPlaceData);
                                    });
                                });
                            });
                        }
                    });
                });
            }
        });
    });
});

app.get('/place', jsonParser, function (req, res) {
    client.keys('place:*', function (err, rep) {

        if (rep.length == 0) {
            res.status(404).json([]);
            return;
        } else {
            var places = [];
            client.mget(rep, function (err, rep) {
                rep.forEach(function (val) {
                    if (val != null) {
                        places.push(JSON.parse(val));
                    }
                });
                res.status(200).json(places);
            })
        }
    })
});


app.get('/place/:PLZ/user', jsonParser, function (req, res) {
    client.keys('placeuser:' + req.params.PLZ + 'user:*', function (err, rep) {

        if (rep.length == 0) {
            res.status(404).json([]);
            return;
        } else {
            var places = [];
            client.mget(rep, function (err, rep) {
                rep.forEach(function (val) {
                    if (val != null) {
                        places.push(JSON.parse(val));
                    }
                });
                res.status(200).json(places);
            })
        }
    })
});
app.delete('/place/:PLZ/user/:USERNAME', jsonParser, function (req, res) {
    var datasetKey = 'placeuser:' + req.params.PLZ + 'user:' + req.params.PLZ;

    client.exists(datasetKey, function (err, rep) {
        if (rep == 1) {
            var username;

            client.get(datasetKey, function (err, rep) {
                username = JSON.parse(rep);
            });

            client.del(datasetKey, function (err, rep) {

                res.status(200).json('Der User : '+username.username+' wurde gelöscht aus der PLZ-Gruppe: '+req.params.PLZ+'gelöscht!');
            })
        } else {
            res.status(404).json('Der User war nicht in der Orstgruppe gelistet');
        }

    });
});


app.listen(1234);