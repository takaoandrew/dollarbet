package com.example.android.firebaseuserandmessageapplication;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.example.android.firebaseuserandmessageapplication.databinding.ActivityUserDetailBinding;
import com.google.firebase.database.DatabaseReference;

import java.io.Console;

public class UserDetailActivity extends AppCompatActivity {

    private final String TAG = UserDetailActivity.class.getSimpleName();
    ActivityUserDetailBinding binding;
    public final int VENMO_REQUEST_CODE = 836;
    public static boolean claimingRequest = false;
    public static int venmoPosition = -1;
    public static String otherUserId = "-1";
    public static String timestamp = "-1";
    public static WonPropositionsAdapter wonPropositionsAdapter;
    public static LostPropositionsAdapter lostPropositionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_detail);
        Intent intent = getIntent();
        binding.currentUser.setText(intent.getStringExtra("user_extra"));
        binding.currentUsername.setText(intent.getStringExtra("username_extra"));
        binding.wonBetsCount.setText(intent.getStringExtra("won_propositions_extra"));
        binding.lostBetsCount.setText(intent.getStringExtra("lost_propositions_extra"));
        Log.d(TAG, String.valueOf(intent.getIntExtra("test_value", 3)));
        checkEmptyAdapters();

        final LinearLayoutManager wonLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    // this avoids trying to handle un-needed calls
                    if (firstVisibleItemPosition == -1)
                        //not initialized, or no items shown, so hide fast-scroller
                        checkEmptyAdapters();
                }
            }
        };
        final LinearLayoutManager lostLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutChildren(final RecyclerView.Recycler recycler, final RecyclerView.State state) {
                super.onLayoutChildren(recycler, state);
                final int firstVisibleItemPosition = findFirstVisibleItemPosition();
                if (firstVisibleItemPosition != 0) {
                    // this avoids trying to handle un-needed calls
                    if (firstVisibleItemPosition == -1)
                        //not initialized, or no items shown, so hide fast-scroller
                        checkEmptyAdapters();
                }
            }
        };

        wonPropositionsAdapter = new WonPropositionsAdapter(this, MainActivity.currentWonMessagesArrayList);
        lostPropositionsAdapter = new LostPropositionsAdapter(this, MainActivity.currentLostMessagesArrayList);
        binding.rvWonPropositions.setLayoutManager(wonLayoutManager);
        binding.rvWonPropositions.setAdapter(wonPropositionsAdapter);
        binding.rvLostPropositions.setLayoutManager(lostLayoutManager);
        binding.rvLostPropositions.setAdapter(lostPropositionsAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        switch(requestCode) {
            case VENMO_REQUEST_CODE: {
                if(resultCode == RESULT_OK) {
                    Log.d(TAG, "resultCode == RESULT_OK");
                    String signedrequest = data.getStringExtra("signedrequest");
                    if(signedrequest != null) {
                        Log.d(TAG, "resultCode == RESULT_OK");
                        VenmoLibrary.VenmoResponse response = (new VenmoLibrary()).validateVenmoPaymentResponse(signedrequest, "secret");
                        Log.d(TAG, "data = " + data);
                        if(claimingRequest) {
                            Log.d(TAG, "claimingRequest");
                            MainActivity.wonMessagesRef.child(timestamp).removeValue();
                            DatabaseReference lostMessagesRef = MainActivity.database.getReference("users/" + otherUserId + "/lostMessages");
                            lostMessagesRef.child(timestamp).removeValue();
                            wonPropositionsAdapter.updateAdapter();
                            //remove from current users win and opposing users loss
                        } else {
                            Log.d(TAG, "NOT claimingRequest");
                            MainActivity.lostMessagesRef.child(timestamp).removeValue();
                            DatabaseReference wonMessagesRef = MainActivity.database.getReference("users/" + otherUserId + "/wonMessages");
                            wonMessagesRef.child(timestamp).removeValue();
                            lostPropositionsAdapter.updateAdapter();
                            //remove from current users loss and opposing users win

                        }
                        if(response.getSuccess().equals("1")) {
                            Log.d(TAG, "response.getSuccess().equals(1)!!!");
                            Log.d(TAG, "and data = " + data);
                            //Payment successful.  Use data from response object to display a success message
                            Log.d("VENMO", "VENMO SUCCESSFUL");
                            String note = response.getNote();
                            String amount = response.getAmount();
                        }
                    }
                    else {
                        String error_message = data.getStringExtra("error_message");
                        Log.d(TAG, error_message);
                        //An error ocurred.  Make sure to display the error_message to the user
                    }
                }
                else if(resultCode == RESULT_CANCELED) {
                    Log.d(TAG, "resultCode == RESULT_CANCELLED");
                    //The user cancelled the payment
                }
                break;
            }
        }
    }

    public void checkEmptyAdapters() {
        if (MainActivity.currentWonMessagesArrayList.size() < 1) {
            binding.noWonPropositions.setVisibility(View.VISIBLE);
        }else{
            binding.noWonPropositions.setVisibility(View.GONE);
        }
        if (MainActivity.currentLostMessagesArrayList.size() < 1) {
            binding.noLostPropositions.setVisibility(View.VISIBLE);
        }else{
            binding.noLostPropositions.setVisibility(View.GONE);
        }
    }
}
