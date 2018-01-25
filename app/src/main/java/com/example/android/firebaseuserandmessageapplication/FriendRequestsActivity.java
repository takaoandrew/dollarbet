package com.example.android.firebaseuserandmessageapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class FriendRequestsActivity extends AppCompatActivity {

    private RecyclerView requestsRecyclerView;
    private RequestsAdapter requestsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);
        requestsRecyclerView = findViewById(R.id.rv_requests);
        requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
