package com.example.android.firebaseuserandmessageapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

/**
 * Created by andrew.takao on 1/25/2018.
 */

public class WonPropositionsAdapter extends RecyclerView.Adapter<WonPropositionsAdapter.MainViewHolder> {
    LayoutInflater inflater;
    ArrayList<Proposition> wonMessages;
    DatabaseReference wonPropositionsRef;
    DatabaseReference lostMessagesRef;
    DatabaseReference wonMessagesRef;
    Context mContext;
    public final int VENMO_REQUEST_CODE = 836;
    private static final String TAG = WonPropositionsAdapter.class.getSimpleName();

    public WonPropositionsAdapter(Context context, ArrayList<Proposition> propositions) {
        mContext = context;
        this.inflater = LayoutInflater.from(context);
        this.wonMessages = propositions;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.won_proposition_recycler_row, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        //holder.bindData();
        holder.mainText.setText(MainActivity.userFromIDs.get((String) wonMessages.get(position).otherUserId)); // value for the given key
        holder.subText.setText((String) wonMessages.get(position).message); // value for the given key
        holder.claimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otherUserId = wonMessages.get(position).otherUserId;
                String otherUser = MainActivity.nameFromIDs.get(otherUserId);
                String message = wonMessages.get(position).message;
                //TODO save the position here, so upon completion, the bet can be removed
                UserDetailActivity.otherUserId = otherUserId;
                UserDetailActivity.timestamp = wonMessages.get(position).timestamp;
                UserDetailActivity.venmoPosition = position;
                UserDetailActivity.claimingRequest = true;
                Intent venmoIntent = VenmoLibrary.openVenmoPayment("appid", "appname", "3237173413", "0.01", message, "pay");
                venmoIntent.putExtra("TYPE_EXTRA", "WIN");
                ((Activity) mContext).startActivityForResult(venmoIntent, VENMO_REQUEST_CODE);
            }
        });
    }

    public void updateAdapter() {
        this.wonMessages = MainActivity.currentWonMessagesArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return wonMessages.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView mainText, subText;
        Button claimButton;

        public MainViewHolder(View itemView) {
            super(itemView);
            mainText = itemView.findViewById(R.id.mainText);
            subText = itemView.findViewById(R.id.subText);
            claimButton = itemView.findViewById(R.id.claim);
        }

    }
}
