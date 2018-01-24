package com.example.android.firebaseuserandmessageapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference userRef;
    private ArrayList<String> existingUsernames;
    private HashMap<String, String> idFromUsers;
    private HashMap<String, String> userFromIDs;
    private static TextView receivedMessageView;
    private static TextView currentUserView;
    private boolean validUsername = false;
    private boolean validRecipient = false;
    private final static int userNameMaximumLength = 15;
    private FirebaseUser user;
    private String currentUserName;
    private MyBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "OnCreate");


        receivedMessageView = findViewById(R.id.received_message);
        currentUserView = findViewById(R.id.current_user);

        database = FirebaseDatabase.getInstance();
        existingUsernames = new ArrayList<>();
        idFromUsers = new HashMap<String, String>();
        userFromIDs = new HashMap<String, String>();
        usersRef = database.getReference("users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                idFromUsers = new HashMap<>();
                userFromIDs = new HashMap<>();
                existingUsernames = new ArrayList<>();
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    Log.d(TAG, "cycling through datasnapshots");
                    existingUsernames.add((String) singleSnapshot.child("userName").getValue());
                    idFromUsers.put(
                            (String) singleSnapshot.child("userName").getValue(),
                            (String) singleSnapshot.child("userId").getValue());
                    userFromIDs.put(
                            (String) singleSnapshot.child("userId").getValue(),
                            (String) singleSnapshot.child("userName").getValue());
                    Log.d(TAG, "the child username has value " + singleSnapshot.child("userName").getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

//        oldRef.setValue("Hello, World!");

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
//            String userId = user.getUid();
////            FirebaseMessaging.getInstance().subscribeToTopic("user_"+userId);
//            Log.d(TAG, "Logged in");
            receiverRegistration();
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

    public void receiverRegistration() {
        Log.d(TAG, "Registering receiver");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.android.firebaseuserandmessageapp.onMessageReceivedGETRIDOFTHIS");
        receiver = new MyBroadcastReceiver();
        registerReceiver(receiver, intentFilter);
    }

    public void receiverUnregistration() {
        Log.d(TAG, "Unregistering receiver");
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            currentUserView.setText("Welcome, " + user.getDisplayName());
            String userId = user.getUid();
            FirebaseMessaging.getInstance().subscribeToTopic("user_"+userId);
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
                    }
                } else {
                    //returning user
                    receiverRegistration();
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

//
    public static void sendNotificationToUser(String sender, String userId, final String message) {
        Log.d(TAG, "attempting sendNotificationToUser");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        DatabaseReference notifications = ref.child("notificationRequests");
//        notifications.setValue("Testing");

        Map notification = new HashMap<>();
        notification.put("username", userId);
        notification.put("message", message);
        notification.put("from", sender);

        notifications.push().setValue(notification);
    }

    public void signOut(View view) {
        //must unsubscribe on sign out
        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_"+user.getUid());
        receiverUnregistration();
        FirebaseAuth.getInstance().signOut();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    //Version 2
    protected void sendMessage(View view) {
        Context context = view.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.send_to_username_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogView);
        final EditText recipient = (EditText) dialogView.findViewById(R.id.et_recipient);
        final TextView recipientPromptTextView = (TextView) dialogView.findViewById(R.id.tv_send_to_username_prompt);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String username = recipient.getText().toString();
                        String userId = idFromUsers.get(username);
                        Log.d(TAG, "Id of this user is " + idFromUsers.get(username));

                        sendNotificationToUser(userFromIDs.get(user.getUid()), userId, "Hello from one user to another!");
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(validRecipient);

        recipient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validRecipient = false;
                if (existingUsernames.contains(charSequence.toString())) {
                    recipientPromptTextView.setText(R.string.recipient_prompt);
                    validRecipient = true;
                }
                else {
                    recipientPromptTextView.setText(R.string.recipient_inexistent);
                    validRecipient = false;
                }

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validRecipient);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        sendNotificationToUser("john", "Hello");
    }

    public void newUsernameDialog(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.create_username_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogView);

        final EditText editText = (EditText) dialogView.findViewById(R.id.et_user_input);
        final TextView usernamePromptTextView = (TextView) dialogView.findViewById(R.id.tv_new_username_prompt);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setUserName(editText.getText().toString());
                        receiverRegistration();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO delete user from Users in firebase
                        dialogInterface.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(validUsername);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validUsername = false;
                if (charSequence.toString().contains(" ")) {
                    usernamePromptTextView.setText(R.string.username_bad_characters);
                    Log.d(TAG, getString(R.string.username_bad_characters));
                }
                else if (charSequence.length()>userNameMaximumLength) {
                    usernamePromptTextView.setText(R.string.username_long);
                }
                else if (existingUsernames.contains(charSequence.toString())) {
                    usernamePromptTextView.setText(R.string.username_taken);
                }
                else if (charSequence.length()==0) {
                    usernamePromptTextView.setText(R.string.username_empty);
                }
                else {
                    usernamePromptTextView.setText(R.string.new_username_prompt);
                    validUsername = true;
                }

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validUsername);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void setUserName(String username) {
        Log.d(TAG, "Uid = " + user.getUid());
        usersRef.child(username).setValue((new User(user.getUid(), user.getDisplayName(), username)));
    }

    public void getUserName() {
//        usersRef.g
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String username = userFromIDs.get(user.getUid());

            String message = intent.getStringExtra(Intent.EXTRA_TEXT);
            String from = intent.getStringExtra("from_extra");
            receivedMessageView.setText(message);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference users = ref.child("users");
            DatabaseReference user = users.child(username);

//            Map friend = new HashMap<>();
//            friend.put(from, "Unconfirmed");

            user.child("friend").child(from).setValue("unconfirmed");
        }
    }
}
