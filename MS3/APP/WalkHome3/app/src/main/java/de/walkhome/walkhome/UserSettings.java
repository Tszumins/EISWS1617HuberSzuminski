package de.walkhome.walkhome;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by yannikhuber on 17.11.16.
 */



public class UserSettings extends Activity {
    Button btnZurueck;
    Button datenAktualisieren;
    Button userLoeschen;
    EditText editVorname;
    EditText editNachname;
    EditText editMobilnummer;
    String url;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

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
        datenAktualisieren = (Button) findViewById(R.id.bt_changeUserData);
        editVorname = (EditText)findViewById(R.id.et_putVorname);
        editNachname = (EditText)findViewById(R.id.et_putNachname);
        editMobilnummer = (EditText)findViewById(R.id.et_putNummer);
        userLoeschen = (Button) findViewById(R.id.buttonUserDelete);

        getUsername();

        userLoeschen.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(UserSettings.this)
                        .setTitle("Löschen")
                        .setMessage("Sind Sie sicher, dass Sie ihren Account löschen wollen?")
                        .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                HttpRestDelete deleteUser = new HttpRestDelete();
                                deleteUser.execute(url);
                            }
                        })
                        .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(R.drawable.ic_loeschen)
                        .show();
            }
        });

        datenAktualisieren.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(url != null) {
                    HttpRestGet2 restg2 = new HttpRestGet2();
                    restg2.execute(url);
                }else{
                    Toast.makeText(getApplicationContext(),"Userdaten müssen noch geladen werden!", Toast.LENGTH_SHORT).show();
                }
            }
        });

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
    /*------------------------------------------------------------------------------------------------
     * ------------------------------------------------------------------------------------------------
      * ------------------------Username mittels androidID holen ------------------------------------*/
    void getUsername(){
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        HttpRestGet getUserName = new HttpRestGet();
        getUserName.execute("http://5.199.129.74:81/userAID/" + androidID);
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
             url = res.substring(1, res.length()-1);
        }
    }
    /*------------------------------------------------------------------------------------------------
    * ------------------------------------------------------------------------------------------------------
    * ----------------GET(USERDATEN VOM SERVER HOLEN ) ------------------------------------------------*/

    public class HttpRestGet2  extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();
        String responsestring;

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
                responsestring = run(params[0]);
            } catch (Exception e) {
                responsestring = e.toString();
            }
            return responsestring;
        }
        @Override
        protected void onPostExecute(String res) {
            Boolean istNummer = false;
            String text = editMobilnummer.getText().toString();
            try {
                int num = Integer.parseInt(text);
                istNummer = true;
            } catch (NumberFormatException e) {
                istNummer = false;
            }
            try{
                JSONObject jsonObject = new JSONObject(res);
                if(!editVorname.getText().toString().equals("")) {
                    jsonObject.put("vorname", editVorname.getText().toString());
                }
                if(!editNachname.getText().toString().equals("")) {
                    jsonObject.put("nachname", editNachname.getText().toString());
                }
                if(istNummer) {
                    jsonObject.put("telefonnummer", editMobilnummer.getText().toString());
                }else{
                    Toast.makeText(getApplicationContext(),"Die Telefonnummer ist nicht korrekt",Toast.LENGTH_SHORT).show();
                }

                HttpRestput putUserdata = new HttpRestput();
                String payload = "{\"androidID\":\""+ jsonObject.getString("androidID") +"\",\"username\":\""+ jsonObject.getString("username")+"\",\"nachname\":\""+ jsonObject.getString("nachname")+"\",\"vorname\":\""+ jsonObject.getString("vorname") +"\",\"telefonnummer\":\""+ jsonObject.getString("telefonnummer")+"\",\"status\":\""+jsonObject.getString("status")+"\",\"fcmID\":\""+jsonObject.getString("fcmID")+"\"}";

                putUserdata.execute("http://5.199.129.74:81/user/"+ jsonObject.getString("username"), payload);

            }catch(JSONException e){
                Toast.makeText(getApplicationContext(),"Fehler!",Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*------------------------------------------------------------------------------------------------
    * ------------------------------------------------------------------------------------------------
    * ------------------------PUT ( USERDATEN AKTUALISISEREN) UND AN DEN SERVER SENDEN-------- */
    public class HttpRestput  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String responsePUT;

        String put(String url, String json) throws IOException {

            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();
            Response response = client.newCall(request).execute();

            return ""+response.isSuccessful();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                responsePUT = put(params[0], params[1]);

            } catch (Exception e) {
                responsePUT = e.toString();
            }
            return responsePUT;
        }

        @Override
        protected void onPostExecute(String res) {

            if(res.equals("true")) {
                Toast.makeText(getApplicationContext(), "Die Userdaten wurden geändert", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Es ist ein Fehler aufgetreten!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*--------------------------------------------------------------------------------------------------------
    * ----------------------DELETE (USER AUS DER DATENBANK LÖSCHEN)-----------------------------------------------------
    * -----------------------------------------------------------------------------------------------------------------*/

    public class HttpRestDelete  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String responseDel;

        String post(String url) throws IOException {

            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .build();

            Response response = client.newCall(request).execute();
            return ""+response.isSuccessful();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                responseDel = post(params[0]);

            } catch (Exception e) {
                responseDel = e.toString();
            }

            return responseDel;
        }

        @Override
        protected void onPostExecute(String res) {
           if(res.equals("true")){

               Toast.makeText(getApplicationContext(),"Dein Account wurde gelöscht!", Toast.LENGTH_SHORT).show();
               Intent intent = new Intent(UserSettings.this, MapsActivity.class);
               startActivity(intent);

           }else {
               Toast.makeText(getApplicationContext(),"Es ist ein Fehler aufgetreten!", Toast.LENGTH_SHORT).show();
           }

        }
    }

}
