package com.example.android.firebaseuserandmessageapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.firebaseuserandmessageapplication.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String userIdPreferenceKey = "userIDPreferenceKey";
    private static final String userDisplayNamePreferenceKey = "userDisplayNamePreferenceKey";

    public static FirebaseDatabase database;
    public static DatabaseReference usersRef;
    public static DatabaseReference incomingRequestsRef;
    public static DatabaseReference outgoingRequestsRef;
    public static DatabaseReference incomingMessagesRef;
    public static DatabaseReference outgoingMessagesRef;
    //    public static DatabaseReference acceptedMessagesRef;
    public static DatabaseReference wonMessagesRef;
    public static DatabaseReference lostMessagesRef;
    public static DatabaseReference friendsRef;
    public static DatabaseReference acceptedMessagesRef;
    public static ArrayList<String> existingUsers;
    public static ArrayList<String> existingUserIds;
    public static HashMap<String, String> idFromUsers;
    public static HashMap<String, String> userFromIDs;
    public static HashMap<String, String> nameFromIDs;
    public static HashMap<String, String> currentIncomingRequests;
    public static HashMap<String, String> currentOutgoingRequests;
    public static ArrayList<Proposition> currentIncomingMessagesArrayList;
    public static ArrayList<Proposition> currentAcceptedMessagesArrayList;
    public static ArrayList<Proposition> currentWonMessagesArrayList;
    public static ArrayList<Proposition> currentLostMessagesArrayList;
    public static HashMap<String, String> currentOutgoingMessages;
    public static HashMap<String, String> currentFriends;
//    public static HashMap<String, String> currentPropositions;
    private boolean validUsername = false;
    private boolean validRecipient = false;
    private boolean validMessage = false;
    private final static int userNameMaximumLength = 15;
    private MyBroadcastReceiver receiver;
    public static String username;
    public static String userId;
    public static String userDisplayName;
    public static Context context;
    ChildEventListener friendsRefListener;
    ChildEventListener usersRefListener;
    ChildEventListener outgoingRequestsRefListener;
    ChildEventListener incomingRequestsRefListener;
    ChildEventListener outgoingMessagesRefListener;
    ChildEventListener incomingMessagesRefListener;
    ChildEventListener acceptedMessagesRefListener;
    ChildEventListener wonMessagesRefListener;
    ChildEventListener lostMessagesRefListener;
//    ChildEventListener propositionsRefListener;

    private RequestsAdapter requestsAdapter;
    private IncomingMessagesAdapter incomingMessagesAdapter;
    private AcceptedMessagesAdapter acceptedMessagesAdapter;
    private Menu menu;
    ActivityMainBinding binding;

    String state;

    int testValue;
    String STATE_USER_ID = "state_user_id";
    String STATE_USER_DISPLAY_NAME = "state_user_display_name";

    boolean previouslyLoggedIn;

    ArrayList<String> filteredByTextList;
    ArrayList<String> filteredByValidityList;
    public static EditText recipient;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onSaveInstanceState");
        savedInstanceState.putString(STATE_USER_ID, userId);
        savedInstanceState.putString(STATE_USER_DISPLAY_NAME, userDisplayName);
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate");
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState != null");
            userId = savedInstanceState.getString(STATE_USER_ID);
            userDisplayName = savedInstanceState.getString(STATE_USER_DISPLAY_NAME);
            if (userId == null) {
                previouslyLoggedIn = false;
            } else {
                previouslyLoggedIn = true;
            }
        } else if (settings.getString(userIdPreferenceKey, null) != null ) {
            userId = settings.getString(userIdPreferenceKey, null);
            previouslyLoggedIn = true;
            userDisplayName = settings.getString(userDisplayNamePreferenceKey, null);
        } else {
            Log.d(TAG, "savedInstanceState == null");
            // Probably initialize members with default values for a new instance
            previouslyLoggedIn = false;
        }


//        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        state = "normalState";

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        //Get user and see if logged in or not
//        user = FirebaseAuth.getInstance().getCurrentUser();
        database = FirebaseDatabase.getInstance();

        currentFriends = new HashMap<>();
        //I believe this is useless
