package com.chatlang.libraries;

import com.chatlang.SessionManager;
import com.chatlang.UtilityClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    SessionManager session;

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        System.out.println("mytag1 firebase_token"+token);
        //whenever a new token is generated, save copy to firebase database

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> userz = session.getUserDetails();
        String userbind = userz.get(SessionManager.KEY_TOKEN);

        UtilityClass.updateFirebaseTokenMethod(userbind, token, this);
    }
}
