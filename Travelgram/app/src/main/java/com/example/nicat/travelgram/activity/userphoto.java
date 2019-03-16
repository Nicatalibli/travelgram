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
import android.widget.ListView;
import android.widget.Toast;

import com.example.nicat.travelgram.Adapter.PostClass;
import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.controller;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class userphoto extends AppCompatActivity {

    //kullanici fotograflarini almak
    ListView listView;
    PostClass adapter;
    FirebaseDatabase firebaseDatabase;

    ArrayList<String> userimageFromFB;
    ArrayList<String> postdateFromFB;


    FirebaseAuth firebaseAuth;

    //extra
    Bundle bundle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userphoto);

        if(isNetworkConnected()==true){
            //tanimlamalar

            hideacitonbar();
            degeraktarma();
            kullanicifotograflarinialmak();




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

            AlertDialog.Builder builder = new AlertDialog.Builder(userphoto.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }



    }



    private void kullanicifotograflarinialmak() {
        DatabaseReference newReference;
        if(controller.MAINACTIVITYKULLANICIGORUNTULEMEK==1){
            //goruntulenen profil fotograflari
            newReference = firebaseDatabase.getReference().child("photo").child((String) bundle.get("photouuid"));
        }else
        {   //kendi fotograflarim
            newReference = firebaseDatabase.getReference().child("photo").child(firebaseAuth.getUid());
        }

        newReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    HashMap<String,String> hashMap =(HashMap<String, String>) ds.getValue();

                    userimageFromFB.add(hashMap.get("photourl"));
                    postdateFromFB.add(hashMap.get("photodate"));


                    adapter.notifyDataSetChanged();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void degeraktarma() {
        //extra
        bundle = getIntent().getExtras();
        firebaseAuth = FirebaseAuth.getInstance();
        //kullanici fotograflarini almak
        listView = findViewById(R.id.pr_listview);
        userimageFromFB = new ArrayList<String>();
        postdateFromFB = new ArrayList<String>();

        firebaseDatabase = FirebaseDatabase.getInstance();

        adapter = new PostClass(userimageFromFB,postdateFromFB,this);
        listView.setAdapter(adapter);
    }
    private void hideacitonbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
        else {
        }
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(userphoto.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