//        currentPropositions = new HashMap<>();
        currentOutgoingRequests = new HashMap<>();
        currentIncomingRequests = new HashMap<>();
        currentOutgoingMessages = new HashMap<>();
        currentIncomingMessagesArrayList = new ArrayList<>();
        currentAcceptedMessagesArrayList = new ArrayList<>();
        currentWonMessagesArrayList = new ArrayList<>();
        currentLostMessagesArrayList = new ArrayList<>();

        binding.rvRequests.setLayoutManager(new LinearLayoutManager(this));
        binding.rvIncomingMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAcceptedMessages.setLayoutManager(new LinearLayoutManager(this));

        context = this;

        binding.currentUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, UserDetailActivity.class);
                intent.putExtra("user_extra", binding.currentUser.getText());
                intent.putExtra("username_extra", binding.currentUsername.getText());
                intent.putExtra("won_propositions_extra", binding.wonBetsCount.getText());
                intent.putExtra("lost_propositions_extra", binding.lostBetsCount.getText());
                startActivity(intent);
            }
        });

        friendsRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getFriends: onChildAdded-- current friends was " + currentFriends.keySet().toString());
                currentFriends.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getFriends: onChildAdded-- current friends now " + currentFriends.keySet().toString());
                updateFriendsCount();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getFriends: onChildChanged-- current friends was " + currentFriends.keySet().toString());
                currentFriends.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getFriends: onChildChanged-- current friends now " + currentFriends.keySet().toString());
                updateFriendsCount();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriends: onChildRemoved-- current friends was " + currentFriends.keySet().toString());
                currentFriends.remove((String) dataSnapshot.getKey());
                Log.d(TAG, "getFriends: onChildRemoved-- current friends now " + currentFriends.keySet().toString());
                updateFriendsCount();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        outgoingRequestsRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getOutgoingRequests: onChildAdded-- currentOutgoingRequests was "
                        + currentOutgoingRequests.keySet().toString());
                currentOutgoingRequests.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("fromName").getValue());

                Log.d(TAG, "getOutgoingRequests: onChildAdded-- currentOutgoingRequests now "
                        + currentOutgoingRequests.keySet().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getOutgoingRequests: onChildChanged-- currentOutgoingRequests was "
                        + currentOutgoingRequests.keySet().toString());
                currentOutgoingRequests.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getOutgoingRequests: onChildChanged-- currentOutgoingRequests now "
                        + currentOutgoingRequests.keySet().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getOutgoingRequests: onChildRemoved-- currentOutgoingRequests was "
                        + currentOutgoingRequests.keySet().toString());
                currentOutgoingRequests.remove((String) dataSnapshot.getKey());
                Log.d(TAG, "getOutgoingRequests: onChildRemoved-- currentOutgoingRequests now "
                        + currentOutgoingRequests.keySet().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        incomingRequestsRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getIncomingRequests: onChildAdded-- currentIncomingRequests was "
                        + currentIncomingRequests.keySet().toString());
                currentIncomingRequests.put((String) dataSnapshot.child("fromId").getValue(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getIncomingRequests: onChildAdded-- currentIncomingRequests now "
                        + currentIncomingRequests.keySet().toString());
                updateRequestsAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getIncomingRequests: onChildChanged-- currentIncomingRequests was "
                        + currentIncomingRequests.keySet().toString());
                currentIncomingRequests.put((String) dataSnapshot.child("fromId").getValue(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getIncomingRequests: onChildChanged-- currentIncomingRequests now "
                        + currentIncomingRequests.keySet().toString());
                updateRequestsAdapter();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getIncomingRequests: onChildRemoved-- currentIncomingRequests was "
                        + currentIncomingRequests.keySet().toString());
                currentIncomingRequests.remove((String) dataSnapshot.child("fromId").getValue());
                Log.d(TAG, "getIncomingRequests: onChildRemoved-- currentIncomingRequests now "
                        + currentIncomingRequests.keySet().toString());
                updateRequestsAdapter();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        outgoingMessagesRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getOutgoingMessages: onChildAdded-- currentOutgoingMessages was "
                        + currentOutgoingMessages.keySet().toString());
                currentOutgoingMessages.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("message").getValue());

                Log.d(TAG, "getOutgoingMessages: onChildAdded-- currentOutgoingMessages now "
                        + currentOutgoingMessages.keySet().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getOutgoingMessages: onChildChanged-- currentOutgoingMessages was "
                        + currentOutgoingMessages.keySet().toString());
                currentOutgoingMessages.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("message").getValue());
                Log.d(TAG, "getOutgoingMessages: onChildChanged-- currentOutgoingMessages now "
                        + currentOutgoingMessages.keySet().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getOutgoingMessages: onChildRemoved-- currentOutgoingMessages was "
                        + currentOutgoingMessages.keySet().toString());
                currentOutgoingMessages.remove((String) dataSnapshot.getKey());
                Log.d(TAG, "getOutgoingMessages: onChildRemoved-- currentOutgoingMessages now "
                        + currentOutgoingMessages.keySet().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        incomingMessagesRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildAdded-- currentIncomingMessagesArrayList length was "
                        + currentIncomingMessagesArrayList.size());
                currentIncomingMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildAdded-- currentIncomingMessagesArrayList length now "
                        + currentIncomingMessagesArrayList.size());
                updateMessagesAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildChanged-- currentIncomingMessagesArrayList length was "
                        + currentIncomingMessagesArrayList.size());
                Log.d(TAG, "currentIncomingMessagesArrayList.get(0): " + currentIncomingMessagesArrayList.get(0).timestamp);
                onChildRemoved(dataSnapshot);
                currentIncomingMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildChanged-- currentIncomingMessagesArrayList length now "
                        + currentIncomingMessagesArrayList.size());
                updateMessagesAdapter();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Remove by searching for matching timestamp
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildRemoved-- currentIncomingMessagesArrayList length was "
                        + currentIncomingMessagesArrayList.size());
                String timestamp = (String) dataSnapshot.child("timestamp").getValue();
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildRemoved-- timestamp = " + dataSnapshot.child("timestamp").getValue());
                int indexRemoved = 0;
                for (Proposition proposition: currentIncomingMessagesArrayList) {
                    if (proposition.timestamp.compareTo(timestamp) != 0) {
                        Log.d(TAG, "currentIncomingMessagesArrayList: onChildRemoved-- timestamp is not same at that index, move on");
                        Log.d(TAG, "currentIncomingMessagesArrayList: onChildRemoved-- timestamp at index " + indexRemoved + " is " + proposition.timestamp);
                        indexRemoved += 1;
                    }
                    else {
                        Log.d(TAG, "currentIncomingMessagesArrayList: onChildRemoved-- removing index " + indexRemoved + " timestamp " + proposition.timestamp);
                        currentIncomingMessagesArrayList.remove(indexRemoved);
                        break;
                    }
                }
