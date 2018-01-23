package com.example.android.firebaseuserandmessageapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String SENDER_ID = "1041829635688";
    private static int msgId = 0;
    private DatabaseReference userRef;
    private FirebaseDatabase database;
    private ArrayList<String> existingUsernames;
    private boolean creatingUsername = true;
    private final static int userNameMinimumLength = 6;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance();
        existingUsernames = new ArrayList<>();
        userRef = database.getReference("users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "cycling through datasnapshots");
                    existingUsernames.add((String) singleSnapshot.child("userName").getValue());
                    Log.d(TAG, "the child username has value " + singleSnapshot.child("userName").getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        oldRef.setValue("Hello, World!");

        String username = "john";
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("user_"+username);
            Log.d(TAG, "Logged in");
        }

        else {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                user = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseUserMetadata metadata = user.getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    if (user.getDisplayName() != null && user.getUid() != null) {
                        newUsernameDialog(this);
                        Log.d(TAG, "After userNameDialog");
//                        if (newUsernameDialog(this)) {
//                            userRef.child(user.getDisplayName()).setValue((new User(user.getUid(), user.getDisplayName())));
//                        }
                    }
                }
                // Successfully signed in
                Log.d(TAG, "Successful sign in");
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "The token is " + FirebaseInstanceId.getInstance().getToken());
            } else {
                Log.d(TAG, "Unsuccessful sign in");
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

        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);

//        notifications.push().setValue(notification);
        notifications.push().setValue(notification);
    }

    public void signOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void newUsernameDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.create_username_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogView);

        boolean uniqueUsername = false;
        final EditText editText = dialogView.findViewById(R.id.et_user_input);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setUserName(editText.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        creatingUsername = false;
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().contains(" ")) {
                    Log.d(TAG, "Please pick a username without spaces");
                }
                else if (charSequence.length()<userNameMinimumLength) {
                    Log.d(TAG, "Please pick a username of at least 6 characters");
                }
                else if (existingUsernames.contains(charSequence.toString())) {
                    Log.d(TAG, "This username already exists: " + charSequence);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setUserName(String username) {


    }
}
