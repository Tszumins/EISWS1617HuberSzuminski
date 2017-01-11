package de.walkhome.walkhome;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.Space;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by yannikhuber on 21.11.16.
 */

public class Contacts extends Activity {
    Button btnZurueck;
    private ArrayList<Button> contactBuuttons;
    private ArrayList<Button> deleteContactButtons;
    private ArrayList<Button> userButtons;
    private ArrayList<LinearLayout> userLayouts;
    private ArrayList<Space> spaces;
    LocationService lS;
    Boolean isBound = false;
    EditText sUser;
    LinearLayout rl;
    String usernameSpeicher ="";



    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        btnZurueck = (Button) findViewById(R.id.buttonZurueck);
        contactBuuttons = new ArrayList<Button>();
        userButtons = new ArrayList<Button>();
        deleteContactButtons = new ArrayList<Button>();
        userLayouts = new ArrayList<LinearLayout>();
        spaces = new ArrayList<Space>();

        sUser = (EditText) findViewById(R.id.searchUser);
        rl = (LinearLayout)findViewById(R.id.userContainer);

        btnZurueck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(Contacts.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        TextWatcher fieldValidatorTextWatcher = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                rl.removeAllViews();
                if(sUser.getText().toString().length()>0) {
                    HttpRestGetotherUsers getusers = new HttpRestGetotherUsers();
                    getusers.execute("http://5.199.129.74:81/user");
                }
            }
        };
        sUser.addTextChangedListener(fieldValidatorTextWatcher);

        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        HttpRestGet restg = new HttpRestGet();
        restg.execute("http://5.199.129.74:81/userAID/" + androidID);


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


    void createContactButtons(int btnCount){
        for (int i=0 ; i<btnCount ; i++) {
            contactBuuttons.add(new Button(this));
        }
    }
    void createuserButtons(int btnCount){
        for (int i=0 ; i<btnCount ; i++) {
            userButtons.add(new Button(this));
        }
    }
    void createdeleteContactButtons(int btnCount){
        for (int i=0 ; i<btnCount ; i++) {
           deleteContactButtons.add(new Button(this));
        }
    }
    void createLayouts(int layoutCount){
        for(int i=0; i<layoutCount;i++){
            userLayouts.add(new LinearLayout(this));
        }
    }
    void createSpaces(int spaceCount){
        for(int i=0; i<spaceCount;i++){
            spaces.add(new Space(this));
        }
    }

    /*----------------------------------- GET AUF DIE ANDROID ID ---------- -------------------------------------
    * -----------------------------------------------------------------------------------------------------------*/

    public class HttpRestGet  extends AsyncTask<String, Void, String> {
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
            String url = res.substring(1, res.length()-1);
            HttpRestGet2 restg2 = new HttpRestGet2();
            usernameFilter(res);
            restg2.execute(url + "/contact");
        }
    }

    void usernameFilter(String url){
        url = url.substring(1, url.length()-1);
        int count = 0;
        usernameSpeicher = "";

        for(int i = 0;i < url.length();i++){
            String s = "" + url.charAt(i);

            if(count == 4){
                usernameSpeicher = usernameSpeicher + url.charAt(i);
            }
            if(s.contains("/")) {
               count++;
            }
        }
    }

    /*----------------------------------- GET auf die KONTAKTE DES USERS ---------- -------------------------------------
   * -----------------------------------------------------------------------------------------------------------*/

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

            try {
                onpostex(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void onpostex(String data) throws JSONException {

        try {
            JSONObject jsonData = new JSONObject(data);
            JSONArray jsonContacts = jsonData.getJSONArray("users");
            contactBuuttons.clear();
            deleteContactButtons.clear();
            spaces.clear();
            userLayouts.clear();

            createContactButtons(jsonContacts.length());
            createLayouts(jsonContacts.length());
            createdeleteContactButtons(jsonContacts.length());
            createSpaces(jsonContacts.length());

            LinearLayout ll = (LinearLayout) findViewById(R.id.contactButton);
            ll.removeAllViews();

            for (int i = 0; i < jsonContacts.length(); i++) {
                JSONObject jsonContact = jsonContacts.getJSONObject(i);
                final String username = jsonContact.getString("contactname");
                final String akzeptiert = jsonContact.getString("akzeptiert");
                contactBuuttons.get(i).getBackground().setColorFilter(Color.parseColor("#E88C0C"), PorterDuff.Mode.DARKEN);
                contactBuuttons.get(i).setTextColor(Color.parseColor("#222222"));

                contactBuuttons.get(i).setText(username);

                if (akzeptiert.contains("nein")) {
                    contactBuuttons.get(i).setEnabled(false);
                    contactBuuttons.get(i).getBackground().setColorFilter(Color.parseColor("#BD720A"), PorterDuff.Mode.DARKEN);
                    HttpRestGet3 checkifContactaddedyou = new HttpRestGet3();
                    checkifContactaddedyou.execute("http://5.199.129.74:81/user/"+username+"/contact/"+usernameSpeicher);
                    contactBuuttons.get(i).setText(contactBuuttons.get(i).getText().toString() +" (noch nicht bestätigt)");
                }


                contactBuuttons.get(i).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        lS.contactName = username;
                        Intent intent = new Intent(Contacts.this, ContactStatus.class);
                        startActivity(intent);
                    }


                });

                deleteContactButtons.get(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertDialog.Builder(Contacts.this)
                                .setTitle("Löschen")
                                .setMessage("Sind Sie sicher, dass Sie "+ username +" löschen wollen?")
                                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        HttpRestDelete deleteContact = new HttpRestDelete();
                                        deleteContact.execute("http://5.199.129.74:81/user/"+ usernameSpeicher + "/contact/" + username);

                                    }
                                })
                                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                });

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                LinearLayout.LayoutParams lpB = new LinearLayout.LayoutParams(0,150,0.85f);
                LinearLayout.LayoutParams lpBdelete = new LinearLayout.LayoutParams(0,130,0.1f);
                LinearLayout.LayoutParams lpS = new LinearLayout.LayoutParams(0,150,0.05f);


                spaces.get(i).setLayoutParams(lpS);
                userLayouts.get(i).setLayoutParams(lp);
                contactBuuttons.get(i).setLayoutParams(lpB);
                deleteContactButtons.get(i).setLayoutParams(lpBdelete);
                deleteContactButtons.get(i).setBackgroundResource(R.drawable.ic_loeschen);

                userLayouts.get(i).setOrientation(LinearLayout.HORIZONTAL);
                userLayouts.get(i).addView(contactBuuttons.get(i));
                userLayouts.get(i).addView(deleteContactButtons.get(i));
                userLayouts.get(i).addView(spaces.get(i));
                ll.addView(userLayouts.get(i));

            }
        }catch (Exception e){
            sUser.append(e.toString());
        }
    }

     /*----------------------------------- GET auf ALLE USER (SUCHE) ---------- -------------------------------------
    * -----------------------------------------------------------------------------------------------------------*/

    public class HttpRestGetotherUsers  extends AsyncTask<String, Void, String> {
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
            try {
                onpostexgetusers(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    void onpostexgetusers(String data) throws JSONException{
        JSONObject jsonData = new JSONObject(data);
        JSONArray jsonContacts = jsonData.getJSONArray("users");

        userButtons.clear();


        createuserButtons(jsonContacts.length());


        for (int i = 0; i < jsonContacts.length(); i++) {
            JSONObject jsonContact = jsonContacts.getJSONObject(i);
            final String username = jsonContact.getString("username");


            if(username.contains(sUser.getText().toString()) && !username.equals(usernameSpeicher) ){
                userButtons.get(i).getBackground().setColorFilter(Color.parseColor("#4EB9FF"), PorterDuff.Mode.DARKEN);
                userButtons.get(i).setTextColor(Color.parseColor("#222222"));
                userButtons.get(i).setText(username);
                userButtons.get(i).setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View view) {
                       userAdd(username);

                    }
                });
                rl.addView(userButtons.get(i));
            }

        }
    }

    void userAdd(String username){
        HttpRestPost postuser = new HttpRestPost();
        postuser.execute("http://5.199.129.74:81/user/"+usernameSpeicher+"/contact", "{\"contactname\":\""+ username +"\",\"akzeptiert\":\"nein\"}");
        sUser.setText("");

    }

     /*----------------------------------- POST (ANDEREN NUTZER HINZUFÜGEN)---------- -------------------------------------
    * -----------------------------------------------------------------------------------------------------------*/

    public class HttpRestPost  extends AsyncTask<String, Void, String> {

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
        HttpRestGet getfriends = new HttpRestGet();
            String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

            getfriends.execute("http://5.199.129.74:81/userAID/" + androidID);
        }
    }

     /*----------------------------------- DELETE (vorhandenen Kontakt löschen)---------- -------------------------------------
    * -----------------------------------------------------------------------------------------------------------*/

    public class HttpRestDelete  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String userDaten;

        String post(String url) throws IOException {

            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .build();

            Response response = client.newCall(request).execute();
            return response.body().string();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                userDaten = post(params[0]);

            } catch (Exception e) {
                userDaten = e.toString();
            }

            return userDaten;
        }

        @Override
        protected void onPostExecute(String res) {
            Toast.makeText(getApplicationContext(),res,Toast.LENGTH_SHORT);

            String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            HttpRestGet restge = new HttpRestGet();
            restge.execute("http://5.199.129.74:81/userAID/" + androidID);
        }
    }

      /*-GET auf die Kontakte deines Kontaktes(überprüfung ob der andere Kontakt einen hinzugefügt hat)--------
   * -----------------------------------------------------------------------------------------------------------*/

    public class HttpRestGet3  extends AsyncTask<String, Void, String> {
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
            if(res.contains("Der Contact mit dem Usernamen")){

            }else{
                try {
                    onpostcheckContactAdd(res);
                } catch (JSONException e) {
                   sUser.append("+++++"+e.toString()+"+++++");
                }
            }
        }

        void onpostcheckContactAdd(String data) throws JSONException{

            JSONObject jsonData = new JSONObject(data);
            JSONObject user = jsonData.getJSONObject("newData");

            String url = user.getString("url");
            int count = 0;
            String contactname ="";

            for(int i = 0;i < url.length();i++){
                String s = "" + url.charAt(i);


                if(count == 4){
                    if(s.contains("/")){
                        break;
                    }else {
                        contactname = contactname + url.charAt(i);
                    }
                }
                if(s.contains("/")) {
                    count++;
                }


            }

            HttpRestPut contactAendern = new HttpRestPut();
            contactAendern.execute("http://5.199.129.74:81/user/"+usernameSpeicher+"/contact/"+contactname, "{\"contactname\":\""+ contactname +"\",\"akzeptiert\":\"ja\"}");
        }
    }

     /*----------------------------------- PUT (contactDaten werden aktualisiert )------------------------------
    * -----------------------------------------------------------------------------------------------------------*/

    public class HttpRestPut  extends AsyncTask<String, Void, String> {

        OkHttpClient client = new OkHttpClient();
        String userDaten;

        String post(String url, String json) throws IOException {

            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
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
           if(res.contains("Kontaktdaten wurden geändert!")){
               onpostPut();
           }else{
               Toast.makeText(getApplicationContext(), "Fehler!", Toast.LENGTH_SHORT).show();
           }
        }
    }
    void onpostPut(){
        String androidID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        HttpRestGet restg = new HttpRestGet();
        restg.execute("http://5.199.129.74:81/userAID/" + androidID);
    }

}