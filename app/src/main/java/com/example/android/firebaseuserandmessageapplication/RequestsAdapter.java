package com.example.android.firebaseuserandmessageapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andrew.takao on 1/25/2018.
 */

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.MainViewHolder> {
    LayoutInflater inflater;
    HashMap<String,String> requestsHashMap;
    ArrayList keys;
    ArrayList values;
    DatabaseReference acceptedsFriendsRef;
    DatabaseReference acceptedsOutgoingRequestsRef;
    private static final String TAG = RequestsAdapter.class.getSimpleName();

    public RequestsAdapter(Context context, HashMap <String,String> requestsHashMap) {
        this.inflater = LayoutInflater.from(context);
        this.requestsHashMap = requestsHashMap;
        this.keys = new ArrayList(requestsHashMap.keySet());
        this.values = new ArrayList(requestsHashMap.values());
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.request_recycler_row, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        //holder.bindData();
        holder.mainText.setText((String) values.get(position)); // value for the given key
//        holder.subText.setText((String) values.get(position)); // value for the given key
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivity.re
                //Remove child from incoming requests
                Log.d(TAG, "You accepted " + position);
                String acceptedFriend = (String) keys.get(position);
                Log.d(TAG, "acceptedFriend = " + acceptedFriend);
                MainActivity.incomingRequestsRef.child(acceptedFriend).removeValue();
                Log.d(TAG, "you removed that child from this users incoming requests");
                Map acceptingsFriend = new HashMap<>();
                Map acceptedsFriend = new HashMap<>();
                acceptedsFriendsRef = MainActivity.database.getReference("users/" + acceptedFriend + "/friends");
                acceptedsOutgoingRequestsRef = MainActivity.database.getReference("users/" + acceptedFriend + "/outgoingRequests");
                acceptedsOutgoingRequestsRef.child(MainActivity.userId).removeValue();
                acceptedsFriend.put("message", MainActivity.userId);
                acceptingsFriend.put("message", acceptedFriend);

                MainActivity.friendsRef.child((String) keys.get(position)).setValue(acceptingsFriend);
                acceptedsFriendsRef.child(MainActivity.userId).setValue(acceptedsFriend);
//                Log.d(TAG, MainActivity.incomingRequestsRef.child((String) keys.get(position)));
                updateAdapter();
            }
        });
        holder.denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "You denied " + position);
            }
        });
    }

    public void updateAdapter() {
        this.requestsHashMap = MainActivity.currentIncomingRequests;
        this.keys = new ArrayList(requestsHashMap.keySet());
        this.values = new ArrayList(requestsHashMap.values());
    }

    @Override
    public int getItemCount() {
        return requestsHashMap.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView mainText, subText;
        Button acceptButton, denyButton;

        public MainViewHolder(View itemView) {
            super(itemView);
            mainText = itemView.findViewById(R.id.mainText);
            subText = itemView.findViewById(R.id.subText);
            acceptButton = itemView.findViewById(R.id.acceptFriend);
            denyButton = itemView.findViewById(R.id.denyFriend);
        }

    }
}