//                currentIncomingMessagesArrayList.rem(dataSnapshot.child("timestamp").getValue());
                Log.d(TAG, "currentIncomingMessagesArrayList: onChildRemoved-- currentIncomingMessagesArrayList length now "
                        + currentIncomingMessagesArrayList.size());
                updateMessagesAdapter();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        acceptedMessagesRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildAdded-- currentAcceptedMessagesArrayList length was "
                        + currentAcceptedMessagesArrayList.size());
                currentAcceptedMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildAdded-- currentAcceptedMessagesArrayList length now "
                        + currentAcceptedMessagesArrayList.size());
                updateAcceptedMessagesAdapter();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildChanged-- currentAcceptedMessagesArrayList length was "
                        + currentAcceptedMessagesArrayList.size());
                Log.d(TAG, "currentAcceptedMessagesArrayList.get(0): " + currentAcceptedMessagesArrayList.get(0).timestamp);
                onChildRemoved(dataSnapshot);
                currentAcceptedMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildChanged-- currentAcceptedMessagesArrayList length now "
                        + currentAcceptedMessagesArrayList.size());
                updateAcceptedMessagesAdapter();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Remove by searching for matching timestamp
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildRemoved-- currentAcceptedMessagesArrayList length was "
                        + currentAcceptedMessagesArrayList.size());
                String timestamp = (String) dataSnapshot.child("timestamp").getValue();
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildRemoved-- timestamp = " + dataSnapshot.child("timestamp").getValue());
                int indexRemoved = 0;
                for (Proposition proposition: currentAcceptedMessagesArrayList) {
                    if (proposition.timestamp.compareTo(timestamp) != 0) {
                        Log.d(TAG, "currentAcceptedMessagesArrayList: onChildRemoved-- timestamp is not same at that index, move on");
                        Log.d(TAG, "currentAcceptedMessagesArrayList: onChildRemoved-- timestamp at index " + indexRemoved + " is " + proposition.timestamp);
                        indexRemoved += 1;
                    }
                    else {
                        Log.d(TAG, "currentAcceptedMessagesArrayList: onChildRemoved-- removing index " + indexRemoved + " timestamp " + proposition.timestamp);
                        currentAcceptedMessagesArrayList.remove(indexRemoved);
                        break;
                    }
                }
//                currentAcceptedMessagesArrayList.rem(dataSnapshot.child("timestamp").getValue());
                Log.d(TAG, "currentAcceptedMessagesArrayList: onChildRemoved-- currentAcceptedMessagesArrayList length now "
                        + currentAcceptedMessagesArrayList.size());
                updateAcceptedMessagesAdapter();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        wonMessagesRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentWonMessagesArrayList: onChildAdded-- currentWonMessagesArrayList length was "
                        + currentWonMessagesArrayList.size());
                currentWonMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentWonMessagesArrayList: onChildAdded-- currentWonMessagesArrayList length now "
                        + currentWonMessagesArrayList.size());
                updateWonBetCount();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentWonMessagesArrayList: onChildChanged-- currentWonMessagesArrayList length was "
                        + currentWonMessagesArrayList.size());
                Log.d(TAG, "currentWonMessagesArrayList.get(0): " + currentWonMessagesArrayList.get(0).timestamp);
                onChildRemoved(dataSnapshot);
                currentWonMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentWonMessagesArrayList: onChildChanged-- currentWonMessagesArrayList length now "
                        + currentWonMessagesArrayList.size());
                updateWonBetCount();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Remove by searching for matching timestamp
                Log.d(TAG, "currentWonMessagesArrayList: onChildRemoved-- currentWonMessagesArrayList length was "
                        + currentWonMessagesArrayList.size());
                String timestamp = (String) dataSnapshot.child("timestamp").getValue();
                Log.d(TAG, "currentWonMessagesArrayList: onChildRemoved-- timestamp = " + dataSnapshot.child("timestamp").getValue());
                int indexRemoved = 0;
                for (Proposition proposition: currentWonMessagesArrayList) {
                    if (proposition.timestamp.compareTo(timestamp) != 0) {
                        Log.d(TAG, "currentWonMessagesArrayList: onChildRemoved-- timestamp is not same at that index, move on");
                        Log.d(TAG, "currentWonMessagesArrayList: onChildRemoved-- timestamp at index " + indexRemoved + " is " + proposition.timestamp);
                        indexRemoved += 1;
                    }
                    else {
                        Log.d(TAG, "currentWonMessagesArrayList: onChildRemoved-- removing index " + indexRemoved + " timestamp " + proposition.timestamp);
                        currentWonMessagesArrayList.remove(indexRemoved);
                        break;
                    }
                }
