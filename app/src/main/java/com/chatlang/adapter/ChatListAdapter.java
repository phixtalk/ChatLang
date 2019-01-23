package com.chatlang.adapter;

/**
 * Created by ADMIN on 12/23/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.chatlang.CircularNetworkImageView;
import com.chatlang.R;
import com.chatlang.app.AppController;
import com.chatlang.data.FeedItem;
import com.chatlang.helper.GetTimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.util.List;

public class ChatListAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    FeedItem item;

    CircularNetworkImageView profilePic;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public ChatListAdapter(Activity activity, List<FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
    }

    @Override
    public int getCount() {
        return feedItems.size();
    }

    @Override
    public Object getItem(int location) {
        return feedItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.list_items, null);

        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();

        TextView name = (TextView) convertView.findViewById(R.id.caption);
        TextView lastseen = (TextView) convertView.findViewById(R.id.lastseen);
        TextView status = (TextView) convertView.findViewById(R.id.statusMeg);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        final ImageView onlineBubble = (ImageView) convertView.findViewById(R.id.onlineBubble);
        //first set the display caption
        name.setText(feedItems.get(position).getName());

        if(feedItems.get(position).getFiletype().equals("1")){//for chat history

        }else if(feedItems.get(position).getFiletype().equals("2")){

        }else if(feedItems.get(position).getFiletype().equals("3")){
            status.setText("Created by: "+feedItems.get(position).getStatus());

            GetTimeAgo getTimeAgo = new GetTimeAgo();
            long lastTime = Long.parseLong(feedItems.get(position).getDateJoined());
            String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, activity);
            lastseen.setText(lastSeenTime);
        }

        if(feedItems.get(position).getFiletype().equals("1")){//for chat history
            DatabaseReference mRootRef;
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mRootRef.child("Users").child(feedItems.get(position).getUserId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        String online = dataSnapshot.child("activestatus").getValue().toString();
                        if(online.equals("1")) {
                            onlineBubble.setVisibility(View.VISIBLE);
                        }else{
                            onlineBubble.setVisibility(View.GONE);
                        }
                    }catch (Exception ex){
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{//else for users list, simple get online status directly
            if(feedItems.get(position).getStatus().equals("1")) {
                onlineBubble.setVisibility(View.VISIBLE);
            }else{
                onlineBubble.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}
