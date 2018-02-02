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

import java.util.ArrayList;

/**
 * Created by andrew.takao on 1/25/2018.
 */

public class LostPropositionsAdapter extends RecyclerView.Adapter<LostPropositionsAdapter.MainViewHolder> {
    LayoutInflater inflater;
    ArrayList<Proposition> lostMessages;
    DatabaseReference lostPropositionsRef;
    DatabaseReference lostMessagesRef;
    private static final String TAG = LostPropositionsAdapter.class.getSimpleName();

    public LostPropositionsAdapter(Context context, ArrayList<Proposition> propositions) {
        this.inflater = LayoutInflater.from(context);
        this.lostMessages = propositions;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.lost_proposition_recycler_row, parent, false);
        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, final int position) {
        //holder.bindData();
        holder.mainText.setText(MainActivity.userFromIDs.get((String) lostMessages.get(position).otherUserId)); // value for the given key
        holder.subText.setText((String) lostMessages.get(position).message); // value for the given key
    }

    public void updateAdapter() {
        this.lostMessages = MainActivity.currentLostMessagesArrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lostMessages.size();
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
