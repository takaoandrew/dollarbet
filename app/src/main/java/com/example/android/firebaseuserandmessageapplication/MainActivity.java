package com.example.android.firebaseuserandmessageapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
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

import com.example.android.firebaseuserandmessageapplication.databinding.ActivityMainBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    private FirebaseUser user;
    private MyBroadcastReceiver receiver;
    public static String username;
    public static String userId;
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
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Log.d(TAG, "OnCreate");

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        //Get user and see if logged in or not
        user = FirebaseAuth.getInstance().getCurrentUser();
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
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAcceptedMessages.setLayoutManager(new LinearLayoutManager(this));

        friendsRefListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getFriends: onChildAdded-- current friends was " + currentFriends.keySet().toString());
                currentFriends.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getFriends: onChildAdded-- current friends now " + currentFriends.keySet().toString());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "getFriends: onChildChanged-- current friends was " + currentFriends.keySet().toString());
                currentFriends.put((String) dataSnapshot.getKey(),
                        (String) dataSnapshot.child("fromName").getValue());
                Log.d(TAG, "getFriends: onChildChanged-- current friends now " + currentFriends.keySet().toString());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "getFriends: onChildRemoved-- current friends was " + currentFriends.keySet().toString());
                currentFriends.remove((String) dataSnapshot.getKey());
                Log.d(TAG, "getFriends: onChildRemoved-- current friends now " + currentFriends.keySet().toString());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
//
//        propositionsRefListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "getAcceptedMessages: onChildAdded-- current propositions was " + currentPropositions.keySet().toString());
//                currentPropositions.put((String) dataSnapshot.getKey(),
//                        (String) dataSnapshot.child("fromName").getValue());
//                Log.d(TAG, "getAcceptedMessages: onChildAdded-- current propositions now " + currentPropositions.keySet().toString());
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "getAcceptedMessages: onChildChanged-- current propositions was " + currentPropositions.keySet().toString());
//                currentPropositions.put((String) dataSnapshot.getKey(),
//                        (String) dataSnapshot.child("fromName").getValue());
//                Log.d(TAG, "getAcceptedMessages: onChildChanged-- current propositions now " + currentPropositions.keySet().toString());
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "getAcceptedMessages: onChildRemoved-- current propositions was " + currentPropositions.keySet().toString());
//                currentPropositions.remove((String) dataSnapshot.getKey());
//                Log.d(TAG, "getAcceptedMessages: onChildRemoved-- current propositions now " + currentPropositions.keySet().toString());
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
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
//
//        incomingMessagesRefListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "getIncomingMessages: onChildAdded-- currentIncomingMessages was "
//                        + currentIncomingMessages.keySet().toString());
//                currentIncomingMessages.put((String) dataSnapshot.child("fromId").getValue(),
//                        (String) dataSnapshot.child("message").getValue());
//                Log.d(TAG, "getIncomingMessages: onChildAdded-- currentIncomingMessages now "
//                        + currentIncomingMessages.keySet().toString());
//                updateMessagesAdapter();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                Log.d(TAG, "getIncomingMessages: onChildChanged-- currentIncomingMessages was "
//                        + currentIncomingMessages.keySet().toString());
//                currentIncomingMessages.put((String) dataSnapshot.child("fromId").getValue(),
//                        (String) dataSnapshot.child("message").getValue());
//                Log.d(TAG, "getIncomingMessages: onChildChanged-- currentIncomingMessages now "
//                        + currentIncomingMessages.keySet().toString());
//                updateMessagesAdapter();
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//                Log.d(TAG, "getIncomingMessages: onChildRemoved-- currentIncomingMessages was "
//                        + currentIncomingMessages.keySet().toString());
//                currentIncomingMessages.remove((String) dataSnapshot.child("fromId").getValue());
//                Log.d(TAG, "getIncomingMessages: onChildRemoved-- currentIncomingMessages now "
//                        + currentIncomingMessages.keySet().toString());
//                updateMessagesAdapter();
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
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
                updateAcceptedMessagesAdapter();
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
                updateAcceptedMessagesAdapter();
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
                updateAcceptedMessagesAdapter();
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
                updateAcceptedMessagesAdapter();
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
                updateAcceptedMessagesAdapter();
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
                updateAcceptedMessagesAdapter();
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
                if (user.getUid().equals((String) dataSnapshot.child("userId").getValue())) {
                    binding.currentUsername.setText((String) dataSnapshot.child("userName").getValue());
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

            }
        };

        getUsers();

        determineLoginStatus();

    }

    @Override
    protected void onDestroy() {
        removeUserSpecificListeners();
        usersRef.removeEventListener(usersRefListener);
        super.onDestroy();
    }

    public void determineLoginStatus() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
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
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            Log.d(TAG, "onResume: Logged in: Current user: " + user.getDisplayName());
            userId = user.getUid();
            binding.currentUser.setText(user.getDisplayName());
            FirebaseMessaging.getInstance().subscribeToTopic("user_"+userId);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_requests) {
            openFriends(null);
//            Intent aboutIntent = new Intent(this, FriendRequestsActivity.class);
//            startActivity(aboutIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void addUserSpecificListeners() {
        Log.d(TAG, "addUserSpecificListeners");
        userId = user.getUid();
//        username = currentUserName();
        getFriends();
        getAcceptedMessages();
        getIncomingRequests();
        getOutgoingRequests();
        getIncomingMessages();
        getOutgoingMessages();
        getWonMessages();
        getLostMessages();

//        getAcceptedMessages();
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
//            Log.d(TAG, "getFriends: username = " + username);
            Log.d(TAG, "getFriends: userId = " + userId);
            friendsRef = database.getReference("users/" + userId + "/friends");
            friendsRef.addChildEventListener(friendsRefListener);
        }
    }

    public void getAcceptedMessages() {
        Log.d(TAG, "getAcceptedMessages");
        if (userId != null) {
//            Log.d(TAG, "getAcceptedMessages: username = " + username);
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
            Log.d(TAG, "username != null");
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
            Log.d(TAG, "username != null");
            incomingMessagesRef = database.getReference("users/" + userId + "/incomingMessages");
            incomingMessagesRef.addChildEventListener(incomingMessagesRefListener);
        }
    }

    public void getWonMessages() {
        Log.d(TAG, "getWonMessages");
        if (userId != null) {
            Log.d(TAG, "username != null");
            wonMessagesRef = database.getReference("users/" + userId + "/wonMessages");
            wonMessagesRef.addChildEventListener(wonMessagesRefListener);
        }
    }

    public void getLostMessages() {
        Log.d(TAG, "getLostMessages");
        if (userId != null) {
            Log.d(TAG, "username != null");
            lostMessagesRef = database.getReference("users/" + userId + "/lostMessages");
            lostMessagesRef.addChildEventListener(lostMessagesRefListener);
        }
    }

