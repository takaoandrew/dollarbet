package com.example.android.firebaseuserandmessageapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by andrew.takao on 1/25/2018.
 */

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.MainViewHolder> {
    LayoutInflater inflater;
    ArrayList<String> friends;
    private static final String TAG = FriendsAdapter.class.getSimpleName();

    public FriendsAdapter(Context context, ArrayList<String> users) {
        this.inflater = LayoutInflater.from(context);
        this.friends = MainActivity.currentFriends;
        Log.d(TAG, friends.toString());
    }
//    public IncomingMessagesAdapter(Context context, HashMap <String,String> messagesHashMap) {
//        this.inflater = LayoutInflater.from(context);
//        this.messagesHashMap = messagesHashMap;
//        this.keys = new ArrayList(messagesHashMap.keySet());
//        this.values = new ArrayList(messagesHashMap.values());
//    }

    public void updateAdapter(ArrayList<String> users) {
        this.friends = users;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.friend_row, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        //holder.bindData();

        holder.subText.setText(friends.get(position));
        holder.mainText.setText(MainActivity.nameFromIDs.get(friends.get(position)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "You accepted " + position);
            }
        });
    }

//    public void updateAdapter() {
//        this.friends = MainActivity.filteredUserArrayList;
//        notifyDataSetChanged();
//    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView mainText, subText;

        public MainViewHolder(View itemView) {
            super(itemView);
            mainText = itemView.findViewById(R.id.mainText);
            subText = itemView.findViewById(R.id.subText);
        }

    }
}
