package com.example.nicat.travelgram.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nicat.travelgram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class loginpage extends AppCompatActivity implements View.OnClickListener{

    //tanimlamalar
    ImageView loglogo;
    EditText logemail,logpassword;
    Button loglogin,logsignup,logforgetpassword;
    //kutuphaneler
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginpage);
        if(isNetworkConnected()==true){
            //tanimlamalar
            hideactionbar();
            degeraktarma();
            if(firebaseAuth.getCurrentUser() != null){
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
            loglogin.setOnClickListener(this);

        }
        else if(isNetworkConnected()==false){
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent chekinternet = getIntent();
                            finish();
                            startActivity(chekinternet);
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(loginpage.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }




    }

    public void hideactionbar() {
        //hideactionbar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
        else {
        }
        View decorView = getWindow().getDecorView();
        int uiOptions =View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


    }
    public void degeraktarma(){
        //view
        loglogo = (ImageView) findViewById(R.id.loglogo);
        logemail = (EditText) findViewById(R.id.logemail);
        logpassword = (EditText) findViewById(R.id.logpassword);
        loglogin = (Button) findViewById(R.id.loglogin);
        logsignup = (Button) findViewById(R.id.logsignup);
        logforgetpassword = (Button) findViewById(R.id.logforgerpassword);
        //kutuphaneler
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void clicklogsignup(View view){
        //progress dialog
        progressDialog.setMessage("Loading...");
        progressDialog.show();
        Intent intent = new Intent(loginpage.this,signup.class);
        startActivity(intent);
    }
    public void clickforgetpassword(View view){
        Intent intent = new Intent(loginpage.this,forgetpassword.class);
        startActivity(intent);
    }
    private void userLogin(){
        //alinicak veriler
        String gmail    = logemail.getText().toString().trim();
        String password = logpassword.getText().toString().trim();

        //tum veri girisleri kontrol
        if(TextUtils.isEmpty(gmail)){
            Toast.makeText(this,"Please enter your email...",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password...",Toast.LENGTH_SHORT).show();
            return;
        }

        //progress dialog
        progressDialog.setMessage("Log in...");
        progressDialog.show();
        //firebase
        firebaseAuth.signInWithEmailAndPassword(gmail,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressDialog.dismiss();
                if(task.isSuccessful()){
                    finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                }
                else{
                    Toast.makeText(loginpage.this,"E-mail or password is incorrect.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onClick(View view) {
        if(view == loglogin){
            userLogin();
        }


    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(loginpage.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
