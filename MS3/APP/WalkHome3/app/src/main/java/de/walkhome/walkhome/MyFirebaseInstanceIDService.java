package de.walkhome.walkhome;

/**
 * Created by tobiasszuminski on 13.01.17.
 */

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public String shareToken;

    private static final String TAG = "MyFirebaseIIDService";


    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        shareToken = refreshedToken;

        sendRegistrationToServer(refreshedToken);
    }


    private void sendRegistrationToServer(String token) {

        Log.d(TAG,"Dein Token ist :" + token);
    }
}