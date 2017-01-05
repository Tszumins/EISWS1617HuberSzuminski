package de.walkhome.walkhome;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created by yannikhuber on 17.11.16.
 */

public class UserSettings extends Activity {
Button btnZurueck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnZurueck = (Button) findViewById(R.id.buttonZurueck);

        btnZurueck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                Intent intent = new Intent(UserSettings.this, MapsActivity.class);

                startActivity(intent);


            }
        });
    }
}
