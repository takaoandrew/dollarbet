package com.example.android.firebaseuserandmessageapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andrew.takao on 1/25/2018.
 */

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.MainViewHolder> {
    LayoutInflater inflater;
    HashMap<String,String> requestsHashMap;
    ArrayList keys;
    ArrayList values;

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
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        holder.mainText.setText((String) keys.get(position)); // value for the given key
        holder.subText.setText((String) values.get(position)); // value for the given key
    }

    @Override
    public int getItemCount() {
        return requestsHashMap.size();
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView mainText, subText;

        public MainViewHolder(View itemView) {
            super(itemView);
            mainText = (TextView) itemView.findViewById(R.id.mainText);
            subText = (TextView) itemView.findViewById(R.id.subText);
        }

    }
}