//                currentWonMessagesArrayList.rem(dataSnapshot.child("timestamp").getValue());
                Log.d(TAG, "currentWonMessagesArrayList: onChildRemoved-- currentWonMessagesArrayList length now "
                        + currentWonMessagesArrayList.size());
                updateWonBetCount();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        lostMessagesRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentLostMessagesArrayList: onChildAdded-- currentLostMessagesArrayList length was "
                        + currentLostMessagesArrayList.size());
                currentLostMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentLostMessagesArrayList: onChildAdded-- currentLostMessagesArrayList length now "
                        + currentLostMessagesArrayList.size());
                updateLostBetCount();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "currentLostMessagesArrayList: onChildChanged-- currentLostMessagesArrayList length was "
                        + currentLostMessagesArrayList.size());
                Log.d(TAG, "currentLostMessagesArrayList.get(0): " + currentLostMessagesArrayList.get(0).timestamp);
                onChildRemoved(dataSnapshot);
                currentLostMessagesArrayList.add(new Proposition((String) dataSnapshot.child("timestamp").getValue(),
                        (String) dataSnapshot.child("otherUserId").getValue(),
                        (String) dataSnapshot.child("message").getValue()));
                Log.d(TAG, "currentLostMessagesArrayList: onChildChanged-- currentLostMessagesArrayList length now "
                        + currentLostMessagesArrayList.size());
                updateLostBetCount();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                //Remove by searching for matching timestamp
                Log.d(TAG, "currentLostMessagesArrayList: onChildRemoved-- currentLostMessagesArrayList length was "
                        + currentLostMessagesArrayList.size());
                String timestamp = (String) dataSnapshot.child("timestamp").getValue();
                Log.d(TAG, "currentLostMessagesArrayList: onChildRemoved-- timestamp = " + dataSnapshot.child("timestamp").getValue());
                int indexRemoved = 0;
                for (Proposition proposition: currentLostMessagesArrayList) {
                    if (proposition.timestamp.compareTo(timestamp) != 0) {
                        Log.d(TAG, "currentLostMessagesArrayList: onChildRemoved-- timestamp is not same at that index, move on");
                        Log.d(TAG, "currentLostMessagesArrayList: onChildRemoved-- timestamp at index " + indexRemoved + " is " + proposition.timestamp);
                        indexRemoved += 1;
                    }
                    else {
                        Log.d(TAG, "currentLostMessagesArrayList: onChildRemoved-- removing index " + indexRemoved + " timestamp " + proposition.timestamp);
                        currentLostMessagesArrayList.remove(indexRemoved);
                        break;
                    }
                }
//                currentLostMessagesArrayList.rem(dataSnapshot.child("timestamp").getValue());
                Log.d(TAG, "currentLostMessagesArrayList: onChildRemoved-- currentLostMessagesArrayList length now "
                        + currentLostMessagesArrayList.size());
                updateLostBetCount();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        usersRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getUsers: onChildAdded-- existingUsers was " + existingUsers.toString());
                idFromUsers.put((String) dataSnapshot.child("userName").getValue(),
                        (String) dataSnapshot.child("userId").getValue());
                existingUserIds.add(dataSnapshot.getKey());
                existingUsers.add((String) dataSnapshot.child("userName").getValue());
                userFromIDs.put((String) dataSnapshot.child("userId").getValue(),
                        (String) dataSnapshot.child("userName").getValue());
                nameFromIDs.put((String) dataSnapshot.child("userId").getValue(),
                        (String) dataSnapshot.child("fullName").getValue());
                if (((String) dataSnapshot.child("userId").getValue()).equals(userId)) {
                    binding.currentUsername.setText("@"+(String) dataSnapshot.child("userName").getValue());
                }
                Log.d(TAG, "getUsers: onChildAdded-- existingUsers now " + existingUsers.toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getUsers: onChildChanged-- existingUsers was " + existingUsers.toString());
                idFromUsers.put((String) dataSnapshot.child("userName").getValue(),
                        (String) dataSnapshot.child("userId").getValue());
                userFromIDs.put((String) dataSnapshot.child("userId").getValue(),
                        (String) dataSnapshot.child("userName").getValue());
                nameFromIDs.put((String) dataSnapshot.child("userId").getValue(),
                        (String) dataSnapshot.child("fullName").getValue());
                Log.d(TAG, "getUsers: onChildChanged-- existingUsers now " + existingUsers.toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getUsers: onChildRemoved-- existingUsers was " + existingUsers.toString());
                idFromUsers.remove((String) dataSnapshot.child("userName").getValue());
                userFromIDs.remove((String) dataSnapshot.child("userId").getValue());
                nameFromIDs.remove((String) dataSnapshot.child("userId").getValue());
                existingUsers.remove(dataSnapshot.getKey());
                Log.d(TAG, "getUsers: onChildRemoved-- existingUsers now " + existingUsers.toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "getUsers: onCancelled");
                Log.d(TAG, "Cancelled because-------" + databaseError.getDetails());
                Log.d(TAG, "Cancelled because-------" + databaseError.getMessage());
            }
        };


        //Temporarily comment out and add userspec
        determineLoginStatus();
//        userId = "AWApsS0yRET4RSVciwF6NzTSy0M2";
//        username = "bbb";
//        userDisplayName = "Bobby Brown";
//        addUserSpecificListeners();
        //End temporary changes, toggle comments to undo



        getUsers();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop");
        Log.d(TAG, "adding to shared preferences, userid = " + userId);
        super.onStop();
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(userIdPreferenceKey, userId);
        editor.putString(userDisplayNamePreferenceKey, userDisplayName);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        removeUserSpecificListeners();
        Log.d(TAG, "onDestroy: usersRef.removeEventListener(usersRefListener)");
