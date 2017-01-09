package de.walkhome.walkhome;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by yannikhuber on 17.11.16.
 */



public class UserSettings extends Activity {
    Button btnZurueck;

    //SharedPreferences benoetigte Variablen
    public static final String MyPREFERENCES = "WalkHomeSettings" ;
    public static final String headphoneEnable = "headphoneEnable";
    public static final String routeTolerance = "routeTolerance";
    public static final String peopleAround = "peopleAround";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch swHeadphone = (Switch)findViewById(R.id.sw_Headphone);
        Switch swRouteTolerance = (Switch)findViewById(R.id.sw_RouteTolerance);
        Switch swPeopleAround = (Switch)findViewById(R.id.sw_PeopleAround);

        EditText etPutVorname = (EditText)findViewById(R.id.et_putVorname);

        //String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        //HttpRestGet getUserName = new HttpRestGet();
        //getUserName.execute("http://5.199.129.74:81/userAID/" + androidID);


        //Zugriff auf SharedPreferences
        //Hier werden die Einstellungen die in der APP Vorgenommen werden können gespeichert.
        SharedPreferences sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedpreferences.edit();

        //OnCreate werden die gespeicherten Einstellungen geladen und gesetzt.
        swHeadphone.setChecked(sharedpreferences.getBoolean(headphoneEnable, false));
        swRouteTolerance.setChecked(sharedpreferences.getBoolean(routeTolerance, true));
        swPeopleAround.setChecked(sharedpreferences.getBoolean(peopleAround, true));


        //Wenn der Schalter geändert wird werden die Änderungen auch in den SharedPrefs geändert.
        swHeadphone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean(headphoneEnable, true);
                    editor.commit();
                } else {
                    editor.putBoolean(headphoneEnable, false);
                    editor.commit();
                }
            }
        });

        swRouteTolerance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean(routeTolerance, true);
                    editor.commit();
                } else {
                    editor.putBoolean(routeTolerance, false);
                    editor.commit();
                }
            }
        });

        swPeopleAround.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean(peopleAround, true);
                    editor.commit();
                } else {
                    editor.putBoolean(peopleAround, false);
                    editor.commit();
                }
            }
        });


        btnZurueck = (Button) findViewById(R.id.buttonZurueck);

        btnZurueck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(UserSettings.this, MapsActivity.class);

                startActivity(intent);


            }
        });
    }

    public class HttpRestGet  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String userData;

        String run(String url) throws IOException {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                userData = run(params[0]);

            } catch (Exception e) {
                userData = e.toString();
            }

            return userData;
        }

        @Override
        protected void onPostExecute(String res) {


        }
    }
}
