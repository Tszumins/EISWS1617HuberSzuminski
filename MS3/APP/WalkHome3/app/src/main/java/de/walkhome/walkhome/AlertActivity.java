package de.walkhome.walkhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by yannikhuber on 04.01.17.
 */

public class AlertActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AlertDialog alertDialog = new AlertDialog.Builder(this).setPositiveButton("Ja", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //counterAbweichung = 11; //wenn der Nutzer sagt dass alles okay ist, wird der counter auf 11 gesetzt und erst wieder auf 0 wenn der User wieder auf der strecke ist
            }
        }).setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //sende Alarm an Notfallkontakte
                    }
                }).create();
        alertDialog.setTitle("your title");
        alertDialog.setMessage("your message");
        alertDialog.setIcon(R.drawable.cast_ic_notification_small_icon);

        alertDialog.show();
    }
}