//        usersRef.removeEventListener(usersRefListener);
        super.onDestroy();
    }

    public void determineLoginStatus() {
        if (previouslyLoggedIn) {
            addUserSpecificListeners();
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
                            .setTheme(R.style.SignInTheme)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (user != null) {
            Log.d(TAG, "onResume: Logged in: Current user: " + userDisplayName);
            binding.currentUser.setText(userDisplayName);
            updateLostBetCount();
            updateWonBetCount();
            FirebaseMessaging.getInstance().subscribeToTopic("user_"+userId);
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_requests) {
            openFriends(null);
            return true;
        } else if (item.getItemId()==R.id.action_sign_out) {
            signOut(null);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
        return true;
    }

    public void addUserSpecificListeners() {
        Log.d(TAG, "addUserSpecificListeners");
        if (userFromIDs!=null && userFromIDs.get(userId)!=null) {
            binding.currentUsername.setText("@"+userFromIDs.get(userId));
        }
//        username = currentUserName();
        getFriends();
        getAcceptedMessages();
        getIncomingRequests();
        getOutgoingRequests();
        getIncomingMessages();
        getOutgoingMessages();
        getWonMessages();
        getLostMessages();
    }

    public void removeUserSpecificListeners() {
        Log.d(TAG, "removeUserSpecificListeners");
        friendsRef.removeEventListener(friendsRefListener);
        outgoingRequestsRef.removeEventListener(outgoingRequestsRefListener);
        incomingRequestsRef.removeEventListener(incomingRequestsRefListener);
        outgoingMessagesRef.removeEventListener(outgoingMessagesRefListener);
        incomingMessagesRef.removeEventListener(incomingMessagesRefListener);
        acceptedMessagesRef.removeEventListener(acceptedMessagesRefListener);
        wonMessagesRef.removeEventListener(wonMessagesRefListener);
        lostMessagesRef.removeEventListener(lostMessagesRefListener);
    }

    public void getFriends() {
        Log.d(TAG, "getFriends");
        if (userId != null) {
//            Log.d(TAG, "getFriends: currentUsername = " + currentUsername);
            Log.d(TAG, "getFriends: userId = " + userId);
            friendsRef = database.getReference("users/" + userId + "/friends");
            friendsRef.addChildEventListener(friendsRefListener);
        }
    }
    public void getAcceptedMessages() {
        Log.d(TAG, "getAcceptedMessages");
        if (userId != null) {
//            Log.d(TAG, "getAcceptedMessages: currentUsername = " + currentUsername);
            Log.d(TAG, "getAcceptedMessages: userId = " + userId);
            acceptedMessagesRef = database.getReference("users/" + userId + "/propositions");
            acceptedMessagesRef.addChildEventListener(acceptedMessagesRefListener);
        }
    }
    public void getOutgoingRequests() {
        Log.d(TAG, "getOutgoingRequests");
        if (userId != null) {
            outgoingRequestsRef = database.getReference("users/" + userId + "/outgoingRequests");
            outgoingRequestsRef.addChildEventListener(outgoingRequestsRefListener);
        }
    }
    public void getIncomingRequests() {
        Log.d(TAG, "getIncomingRequests");
        if (userId != null) {
            Log.d(TAG, "currentUsername != null");
            incomingRequestsRef = database.getReference("users/" + userId + "/incomingRequests");
            incomingRequestsRef.addChildEventListener(incomingRequestsRefListener);
        }
    }
    public void getOutgoingMessages() {
        Log.d(TAG, "getOutgoingMessages");
        if (userId != null) {
            outgoingMessagesRef = database.getReference("users/" + userId + "/outgoingMessages");
            outgoingMessagesRef.addChildEventListener(outgoingMessagesRefListener);
        }
    }
    public void getIncomingMessages() {
        Log.d(TAG, "getIncomingMessages");
        if (userId != null) {
            Log.d(TAG, "currentUsername != null");
            incomingMessagesRef = database.getReference("users/" + userId + "/incomingMessages");
            incomingMessagesRef.addChildEventListener(incomingMessagesRefListener);
        }
    }
    public void getWonMessages() {
        Log.d(TAG, "getWonMessages");
        if (userId != null) {
            Log.d(TAG, "currentUsername != null");
            wonMessagesRef = database.getReference("users/" + userId + "/wonMessages");
            wonMessagesRef.addChildEventListener(wonMessagesRefListener);
        }
    }
    public void getLostMessages() {
        Log.d(TAG, "getLostMessages");
        if (userId != null) {
            Log.d(TAG, "currentUsername != null");
            lostMessagesRef = database.getReference("users/" + userId + "/lostMessages");
            lostMessagesRef.addChildEventListener(lostMessagesRefListener);
        }
    }

    public void updateRequestsAdapter() {
        MenuItem requestsItem = menu.findItem(R.id.action_requests);
        Resources resources = context.getResources();
        if (currentIncomingRequests.size()>5) {
            requestsItem.setIcon(R.drawable.ic_person_black_24dp99);
        } else if (currentIncomingRequests.size()>0) {
            int resourceId = resources.getIdentifier("ic_person_black_24dp"+
                    currentIncomingRequests.size(), "drawable", context.getPackageName());
            requestsItem.setIcon(resources.getDrawable(resourceId));
        } else {
            requestsItem.setIcon(R.drawable.ic_person_black_24dp);
        }

        if (requestsAdapter != null) {
            requestsAdapter.updateAdapter();
        } else {
            requestsAdapter = new RequestsAdapter(this, currentIncomingRequests);
            binding.rvRequests.setAdapter(requestsAdapter);
        }
        if (state.equals("friendsState")) {
            showEmptinessMessage(requestsAdapter.getItemCount(), binding.noFriendRequests);
        }
//        countFriendRequests(requestsAdapter.getItemCount());
    }

    public void updateMessagesAdapter() {
        if (incomingMessagesAdapter != null) {
            incomingMessagesAdapter.updateAdapter();
        } else {
            incomingMessagesAdapter = new IncomingMessagesAdapter(this, currentIncomingMessagesArrayList);
            binding.rvIncomingMessages.setAdapter(incomingMessagesAdapter);
        }
        showEmptinessMessage(incomingMessagesAdapter.getItemCount(), binding.noIncomingMessages);
//        countIncomingMessages(incomingMessagesAdapter.getItemCount());
    }

    public void updateAcceptedMessagesAdapter() {
        if (acceptedMessagesAdapter != null) {
            acceptedMessagesAdapter.updateAdapter();
        } else {
            acceptedMessagesAdapter = new AcceptedMessagesAdapter(this, currentAcceptedMessagesArrayList);
            binding.rvAcceptedMessages.setAdapter(acceptedMessagesAdapter);
        }
        showEmptinessMessage(acceptedMessagesAdapter.getItemCount(), binding.noAcceptedMessages);
//        countAcceptedMessages(acceptedMessagesAdapter.getItemCount());
    }

    public void updateWonBetCount() {
        if (currentWonMessagesArrayList.size()>0) {
            binding.wonBetsCount.setText(String.valueOf(currentWonMessagesArrayList.size()));
        } else {
            binding.wonBetsCount.setText("0");
        }
        if (currentWonMessagesArrayList.size()+currentLostMessagesArrayList.size()>0) {
            binding.totalBetsCount.setText(String.valueOf(currentWonMessagesArrayList.size()+currentLostMessagesArrayList.size()));
        } else {
            binding.totalBetsCount.setText("0");
        }
    }

    public void updateLostBetCount() {
        if (currentLostMessagesArrayList.size()>0) {
            binding.lostBetsCount.setText(String.valueOf(currentLostMessagesArrayList.size()));
        } else {
            binding.lostBetsCount.setText("0");
        }
        if (currentWonMessagesArrayList.size()+currentLostMessagesArrayList.size()>0) {
            binding.totalBetsCount.setText(String.valueOf(currentWonMessagesArrayList.size()+currentLostMessagesArrayList.size()));
        } else {
            binding.totalBetsCount.setText("0");
        }
    }

    public void updateFriendsCount() {
        if (currentFriends.size()>0) {
            binding.totalFriendCount.setText(String.valueOf(currentFriends.size()));
        } else {
            binding.totalFriendCount.setText("0");
        }
    }

    public void getUsers() {
        Log.d(TAG, "getUsers");
        //This should update local data every time a user's data changes in the firebase database
        existingUsers = new ArrayList<>();
        existingUserIds = new ArrayList<>();
        idFromUsers = new HashMap<String, String>();
        userFromIDs = new HashMap<String, String>();
        nameFromIDs = new HashMap<String, String>();
        usersRef = database.getReference("users");
        usersRef.addChildEventListener(usersRefListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                //this is the only time the user is called
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                userDisplayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();
                FirebaseAuth.getInstance().signOut();

                Log.d(TAG, "onActivityResult: Logged in: Current user: " + userDisplayName);
                //Created a new user
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    if (userDisplayName != null && userId != null) {
                        newUsernameDialog(this);
                        Log.d(TAG, "After userNameDialog");
                    }
                } else {
                    //returning user
//                    receiverRegistration();
                    getUsers();
                    addUserSpecificListeners();
                }
                // Successfully signed in
                Log.d(TAG, "Successful sign in");
                Log.d(TAG, "The token is " + FirebaseInstanceId.getInstance().getToken());
            } else {
                Log.d(TAG, "Unsuccessful sign in");
            }
        }
    }

    public static void sendNotificationToUser(String sender, String receiver, final String senderName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        DatabaseReference incomingRequests = ref.child("users").child(receiver).child("incomingRequests").child(sender);
        DatabaseReference outgoingRequests = ref.child("users").child(sender).child("outgoingRequests").child(receiver);

        Map incomingRequest = new HashMap<>();
        incomingRequest.put("fromName", senderName);
        incomingRequest.put("fromId", sender);

        Map outgoingRequest = new HashMap<>();
        outgoingRequest.put("fromName", senderName);
        outgoingRequest.put("fromId", sender);

        incomingRequests.setValue(incomingRequest);
        outgoingRequests.setValue(outgoingRequest);
    }

    public static void sendMessageToUser(String sender, String receiver, String message, String senderName) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        String timestamp = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss").format(new java.util.Date());
        DatabaseReference incomingMessages = ref.child("users").child(receiver).child("incomingMessages").child(timestamp);
        DatabaseReference outgoingMessages = ref.child("users").child(sender).child("outgoingMessages").child(timestamp);

        Proposition incomingMessage = new Proposition(timestamp, sender, message);

        Proposition outgoingMessage = new Proposition(timestamp, receiver, message);

        incomingMessages.setValue(incomingMessage);
        outgoingMessages.setValue(outgoingMessage);
    }

    //Must remove listeners
    public void signOut(View view) {
        //must unsubscribe on sign out
        Log.d(TAG, "signOut");
        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_"+userId);
        removeUserSpecificListeners();

        resetListsAndMaps();
        previouslyLoggedIn = false;

        updateRequestsAdapter();
        updateMessagesAdapter();
        updateAcceptedMessagesAdapter();

//        receiverUnregistration();
//        Intent intent = getIntent();
//        finish();
//        startActivity(intent);

        determineLoginStatus();
    }

    public void resetListsAndMaps() {
       //New data
        username = null;
        userId = null;
        userDisplayName = null;
        currentFriends = new HashMap<>();
        currentOutgoingRequests = new HashMap<>();
        currentIncomingRequests = new HashMap<>();
        currentOutgoingMessages = new HashMap<>();
        currentIncomingMessagesArrayList = new ArrayList<>();
        currentAcceptedMessagesArrayList = new ArrayList<>();
        currentWonMessagesArrayList = new ArrayList<>();
        currentLostMessagesArrayList = new ArrayList<>();
    }

    //To be called after deleting a new account
    public void signOutNewUser() {
        resetListsAndMaps();
//        receiverUnregistration();
//        Intent intent = getIntent();
//        finish();
//        startActivity(intent);
        determineLoginStatus();
    }

    //Only send to friends confirmed
    public void sendMessage(View view) {
        Context context = view.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.propose_to_username_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogView);
        final EditText recipient = (EditText) dialogView.findViewById(R.id.et_message_recipient);
        final TextView recipientPromptTextView = (TextView) dialogView.findViewById(R.id.tv_propose_to_username_prompt);
        final EditText messageEditText = (EditText) dialogView.findViewById(R.id.et_message);
        final TextView messagePromptTextView = (TextView) dialogView.findViewById(R.id.tv_message_prompt);

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String username = recipient.getText().toString();
                        String recipientUserId = idFromUsers.get(username);
                        String message = messageEditText.getText().toString();
                        Log.d(TAG, "Id of this user is " + idFromUsers.get(username));
                        //Version 1
