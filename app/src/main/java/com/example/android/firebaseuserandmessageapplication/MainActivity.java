package com.example.android.firebaseuserandmessageapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.io.Console;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SENDER_ID = "1041829635688";
    private static int msgId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String username = "john";
        FirebaseMessaging.getInstance().subscribeToTopic("user_"+username);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Log.d(TAG, "Logged in");
        } else {
            Log.d(TAG, "Not logged in");
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build());

// Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    RC_SIGN_IN);
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

//        1041829635688

// ...


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                Log.d(TAG, "Successful sign in");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "The token is " + FirebaseInstanceId.getInstance().getToken());
                // ...
            } else {
                Log.d(TAG, "Unsuccessful sign in");

                // Sign in failed, check response for error code
                // ...
            }
        }
    }
    //Version 1
//    protected void sendMessage(View view) {
//        FirebaseMessaging fm = FirebaseMessaging.getInstance();
//        fm.send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
//                .setMessageId(Integer.toString(incrementAndGet()))
//                .addData("my_message", "Hello Andrew")
//                .addData("my_action","SAY_HELLO")
//                .build());
//    }

    //Version 2
    protected void sendMessage(View view) {
        sendNotificationToUser("john", "Hello");
    }
//
    public static void sendNotificationToUser(String user, final String message) {
        Log.d(TAG, "attempting sendNotificationToUser");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference notifications = ref.child("notificationRequests");
        notifications.setValue("Testing");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);

//        notifications.push().setValue(notification);
        notifications.push().setValue(notification);
    }

    protected int incrementAndGet() {
        msgId += 1;
        return msgId;
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
    }
}
