package com.example.android.firebaseuserandmessageapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by andrew.takao on 1/25/2018.
 */

public class IncomingMessagesAdapter extends RecyclerView.Adapter<IncomingMessagesAdapter.MainViewHolder> {
    LayoutInflater inflater;
//    HashMap<String,String> messagesHashMap;
    ArrayList<Proposition> messagePropositions;
    DatabaseReference acceptedsPropositionsRef;
    DatabaseReference acceptedsOutgoingMessagesRef;
    private static final String TAG = IncomingMessagesAdapter.class.getSimpleName();

    public IncomingMessagesAdapter(Context context, ArrayList<Proposition> propositions) {
        this.inflater = LayoutInflater.from(context);
        this.messagePropositions = propositions;
    }
//    public IncomingMessagesAdapter(Context context, HashMap <String,String> messagesHashMap) {
//        this.inflater = LayoutInflater.from(context);
//        this.messagesHashMap = messagesHashMap;
//        this.keys = new ArrayList(messagesHashMap.keySet());
//        this.values = new ArrayList(messagesHashMap.values());
//    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.incoming_proposition_recycler_row, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        //holder.bindData();
        holder.mainText.setText(MainActivity.userFromIDs.get((String) messagePropositions.get(position).otherUserId)); // value for the given key
        holder.subText.setText((String) messagePropositions.get(position).message); // value for the given key
        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                MainActivity.re
                //Remove child from incoming messages
                Log.d(TAG, "You accepted " + position);
//                String acceptedProposition = (String) filteredUsers.get(position);
                String timestamp = messagePropositions.get(position).timestamp;
                String senderId = messagePropositions.get(position).otherUserId;
                String message = messagePropositions.get(position).message;
                Log.d(TAG, "message = " + messagePropositions.get(position).message);
                Log.d(TAG, "timestamp = " + messagePropositions.get(position).timestamp);
                Log.d(TAG, "senderId = " + messagePropositions.get(position).otherUserId);
                MainActivity.incomingMessagesRef.child(timestamp).removeValue();
                Log.d(TAG, "you removed that child from this users incoming messages");
                Proposition acceptingsProposition = new Proposition(timestamp, senderId, message);
                Proposition acceptedsProposition = new Proposition(timestamp, MainActivity.userId, message);
                acceptedsPropositionsRef = MainActivity.database.getReference("users/" + senderId + "/propositions");
                acceptedsOutgoingMessagesRef = MainActivity.database.getReference("users/" + senderId + "/outgoingMessages");
                acceptedsOutgoingMessagesRef.child(timestamp).removeValue();

                MainActivity.acceptedMessagesRef.child(timestamp).setValue(acceptingsProposition);
                acceptedsPropositionsRef.child(timestamp).setValue(acceptedsProposition);
//                Log.d(TAG, MainActivity.incomingMessagesRef.child((String) keys.get(position)));
                updateAdapter();
            }
        });
        holder.denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String timestamp = messagePropositions.get(position).timestamp;
                String senderId = messagePropositions.get(position).otherUserId;
                MainActivity.incomingMessagesRef.child(timestamp).removeValue();
                acceptedsOutgoingMessagesRef = MainActivity.database.getReference("users/" + senderId + "/outgoingMessages");
                acceptedsOutgoingMessagesRef.child(timestamp).removeValue();
                Log.d(TAG, "You denied " + position);
            }
        });
    }

    public void updateAdapter() {
        this.messagePropositions = MainActivity.currentIncomingMessagesArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return messagePropositions.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView mainText, subText;
        ImageButton acceptButton, denyButton;

        public MainViewHolder(View itemView) {
            super(itemView);
            mainText = itemView.findViewById(R.id.mainText);
            subText = itemView.findViewById(R.id.subText);
            acceptButton = itemView.findViewById(R.id.acceptProposition);
            denyButton = itemView.findViewById(R.id.denyProposition);
        }

    }
}
