package com.chatlang;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.IntentCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.chatlang.adapter.ViewPagerAdapter;
import com.chatlang.fragment.ChatFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    SessionManager session;
    String userbind, usernames;
    private View mProgressView;
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(getApplicationContext());
        HashMap<String, String> userz = session.getUserDetails();
        userbind = userz.get(SessionManager.KEY_TOKEN);
        usernames = userz.get(SessionManager.KEY_USERNAMES);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(ChatFragment.newInstance("1"), "CHATS");//SolverFragment.newInstance("1")
        viewPagerAdapter.addFragments(ChatFragment.newInstance("2"), "FRIENDS");
        viewPagerAdapter.addFragments(ChatFragment.newInstance("3"), "GROUPS");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        //if new page was specified
        Intent pageIntent = getIntent();
        Bundle recdData = pageIntent.getExtras();
        if (pageIntent.hasExtra("position")) {
            viewPager.setCurrentItem(Integer.parseInt(recdData.getString("position")));
        }

        mProgressView = findViewById(R.id.progress_overlay);

        final String firebase_token = FirebaseInstanceId.getInstance().getToken();
        UtilityClass.updateFirebaseTokenMethod(userbind, firebase_token, this);

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            requestForSpecificPermission();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createGroupDialog();
                /*Snackbar.make(view, "Have a new new chat experience", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                */

            }
        });

        //fetchContacts();

    }
    private void createGroupDialog(){
        //String newKeyword = UtilityClass.callSuggestKeyword(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.prompts, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                String newGroup = userInput.getText().toString();
                                if(!TextUtils.isEmpty(newGroup)){
                                    createNewGroup(newGroup);
                                }else{
                                    Toast.makeText(MainActivity.this, "Group title cannot be empty.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }
    private void createNewGroup(final String newGroup){
        showProgress(true);
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        final DatabaseReference current_user_db = postRef.push();
        Map newPost = new HashMap();
        newPost.put("title", newGroup);
        newPost.put("picture", "");
        newPost.put("totalmembers", 1);
        newPost.put("createdby", userbind);
        newPost.put("createdbyname", usernames);
        newPost.put("createdon", ServerValue.TIMESTAMP);//active
        current_user_db.setValue(newPost, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                showProgress(false);
                if (databaseError != null) {
                    messageAlertBox("New Group Error", "Something went wrong. Please try again later.", "error");
                } else{
                    //next add user to created group

                    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference().child("GroupMembers");
                    final DatabaseReference current_user_dbs = postRef.push();
                    Map newPost = new HashMap();
                    newPost.put("groupid", current_user_db.getKey());
                    newPost.put("title", newGroup);
                    newPost.put("picture", "");
                    newPost.put("createdby", userbind);
                    newPost.put("createdbyname", usernames);
                    newPost.put("createdon", ServerValue.TIMESTAMP);
                    newPost.put("userid", userbind);
                    newPost.put("user_group_id", userbind+"_"+current_user_db.getKey());
                    newPost.put("joinedon", ServerValue.TIMESTAMP);

                    current_user_dbs.setValue(newPost, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            //showProgress(false);
                            if (databaseError != null) {//on error
                                messageAlertBox("New Group Error", "Something went wrong. Please try again later.", "error");
                            } else{//on success, increment the total members count by 1
                                //UtilityClass.updateFieldByValue("Groups", feedItems.get(position).getUserId(), "totalmembers", 1l, "increase");
                                messageAlertBox("New Group Success", "Group Was Created Successfully.", "success");
                            }
                        }
                    });
                }
            }
        });
    }
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    private void messageAlertBox(String title, String message, final String action){
        AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if(action.equals("success")){
                                    if(viewPager.getCurrentItem()!=2){
                                        viewPager.setCurrentItem(2);
                                    }
                                }else{
                                    dialog.cancel();
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    public void fetchContacts() {
        String phoneNumber = null;
        String email = null;
        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;
        StringBuffer output = new StringBuffer();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);
        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));
                if (hasPhoneNumber > 0) {
                    output.append("\n First Name:" + name);
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);
                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                        output.append("\n Phone number:" + phoneNumber);
                        System.out.println("ANDROID_CONTACTS response: "+name+" --- "+phoneNumber);
                    }
                    phoneCursor.close();
                    // Query and loop for every email of the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,    null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);
                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                        output.append("\nEmail:" + email);
                    }
                    emailCursor.close();
                }
                output.append("\n");
            }
            //System.out.println("ANDROID_CONTACTS response: "+output);
            //outputText.setText(output);
        }
    }
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 101);
        //android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getApplicationContext(), "ChatLang Needs Contacts Permission to Work Properly.", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }  else if (id == R.id.action_invite) {
            callInviteFriend();
            return true;
        } else if(id == R.id.action_groups || id == R.id.action_all_groups){
            Intent i = new Intent(this, GroupsActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void callInviteFriend(){
        UtilityClass.inviteFriend(this);
    }
    @Override
    public void onResume(){
        super.onResume();
        //NEXT set listeners for both parties On resume
        try{
            UtilityClass.updateUserStatus(userbind,"1",this);
        }catch (Exception ex){
        }
    }
    @Override
    public void onPause() {
        //Stop listening when user is not on current activity again
        super.onPause();
        try{
            UtilityClass.updateUserStatus(userbind,"0",this);
        }catch (Exception ex){
        }
    }
}
