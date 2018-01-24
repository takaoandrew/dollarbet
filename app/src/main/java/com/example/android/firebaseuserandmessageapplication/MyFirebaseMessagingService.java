package com.example.android.firebaseuserandmessageapplication;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by andrew.takao on 1/19/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Message data payload: "+remoteMessage.getData());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_TEXT, remoteMessage.getNotification().getTitle());
            intent.putExtra("from_extra", remoteMessage.getNotification().getBody());
            intent.setAction("com.example.android.firebaseuserandmessageapp.onMessageReceivedGETRIDOFTHIS");
            sendBroadcast(intent);
        }
    }
}
