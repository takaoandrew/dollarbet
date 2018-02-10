package com.example.android.firebaseuserandmessageapplication;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;

import com.example.android.firebaseuserandmessageapplication.databinding.ActivityFriendsBinding;

public class FriendsActivity extends AppCompatActivity {
    ActivityFriendsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_friends);
        FriendsAdapter friendsAdapter = new FriendsAdapter(this, MainActivity.currentFriends);
        binding.rvFriends.setAdapter(friendsAdapter);
        binding.rvFriends.setLayoutManager(new LinearLayoutManager(this));


    }
}
