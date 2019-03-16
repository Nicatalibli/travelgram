package com.example.nicat.travelgram.activity;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.nicat.travelgram.Adapter.PostAdapter;
import com.example.nicat.travelgram.BottomNavigationHelper;
import com.example.nicat.travelgram.Model.Post;
import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.fragment_search;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class trend extends AppCompatActivity {
    private BottomNavigationView trend_nav;
    ImageButton trend_searchbar;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    private List<String> followingList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trend);
        degeraktarma();
        navigationeffectsilme();
        navigationbar();
        hideacitonbar();

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getApplicationContext(),postList);
        recyclerView.setAdapter(postAdapter);

        checkFollowing();

    }

    private void checkFollowing(){
        followingList = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    followingList.add(snapshot.getKey());
                }

                readPosts();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readPosts(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("photo");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    for(DataSnapshot snapshot1 : snapshot.getChildren()){

                        Post post = snapshot1.getValue(Post.class);
                        for (String id : followingList){

                            if (post.getPublisher().equals(id)){
                            postList.add(post);
                             }


                        }
                    }
                }



                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void trendsearchbar (View view){
        trend_searchbar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        android.support.v4.app.FragmentTransaction f = getSupportFragmentManager().beginTransaction();
        f.replace(R.id.fragment,new fragment_search());
        f.commit();
    }

    private void degeraktarma() {
        trend_nav = (BottomNavigationView) findViewById(R.id.trend_nav);
        trend_searchbar = (ImageButton) findViewById(R.id.trend_searchbar);
    }
    private void navigationeffectsilme() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.trend_nav);
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
    }
    private void navigationbar() {
        //secilmis iconu secmek
        trend_nav.getMenu().findItem(R.id.nav_trend).setChecked(true);
        trend_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_world:
                        finish();
                        startActivity(new Intent(trend.this,MainActivity.class));
                        return true;
                    case R.id.nav_binoculars:
                        finish();
                        startActivity(new Intent(trend.this,Binoculars.class));
                        return true;
                    case R.id.nav_camera:
                        finish();
                        startActivity(new Intent(trend.this,camera.class));
                        return true;
                    case R.id.nav_trend:
                        finish();
                        startActivity(new Intent(trend.this,trend.class));
                        return true;
                    case R.id.nav_profile:
                        finish();
                        startActivity(new Intent(trend.this,profile.class));
                        return true;
                    default:
                        return false;
                }}});
    }
    private void hideacitonbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
        else {
        }
    }
}
