package com.chatlang;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText mChatNameView;
    private EditText mPhoneView;
    private EditText mSMSCodeView;
    private View mProgressView;
    private View mLoginFormView;

    private TextView resendSMSCode;
    private TextView message;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationid;
    private PhoneAuthCredential phoneCredential;

    SessionManager session;
    RequestQueue requestQueue;

    FirebaseUser user = null;

    Button mEmailSignInButton, verifyPhoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        session = new SessionManager(getApplicationContext());
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        message = (TextView) findViewById(R.id.message);
        mChatNameView = (EditText) findViewById(R.id.chatname);

        mAuth = FirebaseAuth.getInstance();

        mSMSCodeView = (EditText) findViewById(R.id.smscode);
        mSMSCodeView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptSMSCodeVerication();
                    return true;
                }
                return false;
            }
        });


        mPhoneView = (EditText) findViewById(R.id.phone);
        mPhoneView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        verifyPhoneButton = (Button) findViewById(R.id.verify_auth_button);
        verifyPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSMSCodeVerication();
            }
        });

        resendSMSCode = (TextView) findViewById(R.id.action_resend_smscode);
        resendSMSCode.setPaintFlags(resendSMSCode.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        resendSMSCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendSMSCodeCallBack();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private String chatname, phone;
    private void attemptLogin() {
        // Reset errors.
        mChatNameView.setError(null);
        mPhoneView.setError(null);

        // Store values at the time of the login attempt.
        chatname = mChatNameView.getText().toString();
        phone = mPhoneView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(chatname)) {
            mChatNameView.setError(getString(R.string.error_field_required));
            focusView = mChatNameView;
            cancel = true;
        }else if(TextUtils.isEmpty(phone)){
            mPhoneView.setError(getString(R.string.error_field_required));
            focusView = mPhoneView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            startPhoneNumberVerification();
        }
    }


    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void attemptSMSCodeVerication() {
        mSMSCodeView.setError(null);
        String smscode = mSMSCodeView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(smscode)) {
            mSMSCodeView.setError(getString(R.string.error_field_required));
            focusView = mSMSCodeView;
            cancel = true;
        }

        if (cancel) {
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            showProgress(true);
            verifyPhoneNumberWithCode(verificationid, smscode);
        }
    }

    private void startPhoneNumberVerification() {
        showProgress(true);
        //this will send sms code to users phone
        String phoneNumber = mPhoneView.getText().toString();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                120,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }
    private void resendSMSCodeCallBack(){
        showProgress(true);
        String phoneNumber = mPhoneView.getText().toString();

        setUpVerificationCallbacks();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                mResendToken);
    }
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void setUpVerificationCallbacks(){
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                System.out.println("phoneAuth: verified ");
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                showProgress(false);
                message.setVisibility(View.VISIBLE);
                message.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.failed_msg));
                if(e instanceof FirebaseAuthInvalidCredentialsException){
                    message.setText("Invalid Credentials: \n " + e.getLocalizedMessage().toString());
                }else if(e instanceof FirebaseTooManyRequestsException){
                    message.setText("Something went wrong: \n SMS Quota Exceeded");
                }else{
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    message.setText("Something went wrong: \n " + e.getMessage().toString());
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                //System.out.println("phoneAuth: code sent ");
                showProgress(false);

                mResendToken = forceResendingToken;
                verificationid = s;

                mEmailSignInButton.setVisibility(View.GONE);
                mChatNameView.setVisibility(View.GONE);
                mPhoneView.setVisibility(View.GONE);

                mSMSCodeView.setVisibility(View.VISIBLE);//make sms code opt field visible
                verifyPhoneButton.setVisibility(View.VISIBLE);
                resendSMSCode.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if(mAuth.getCurrentUser()!=null) {//firebase is active
                                //check for new keywords to sync
                                final String userId = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                current_user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.getChildrenCount() == 0) {//next, if no such record exists, then create a new one
                                            DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                                            Map newPost = new HashMap();
                                            newPost.put("picture", "");
                                            newPost.put("usernames", chatname);
                                            newPost.put("phonenumber", phone);//use updated phone number
                                            newPost.put("activestatus", "1");
                                            newPost.put("datecreated", ServerValue.TIMESTAMP);
                                            newPost.put("accountstatus", "1");
                                            newPost.put("firebasetoken", "");
                                            newPost.put("lastseen", ServerValue.TIMESTAMP);
                                            newPost.put("isadmin", "0");
                                            //save to firebase nosql
                                            current_user_db.setValue(newPost);
                                            //save to sql
                                            saveDataToSQL(userId,chatname,phone);
                                            //save to offline
                                            session.createSessionData(userId,chatname,phone);
                                        }else{//user already has account before, log him in
                                            //String mphonenumber = String.valueOf(dataSnapshot.child("phonenumber").getValue());
                                            //String musernames = String.valueOf(dataSnapshot.child("usernames").getValue());
                                            //session.updateSessionField("accountstatus", maccountstatus);//update session value
                                        }
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        showProgress(false);
                                        message.setVisibility(View.VISIBLE);
                                        message.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.failed_msg));
                                        message.setText("Something went wrong: \n " + databaseError.getMessage().toString());
                                    }
                                });

                            }

                            //showProgress(false);
                            System.out.println("phoneAuth: login success ");
                            //on successful verification, do nothing, allow firebase auth listener to pick up the change of login state, and perform further verification
                        } else {
                            showProgress(false);
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                //Toast.makeText(LoginActivity.this, "", Toast.LENGTH_SHORT).show();
                                message.setVisibility(View.VISIBLE);
                                message.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.failed_msg));
                                message.setText("Something went wrong: \n " + task.getException().getMessage().toString());
                            }
                        }
                    }
                });

    }

    private void saveDataToSQL(String userid,String usernames,String phonenumber){
        try {
            String link = getString(R.string.base_url).concat("register");
            String firebase_token = FirebaseInstanceId.getInstance().getToken();//get current firebase token manually

            Map<String, String> postParam= new HashMap<String, String>();
            postParam.put("picture", "");
            postParam.put("userid",userid);
            postParam.put("usernames",usernames);
            postParam.put("phonenumber",phonenumber);
            postParam.put("accountstatus", "1");
            postParam.put("firebasetoken",firebase_token);
            postParam.put("isadmin", "0");

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, link, new JSONObject(postParam),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            System.out.println("mytag response:"+response.toString());
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("mytag response:"+error.getMessage().toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            // Adding request to request queue
            requestQueue.add(jsonObjReq);

        } catch (Exception e) {
            System.out.println("mytag response:"+e.getMessage().toString());
        }
    }

}
