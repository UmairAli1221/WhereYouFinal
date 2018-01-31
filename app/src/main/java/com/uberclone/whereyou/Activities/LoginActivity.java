package com.uberclone.whereyou.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.uberclone.whereyou.MainActivity;
import com.uberclone.whereyou.R;

import dmax.dialog.SpotsDialog;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {
    private TextView tv_login, tv_signUp, tv_forgot;
    private EditText edt_mail, edt_password, edt_username;
    private Button btn_facebook, btn_google;
    private ImageButton imgbtn_next, imgbtn_next2;
    private TextInputLayout username_textInputLayout;
    private DatabaseReference mFirebasedatabase;
    private FirebaseAuth mAuth;
    private String mEmail, mPassword, mUsername;
    private RelativeLayout rootLayout;
    private SpotsDialog waitingdialog;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //import Custome Font!
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Poppins-Regular.ttf").setFontAttrId(R.attr.fontPath).build());
        setContentView(R.layout.activity_login);

        //initViews
        tv_login = (TextView) findViewById(R.id.login_text);
        tv_signUp = (TextView) findViewById(R.id.signup_text);
        edt_mail = (EditText) findViewById(R.id.emailEditText);
        edt_password = (EditText) findViewById(R.id.passwordEditText);
        edt_username = (EditText) findViewById(R.id.usernameEditText);
        btn_facebook = (Button) findViewById(R.id.facebook_login);
        btn_google = (Button) findViewById(R.id.google_login);
        tv_forgot = (TextView) findViewById(R.id.forgotButton);
        imgbtn_next = (ImageButton) findViewById(R.id.next1);
        imgbtn_next2 = (ImageButton) findViewById(R.id.next2);

        username_textInputLayout = (TextInputLayout) findViewById(R.id.username_hint);
        rootLayout = (RelativeLayout) findViewById(R.id.rootlayout);

        //init Firebase
        mFirebasedatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //init Waitng SpotProgress
        waitingdialog = new SpotsDialog(LoginActivity.this);


        //Login Text Click
        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username_textInputLayout.setVisibility(View.GONE);
                tv_login.setTypeface(tv_login.getTypeface(), Typeface.BOLD);
                tv_signUp.setTypeface(tv_signUp.getTypeface(), Typeface.NORMAL);
                tv_signUp.setTextColor(getResources().getColor(R.color.press_login_signup));
                tv_login.setTextColor(getResources().getColor(R.color.colorWhite));
                imgbtn_next.setVisibility(View.VISIBLE);
                imgbtn_next2.setVisibility(View.GONE);
            }
        });

        //SignUp Click
        tv_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username_textInputLayout.setVisibility(View.VISIBLE);
                tv_login.setTypeface(tv_login.getTypeface(), Typeface.NORMAL);
                tv_signUp.setTypeface(tv_signUp.getTypeface(), Typeface.BOLD);
                tv_login.setTextColor(getResources().getColor(R.color.press_login_signup));
                tv_signUp.setTextColor(getResources().getColor(R.color.colorWhite));
                imgbtn_next.setVisibility(View.GONE);
                imgbtn_next2.setVisibility(View.VISIBLE);
            }
        });
        tv_forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showForgotDialouge();
            }
        });

        imgbtn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //init strings
                mEmail = edt_mail.getText().toString();
                mPassword = edt_password.getText().toString();
                //Login
                if (TextUtils.isEmpty(mEmail)) {
                    edt_mail.setError("Email Cannot Be Empty");
                    edt_mail.requestFocus();
                } else if (TextUtils.isEmpty(mPassword)) {
                    edt_password.setError("Password Cannot Be Empty");
                    edt_password.requestFocus();
                } else {
                    waitingdialog.show();
                    mAuth.signInWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String mUserID = mAuth.getCurrentUser().getUid();
                                String device_token = FirebaseInstanceId.getInstance().getToken();
                                mFirebasedatabase.child("Users").child(mUserID).child("device_token").setValue(device_token);
                                mFirebasedatabase.child("Users").child(mUserID).child("Uid").setValue(mUserID);
                                waitingdialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                waitingdialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch (FirebaseNetworkException e) {
                                    Snackbar.make(rootLayout, "No Network!", Snackbar.LENGTH_SHORT).show();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    edt_mail.setError("Weak Password");
                                    edt_mail.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    edt_mail.setError("Invalid Username or Password");
                                    edt_mail.requestFocus();
                                    edt_password.setError("Inavalid Username or Pasword");
                                    edt_password.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    edt_mail.setError("User Already Exist");
                                    edt_mail.requestFocus();
                                } catch (Exception e) {
                                    Snackbar.make(rootLayout, "" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
        imgbtn_next2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmail = edt_mail.getText().toString();
                mPassword = edt_password.getText().toString();
                mUsername = edt_username.getText().toString();
                //signUp
                if (TextUtils.isEmpty(mEmail)) {
                    edt_mail.setError("Email Cannot Be Empty");
                    edt_mail.requestFocus();
                } else if (TextUtils.isEmpty(mUsername)) {
                    edt_username.setError("Username cannot be empty");
                    edt_username.requestFocus();
                }else if (TextUtils.isEmpty(mPassword)) {
                    edt_password.setError("Password Cannot Be Empty");
                    edt_password.requestFocus();
                } else if (mPassword.length() < 6) {
                    edt_password.setError("Password Too Short");
                    edt_password.requestFocus();
                }  else {
                    waitingdialog.show();
                    mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String mUserID = mAuth.getCurrentUser().getUid();
                                String device_token = FirebaseInstanceId.getInstance().getToken();
                                mFirebasedatabase.child("Users").child(mUserID).child("device_token").setValue(device_token);
                                mFirebasedatabase.child("Users").child(mUserID).child("Uid").setValue(mUserID);
                                mFirebasedatabase.child("Users").child(mUserID).child("name").setValue(mUsername);
                                mFirebasedatabase.child("Users").child(mUserID).child("email").setValue(mEmail);
                                waitingdialog.dismiss();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                waitingdialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch (FirebaseNetworkException e) {
                                    Snackbar.make(rootLayout, "No Network!", Snackbar.LENGTH_SHORT).show();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    edt_mail.setError("Weak Password");
                                    edt_mail.requestFocus();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    edt_mail.setError("Invalid Username or Password");
                                    edt_mail.requestFocus();
                                    edt_password.setError("Inavalid Username or Pasword");
                                    edt_password.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    edt_mail.setError("User Already Exist");
                                    edt_mail.requestFocus();
                                } catch (Exception e) {
                                    Snackbar.make(rootLayout, "" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void showForgotDialouge() {
        AlertDialog.Builder dialoge = new AlertDialog.Builder(this);
        dialoge.setTitle("Reset Password:-");
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View Register_Layout = layoutInflater.inflate(R.layout.layout_forgot_password, null);
        final EditText edt_email = Register_Layout.findViewById(R.id.edt_fg_email);
        dialoge.setView(Register_Layout);
        dialoge.setPositiveButton("Forgot Password", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(edt_email.getText().toString())) {
                    Snackbar.make(rootLayout, "Please Enter Email Address", Snackbar.LENGTH_SHORT).show();
                } else {
                    waitingdialog.show();
                    mAuth.sendPasswordResetEmail(edt_email.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                waitingdialog.dismiss();
                                Snackbar.make(rootLayout, "Link Has Sent To Your Email", Snackbar.LENGTH_SHORT).show();
                            } else {
                                waitingdialog.dismiss();
                                try {
                                    throw task.getException();
                                } catch (FirebaseNetworkException e) {
                                    Snackbar.make(rootLayout, "No Network!", Snackbar.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Snackbar.make(rootLayout, "" + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
        dialoge.setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alert = dialoge.create();
        alert.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.BLACK);
        Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.BLACK);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser()!=null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}
