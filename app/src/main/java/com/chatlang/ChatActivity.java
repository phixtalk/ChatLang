package com.chatlang;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.chatlang.adapter.MessageAdapter;
import com.chatlang.app.AppController;
import com.chatlang.helper.GetTimeAgo;
import com.chatlang.model.ChatModel;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


//EMOJI IMPORTS
import android.content.Context;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.PopupWindow.OnDismissListener;

import com.chatlang.emoji.Emojicon;
import com.chatlang.emoji.EmojiconEditText;
import com.chatlang.emoji.EmojiconGridView;
import com.chatlang.emoji.EmojiconsPopup;
//-- EMOJI IMPORTS

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    private static final int PAGE_LIMIT = 30;

    static final String TAG = MainActivity.class.getSimpleName();
    static final String CHAT_REFERENCE = "ChatMessages";

    //Firebase and GoogleApiClient
    //private FirebaseAuth mFirebaseAuth;
    //private FirebaseUser mFirebaseUser;
    //private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage;
    StorageReference storageReference;

    SessionManager session;

    private EmojiconEditText emojiconEditText;
    //private EditText edMessage;
    //CLass Model
    //private UserModel userModel;

    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btSendMessage,btEmoji;

    private View contentRoot;
    //private EmojIconActions emojIcon;

    ValueEventListener valueEventListenerCurrentUser;
    ValueEventListener valueEventListenerChatUser;

    //File
    private File filePathImageCamera;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //variables from lapit chat
    private String mChatUser, mCurrentUserId, mChatUserToken, mCurrentUsername="", mCurrentPicture="", mChatUsername="", mChatPicture="";
    private Toolbar mChatToolbar;
    private DatabaseReference mRootRef, mRootRefCurrentUser, mRootRefChatUser;
    private TextView mTitleView;
    private CircularNetworkImageView mProfileImage;
    ImageLoader imageLoader = AppController.getInstance().getImageLoader();
    private TextView mLastSeenView;
    // Storage Firebase
    //private StorageReference mImageStorage;

    //private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<ChatModel> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private String url_file="", name_file="", size_file="", latitude="", longitude="", address="";

    //New Solution
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private static final int TOTAL_ITEMS_TO_LOAD = 50;
    private int mCurrentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> userz = session.getUserDetails();
        mCurrentUsername = userz.get(SessionManager.KEY_USERNAMES);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        //get chat user details
        mChatUser = getIntent().getStringExtra("user_id");
        mChatUsername = getIntent().getStringExtra("user_name");

        //get current user details
        mCurrentUserId = getIntent().getStringExtra("current_user_id");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mRootRefCurrentUser = mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser);
        mRootRefChatUser = mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId);

        mTitleView = (TextView) findViewById(R.id.custom_bar_title);
        mLastSeenView = (TextView) findViewById(R.id.custom_bar_seen);
        mProfileImage = (CircularNetworkImageView) findViewById(R.id.custom_bar_image);
        if (imageLoader == null)
            imageLoader = AppController.getInstance().getImageLoader();
        //mProfileImage.setImageBitmap();

        //set page title
        mTitleView.setText(mChatUsername);
        mLastSeenView.setText("Online");

        //check for version and ask permission
        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestForSpecificPermission();
        }

        if (!UtilityClass.verificaConexao(this)){
            UtilityClass.initToast(this,"Network connection failed.");
            finish();
        }else{
            //perform page configurations
            mAdapter = new MessageAdapter(this, messagesList, mCurrentUserId, "private");
            rvListMessage = (RecyclerView) findViewById(R.id.messageRecyclerView);
            mLinearLayoutManager = new LinearLayoutManager(this);
            //rvListMessage.setHasFixedSize(true);//for performance benefits
            rvListMessage.setLayoutManager(mLinearLayoutManager);
            rvListMessage.setAdapter(mAdapter);
            mLinearLayoutManager.setStackFromEnd(true);
            mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_swipe_layout);
            //set button listeners and get other page layouts
            contentRoot = findViewById(R.id.contentRoot);
            //edMessage = (EditText) findViewById(R.id.editTextMessage);
            btSendMessage = (ImageView)findViewById(R.id.buttonMessage);
            btSendMessage.setOnClickListener(this);
            /*
            btEmoji = (ImageView)findViewById(R.id.buttonEmoji);
            btEmoji.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    photoGalleryIntent();
                }
            });
            */

            //load messages
            loadMessages();
        }

        //------- IMAGE STORAGE ---------
        //get current user's picture
        mRootRef.child("Users").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    mCurrentUsername = dataSnapshot.child("lastname").getValue().toString()+" "+dataSnapshot.child("firstname").getValue().toString();
                    mCurrentPicture = dataSnapshot.child("picture").getValue().toString();
                }catch (Exception ex){
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        System.out.println("mCurrentUserId: "+mCurrentUserId);

        //next get the online status and picture of the chat user
        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    String online = dataSnapshot.child("activestatus").getValue().toString();
                    String timeAgo = dataSnapshot.child("lastseen").getValue().toString();
                    mChatPicture = dataSnapshot.child("picture").getValue().toString();
                    mChatUserToken = dataSnapshot.child("firebasetoken").getValue().toString();
                    System.out.println("get_fire_token: "+dataSnapshot.child("firebasetoken").getValue().toString());
                    if(!mChatPicture.equals("")){
                        mProfileImage.setImageUrl(mChatPicture, imageLoader);
                    }
                    if(online.equals("1")) {
                        mLastSeenView.setText("Online");
                    } else {
                        GetTimeAgo getTimeAgo = new GetTimeAgo();
                        long lastTime = Long.parseLong(timeAgo);
                        String lastSeenTime = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                        mLastSeenView.setText("Last seen: "+lastSeenTime);
                        //mLastSeenView.setText(UtilityClass.converteTimestamp(timeAgo));

                    }
                }catch (Exception ex){
                    System.out.println("statusError: "+ex.getMessage());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        try{
            mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    itemPos = 0;
                    loadMoreMessages();
                    mCurrentPage++;
                }
            });
        }catch (Exception ex){

        }

        //STEPS FOR DETECTING WHEN TO SEND NEW MESSAGE PUSH MOTIFICATIONS

        //next check if a chat history exist between these users friends, if it doesnt exist, then create it
        mRootRef.child("Chat").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(mChatUser)){//if this is the first time of chatting with this user
                    Map chatAddMapA = new HashMap();
                    chatAddMapA.put("usernames", mChatUsername);
                    chatAddMapA.put("picture", mChatPicture);
                    chatAddMapA.put("seen", false);
                    chatAddMapA.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMapA.put("notify", "1");//dont sent push notification yet

                    Map chatAddMapB = new HashMap();
                    chatAddMapB.put("usernames", mCurrentUsername);
                    chatAddMapB.put("picture", mCurrentPicture);
                    chatAddMapB.put("seen", false);
                    chatAddMapB.put("timestamp", ServerValue.TIMESTAMP);
                    chatAddMapB.put("notify", "1");//dont sent push notification yet

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser, chatAddMapA);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId, chatAddMapB);
                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }else{
                    //update username and picture, just once, should if ever change, so that even if it was missed
                    Map<String, Object> updateCurrentUser = new HashMap<String, Object>();
                    updateCurrentUser.put("usernames", mChatUsername);
                    updateCurrentUser.put("picture", mChatPicture);
                    updateCurrentUser.put("seen", true);
                    updateCurrentUser.put("notify", "1");
                    mRootRefCurrentUser.updateChildren(updateCurrentUser);
                    //next do same for chat user, just once as well
                    Map<String, Object> updateChatUser = new HashMap<String, Object>();
                    updateChatUser.put("usernames", mCurrentUsername);
                    updateChatUser.put("picture", mCurrentPicture);
                    updateChatUser.put("notify", "1");//set flag to notify chat user
                    mRootRefChatUser.updateChildren(updateChatUser);

                    //mRootRefCurrentUser.child("seen").setValue(true);
                    //set this defaults to avoid sending push notifications when page loads
                    //mRootRefCurrentUser.child("notify").setValue("1");
                    //mRootRefChatUser.child("notify").setValue("1");

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });









        //EMOJIS DECLARATIONS
        emojiconEditText = (EmojiconEditText) findViewById(R.id.editTextMessage);
        final View rootView = findViewById(R.id.contentRoot);
        final ImageView emojiButton = (ImageView) findViewById(R.id.buttonEmoji);
        //final ImageView submitButton = (ImageView) findViewById(R.id.submit_btn);

        emojiconEditText.setText("");//set status to current value

        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (emojiconEditText == null || emojicon == null) {
                    return;
                }

                int start = emojiconEditText.getSelectionStart();
                int end = emojiconEditText.getSelectionEnd();
                if (start < 0) {
                    emojiconEditText.append(emojicon.getEmoji());
                } else {
                    emojiconEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                emojiconEditText.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else {
                        emojiconEditText.setFocusableInTouchMode(true);
                        emojiconEditText.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(emojiconEditText, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else {
                    popup.dismiss();
                }
            }
        });
        // -- EMOJI DECLARATIONS
    }

    // -- EMOJI METHODS
    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
    }
    // -- EMOJI METHODS


    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public static String getExtension(String fileName) {
        String encoded;
        try { encoded = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20"); }
        catch(UnsupportedEncodingException e) { encoded = fileName; }
        return MimeTypeMap.getFileExtensionFromUrl(encoded).toLowerCase();
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
        //android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST){
            if (resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    sendFileFirebase(storageReference,selectedImageUri);
                }else{
                    //URI IS NULL
                }
            }
        }else if (requestCode == IMAGE_CAMERA_REQUEST){
            /*
            if (resultCode == RESULT_OK){
                if (filePathImageCamera != null && filePathImageCamera.exists()){
                    StorageReference imageCameraRef = storageReference.child(filePathImageCamera.getName()+"_camera");
                    sendFileFirebase(imageCameraRef,filePathImageCamera);
                }else{
                    //IS NULL
                }
            }
            */
        }else if (requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place!=null){
                    LatLng latLng = place.getLatLng();
                    /*
                    MapModel mapModel = new MapModel(latLng.latitude+"",latLng.longitude+"");
                    ChatModel chatModel = new ChatModel(userModel,Calendar.getInstance().getTime().getTime()+"",mapModel);
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                    */
                    address = place.getAddress().toString();
                    latitude = latLng.latitude+"";
                    longitude = latLng.longitude+"";
                    sendMessage("location");
                }else{
                    //PLACE IS NULL
                }
            }
        }

    }

    /**
     * Envia o arvquivo para o firebase
     */
    private void sendFileFirebase(StorageReference storageReference, final Uri file){

        /*
        pictureFile = "chat_images/" + UtilityClass.getCurrentTimeStamp().concat(UUID.randomUUID().toString()) + "." + ftype;
        final StorageReference ref = storageReference.child(pictureFile);
        ref.putFile(selectedImage)
        */
        String imgPath = null;
        try {
            imgPath = getRealPathFromURI(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File chkfile = new File(imgPath);
        final long length = chkfile.length() / 1024;
        String ftype = getExtension(chkfile.getName());
        if (ftype.equals("gif") || ftype.equals("jpg") || ftype.equals("png") || ftype.equals("jpeg")) {
            if (length > 4096) {//exceeds 4mb
                //notify user of incorrect file format
                UtilityClass.initToast(this,"Picture size exceeds 4Mb.");
            } else {
                //Util.initToast(this,"Format ok.");
                if (storageReference != null){
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Sending picture...");
                    progressDialog.setCancelable(false);
                    //progressDialog.setCanceledOnTouchOutside(false);//no need
                    progressDialog.show();
                    //Util.initToast(this,"Ref not null.");
                    //final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString().concat(UUID.randomUUID().toString());
                    final String name = "chat_images/" + DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString().concat(UUID.randomUUID().toString()) + "." + ftype;
                    final StorageReference imageGalleryRef = storageReference.child(name);
                    //update the firebase database too
                    imageGalleryRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            UtilityClass.initToast(ChatActivity.this,"File sent");
                            //next, update picture-field in firebase database
                            imageGalleryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Util.initToast(ChatActivity.this,"Success download image link");
                                    //send picture to firebase
                                    url_file = uri.toString();
                                    name_file = name;
                                    size_file = Long.toString(length)+"";
                                    sendMessage("image");
                                    progressDialog.dismiss();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    UtilityClass.initToast(ChatActivity.this,"failure to upload");
                                    progressDialog.dismiss();
                                    // Handle any errors
                                }
                            });
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());

                                }
                            });
                }
            }
        }else{
            UtilityClass.initToast(this,"Picture format not supported.");
        }
    }

    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }
    private void locationPlacesIntent(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sendLocation:
                locationPlacesIntent();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonMessage:
                //sendMessageFirebase();
                sendMessage("text");
                break;
        }
    }


    private String chatMessage = "";
    private void sendMessage(String msgType) {
        String message = emojiconEditText.getText().toString().trim();
        chatMessage = message;
        boolean saveNew = false;//set default to false
        if(msgType.equals("text")&&!TextUtils.isEmpty(message)){//if its a text msg, it cant be empty
            saveNew = true;
        }else if(msgType.equals("image")&&!TextUtils.isEmpty(url_file)){
            saveNew = true;
        }else if(msgType.equals("location")&&!TextUtils.isEmpty(latitude)&&!TextUtils.isEmpty(longitude)){
            saveNew = true;
        }
        if(saveNew){
            String current_user_ref = "Messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "Messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference user_message_push = mRootRef.child("Messages")
                    .child(mCurrentUserId).child(mChatUser).push();

            String push_id = user_message_push.getKey();

            Map messageMap = new HashMap();
            if(msgType.equals("text")){
                messageMap.put("message", message);

            }else if(msgType.equals("image")){
                messageMap.put("message", "");
                messageMap.put("url_file", url_file);
                messageMap.put("name_file", name_file);
                messageMap.put("size_file", size_file);
            }else if(msgType.equals("location")){
                messageMap.put("message", "");
                messageMap.put("latitude", latitude);
                messageMap.put("longitude", longitude);
                messageMap.put("address", address);
            }
            messageMap.put("type", msgType);
            messageMap.put("seen", false);
            messageMap.put("time", Calendar.getInstance().getTime().getTime()+"");//ServerValue.TIMESTAMP
            messageMap.put("from", mCurrentUserId);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            emojiconEditText.setText("");

            //set flag that mCurrentUserId has read mChatUser users chat (true)
            //mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("seen").setValue(true);
            //mRootRef.child("Chat").child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP);
            //set flag that mChatUser has not read mCurrentUserId users chat (false)
            //mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("seen").setValue(false);
            //mRootRef.child("Chat").child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }else{
                        Log.d("CHAT_LOG", "Success");
                    }
                }
            });

            //update seen for current user
            Map<String, Object> updateCurrentUser = new HashMap<String, Object>();
            updateCurrentUser.put("seen", true);
            updateCurrentUser.put("timestamp", ServerValue.TIMESTAMP);
            updateCurrentUser.put("notify", "1");
            mRootRefCurrentUser.updateChildren(updateCurrentUser);
            //update seen for chat user
            Map<String, Object> updateChatUser = new HashMap<String, Object>();
            updateChatUser.put("seen", false);
            updateChatUser.put("timestamp", ServerValue.TIMESTAMP);
            updateChatUser.put("notify", "0");//set flag to notify chat user
            mRootRefChatUser.updateChildren(updateChatUser);

        }
    }

    /**
     * Ler collections chatmodel Firebase
     */
    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //Log.d("CHAT_LOG_PAGINATION_KEY", "Hello got here");
                ChatModel message = dataSnapshot.getValue(ChatModel.class);
                itemPos++;
                if(itemPos == 1){
                    String messageKey = dataSnapshot.getKey();
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                messagesList.add(message);
                mAdapter.notifyDataSetChanged();
                //mLinearLayoutManager.scrollToPosition(messagesList.size() - 1);
                rvListMessage.smoothScrollToPosition(messagesList.size() - 1);
                mRefreshLayout.setRefreshing(false);
                //mMessagesList.scrollToPosition(messagesList.size() - 1);

                //rvListMessage.setAdapter(mAdapter);

                //extra performance benefits
                //rvListMessage.setItemViewCacheSize(20);
                //rvListMessage.setDrawingCacheEnabled(true);
                //rvListMessage.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
                //extra performance benefits

            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserId).child(mChatUser);
        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(PAGE_LIMIT);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ChatModel message = dataSnapshot.getValue(ChatModel.class);
                String messageKey = dataSnapshot.getKey();
                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }
                if(itemPos == 1) {
                    mLastKey = messageKey;
                }
                //Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey + " | itemPos : " + itemPos);
                Log.d("TOTALKEYS", "itemPos : " + itemPos + " | itemPos : " + messagesList.size() + " | CURRENT_POS : "+(messagesList.size() - (itemPos*mCurrentPage)));
                mAdapter.notifyDataSetChanged();
                mRefreshLayout.setRefreshing(false);
                //mLinearLayoutManager.scrollToPositionWithOffset(PAGE_LIMIT, 0);
                //rvListMessage.smoothScrollToPosition(messagesList.size() - (itemPos*mCurrentPage));
                //mLinearLayout.scrollToPositionWithOffset(10, 0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        //NEXT set listeners for both parties On resume
        try{
            //first set the online status
            UtilityClass.updateUserStatus(mCurrentUserId,"1",this);
            //Now for notification on messages, we create a listener, when there is a change to this map, update to seen
            //FIRST, ADD A LISTENER FOR CURRENT USER
            valueEventListenerCurrentUser = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("PUSH_NOTIFICATION_1ST");
                    mRootRefCurrentUser.child("notify").setValue("1");
                    mRootRefCurrentUser.child("seen").setValue(true);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mRootRefCurrentUser.addValueEventListener(valueEventListenerCurrentUser);
            //NEXT ADD A LISTENER FOR CHAT USER
            valueEventListenerChatUser = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Now before sending push, pause a while to make sure, other device has gotten the msg is he acurrent active
                    String checkNotify = String.valueOf(dataSnapshot.child("notify").getValue());
                    System.out.println("CHECK_NOTIFICATION_VAL__"+checkNotify);
                    if(checkNotify.equals("0")){//send push just once,
                        //SEND PUSH NOTIFICATION HERE
                        String notifyTicker = "Chat Message";
                        String notifyTitle = "New Chat Message";
                        String  notifyMessage = mCurrentUsername.toLowerCase() + ": "+chatMessage;
                        //send push
                        UtilityClass.sendChatNotification(getApplicationContext(),mCurrentUserId,mCurrentUsername,mChatUser,mChatUserToken,notifyTicker,notifyTitle,notifyMessage);

                        System.out.println("CHECK_NOTIFICATION_VAL__PUSH_SENT");
                        //UPDATE NOTIFY FLAG TO SENT
                        mRootRefChatUser.child("notify").setValue("1");
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mRootRefChatUser.addValueEventListener(valueEventListenerChatUser);
        }catch (Exception ex){
        }
    }
    @Override
    public void onPause() {
        //Stop listening when user is not on current activity again
        super.onPause();
        try{
            //first set offline status to true
            UtilityClass.updateUserStatus(mCurrentUserId,"0",this);
            System.out.println("onpause: ");
            if (valueEventListenerCurrentUser != null) {
                mRootRefCurrentUser.removeEventListener(valueEventListenerCurrentUser);
            }
            if (valueEventListenerChatUser != null) {
                mRootRefChatUser.removeEventListener(valueEventListenerChatUser);
            }
        }catch (Exception ex){

        }
    }
}
