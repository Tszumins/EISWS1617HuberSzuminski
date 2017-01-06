package de.walkhome.walkhome;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class LocationService extends Service  {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private final IBinder iBinder = new LocalBinder();
    List<Route> route2;
    List<Location> allpoints = new ArrayList<>();
    public Boolean isActivated = false;
    public String contactName;


    int counterSameDistance = 0;
    int counter1 = 0;
    int counterAbweichung = 0;

    public class LocalBinder extends Binder {
        LocationService getService(){
            return LocationService.this;
        }
    }

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;


        public LocationListener(String provider) {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);

            if(mLastLocation != null) {


                int oldnewdistance = entfernungBerechnen(location.getLatitude(), location.getLongitude(), mLastLocation.getLatitude(), mLastLocation.getLongitude());
                if (oldnewdistance < 10) {
                    counterSameDistance++;
                    if (counterSameDistance == 7) {
                        AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getApplication(), R.style.dialog))
                                .setTitle("WalkHome")
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
                                .create();

                        alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                        alertDialog.show();
                    }
                }else{
                    counterSameDistance = 0;
                }
            }
            mLastLocation.set(location);

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

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
                                AlertDialog alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(getApplication(), R.style.dialog))
                                        .setTitle("WalkHome")
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
                                        .create();

                                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
                                alertDialog.show();


                            }
                            if(counterAbweichung == 10){ //wenn der Counter auf 10 steigt wird ein notfall ausgelöst, da der Nutzer nicht mit "JA" geantwortet hat
                                //sende Alarm an Nofallkontakte
                            }
                            counter1 = 0;
                        }

                    }
                }

            }


        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
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

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
}