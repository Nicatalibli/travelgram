package com.example.nicat.travelgram.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nicat.travelgram.BottomNavigationHelper;
import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.controller;
import com.example.nicat.travelgram.getuserphotodata;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,GoogleMap.OnMarkerClickListener {

    //view
    private BottomNavigationView mMainNav;
    //kutuphane
    private FirebaseAuth firebaseAuth;
    private GoogleMap mMap;
    //fotograf verileri alma
    private DatabaseReference fotografverialma ; //ALINAN FOTOGRAFLARIN VERILERINI ALMAK
    Marker marker;
    ProgressDialog progressDialog;
    private View popup=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(isNetworkConnected()==true){
            //tanimlamalar

            degeraktarma();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            girisyapilmakontrol();

            navigationeffectsilme();
            navigationekleme();

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

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }



    }

    


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setPadding(0, 0, 0, 200);
        mMap.getUiSettings().setZoomControlsEnabled(true);






        fotografverialma.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //TUM KULLANICILARIN PINLERINI ALMAK
                for(DataSnapshot uc: dataSnapshot.getChildren()){

                    String uuidismi = uc.getKey();

                    for(DataSnapshot ucc: uc.getChildren()){

                        getuserphotodata users = ucc.getValue(getuserphotodata.class);
                        LatLng location = new LatLng(users.latitude,users.longitude);

                        MarkerOptions marker = new MarkerOptions();


                        marker.position(location);
                        marker.title(uuidismi);
                        marker.snippet(users.photourl);

                        int height = 200;
                        int width = 200;

                        BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.worldpinphoto);
                        Bitmap b=bitmapdraw.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                        marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));




                        mMap.addMarker(marker);
                    }}}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if(popup == null){
                    popup = getLayoutInflater().inflate(R.layout.custom_adress,null);
                }

                ImageView customadresimage = (ImageView) popup.findViewById(R.id.customadresimage);
                Picasso.get().load(marker.getSnippet()).resize(200,200).into(customadresimage);  //picasso ile fotograf cekmek


                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    public void onInfoWindowClick(Marker marker)
                    {

                        Intent photouuid = new Intent(MainActivity.this,profile.class);
                        photouuid.putExtra("photouuid",marker.getTitle());
                        controller.MAINACTIVITYKULLANICIGORUNTULEMEK=1;
                        startActivity(photouuid);

                    }
                });

                return popup;
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;

    }

    private void degeraktarma() {
        //view
        mMainNav = (BottomNavigationView) findViewById(R.id.main_nav);
        //kutuphane
        firebaseAuth = FirebaseAuth.getInstance();
        //FOTOGRAF VERILERI ALMAK
        fotografverialma= FirebaseDatabase.getInstance().getReference("photo");
        fotografverialma.push().setValue(marker);
        progressDialog= new ProgressDialog(this);
    }

    private void girisyapilmakontrol() {
        if(firebaseAuth.getCurrentUser() == null){
            finish();
            startActivity(new Intent(this,loginpage.class));
        }
    }

    private void navigationeffectsilme() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.main_nav);
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
    }
    private void navigationekleme() {
        mMainNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_world:
                        finish();
                        startActivity(new Intent(MainActivity.this,MainActivity.class));
                        return true;
                    case R.id.nav_binoculars:
                        finish();
                        startActivity(new Intent(MainActivity.this,Binoculars.class));
                        return true;
                    case R.id.nav_camera:
                        finish();
                        startActivity(new Intent(MainActivity.this,camera.class));
                        return true;
                    case R.id.nav_trend:
                        finish();
                        startActivity(new Intent(MainActivity.this,trend.class));
                        return true;
                    case R.id.nav_profile:
                        finish();
                        startActivity(new Intent(MainActivity.this,profile.class));
                        return true;
                    default:
                        return false;
                }}});
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(MainActivity.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }


}
