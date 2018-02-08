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

public class AcceptedMessagesAdapter extends RecyclerView.Adapter<AcceptedMessagesAdapter.MainViewHolder> {
    LayoutInflater inflater;
//    HashMap<String,String> messagesHashMap;
    ArrayList<Proposition> messagePropositions;
    DatabaseReference acceptedsPropositionsRef;
    DatabaseReference acceptedsOutgoingMessagesRef;
    DatabaseReference lostMessagesRef;
    DatabaseReference wonMessagesRef;
    private static final String TAG = AcceptedMessagesAdapter.class.getSimpleName();

    public AcceptedMessagesAdapter(Context context, ArrayList<Proposition> propositions) {
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
        View view = inflater.inflate(R.layout.accepted_proposition_recycler_row, parent, false);
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
                Log.d(TAG, "You claim to have won " + position);
//                String acceptedProposition = (String) messagePropositions.get(position);
                String timestamp = messagePropositions.get(position).timestamp;
                String senderId = messagePropositions.get(position).otherUserId;
                String message = messagePropositions.get(position).message;
                Log.d(TAG, "message = " + messagePropositions.get(position).message);
                Log.d(TAG, "timestamp = " + messagePropositions.get(position).timestamp);
                Log.d(TAG, "senderId = " + messagePropositions.get(position).otherUserId);

                Proposition acceptedsProposition = new Proposition(timestamp, MainActivity.userId, message);
                Proposition acceptersProposition = new Proposition(timestamp, senderId, message);

                // Set winner to won, loser to lost
                MainActivity.wonMessagesRef.child(timestamp).setValue(acceptersProposition);
                lostMessagesRef = MainActivity.database.getReference("users/" + senderId + "/lostMessages");
                lostMessagesRef.child(timestamp).setValue(acceptedsProposition);

                //Remove both accepted propositions
                acceptedsPropositionsRef = MainActivity.database.getReference("users/" + senderId + "/propositions");
                acceptedsPropositionsRef.child(timestamp).removeValue();

                MainActivity.acceptedMessagesRef.child(timestamp).removeValue();

                updateAdapter();
            }
        });
        holder.denyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "You denied " + position);

                String timestamp = messagePropositions.get(position).timestamp;
                String otherUserId = messagePropositions.get(position).otherUserId;
                String message = messagePropositions.get(position).message;
                Log.d(TAG, "message = " + messagePropositions.get(position).message);
                Log.d(TAG, "timestamp = " + messagePropositions.get(position).timestamp);
                Log.d(TAG, "senderId = " + messagePropositions.get(position).otherUserId);

                Proposition acceptedsProposition = new Proposition(timestamp, MainActivity.userId, message);
                Proposition acceptersProposition = new Proposition(timestamp, otherUserId, message);

                // Set winner to won, loser to lost
                MainActivity.lostMessagesRef.child(timestamp).setValue(acceptersProposition);
                wonMessagesRef = MainActivity.database.getReference("users/" + otherUserId + "/wonMessages");
                wonMessagesRef.child(timestamp).setValue(acceptedsProposition);

                //Remove both accepted propositions
                acceptedsPropositionsRef = MainActivity.database.getReference("users/" + otherUserId + "/propositions");
                acceptedsPropositionsRef.child(timestamp).removeValue();

                MainActivity.acceptedMessagesRef.child(timestamp).removeValue();

                updateAdapter();
            }
        });
    }

    public void updateAdapter() {
        this.messagePropositions = MainActivity.currentAcceptedMessagesArrayList;
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
            acceptButton = itemView.findViewById(R.id.won_proposition);
            denyButton = itemView.findViewById(R.id.lost_proposition);
        }

    }
}
