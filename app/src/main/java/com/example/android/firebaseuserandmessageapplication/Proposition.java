package com.example.android.firebaseuserandmessageapplication;

import java.util.ArrayList;

/**
 * Created by andrew.takao on 1/29/2018.
 */

public class Proposition {
    public String timestamp;
    public String senderUserId;
    public String message;

    public Proposition(String tempTimestamp, String tempSenderUserId, String tempMessage) {
        timestamp = tempTimestamp;
        senderUserId = tempSenderUserId;
        message = tempMessage;
    }

}