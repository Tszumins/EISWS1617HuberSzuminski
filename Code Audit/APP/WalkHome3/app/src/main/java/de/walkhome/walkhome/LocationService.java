package de.walkhome.walkhome;


import android.app.Service;

import android.content.Intent;

import android.os.Binder;

import android.os.IBinder;

import android.support.annotation.Nullable;

import java.util.List;

import java.util.concurrent.RunnableFuture;

/**
 * Created by yannikhuber on 22.11.16.
 */

public class LocationService extends Service  {

    private final IBinder iBinder = new LocalBinder();
    List<Route> route2;
    public Boolean isActivated = false;
    public String contactName;


    public class LocalBinder extends Binder {
        LocationService getService(){
            return LocationService.this;
        }
    }


  /*  final class Thread1 implements Runnable{
        int serviceID;

        Thread1(int serviceID){
            this.serviceID = serviceID;

        }

        @Override
        public void run() {

        }
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return iBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //Thread thread = new Thread(new Thread1(startId));
        //thread.start();



        return START_STICKY;
    }

    public String abc(String s ){
        return "hallo:"+s;
    }



}
