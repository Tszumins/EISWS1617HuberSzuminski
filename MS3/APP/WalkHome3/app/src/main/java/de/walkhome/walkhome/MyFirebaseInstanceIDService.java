
package de.walkhome.walkhome;

/**
 * Created by tobiasszuminski on 13.01.17.
 */

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {



    private static final String TAG = "MyFirebaseIIDService";


    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();


        final Intent intent = new Intent("tokenReceiver");
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        intent.putExtra("token", refreshedToken);
        broadcastManager.sendBroadcast(intent);



        sendRegistrationToServer(refreshedToken);
    }


    private void sendRegistrationToServer(String token) {

        Log.d(TAG,"Dein Token ist :" + token);
    }
}