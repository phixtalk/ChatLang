package com.chatlang.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.chatlang.ChatActivity;
import com.chatlang.CircularNetworkImageView;
import com.chatlang.GroupChatActivity;
import com.chatlang.GroupsActivity;
import com.chatlang.R;
import com.chatlang.SessionManager;
//import com.vanityapp.adapter.AwardeesAdapter;
import com.chatlang.adapter.ChatListAdapter;
import com.chatlang.app.AppController;
import com.chatlang.data.FeedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
//import com.vanityapp.helper.RowItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";

    public ChatFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);

        fragment.setArguments(args);
        return fragment;
    }

    SessionManager session;
    String thisUserId, thisUserName;
    String userAcctType;
    private View mProgressView;
    private String qtype;
    private ListView listView;
    private ChatListAdapter listAdapter;
    private List<FeedItem> feedItems;
    FeedItem item;
    TextView message;
    boolean hasGroup = false;

    ViewPager pager;
    ValueEventListener valueEventListenerChatUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_chat_messages, container, false);
        // Inflate the layout for this fragment

        mProgressView = getActivity().findViewById(R.id.progress_overlay);

        pager = (ViewPager) container;
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                // Check if this is the page you want.
                /*
                if(position==2){//check for the last tab
                    message = getActivity().findViewById(R.id.message);
                    message.setVisibility(View.VISIBLE);
                }else{
                    message = getActivity().findViewById(R.id.message);
                    message.setVisibility(View.GONE);
                }
                */
            }
        });

        /*
        final SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_away);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        ((AwardeesActivity) getActivity()).refreshNow();
                        Toast.makeText(getContext(), "Page Refreshed Successfully", Toast.LENGTH_LONG).show();
                    }
                }
        );
        */

        session = new SessionManager(getActivity());
        HashMap<String, String> userz = session.getUserDetails();
        thisUserId = userz.get(SessionManager.KEY_TOKEN);
        thisUserName = userz.get(SessionManager.KEY_USERNAMES);

        //setup the list view
        listView = (ListView) view.findViewById(R.id.theListView);
        feedItems = new ArrayList<FeedItem>();
        listAdapter = new ChatListAdapter(getActivity(), feedItems);
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //item = feedItems.get(position);
                if(qtype.equals("1") || qtype.equals("2")){
                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("user_id", feedItems.get(position).getUserId());
                    i.putExtra("user_name", feedItems.get(position).getName());
                    i.putExtra("current_user_id", thisUserId);
                    startActivity(i);
                }else{//for groups
                    Intent i = new Intent(getActivity(), GroupChatActivity.class);
                    i.putExtra("group_id", feedItems.get(position).getPostTags());
                    i.putExtra("group_name", feedItems.get(position).getName());
                    i.putExtra("created_on", feedItems.get(position).getDateJoined());
                    //i.putExtra("current_user_id", thisUserId);
                    startActivity(i);
                }
            }
        });

        //THIS CODE TOOK TWO DAYS TO FIND, LOVE WITH CARE
        ViewCompat.setNestedScrollingEnabled(listView,true);
        //IT ACTUALLY HIDES THE TOOLBAR WHEN YOU SCROLL DOWN
        //AND SHOWS IT, WHEN YOU SCROLL UP

        SessionManager session;
        HashMap<String, String> thisuser;
        session = new SessionManager(getActivity().getApplicationContext());
        // get user data from session
        thisuser = session.getUserDetails();
        String profile_pic = "";//thisuser.get(SessionManager.KEY_PICTURE);
        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
        CircularNetworkImageView preImage;

        //load trending weekly feed
        //getTrendingWeekly();
        showProgress(true);
        loadAllChatMessages();

        return view;
    }
    private void loadAllChatMessages(){
        FirebaseUser thisuser = FirebaseAuth.getInstance().getCurrentUser();
        if(thisuser!=null){//firebase still active
            DatabaseReference mChatDatabase = null;
            Query queryRef = null;
            if(qtype.equals("1")){
                mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(thisUserId);
            }else if(qtype.equals("2")){
                mChatDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
            }else{//for groups
                mChatDatabase = FirebaseDatabase.getInstance().getReference().child("GroupMembers");
                queryRef = mChatDatabase.orderByChild("userid").equalTo(thisUserId);
            }
            valueEventListenerChatUser = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    showProgress(false);
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        Map<String, Object> businessMap = (Map<String, Object>) dataSnapshot.getValue();
                        //count_projects = Long.toString(dataSnapshot.getChildrenCount());
                        //headerTitle.setText("You Have "+count_projects+" "+settitle);
                        for (DataSnapshot recordSnapshot: dataSnapshot.getChildren()) {
                            if(!findItemById(recordSnapshot.getKey()) && !recordSnapshot.getKey().equals(thisUserId)) {
                                FeedItem item = new FeedItem();
                                item.setUserId(recordSnapshot.getKey());
                                item.setFiletype(qtype);
                                item.setPostTags("");
                                if(qtype.equals("1")){
                                    item.setName(recordSnapshot.child("usernames").getValue().toString());
                                    item.setStatus("");
                                    item.setDateJoined("");
                                    item.setProfilePic(recordSnapshot.child("picture").getValue().toString());
                                }else if(qtype.equals("2")){
                                    item.setName(recordSnapshot.child("usernames").getValue().toString());
                                    item.setStatus(recordSnapshot.child("activestatus").getValue().toString());
                                    item.setDateJoined(recordSnapshot.child("lastseen").getValue().toString());
                                    item.setProfilePic(recordSnapshot.child("picture").getValue().toString());
                                }else if(qtype.equals("3")){
                                    item.setName(recordSnapshot.child("title").getValue().toString());
                                    item.setStatus(recordSnapshot.child("createdbyname").getValue().toString());
                                    item.setDateJoined(recordSnapshot.child("createdon").getValue().toString());
                                    item.setPostTags(recordSnapshot.child("groupid").getValue().toString());
                                    hasGroup = true;
                                }
                                feedItems.add(item);
                            }
                        }
                        //Collections.reverse(feedItems);
                        //listView.setAdapter(listAdapter);
                        listAdapter.notifyDataSetChanged();
                    }else{
                        if(qtype.equals("3")){
                            //message
                            /*
                            Intent i = new Intent(getActivity(), GroupsActivity.class);
                            startActivity(i);
                            */
                            //GroupsActivity
                        }
                        //Toast.makeText(LibraryWordsActivity.this, "Sorry we could not find any records.", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            //listen for updates
            if(qtype.equals("1") || qtype.equals("2")){
                mChatDatabase.addValueEventListener(valueEventListenerChatUser);
            }else{
                queryRef.addValueEventListener(valueEventListenerChatUser);
            }
        }
    }

    private boolean findItemById(String itemKey){
        boolean itemFound = false;
        for (FeedItem fitem : feedItems) {
            if (fitem.getUserId().equals(itemKey)) {
                itemFound = true;
            }
        }
        if(itemFound){//that means a match was found
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            qtype = getArguments().getString(ARG_PARAM1);
        }
    }
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