//    public void getAcceptedMessages() {
//        Log.d(TAG, "getAcceptedMessages");
//        if (userId != null) {
//            Log.d(TAG, "username != null");
//            acceptedMessagesRef = database.getReference("users/" + userId + "/propositions");
//            acceptedMessagesRef.addChildEventListener(acceptedMessagesRefListener);
//        }
//    }

    public void updateRequestsAdapter() {
        if (requestsAdapter != null) {
            requestsAdapter.updateAdapter();
        } else {
            requestsAdapter = new RequestsAdapter(this, currentIncomingRequests);
            binding.rvRequests.setAdapter(requestsAdapter);
        }
        countFriendRequests(requestsAdapter.getItemCount());
    }

    public void updateMessagesAdapter() {
        if (incomingMessagesAdapter != null) {
            incomingMessagesAdapter.updateAdapter();
        } else {
            incomingMessagesAdapter = new IncomingMessagesAdapter(this, currentIncomingMessagesArrayList);
            binding.rvMessages.setAdapter(incomingMessagesAdapter);
        }
    }

    public void updateAcceptedMessagesAdapter() {
        if (acceptedMessagesAdapter != null) {
            acceptedMessagesAdapter.updateAdapter();
        } else {
            acceptedMessagesAdapter = new AcceptedMessagesAdapter(this, currentAcceptedMessagesArrayList);
            binding.rvAcceptedMessages.setAdapter(acceptedMessagesAdapter);
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
                user = FirebaseAuth.getInstance().getCurrentUser();
                Log.d(TAG, "onActivityResult: Logged in: Current user: " + user.getDisplayName());
                FirebaseUserMetadata metadata = user.getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp()) {
                    if (user.getDisplayName() != null && user.getUid() != null) {
                        newUsernameDialog(this);
                        Log.d(TAG, "After userNameDialog");
                    }
                } else {
                    //returning user
//                    receiverRegistration();
//                    getUsers();
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
    //Version 1
//    protected void sendMessage(View view) {
//        FirebaseMessaging fm = FirebaseMessaging.getInstance();
//        fm.send(new RemoteMessage.Builder(SENDER_ID + "@gcm.googleapis.com")
//                .setMessageId(Integer.toString(incrementAndGet()))
//                .addData("my_message", "Hello Andrew")
//                .addData("my_action","SAY_HELLO")
//                .build());
//    }

////Version 1
//    public static void sendNotificationToUser(String sender, String userId, final String message) {
//        Log.d(TAG, "attempting sendNotificationToUser");
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//
//        DatabaseReference notifications = ref.child("notificationRequests");
////        notifications.setValue("Testing");
//
//        Map notification = new HashMap<>();
//        notification.put("username", userId);
//        notification.put("message", message);
//        notification.put("from", sender);
//
//        notifications.push().setValue(notification);
//    }

    //Version 2
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
        FirebaseMessaging.getInstance().unsubscribeFromTopic("user_"+user.getUid());
        removeUserSpecificListeners();

        resetListsAndMaps();

        updateRequestsAdapter();
        updateMessagesAdapter();
        updateAcceptedMessagesAdapter();

//        receiverUnregistration();
        FirebaseAuth.getInstance().signOut();
//        Intent intent = getIntent();
//        finish();
//        startActivity(intent);
        determineLoginStatus();
    }

    public void resetListsAndMaps() {

        //New data
        currentFriends = new HashMap<>();
//        currentPropositions = new HashMap<>();
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
        FirebaseAuth.getInstance().signOut();
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
                        sendMessageToUser(user.getUid(), recipientUserId, message, nameFromIDs.get(user.getUid()));
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
                else {
                    recipientPromptTextView.setText(R.string.recipient_not_friend);
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

    public void sendRequest(View view) {
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
                        String recipientUsername = recipient.getText().toString();
                        String recipientUserId = idFromUsers.get(recipientUsername);
                        Log.d(TAG, "Id of this user is " + idFromUsers.get(recipientUsername));
                        //Version 1
//                        sendNotificationToUser(userFromIDs.get(user.getUid()), userId, "Hello from one user to another!");
                        //Version 2
                        sendNotificationToUser(user.getUid(), recipientUserId, nameFromIDs.get(user.getUid()));
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
                else if (charSequence.toString().equals(currentUserName())) {
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
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        user.delete();
                        signOutNewUser();
                    }
                });

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

    //Set username in firebase, called after creating new account
    public void setUserName(String username) {
        Log.d(TAG, "Uid = " + user.getUid());
        usersRef.child(userId).setValue((new User(user.getUid(), user.getDisplayName(), username)));
        //A new user was just added
        addUserSpecificListeners();
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            String username = userFromIDs.get(user.getUid());

            String message = intent.getStringExtra(Intent.EXTRA_TEXT);
            String from = intent.getStringExtra("from_extra");
            binding.receivedMessage.setText(message);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            DatabaseReference users = ref.child("users");
            DatabaseReference user = users.child(username);

//            Map friend = new HashMap<>();
//            friend.put(from, "Unconfirmed");

            user.child("friend").child(from).setValue("unconfirmed");
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

//    public void viewRequests(View view) {
//        TextView requestView = findViewById(R.id.requests);
//        requestView.setText("");
//        if (currentIncomingRequests != null) {
//            for (String key : currentIncomingRequests.keySet()) {
//                requestView.append(key + "\n");
//            }
//        }
//    }

    public void openFriends(View view) {
        binding.rvRequests.setVisibility(View.VISIBLE);
        binding.closeFriends.setVisibility(View.VISIBLE);
        binding.noFriendRequests.setVisibility(View.VISIBLE);
        binding.rvMessages.setVisibility(View.GONE);
        binding.rvAcceptedMessages.setVisibility(View.GONE);
        binding.messages.setVisibility(View.GONE);
        binding.signOut.setVisibility(View.GONE);
        binding.currentUser.setVisibility(View.GONE);
        binding.receivedMessage.setVisibility(View.GONE);

        updateRequestsAdapter();
        countFriendRequests(requestsAdapter.getItemCount());
    }

    public void closeFriends(View view) {
        binding.rvRequests.setVisibility(View.INVISIBLE);
        binding.closeFriends.setVisibility(View.INVISIBLE);
        binding.noFriendRequests.setVisibility(View.INVISIBLE);
        binding.rvMessages.setVisibility(View.VISIBLE);
        binding.rvAcceptedMessages.setVisibility(View.VISIBLE);
        binding.messages.setVisibility(View.VISIBLE);
        binding.signOut.setVisibility(View.VISIBLE);
        binding.currentUser.setVisibility(View.VISIBLE);
        binding.receivedMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (binding.rvRequests.getVisibility()==View.VISIBLE) {
            closeFriends(null);
        }
        else {
            super.onBackPressed();
        }
    }

    public String currentUserName() {
        //This is sometimes null
        return (String) userFromIDs.get(user.getUid());
    }

    public void countFriendRequests(int count) {
        if (count == 0 && binding.rvRequests.getVisibility()==View.VISIBLE) {
            Log.d(TAG, "countFriendRequests, count is 0");
            binding.noFriendRequests.setVisibility(View.VISIBLE);
        }
        else {
            binding.noFriendRequests.setVisibility(View.GONE);
        }
    }

    public void toggleAddOptions(View view) {
        if (binding.fabAddProposition.getVisibility()==View.VISIBLE) {
            binding.fabAddFriend.setVisibility(View.GONE);
            binding.fabAddProposition.setVisibility(View.GONE);

        } else {
            binding.fabAddFriend.setVisibility(View.VISIBLE);
            binding.fabAddProposition.setVisibility(View.VISIBLE);
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

}
