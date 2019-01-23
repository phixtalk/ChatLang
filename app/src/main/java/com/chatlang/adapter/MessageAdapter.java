package com.chatlang.adapter;

/**
 * Created by ADMIN on 12/4/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chatlang.Messages;
import com.chatlang.R;
import com.chatlang.UtilityClass;
import com.chatlang.model.ChatModel;
import com.chatlang.view.FullScreenImageActivity;
//import com.squareup.picasso.Picasso;

import java.security.PublicKey;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{
    private List<ChatModel> mMessageList;
    private DatabaseReference mUserDatabase;
    private Activity activity;
    private String userBindID, pageMode;

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    public MessageAdapter(Activity activity, List<ChatModel> mMessageList, String userBindId, String pageMode) {
        this.activity = activity;
        this.mMessageList = mMessageList;
        this.userBindID = userBindId;
        this.pageMode = pageMode;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right,parent,false);
            return new MessageViewHolder(view, mMessageList);
        }else if (viewType == LEFT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left,parent,false);
            return new MessageViewHolder(view, mMessageList);
        }else if (viewType == RIGHT_MSG_IMG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img,parent,false);
            return new MessageViewHolder(view, mMessageList);
        }else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img,parent,false);
            return new MessageViewHolder(view, mMessageList);
        }
    }

    @Override
    public int getItemViewType(int position) {
        //FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        //FirebaseUser mFirebaseUser;
        //mFirebaseUser = mFirebaseAuth.getCurrentUser();
        //String current_user_id = mFirebaseUser.getUid();
        String current_user_id = userBindID;
        ChatModel model = mMessageList.get(position);

        if(model.getType().equals("text")){//for text messages
            if(model.getFrom().equals(current_user_id)){
                return RIGHT_MSG;
            }else{
                return LEFT_MSG;
            }
        }else if(model.getType().equals("image")){
            if(model.getFrom().equals(current_user_id)){
                return RIGHT_MSG_IMG;
            }else{
                return LEFT_MSG_IMG;
            }
        }else if(model.getType().equals("location")){
            if(model.getFrom().equals(current_user_id)){
                return RIGHT_MSG_IMG;
            }else{
                return LEFT_MSG_IMG;
            }
        }else{
            return RIGHT_MSG;
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp,tvLocation;
        TextView txtMessage, txtUsernames;
        ImageView ivUser,ivChatPhoto;

        public MessageViewHolder(View itemView, final List<ChatModel> mList) {
            super(itemView);
            //pageMode
            txtUsernames = (TextView) itemView.findViewById(R.id.username);
            tvTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUserChat);
        }

        public void setTxtUsernames(String userid, String usernames, String currentUserid){
            if(pageMode.equals("group") && !userid.equals(currentUserid)){
                txtUsernames.setVisibility(View.VISIBLE);
                txtUsernames.setText(usernames);
            }else{
                txtUsernames.setVisibility(View.GONE);
            }

        }
        public void setTxtMessage(String message){
            if (txtMessage == null)return;
            txtMessage.setText(message);
            Typeface custom_font = Typeface.createFromAsset(activity.getAssets(),  activity.getString(R.string.font_family));
            txtMessage.setTypeface(custom_font,1);
        }
        public void setLocationAddress(String address){
            if (address == null)return;
            tvLocation.setText(address);
        }

        public void setIvUser(String urlPhotoUser){
            if (ivUser == null)return;
            //Glide.with(ivUser.getContext()).load(urlPhotoUser).centerCrop().transform(new CircleTransform(ivUser.getContext())).override(40,40).into(ivUser);
        }

        public void setTvTimestamp(String timestamp){
            if (tvTimestamp == null)return;
            tvTimestamp.setText(UtilityClass.converteTimestamp(timestamp));
        }

        public void setIvChatPhoto(String url){

            if (ivChatPhoto == null){
                return;
            }else{
                Glide.with(ivChatPhoto.getContext()).load(url)
                        .into(ivChatPhoto);
                //ivChatPhoto.setOnClickListener(this);
            }

            /*
            .override(100, 100)
                    .fitCenter()
                    */
        }

        public void tvIsLocation(int visible){
            if (tvLocation == null)return;
            tvLocation.setVisibility(visible);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, final int i) {
        final ChatModel model = mMessageList.get(i);
        viewHolder.setTxtUsernames( model.getFrom(), model.getFromName(), userBindID);
        viewHolder.setTxtMessage(model.getMessage());
        viewHolder.setTvTimestamp(model.getTime());
        viewHolder.tvIsLocation(View.GONE);

        //first, check if chat has picture attached
        if (model.getUrl_file() != null && !model.getUrl_file().equals("null")){
            viewHolder.tvIsLocation(View.GONE);
            viewHolder.setIvChatPhoto(model.getUrl_file());
        }else if(model.getType().equals("location")){//next check if, location was uploaded
            viewHolder.setIvChatPhoto(UtilityClass.local(model.getLatitude(),model.getLongitude()));
            viewHolder.tvIsLocation(View.VISIBLE);
            viewHolder.setLocationAddress(model.getAddress());
        }

        if (((MessageViewHolder) viewHolder).ivChatPhoto != null) {
            ((MessageViewHolder) viewHolder).ivChatPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (model.getUrl_file() != null && !model.getUrl_file().equals("null")){
                        clickImageChat(v,i,model.getFrom(),model.getUrl_file());
                    }else if(model.getType().equals("location")){
                        clickImageMapChat(v,i,model.getLatitude(),model.getLongitude());
                    }
                }
            });
        }

        if (((MessageViewHolder) viewHolder).tvLocation != null) {
            ((MessageViewHolder) viewHolder).tvLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickImageMapChat(v,i,model.getLatitude(),model.getLongitude());
                }
            });
        }
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public void clickImageChat(View view, int position, String nameUser, String urlPhotoClick) {
        Intent intent = new Intent(activity.getApplicationContext(),FullScreenImageActivity.class);
        intent.putExtra("userId",nameUser);
        intent.putExtra("urlPhotoUser","");
        intent.putExtra("urlPhotoClick",urlPhotoClick);
        activity.startActivity(intent);
    }
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude,longitude,latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        activity.startActivity(intent);
    }
}
