package com.chatlang.view;

/**
 * Created by ADMIN on 12/4/2018.
 */
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;

//import com.bumptech.glide.request.transition.Transition;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.chatlang.R;
import com.chatlang.adapter.CircleTransform;

import java.util.Map;

public class FullScreenImageActivity extends AppCompatActivity {

    private TouchImageView mImageView;
    private ImageView ivUser;
    private TextView tvUser;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);
        bindViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setValues();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private void bindViews(){
        progressDialog = new ProgressDialog(this);
        mImageView = (TouchImageView) findViewById(R.id.imageView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ivUser = (ImageView)toolbar.findViewById(R.id.avatar);
        tvUser = (TextView)toolbar.findViewById(R.id.title);
    }

    private void setValues(){
        String nameUser,urlPhotoUser,urlPhotoClick;
        String userId = getIntent().getStringExtra("userId");
        urlPhotoUser = getIntent().getStringExtra("urlPhotoUser");
        urlPhotoClick = getIntent().getStringExtra("urlPhotoClick");

        /*
        FirebaseUser thisuser = FirebaseAuth.getInstance().getCurrentUser();
        if(thisuser!=null){//firebase still active//addListenerForSingleValueEvent
            DatabaseReference mBusinessDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            mBusinessDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        Map<String, Object> businessMap = (Map<String, Object>) dataSnapshot.getValue();
                        tvUser.setText(businessMap.get("usernames").toString());
                        if(!TextUtils.isEmpty(businessMap.get("picture").toString())){
                            Glide.with(FullScreenImageActivity.this).load(businessMap.get("picture").toString()).centerCrop().
                                    transform(new CircleTransform(FullScreenImageActivity.this)).override(40,40).into(ivUser);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        }
        */
        Glide.with(this).load( urlPhotoClick).asBitmap().override(640,640).fitCenter().into(new SimpleTarget<Bitmap>() {

            @Override
            public void onLoadStarted(Drawable placeholder) {
                progressDialog.setMessage("Fetching picture...");
                progressDialog.show();
            }
            /*
            @Override
            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {

            }
            */
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                progressDialog.dismiss();
                mImageView.setImageBitmap(resource);
            }
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Toast.makeText(FullScreenImageActivity.this,"Something went wrong.",Toast.LENGTH_LONG).show();
                progressDialog.dismiss();
            }
        });
    }
}
