package com.example.nicat.travelgram.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicat.travelgram.BottomNavigationHelper;
import com.example.nicat.travelgram.Adapter.MyItem;
import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.controller;
import com.example.nicat.travelgram.getuserphotodata;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

public class profile extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private BottomNavigationView profile_nav;

    //firebase
    FirebaseAuth firebaseAuth;
    DatabaseReference kullanici;
    DatabaseReference digerkullanici;

    //kullanicibilgileri
    CircularImageView pr_profilepictures;
    TextView pr_profilename,pr_username,pr_countryname,pr_email,pr_photocounter;
    ImageView pr_logout,pr_editprofile;

    //map
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey" ;
    private MapView mMapView;
    private GoogleMap PMap;
    private DatabaseReference pUsers;
    private DatabaseReference DigerUser;
    Marker marker;
    private ClusterManager<MyItem> mClusterManager;

    //view
    Button pr_userphoto;


    //extra
    Bundle bundle;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if(isNetworkConnected()==true){

            //tanimlamalar
            degeraktarma();
            navigationeffectsilme();
            hideacitonbar();
            //secilmis iconu secmek
            profile_nav.getMenu().findItem(R.id.nav_profile).setChecked(true);
            navigationbar();

            //progress dialog
            progressDialog.setMessage("Loading User Information");
            progressDialog.show();


            //harita ekleme
            Bundle mapViewBundle = null;
            if (savedInstanceState != null) {
                mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
            }
            mMapView = (MapView) findViewById(R.id.mapView);
            mMapView.onCreate(mapViewBundle);
            mMapView.getMapAsync(this);

            if(controller.MAINACTIVITYKULLANICIGORUNTULEMEK == 1){
                //digerkullanici verilerini almak
                digerkullaniciverilerialmak();
                pr_logout.setVisibility(View.INVISIBLE);
                pr_editprofile.setVisibility(View.INVISIBLE);
            }else{
                //kullanici verilerini almak
                kullaniciverilerialmak();
            }

            //log out
            pr_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    FirebaseAuth.getInstance().signOut();
                                    controller.MAINACTIVITYKULLANICIGORUNTULEMEK=0;
                                    Toast.makeText(profile.this,"Log Out ",Toast.LENGTH_SHORT).show();
                                    Intent signout = new Intent(profile.this,loginpage.class);
                                    startActivity(signout);
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder builder = new AlertDialog.Builder(profile.this);
                    builder.setTitle("Logout").setMessage("Are you sure you want to Logout ?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();



                }
            });
            //Edit Profile
            pr_editprofile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editprofilegec= new Intent(profile.this,editprofile.class);
                    startActivity(editprofilegec);
                }
            });

            pr_userphoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (controller.MAINACTIVITYKULLANICIGORUNTULEMEK == 1) {
                        Intent intent = new Intent(profile.this,userphoto.class);
                        intent.putExtra("photouuid", String.valueOf(bundle.get("photouuid")));
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(profile.this,userphoto.class);
                        startActivity(intent);
                    }
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

            AlertDialog.Builder builder = new AlertDialog.Builder(profile.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }



    }




    @Override
    public void onMapReady(final GoogleMap map) {
        PMap = map;

        mClusterManager = new ClusterManager<MyItem>(this, PMap);
        PMap.setOnCameraIdleListener(mClusterManager);
        PMap.setOnMarkerClickListener(mClusterManager);

        //DIGER FOTOGRAFLARIN BILGILERINI ALMAK
        map.setOnMarkerClickListener(profile.this);
        if(controller.MAINACTIVITYKULLANICIGORUNTULEMEK==1)
        {
            DigerUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                        //get user photo data
                        getuserphotodata getuserlocation = s.getValue(getuserphotodata.class);
                        MyItem offsetItem = new MyItem(getuserlocation.latitude, getuserlocation.longitude);
                        mClusterManager.addItem(offsetItem);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else {
            pUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot s : dataSnapshot.getChildren()) {
                            //get user photo data
                            getuserphotodata getuserlocation = s.getValue(getuserphotodata.class);
                            MyItem offsetItem = new MyItem(getuserlocation.latitude, getuserlocation.longitude);
                            mClusterManager.addItem(offsetItem);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private void kullaniciverilerialmak() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //verileri almak
                String Fprofilepictures = dataSnapshot.child("profilepicture").getValue(String.class);
                String Fnamesurname = dataSnapshot.child("namesurname").getValue(String.class);
                String Fusername    = dataSnapshot.child("username").getValue(String.class);
                String Fcountryname = dataSnapshot.child("country").getValue(String.class);
                String Femail = dataSnapshot.child("gmail").getValue(String.class);
                String Fphotocounter = dataSnapshot.child("photocounter").getValue(String.class);

                //verileri yazdirmak
                Picasso.get().load(Fprofilepictures).into(pr_profilepictures);  //picasso ile fotograf cekmek
                pr_profilename.setText(Fnamesurname);
                pr_username.setText("@"+Fusername);
                pr_countryname.setText(Fcountryname);
                pr_email.setText(Femail);
                pr_photocounter.setText(Fphotocounter);



                progressDialog.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        kullanici.addValueEventListener(postListener);
    }

    private void digerkullaniciverilerialmak() {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //verileri almak
                String Fprofilepictures = dataSnapshot.child("profilepicture").getValue(String.class);
                String Fnamesurname = dataSnapshot.child("namesurname").getValue(String.class);
                String Fusername    = dataSnapshot.child("username").getValue(String.class);
                String Fcountryname = dataSnapshot.child("country").getValue(String.class);
                String Femail = dataSnapshot.child("gmail").getValue(String.class);
                String Fphotocounter = dataSnapshot.child("photocounter").getValue(String.class);

                //verileri yazdirmak
                Picasso.get().load(Fprofilepictures).into(pr_profilepictures);  //picasso ile fotograf cekmek
                pr_profilename.setText(Fnamesurname);
                pr_username.setText("@"+Fusername);
                pr_countryname.setText(Fcountryname);
                pr_email.setText(Femail);
                pr_photocounter.setText(Fphotocounter);

                progressDialog.hide();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        digerkullanici.addValueEventListener(postListener);
    }


    private void degeraktarma() {
        //extra
        bundle = getIntent().getExtras();
        profile_nav = (BottomNavigationView) findViewById(R.id.profile_nav);
        firebaseAuth = FirebaseAuth.getInstance();
        kullanici = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseAuth.getUid());
        //kullanici map verileri gosterme
        pUsers = FirebaseDatabase.getInstance().getReference("photo").child(firebaseAuth.getUid());
        pUsers.push().setValue(marker);
        //diger kullanici map verileri gosterme
        if(controller.MAINACTIVITYKULLANICIGORUNTULEMEK==1){
            digerkullanici = FirebaseDatabase.getInstance().getReference().child("user").child((String) bundle.get("photouuid").toString());
            DigerUser = FirebaseDatabase.getInstance().getReference("photo").child((String) bundle.get("photouuid").toString());
            DigerUser.push().setValue(marker);
        }
        //kullaniciverileri gosterme
        pr_profilepictures = (CircularImageView) findViewById(R.id.pr_profilepictures);
        pr_profilename = (TextView) findViewById(R.id.pr_profilename);
        pr_username = (TextView) findViewById(R.id.pr_username);
        pr_countryname = (TextView) findViewById(R.id.pr_countryname);
        pr_email = (TextView) findViewById(R.id.pr_email);
        pr_photocounter = (TextView) findViewById(R.id.pr_photocounter);
        pr_userphoto = (Button) findViewById(R.id.pr_userphoto);
        pr_logout = (ImageView) findViewById(R.id.pr_logout);
        pr_editprofile = (ImageView) findViewById(R.id.pr_editprofile);


        //dialog
        progressDialog = new ProgressDialog(profile.this);




    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void hideacitonbar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }
        else {
        }
    }
    private void navigationeffectsilme() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.profile_nav);
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
    }
    private void navigationbar() {
        profile_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(controller.MAINACTIVITYKULLANICIGORUNTULEMEK==1){
                    controller.MAINACTIVITYKULLANICIGORUNTULEMEK=0;
                }
                switch (item.getItemId()) {
                    case R.id.nav_world:
                        finish();
                        startActivity(new Intent(profile.this, MainActivity.class));
                        return true;
                    case R.id.nav_binoculars:
                        finish();
                        startActivity(new Intent(profile.this, Binoculars.class));
                        return true;
                    case R.id.nav_camera:
                        finish();
                        startActivity(new Intent(profile.this, camera.class));
                        return true;
                    case R.id.nav_trend:
                        finish();
                        startActivity(new Intent(profile.this, trend.class));
                        return true;
                    case R.id.nav_profile:
                        finish();
                        startActivity(new Intent(profile.this, profile.class));
                        return true;
                    default:
                        return false;
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        Toast.makeText(profile.this,"Geriye Getdiyy",Toast.LENGTH_SHORT).show();

        if(controller.MAINACTIVITYKULLANICIGORUNTULEMEK==1){
            controller.MAINACTIVITYKULLANICIGORUNTULEMEK=0;
        }


        super.onBackPressed();
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(profile.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


}