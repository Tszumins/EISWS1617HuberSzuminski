package de.walkhome.walkhome;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by yannikhuber on 21.11.16.
 */

public class Contacts extends Activity {
    Button btnZurueck;
    private ArrayList<Button> contactBuuttons;
    LocationService lS;
    Boolean isBound = false;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        btnZurueck = (Button) findViewById(R.id.buttonZurueck);
        contactBuuttons = new ArrayList<Button>();

        btnZurueck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Contacts.this, MapsActivity.class);
                startActivity(intent);
            }
        });
        createButton(10);
        LinearLayout ll = (LinearLayout)findViewById(R.id.contactButton);

        for(int i=0; i<contactBuuttons.size();i++) {
            contactBuuttons.get(i).setText("Kontakt");
            ll.addView(contactBuuttons.get(i));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);


    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (isBound) {
            unbindService(mConnection);
            isBound = false;
        }
    }
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            lS = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };


    void createButton(int btnCount){
        for (int i=0 ; i<btnCount ; i++) {
            contactBuuttons.add(new Button(this));
        }
    }
}