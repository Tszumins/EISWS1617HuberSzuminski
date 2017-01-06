package de.walkhome.walkhome;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.walkhome.walkhome.LocationService.LocalBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, DirectionFinderListener {

    GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Button btnFindPath;
    Button btnSettings;
    Button btnContacts;
    Button btnCancel;
    boolean followUserGPS = true;
    boolean btnFindPathaktiv = true;

    EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private List<Double> dlon = new ArrayList<>();
    private List<Double> dlat = new ArrayList<>();
    List<Location> allpoints = new ArrayList<>();
    List<LatLng> waypoints = new ArrayList<>();
    int counter1 = 0;
    int counterSameDistance = 0;
    int counterAbweichung = 0;
    AudioManager audioManager;
    boolean isBound = false;
    LocationService lS;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btnFindPath = (Button) findViewById(R.id.buttonRoute);
        btnSettings = (Button) findViewById(R.id.buttonEinstellungen);
        etDestination = (EditText) findViewById(R.id.texteditZiel);
        btnContacts = (Button) findViewById(R.id.buttonKontakte);
        btnCancel = (Button) findViewById(R.id.buttonAbbrechen);

        btnSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(MapsActivity.this, UserSettings.class);

                startActivity(intent);
            }
        });
        btnContacts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(MapsActivity.this, Contacts.class);

                startActivity(intent);
            }
        });

        btnFindPath.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(btnFindPathaktiv) {
                    sendRequest();
                }else{
                    followUserGPS = true;
                    btnFindPath.setText("navigiert...");
                    btnFindPathaktiv = true;
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                followUserGPS = true;
                btnFindPathaktiv = true;
                btnFindPath.setText("Route suchen");
                btnFindPath.getBackground().setColorFilter(Color.parseColor("#4eb9ff"), PorterDuff.Mode.DARKEN);

                if (originMarkers != null) {
                    for (Marker marker : originMarkers) {
                        marker.remove();
                    }
                }

                if (destinationMarkers != null) {
                    for (Marker marker : destinationMarkers) {
                        marker.remove();
                    }
                }

                if (polylinePaths != null) {
                    for (Polyline polyline:polylinePaths ) {
                        polyline.remove();
                    }
                }

                allpoints.clear();
                lS.allpoints.clear();
                dlat.clear();
                dlon.clear();

                etDestination.setText("");
                btnCancel.setVisibility(View.GONE);
            }
        });



        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        Intent intent1 = new Intent(this,LocationService.class);

        bindService(intent1 , serviceConnection, Context.BIND_AUTO_CREATE);

    }



    private void  sendRequest() {  //startet DirectionFinder und sendet die Start und Zieladresse aus den beiden texteingaben

        if (mLastLocation != null) {
            String origin = mLastLocation.getLatitude() + "," + mLastLocation.getLongitude();

            String destination = etDestination.getText().toString();
            if (origin.isEmpty()) {
                Toast.makeText(this, "Fehler!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (destination.isEmpty()) {
                Toast.makeText(this, "Bitte Zieladdresse eingeben!", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                new DirectionFinder(this, origin, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(this, "Es konnte kein Standort ermittelt werden!", Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onDirectionFinderStart() {    //löscht die bisherigen Maker und Polylines auf der karte
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) { //wenn eine Route gefunden wurde wird diese Methode ausgeführt
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();


        lS.route2 = routes;
        lS.isActivated = true;



        //übergibt die Distanz und Dauer der Strecke, verbindet die Punkte auf der Karte mit Polylines
        //und fügt zwischen den Punkten weitere punkte ein, diese werden in eine Liste gespeichert und nachher zur Berechnung
        //der Abweichung vom Weg Benutzt
        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            ((TextView) findViewById(R.id.textviewDauer)).setText(route.duration.text);
            ((TextView) findViewById(R.id.textviewDistanz)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);


            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));

            }


            waypoints = polylineOptions.getPoints();
            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }

        if(allpoints.isEmpty() == false){
            allpoints.clear();
            lS.allpoints.clear();
            dlat.clear();
            dlon.clear();
        }
            for(int i = 0; i<waypoints.size();i++){
                dlat.add(waypoints.get(i).latitude);
                dlon.add(waypoints.get(i).longitude);
            }



            for(int i = 0;i < dlat.size();i++){

                //double distance = Math.sqrt((distancelat*distancelat)+(distancelon*distancelon));

                Location a = new Location("");
                a.setLatitude(dlat.get(i));
                a.setLongitude(dlon.get(i));
                allpoints.add(a);


                if(i < dlat.size()-1) {
                    Location b = new Location("");
                    double latpoint = (dlat.get(i) + dlat.get(i+1)) / 2;
                    double lonpoint = (dlon.get(i) + dlon.get(i+1)) / 2;
                    b.setLatitude(latpoint);
                    b.setLongitude(lonpoint);
                    allpoints.add(b);
                }

            }

        lS.allpoints = allpoints;
        followUserGPS = false;
       //die kartenübersicht wird dem User angezeigt
            LatLng first = new LatLng(allpoints.get(0).getLatitude(), allpoints.get(0).getLongitude());
            LatLng last = new LatLng(allpoints.get(allpoints.size()-1).getLatitude(), allpoints.get(allpoints.size()-1).getLongitude());


            LatLngBounds.Builder uebersicht;
            uebersicht = new LatLngBounds.Builder();
            uebersicht.include(first);
            uebersicht.include(last);


            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(uebersicht.build(), 150));
        btnFindPath.setText("Navigieren!");
        btnFindPath.getBackground().setColorFilter(Color.parseColor("#ff6241"), PorterDuff.Mode.DARKEN);
        btnFindPathaktiv = false;
        btnCancel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        super.onPause();

        //stoppt die LocaionUpdates wenn die Activity nicht mehr aktiv ist
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
    @Override
    public void onMapReady(GoogleMap map) { //wenn die Karte geladen ist, wird die android version geprüft und im zweifel nach permissions gefragt
                                            // danach wird die GPS- Lokation aktiviert.
        mMap = map;


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

          try {

              if(lS.isActivated == true) {



                  polylinePaths = new ArrayList<>();
                  originMarkers = new ArrayList<>();
                  destinationMarkers = new ArrayList<>();

                  for (Route route : lS.route2) {



                      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
                      originMarkers.add(mMap.addMarker(new MarkerOptions()
                              .title(route.startAddress)
                              .position(route.startLocation)));
                      destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                              .title(route.endAddress)
                              .position(route.endLocation)));

                      PolylineOptions polylineOptions = new PolylineOptions().
                              geodesic(true).
                              color(Color.BLUE).
                              width(10);
                      for (int i = 0; i < route.points.size(); i++){
                          polylineOptions.add(route.points.get(i));

                      }

                      polylinePaths.add(mMap.addPolyline(polylineOptions));
                  }

              }
        }catch (Exception e){
            etDestination.append(e.toString());
        }

    }

    //berechnet die entfernung zwischen zwei latlng punkten
    public int entfernungBerechnen(double lat1, double lon1, double lat2, double lon2){
        double radius = 6378.137; //Erdradius
        double dLat = lat2 * Math.PI / 180 - lat1 * Math.PI / 180;
        double dLon = lon2 * Math.PI / 180 - lon1 * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)+Math.cos(lat1 * Math.PI /180) * Math.cos(lat2 * Math.PI
        / 180)* Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = radius * c;
        d = d * 1000;
        return (int) d;


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    // wenn der standort aktualiesiert wird, wird die map neu zentriert und der marker für den standort aktualisiert
    // wenn das handy 10 mal hintereinander nicht den standort wechselt, wird der nutzer gefragt ob alles in ordnung ist
    // dies wird noch nur in die texteingabe geschrieben, bei der fertigen app soll ein post auf den Dienstnutzer stattfinden
    // wenn eine Route vorliegt wird die abweichung von der Route berechnet
    @Override
    public void onLocationChanged(Location location) {


       /* if(mLastLocation != null) {


            int oldnewdistance = entfernungBerechnen(location.getLatitude(), location.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if (oldnewdistance < 10) {
                counterSameDistance++;
                if (counterSameDistance == 7) {
                    new AlertDialog.Builder(MapsActivity.this)
                            .setTitle("Nachfrage")
                            .setMessage("Ist alles in Ordnung?")
                            .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    counterSameDistance = 8;
                                }
                            })
                            .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //sende Alarm an Notfallkontakte
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            }else{
                counterSameDistance = 0;
            }
        }*/
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);



        //move map camera
        if(followUserGPS) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
        }
        /*
        if(allpoints.isEmpty()==true){

        }else {
            for (int i = 0; i < allpoints.size(); i++) {

                int distanz = entfernungBerechnen(allpoints.get(i).getLatitude(), allpoints.get(i).getLongitude(), latLng.latitude, latLng.longitude);



                if (distanz < 100) {

                    counter1 = 0;
                    counterAbweichung = 0;
                    break;
                } else {
                    counter1++;
                    if (counter1 == allpoints.size()) {

                        counterAbweichung++;
                        if(counterAbweichung == 2) { //wenn die position 2 mal nicht auf der route war wird eine Abfrage an den User ausgelöst
                            new AlertDialog.Builder(MapsActivity.this)
                                    .setTitle("Nachfrage")
                                    .setMessage("Ist alles in Ordnung?")
                                    .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            counterAbweichung = 11; //wenn der Nutzer sagt dass alles okay ist, wird der counter auf 11 gesetzt und erst wieder auf 0 wenn der User wieder auf der strecke ist
                                        }
                                    })
                                    .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //sende Alarm an Notfallkontakte
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }
                        if(counterAbweichung == 10){ //wenn der Counter auf 10 steigt wird ein notfall ausgelöst, da der Nutzer nicht mit "JA" geantwortet hat
                            //sende Alarm an Nofallkontakte
                        }
                        counter1 = 0;
                    }

                }
            }

        }

        */

        //location updates werden gestoppt
        /*if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    //keine Permission
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }


        }
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            LocalBinder binder = (LocalBinder) iBinder;
            lS = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;

        }
    };


}
