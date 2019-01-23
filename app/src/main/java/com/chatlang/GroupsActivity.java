package com.chatlang;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.chatlang.adapter.GroupsAdapter;
import com.chatlang.data.FeedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupsActivity extends AppCompatActivity {

    private ListView listView;
    private GroupsAdapter listAdapter;
    private List<FeedItem> feedItems;
    SessionManager session;
    FeedItem item;
    String userbind, usernames;

    private View mProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        //show back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //get session variable
        session = new SessionManager(this.getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        userbind = user.get(SessionManager.KEY_TOKEN);
        usernames = user.get(SessionManager.KEY_USERNAMES);

        setTitle("Available Groups");
        //start loader
        mProgressView = findViewById(R.id.progress_overlay);
        showProgress(true);
        //prepage list for data
        listView = (ListView) findViewById(R.id.list);
        //listView.addHeaderView(viewHeader, null, false);
        feedItems = new ArrayList<FeedItem>();
        listAdapter = new GroupsAdapter(this, feedItems);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                /*
                Intent i = new Intent(getActivity(), ChatActivity.class);
                i.putExtra("user_id", feedItems.get(position).getUserId());
                i.putExtra("user_name", feedItems.get(position).getName());
                i.putExtra("current_user_id", thisUserId);
                startActivity(i);
                */


                AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(GroupsActivity.this);
                builder.setTitle("Join "+feedItems.get(position).getName())
                        .setMessage("You will be added to "+feedItems.get(position).getName()+"?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //first check if this user already belongs to in this group
                                        DatabaseReference mGroupChecker = FirebaseDatabase.getInstance().getReference().child("GroupMembers");
                                        Query queryRef = mGroupChecker.orderByChild("user_group_id").equalTo(userbind+"_"+feedItems.get(position).getUserId());
                                        //listen for updates
                                        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getChildrenCount() == 0) {//first time joining
                                                    final ProgressDialog progressDialog = new ProgressDialog(GroupsActivity.this);
                                                    progressDialog.setTitle("Joining...");
                                                    progressDialog.show();
                                                    //insert new member
                                                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("GroupMembers");
                                                    final DatabaseReference current_user_db = postRef.push();
                                                    Map newPost = new HashMap();
                                                    newPost.put("groupid", feedItems.get(position).getUserId());
                                                    newPost.put("title", feedItems.get(position).getName());
                                                    newPost.put("picture", "");
                                                    newPost.put("createdby", feedItems.get(position).getShowIdentity());
                                                    newPost.put("createdbyname", feedItems.get(position).getStatus());
                                                    newPost.put("createdon", feedItems.get(position).getDateJoined());
                                                    newPost.put("userid", userbind);
                                                    newPost.put("user_group_id", userbind+"_"+feedItems.get(position).getUserId());
                                                    newPost.put("joinedon", ServerValue.TIMESTAMP);

                                                    current_user_db.setValue(newPost, new DatabaseReference.CompletionListener() {
                                                        @Override
                                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                            //showProgress(false);
                                                            if (databaseError != null) {//on error
                                                                Toast.makeText(GroupsActivity.this, "Something went wrong, please try again later.", Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                            } else{//on success, increment the total members count by 1
                                                                UtilityClass.updateFieldByValue("Groups", feedItems.get(position).getUserId(),
                                                                        "totalmembers", 1l, "increase");
                                                                Toast.makeText(GroupsActivity.this, "You have been added to group", Toast.LENGTH_SHORT).show();
                                                                progressDialog.dismiss();
                                                                //close all activities and launch main activity again
                                                                Intent intent = new Intent(GroupsActivity.this, MainActivity.class);
                                                                intent.putExtra("position", "2");
                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });

                                                }else{//cant join group twice, notify user
                                                    Toast.makeText(GroupsActivity.this, "You already belong in "+feedItems.get(position).getName()+"'s group.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }
                                })
                        .setNegativeButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();





            }
        });
        //get data from firebase database
        getAllGroups();
    }

    private void getAllGroups() {
        FirebaseUser thisuser = FirebaseAuth.getInstance().getCurrentUser();
        if(thisuser!=null){//firebase still active
            DatabaseReference mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Groups");
            //listen for updates
            mChatDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    showProgress(false);
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        Map<String, Object> businessMap = (Map<String, Object>) dataSnapshot.getValue();
                        //count_projects = Long.toString(dataSnapshot.getChildrenCount());
                        //headerTitle.setText("You Have "+count_projects+" "+settitle);
                        for (DataSnapshot recordSnapshot: dataSnapshot.getChildren()) {
                            FeedItem item = new FeedItem();
                            item.setUserId(recordSnapshot.getKey());
                            item.setProfilePic(recordSnapshot.child("picture").getValue().toString());
                            item.setName(recordSnapshot.child("title").getValue().toString());
                            item.setShowIdentity(recordSnapshot.child("createdby").getValue().toString());
                            item.setStatus(recordSnapshot.child("createdbyname").getValue().toString());
                            item.setDateJoined(recordSnapshot.child("createdon").getValue().toString());
                            feedItems.add(item);
                        }
                        //Collections.reverse(feedItems);
                        //listView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                    }else{
                        //Toast.makeText(LibraryWordsActivity.this, "Sorry we could not find any records.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
    }

    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    //make back button work
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
