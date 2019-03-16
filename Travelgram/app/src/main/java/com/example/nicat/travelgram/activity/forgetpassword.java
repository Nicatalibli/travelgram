package com.example.nicat.travelgram.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nicat.travelgram.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class forgetpassword extends AppCompatActivity {

    //view
    EditText forgetemail;
    Button forgetsendbutton;
    //kutuphane
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgetpassword);
        if(isNetworkConnected()==true){
            //tanimlamalar
            hideactionbar();
            degerakarma();
            forgetsendbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseAuth.sendPasswordResetEmail(forgetemail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(forgetpassword.this,"Your password reset link sent to your email.",Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(forgetpassword.this,loginpage.class));
                            }
                            else {
                                Toast.makeText(forgetpassword.this,"Your password reset link don't send to your email. Check your internet conneciton.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });



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

            AlertDialog.Builder builder = new AlertDialog.Builder(forgetpassword.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }




    }

    public void degerakarma(){
        //view deger aktarma
        forgetsendbutton = (Button) findViewById(R.id.forgetsendbutton);
        forgetemail = (EditText) findViewById(R.id.forgetemail);
        //kutuphane tanimlama
        firebaseAuth = FirebaseAuth.getInstance();
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
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(forgetpassword.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
