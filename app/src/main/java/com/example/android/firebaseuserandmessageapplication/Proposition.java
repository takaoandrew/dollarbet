package com.example.android.firebaseuserandmessageapplication;

import java.util.ArrayList;

/**
 * Created by andrew.takao on 1/29/2018.
 */

public class Proposition {
    public String timestamp;
    public String otherUserId;
    public String message;
    public String resolution;

    public Proposition(String tempTimestamp, String tempotherUserId, String tempMessage) {
        timestamp = tempTimestamp;
        otherUserId = tempotherUserId;
        message = tempMessage;
        resolution = "unresolved";
    }

    public Proposition(String tempTimestamp, String tempotherUserId, String tempMessage, String tempResolution) {
        timestamp = tempTimestamp;
        otherUserId = tempotherUserId;
        message = tempMessage;
        resolution = tempResolution;
    }

}