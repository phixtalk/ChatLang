package com.chatlang.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

/**
 * Created by ADMIN on 12/29/2018.
 */

public class GroupsAdapter extends BaseAdapter {
    private Activity activity;
    private LayoutInflater inflater;
    private List<FeedItem> feedItems;
    FeedItem item;

    CircularNetworkImageView profilePic;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();

    public GroupsAdapter(Activity activity, List<FeedItem> feedItems) {
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

        //first set the display caption
        name.setText(feedItems.get(position).getName());
        status.setText("Created by: "+feedItems.get(position).getStatus());
        lastseen.setText("+JOIN");
        lastseen.setTextColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.success_msg));
        lastseen.setTextSize(TypedValue.COMPLEX_UNIT_DIP,14);
        return convertView;
    }
}
