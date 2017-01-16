package de.walkhome.walkhome;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * Created by yannikhuber on 09.01.17.
 */

public class Registrieren extends Activity {

    Button btn_zurueck;
    Button btn_registration;
    EditText edt_vorname;
    EditText edt_name;
    EditText edt_username;
    EditText edt_telefon;
    String usernameSpeicher;
    String fcmToken;
    final String hostUrl = "http://5.199.129.74:81";


    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_registration);

            btn_zurueck = (Button) findViewById(R.id.btnzuruck);
            btn_registration = (Button) findViewById(R.id.button_absenden);
            edt_vorname = (EditText) findViewById(R.id.editText_vorname);
            edt_name = (EditText) findViewById(R.id.editText_name);
            edt_telefon = (EditText) findViewById(R.id.editText_telefon);
            edt_username = (EditText) findViewById(R.id.editText_username);

            LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver, new IntentFilter("tokenReceiver"));


            btn_zurueck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Registrieren.this, MapsActivity.class);
                    startActivity(intent);
                }
            });

            btn_registration.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onClickreg();


                }
            });
        }catch(Exception e){
            Toast.makeText(getApplicationContext(),"Fehler!"+ e.toString(),Toast.LENGTH_SHORT).show();
        }

    }

    void onClickreg(){
        Boolean istNummer = false;
        String text = edt_telefon.getText().toString();
        try {
            long num = Long.parseLong(text);
            istNummer = true;
        } catch (NumberFormatException e) {
            istNummer = false;
        }
        try{
        if(istNummer && edt_username.getText().toString() != "" && edt_vorname.getText().toString() != "" && edt_name.getText().toString() != ""){

            String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            HttpRestPost restp = new HttpRestPost();
            usernameSpeicher = edt_username.getText().toString();


            restp.execute(hostUrl +"/user", "{\"androidID\":\""+ androidID +"\",\"username\":\""+ edt_username.getText().toString()+"\",\"nachname\":\""+ edt_name.getText().toString()+"\",\"vorname\":\""+ edt_vorname.getText().toString() +"\",\"telefonnummer\":\""+ edt_telefon.getText().toString()+"\",\"status\":\"zuHause\",\"fcmID\":\""+ fcmToken + "\"}","post");

            btn_registration.setEnabled(false);

        }else{
            Toast.makeText(this,"Bitte alle Felder korrekt ausfüllen!", Toast.LENGTH_SHORT).show();
        }}catch(Exception e){
            Toast.makeText(this,"Fehler!", Toast.LENGTH_SHORT).show();
            btn_registration.setEnabled(true);
        }
    }

    BroadcastReceiver tokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra("token");
            if(token != null){
                fcmToken = token;
                Log.e(TAG, "Token empfangen: " + fcmToken);
            }
            else{

            }
        }
    };


    public class HttpRestPost  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String userDaten;

        String post(String url, String json, String methode) throws IOException {
            if(methode == "post") {
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            }else if(methode == "put"){
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .put(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            }else{
                return "das hat nicht geklappt";
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                userDaten = post(params[0], params[1], params[2]);

            } catch (Exception e) {
                userDaten = e.toString();
            }

            return userDaten;
        }

        @Override
        protected void onPostExecute(String res) {
            if(res.contains("Der Username ist schon vergeben!")){
                Toast.makeText(getApplicationContext() ,"Der Nutzername ist schon vergeben!", Toast.LENGTH_LONG).show();
                btn_registration.setEnabled(true);
            }else{
                onpostex();
            }
        }
    }

    void onpostex (){
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        HttpRestPost2 restp2 = new HttpRestPost2();
        restp2.execute(hostUrl+"/userAID" , "{\"androidID\":\""+ androidID +"\",\"username\":\""+ edt_username.getText().toString() + "\"}", "post");
    }


    public class HttpRestPost2  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String abcde;

        String post(String url, String json, String methode) throws IOException {
            if(methode == "post") {
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            }else if(methode == "put"){
                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .put(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            }else{
                return "das hat nicht geklappt";
            }
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                abcde = post(params[0], params[1], params[2]);

            } catch (Exception e) {
                abcde = e.toString();
            }

            return abcde;
        }

        @Override
        protected void onPostExecute(String res) {

            Time zeit = new Time();
            zeit.setToNow();

            HttpRestPostAlarm alarmpost = new HttpRestPostAlarm();
            alarmpost.execute(hostUrl+"/user/"+usernameSpeicher+"/alarm", "{\"time\":\""+zeit.format("%H:%M").toString()+"\"}");

            Toast.makeText(getApplicationContext(), "Registrierung Erfolgreich!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }
    }

    /* ALARM WIRD ANGELEGT UM IHN SPÄTER MIT dEN GPS DATEN ZU AKTUALISIEren  */

    public class HttpRestPostAlarm  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String userDaten;

        String post(String url, String json) throws IOException {

                RequestBody body = RequestBody.create(JSON, json);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Response response = client.newCall(request).execute();
                return response.body().string();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                userDaten = post(params[0], params[1]);

            } catch (Exception e) {
                userDaten = e.toString();
            }
            return userDaten;
        }

        @Override
        protected void onPostExecute(String res) {

        }
    }



}
