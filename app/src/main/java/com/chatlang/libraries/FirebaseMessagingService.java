package com.chatlang.libraries;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.chatlang.GroupChatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;
import com.chatlang.ChatActivity;
import com.chatlang.MainActivity;
import com.chatlang.R;

/**
 * Created by filipp on 5/23/2016.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            final String pushTicker = remoteMessage.getData().get("ticker");
            final String pushTitle = remoteMessage.getData().get("title");
            final String pushContent = remoteMessage.getData().get("message");

            String gotopage = remoteMessage.getData().get("gotopage");

            String senduserid = remoteMessage.getData().get("senduserid");
            String sendusername = remoteMessage.getData().get("sendusername");
            String receiveruserid = remoteMessage.getData().get("receiveruserid");

            //next alert user on phone and navigate to appropiate page
            Intent i = null;
            final PendingIntent pendingIntent;

            if(gotopage.equals("testpush")){
                i = new Intent(this,MainActivity.class);
                i.putExtra("from_push", "true");
            }else if(gotopage.equals("chatmessage")){//for chat messages
                i = new Intent(this,ChatActivity.class);
                i.putExtra("user_id", senduserid);
                i.putExtra("user_name", sendusername);
                i.putExtra("current_user_id", receiveruserid);
            }else if(gotopage.equals("chatgroupmessage")){//for group chat messages
                i = new Intent(this, GroupChatActivity.class);
                i.putExtra("group_id", remoteMessage.getData().get("groupid"));
                i.putExtra("group_name", remoteMessage.getData().get("groupname"));
                i.putExtra("created_on", remoteMessage.getData().get("createdon"));
            }

            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            pendingIntent = PendingIntent.getActivity(this,0,i,PendingIntent.FLAG_UPDATE_CURRENT);

            if(gotopage.equals("chatmessage")){
                //first check if user has seen chat before alerting
                DatabaseReference mRootRef, mRootRefCurrentUser;
                mRootRef = FirebaseDatabase.getInstance().getReference();
                mRootRefCurrentUser = mRootRef.child("Chat").child(receiveruserid).child(senduserid);
                mRootRefCurrentUser.addListenerForSingleValueEvent(new ValueEventListener() {//listen only once
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String checkSeen = String.valueOf(dataSnapshot.child("seen").getValue());
                        if(checkSeen.equals("false")){//send notification
                            //System.out.println("checkSeen: false");
                            showNotification(pendingIntent, pushTitle,pushContent,pushTicker);
                        }else{
                            //System.out.println("checkSeen: true");
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }else{
                showNotification(pendingIntent, pushTitle,pushContent,pushTicker);
            }
        }catch (Exception ex){
            System.out.println("myfire_tag:"+ex.getMessage());//set to know how many times its called
        }
    }
    private void showNotification(PendingIntent pendingIntent, String pushTitle,String pushContent,String pushTicker){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(pushTitle)
                .setContentText(pushContent)
                .setTicker(pushTicker)
                .setSmallIcon(R.drawable.logo)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(pendingIntent);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(0,builder.build());
    }
}