//                        sendNotificationToUser(userFromIDs.get(user.getUid()), userId, "Hello from one user to another!");
                        //Version 2
                        sendMessageToUser(userId, recipientUserId, message, nameFromIDs.get(userId));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(validRecipient&&validMessage);

        recipient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validRecipient = false;
                if (currentFriends.containsKey(idFromUsers.get(charSequence.toString()))) {
                    recipientPromptTextView.setText(R.string.recipient_valid);
                    recipientPromptTextView.append(" "+charSequence.toString());
                    validRecipient = true;
                }
                else if (userId.equals(idFromUsers.get(charSequence.toString()))){
                    recipientPromptTextView.setText(R.string.recipient_is_current_user);
                }
                else if (idFromUsers.get(charSequence.toString()) != null){
                    recipientPromptTextView.setText(R.string.recipient_not_friend);
                    recipientPromptTextView.append(charSequence);
                }
                else {
                    recipientPromptTextView.setText(R.string.recipient_prompt);
                }

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validRecipient&&validMessage);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                validMessage = false;
                if (charSequence.length()<1) {
                    messagePromptTextView.setText(R.string.message_empty);
                }
                else {
                    validMessage = true;
                    messagePromptTextView.setText(R.string.message_prompt);
                }

                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(validRecipient&&validMessage);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public static void sendRequestFromAdapter(String recipientUsername) {

    }

    public void sendRequest(View view) {
        final Context context = view.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.send_to_username_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setView(dialogView);
        recipient = (EditText) dialogView.findViewById(R.id.et_recipient);
        final TextView recipientPromptTextView = (TextView) dialogView.findViewById(R.id.tv_send_to_username_prompt);
        final RecyclerView filteredUsersRecyclerView = (RecyclerView) dialogView.findViewById(R.id.rv_add_friends_suggestion);
        filteredUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //TODO change from allUsers in scroll view to valid users

        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String recipientUsername = recipient.getText().toString();
                        String recipientUserId = idFromUsers.get(recipientUsername);
                        Log.d(TAG, "Id of this user is " + idFromUsers.get(recipientUsername));
                        //Version 1
//                        sendNotificationToUser(userFromIDs.get(user.getUid()), userId, "Hello from one user to another!");
                        //Version 2
                        sendNotificationToUser(userId, recipientUserId, nameFromIDs.get(userId));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
                filteredByTextList = Lists.newArrayList(Collections2.filter(
                        existingUsers, Predicates.containsPattern(charSequence.toString())));
                filteredByValidityList = new ArrayList<>();

                if (filteredByTextList.size()>0) {
                    for (String user : filteredByTextList) {
                        Log.d(TAG, "user = " + user);
                        Log.d(TAG, "username = " + userFromIDs.get(userId));
                        if (currentFriends.containsKey(idFromUsers.get(user)) || user.equals(userFromIDs.get(userId))) {
                            Log.d(TAG, "current friends.get(user) = " + currentFriends.get(user));
//                            filteredByTextList.remove(user);
                        }
                        else {
                            filteredByValidityList.add(user);
                        }
                    }
                }
                if (filteredByValidityList.size() > 2) {
                    filteredByValidityList = new ArrayList<String>(filteredByValidityList.subList(0,2));
                }
                if (charSequence.length()==0) {
                    Log.d(TAG, "charSequence empty");
                    filteredByValidityList.clear();
                }
                filteredUsersRecyclerView.setAdapter(new AddFriendAdapter(context, filteredByValidityList));
                filteredUsersRecyclerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                });

                if (currentOutgoingRequests.containsKey(idFromUsers.get(charSequence.toString()))) {
                    recipientPromptTextView.setText(R.string.recipient_already_requested);
                }
                else if (currentFriends.containsKey(idFromUsers.get(charSequence.toString()))) {
                    recipientPromptTextView.setText(R.string.recipient_already_friends);
                    recipientPromptTextView.append(" " +charSequence.toString());
                }
                else if (currentIncomingRequests.containsKey(idFromUsers.get(charSequence.toString()))) {
                    recipientPromptTextView.setText(R.string.recipient_requested_sender);
                }
                else if (charSequence.toString().equals(username)) {
                    recipientPromptTextView.setText(R.string.recipient_is_current_user);
                }
                else if (existingUsers.contains(charSequence.toString())) {
                    recipientPromptTextView.setText(R.string.recipient_prompt);
                    validRecipient = true;
                }
                else if (charSequence.length() == 0) {
                    recipientPromptTextView.setText(R.string.recipient_prompt);
                }
                else {
                    recipientPromptTextView.setText(R.string.recipient_inexistent);
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
//                        receiverRegistration();
                    }
                });
        //TODO Figure out how to delete user
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.cancel();
//                        user.delete();
//                        signOutNewUser();
//                    }
//                });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
                else if (existingUsers.contains(charSequence.toString())) {
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

    //Set currentUsername in firebase, called after creating new account
    public void setUserName(String username) {
        Log.d(TAG, "Uid = " + userId);
        usersRef.child(userId).setValue((new User(userId, userDisplayName, username)));
        //A new user was just added
        addUserSpecificListeners();
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String username = userFromIDs.get(userId);

            String message = intent.getStringExtra(Intent.EXTRA_TEXT);
            String from = intent.getStringExtra("from_extra");
            binding.receivedMessage.setText(message);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference users = ref.child("users");
            DatabaseReference userRef = users.child(username);

//            Map friend = new HashMap<>();
//            friend.put(from, "Unconfirmed");

            userRef.child("friend").child(from).setValue("unconfirmed");
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

    public void openFriends(View view) {
        state = "friendsState";
        binding.rvRequests.setVisibility(View.VISIBLE);
        binding.closeFriends.setVisibility(View.VISIBLE);
        binding.friendRequests.setVisibility(View.VISIBLE);
        binding.noFriendRequests.setVisibility(View.VISIBLE);
        binding.betsLabel.setVisibility(View.GONE);
        binding.winsLabel.setVisibility(View.GONE);
        binding.friendsLabel.setVisibility(View.GONE);
        binding.totalBetsCount.setVisibility(View.GONE);
        binding.wonBetsCount.setVisibility(View.GONE);
        binding.totalFriendCount.setVisibility(View.GONE);
        binding.profileImage.setVisibility(View.GONE);

        binding.rvIncomingMessages.setVisibility(View.GONE);
        binding.rvAcceptedMessages.setVisibility(View.GONE);
        binding.acceptedMessages.setVisibility(View.GONE);
        binding.incomingMessages.setVisibility(View.GONE);
        binding.currentUser.setVisibility(View.GONE);
        binding.currentUsername.setVisibility(View.GONE);
        binding.wonBetsCount.setVisibility(View.GONE);
        binding.lostBetsCount.setVisibility(View.GONE);
        binding.noIncomingMessages.setVisibility(View.GONE);
        binding.noAcceptedMessages.setVisibility(View.GONE);

        updateRequestsAdapter();
//        showEmptinessMessage(requestsAdapter.getItemCount(), binding.noFriendRequests);
//        countFriendRequests(requestsAdapter.getItemCount());
    }

    public void closeFriends(View view) {
        state = "normalState";
        binding.rvRequests.setVisibility(View.INVISIBLE);
        binding.closeFriends.setVisibility(View.INVISIBLE);
        binding.friendRequests.setVisibility(View.INVISIBLE);
        binding.noFriendRequests.setVisibility(View.INVISIBLE);
        binding.betsLabel.setVisibility(View.VISIBLE);
        binding.winsLabel.setVisibility(View.VISIBLE);
        binding.friendsLabel.setVisibility(View.VISIBLE);
        binding.totalBetsCount.setVisibility(View.VISIBLE);
        binding.wonBetsCount.setVisibility(View.VISIBLE);
        binding.totalFriendCount.setVisibility(View.VISIBLE);
        binding.profileImage.setVisibility(View.VISIBLE);
        binding.rvIncomingMessages.setVisibility(View.VISIBLE);
        binding.rvAcceptedMessages.setVisibility(View.VISIBLE);
        binding.acceptedMessages.setVisibility(View.VISIBLE);
        binding.incomingMessages.setVisibility(View.VISIBLE);
        binding.currentUsername.setVisibility(View.VISIBLE);
        binding.wonBetsCount.setVisibility(View.VISIBLE);
        binding.currentUser.setVisibility(View.VISIBLE);
        showEmptinessMessage(currentIncomingMessagesArrayList.size(), binding.noIncomingMessages);
        showEmptinessMessage(currentAcceptedMessagesArrayList.size(), binding.noAcceptedMessages);
    }

    public void showEmptinessMessage(int count, View view) {
        if (count == 0) {
            view.setVisibility(View.VISIBLE);
        }
        else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.addFriendFabHint.getVisibility()==View.VISIBLE) {
            toggleAddOptions(null);
        }
        else if (binding.rvRequests.getVisibility()==View.VISIBLE) {
            closeFriends(null);
        }
        else {
            super.onBackPressed();
        }
    }

    public void toggleAddOptions(View view) {
        if (binding.fabAddProposition.getVisibility()==View.VISIBLE) {
            binding.fabAddFriend.setVisibility(View.GONE);
            binding.fabAddProposition.setVisibility(View.GONE);
            binding.addFriendFabHint.setVisibility(View.GONE);
            binding.newPropositionFabHint.setVisibility(View.GONE);

        } else {
            binding.fabAddFriend.setVisibility(View.VISIBLE);
            binding.fabAddProposition.setVisibility(View.VISIBLE);
            binding.addFriendFabHint.setVisibility(View.VISIBLE);
            binding.newPropositionFabHint.setVisibility(View.VISIBLE);
        }
    }

    public void addFriend(View view) {
        sendRequest(view);
        toggleAddOptions(view);
    }
    public void addProposition(View view) {
        sendMessage(view);
        toggleAddOptions(view);
    }


    public void makeToast(String text) {
        Toast toast = new Toast(this);
        toast.setText(text);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }

}
