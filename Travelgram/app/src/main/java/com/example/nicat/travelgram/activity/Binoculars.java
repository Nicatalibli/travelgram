package com.example.nicat.travelgram.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nicat.travelgram.BottomNavigationHelper;
import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.controller;
import com.example.nicat.travelgram.getuserphotodata;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.UUID;

public class Binoculars extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
    private BottomNavigationView bino_nav;
    private GoogleMap BMap;

    //kullanici fotograf verilerini alma
    private DatabaseReference bUsers;
    Marker marker;
    Marker userlocationM;

    LocationManager BlocationManager;
    LocationListener BlocationListener;
    Bundle bundle;
    private StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    final UUID uuid = UUID.randomUUID();


    int markercontrol = 0;
    private View popup=null;

    //Dialog
    ProgressDialog progressDialog;




    //photocountersave
    int converter ;
    int photocountercontroller = 0;

    @Override
    protected void onStart() {

        if(controller.CAMERACAMERACONTROL == 1){
            progressDialog.setMessage("Uploading Photo...");
            progressDialog.show();
        } else if(controller.CAMERASELECTORCONTROL == 1){
            progressDialog.setMessage("Uploading Photo...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else{

        }

        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binoculars);
        if(isNetworkConnected()==true){
            //tanimlamalar

            degeraktarma();
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.Bmap);
            mapFragment.getMapAsync(this);
            navigationeffectsilme();



            //secilmis iconu secmek
            bino_nav.getMenu().findItem(R.id.nav_binoculars).setChecked(true);
            navigationbar();



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

            AlertDialog.Builder builder = new AlertDialog.Builder(Binoculars.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection problem",Toast.LENGTH_SHORT).show();
        }


    }




    @Override
    public void onMapReady(final GoogleMap googleMap) {
        BMap = googleMap;
        googleMap.setOnMarkerClickListener(this);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        BMap.setPadding(0, 0, 0, 200);
        BMap.getUiSettings().setZoomControlsEnabled(true);



        //DIGER FOTOGRAFLARIN BILGILERINI ALMAK
        googleMap.setOnMarkerClickListener(Binoculars.this);
        bUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot s: dataSnapshot.getChildren()){

                    //KULLANICI FOTOGRAFLARINI ALMAK

                    getuserphotodata users = s.getValue(getuserphotodata.class);
                    LatLng location = new LatLng(users.latitude,users.longitude);

                    MarkerOptions marker = new MarkerOptions();


                    marker.position(location);
                    marker.title(users.photodate);
                    marker.snippet(users.photourl);

                    int height = 150;
                    int width = 150;

                    BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.userpinphoto);
                    Bitmap b=bitmapdraw.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                    marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));


                    BMap.addMarker(marker);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        BMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){
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


                BMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    public void onInfoWindowClick(Marker marker)
                    {

                    }
                });

                return popup;
            }
        });



        //kendi lokasyonu
        BlocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        BlocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                LatLng userlocation = new LatLng(location.getLatitude(),location.getLongitude());

                if(markercontrol == 1){
                    userlocationM.remove();
                    BMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation,10));
                }




                MarkerOptions marker = new MarkerOptions();


                marker.position(userlocation);
                marker.title("Your Location");

                int height = 200;
                int width = 200;
                BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.mylocation);
                Bitmap b=bitmapdraw.getBitmap();
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                userlocationM = BMap.addMarker(marker);
                markercontrol=1;

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
        };



        //izin kontrol
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this ,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else {

            BlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,60,10,BlocationListener);
            Location lastlocation = BlocationManager.getLastKnownLocation(BlocationManager.GPS_PROVIDER);

             if(lastlocation != null) {
                 //izin varsa neyapsin
                 //ESKI LOKASYON ALMA VE GOSTERME

                 LatLng userlastlocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());

                 if(markercontrol == 1){
                     userlocationM.remove();
                     BMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlastlocation, 10));
                 }
                 MarkerOptions marker = new MarkerOptions();

                 marker.position(userlastlocation);
                 marker.title("Your Location");

                 int height = 200;
                 int width = 200;
                 BitmapDrawable bitmapdraw=(BitmapDrawable)getResources().getDrawable(R.drawable.mylocation);
                 Bitmap b=bitmapdraw.getBitmap();
                 Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

                 marker.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));

                 userlocationM = BMap.addMarker(marker);
                 markercontrol = 1;
            }
            else if(lastlocation == null){
                 Toast.makeText(Binoculars.this,"Please wait, travelgram finds your location. If the GPS is not activated, please activate it.",Toast.LENGTH_SHORT).show();
             }

             if(controller.CAMERASELECTORBINOCULARSSAVELOCATION == 1){
                 if(lastlocation != null) {
                   //upload photonu kayit etme
                     try {
                         intentphotoalma();
                     } catch (FileNotFoundException e) {
                         e.printStackTrace();
                     }


                     FirebaseDatabase database = FirebaseDatabase.getInstance();
                     DatabaseReference latitude = database.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("latitude");
                     DatabaseReference longitude = database.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("longitude");
                     DatabaseReference publisher = database.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("publisher");
                     DatabaseReference photoid = database.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("photoid");


                     latitude.setValue(lastlocation.getLatitude());
                     longitude.setValue(lastlocation.getLongitude());
                     publisher.setValue(firebaseAuth.getUid().toString());
                     photoid.setValue(uuid.toString());

                     LatLng userlastlocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                     BMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlastlocation,20));
                     Toast.makeText(Binoculars.this,"Your photo save successfully.",Toast.LENGTH_LONG).show();

                     controller.CAMERASELECTORBINOCULARSSAVELOCATION = 0;

                 }
                 else if(lastlocation == null){
                     Toast.makeText(Binoculars.this,"Your photo can not save. Travelgram can not find your Location. Wait until you see your location and please try again.",Toast.LENGTH_LONG).show();

                     controller.CAMERASELECTORBINOCULARSSAVELOCATION=0;
                     controller.CAMERASELECTORCONTROL = 0;

                     progressDialog.hide();
                 }
               }
               else if(controller.CAMERACAMERABINOCULARSSAVELOCATION == 1){
                 if(lastlocation != null){
                     //camera ile cekilen fotografi kayit etme

                     try {
                         intentphotoalma();
                     } catch (FileNotFoundException e) {
                         e.printStackTrace();
                     }

                     FirebaseDatabase database = FirebaseDatabase.getInstance();
                     DatabaseReference latitude = database.getReference("photo").child(firebaseAuth.getUid()).child(bundle.get("uuid").toString()).child("latitude");
                     DatabaseReference longitude = database.getReference("photo").child(firebaseAuth.getUid()).child(bundle.get("uuid").toString()).child("longitude");
                     DatabaseReference imageurl = database.getReference("photo").child(firebaseAuth.getUid()).child(bundle.get("uuid").toString()).child("photourl");
                     DatabaseReference publisher = database.getReference("photo").child(firebaseAuth.getUid()).child(bundle.get("uuid").toString()).child("publisher");
                     DatabaseReference photoid = database.getReference("photo").child(firebaseAuth.getUid()).child(bundle.get("uuid").toString()).child("photoid");


                     latitude.setValue(lastlocation.getLatitude());
                     longitude.setValue(lastlocation.getLongitude());
                     imageurl.setValue(bundle.get("camerauri").toString());
                     publisher.setValue(firebaseAuth.getUid().toString());
                     photoid.setValue(bundle.get("uuid").toString());

                     LatLng userlastlocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                     BMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlastlocation,20));
                     Toast.makeText(Binoculars.this,"Your photo save successfully.",Toast.LENGTH_LONG).show();

                     controller.CAMERACAMERABINOCULARSSAVELOCATION = 0;

                 }
                 else if(lastlocation == null){
                     Toast.makeText(Binoculars.this,"Your photo can not save. Travelgram can not find your Location.Please find your locaiton and repeat again.",Toast.LENGTH_LONG).show();

                     controller.CAMERACAMERACONTROL = 0;
                     controller.CAMERACAMERABINOCULARSSAVELOCATION = 0;

                     progressDialog.hide();

                 }
             }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //izin verildiginde ne olsun
        if(grantResults.length > 0){
            if(requestCode == 1){
                if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    BlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,0,BlocationListener);

                    Intent sayfayenile = new Intent(Binoculars.this,Binoculars.class);
                    startActivity(sayfayenile);
                }
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void degeraktarma() {
        bino_nav = (BottomNavigationView) findViewById(R.id.bino_nav);
        bundle = getIntent().getExtras();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        //kullanici fotograflari gosterme
        bUsers = FirebaseDatabase.getInstance().getReference("photo").child(firebaseAuth.getUid());
        bUsers.push().setValue(marker);

        //dialog
        progressDialog = new ProgressDialog(Binoculars.this);

    }

    private void intentphotoalma() throws FileNotFoundException {
        if(bundle != null){
            if(controller.CAMERACAMERACONTROL==1){ //CAMERADAN GELEN VERIYI ALMAK

                photocountersave();


                progressDialog.hide();
                controller.CAMERACAMERACONTROL=0;
            }
            else if(controller.CAMERASELECTORCONTROL==1){  //SECILMIS FOTOGRAF ALMAK

                //URINI BITMAPA DONUSTURUB KALITESINI INDIRMEK
                InputStream imageStream = getContentResolver().openInputStream((Uri) bundle.get("selectedphoto"));
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                selectedImage = getResizedBitmap(selectedImage, 1000);// 400 is for example, replace with desired size


                final String imagename = "images/" + uuid +".jpg";


                StorageReference selectedsave = storageReference.child(imagename);
                photocountersave();
                selectedsave.putFile(getImageUri(Binoculars.this,selectedImage)).addOnSuccessListener(Binoculars.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        StorageReference newReference = FirebaseStorage.getInstance().getReference(imagename);
                        newReference.getDownloadUrl().addOnSuccessListener(Binoculars.this, new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //PHOTO URL KAYIT ETMEK
                                String downloadurl = uri.toString();
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference myRef = database.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("photourl");
                                myRef.setValue(downloadurl);

                                //tarihkayitetmek
                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                String formattedDate = df.format(c);
                                DatabaseReference datesave = database.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("photodate");
                                datesave.setValue(formattedDate);

                                progressDialog.hide();
                                controller.CAMERASELECTORCONTROL=0;
                            }
                        });
                    }
                }).addOnFailureListener(Binoculars.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Binoculars.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });




                controller.CAMERASELECTORCONTROL=0;
            }
        }
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private void navigationeffectsilme() {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bino_nav);
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
    }
    private void navigationbar() {
        bino_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_world:
                        finish();
                        startActivity(new Intent(Binoculars.this,MainActivity.class));
                        return true;
                    case R.id.nav_binoculars:
                        finish();
                        startActivity(new Intent(Binoculars.this,Binoculars.class));
                        return true;
                    case R.id.nav_camera:
                        finish();
                        startActivity(new Intent(Binoculars.this,camera.class));
                        return true;
                    case R.id.nav_trend:
                        finish();
                        startActivity(new Intent(Binoculars.this,trend.class));
                        return true;
                    case R.id.nav_profile:
                        finish();
                        startActivity(new Intent(Binoculars.this,profile.class));
                        return true;
                    default:
                        return false;
                }}});

    }
    private void photocountersave(){
        DatabaseReference countersave = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseAuth.getUid());

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //verileri almak
                String Bphotocounter = dataSnapshot.child("photocounter").getValue(String.class);

                if(Bphotocounter != null){
                    converter  = Integer.parseInt(Bphotocounter);
                }

                if(photocountercontroller ==0){
                converter++;
                DatabaseReference uidkayit = FirebaseDatabase.getInstance().getReference().child("user").child(firebaseAuth.getUid());
                uidkayit.child("photocounter").setValue(""+converter);
                photocountercontroller =1;
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        countersave.addValueEventListener(postListener);



    }



    @Override
    public boolean onMarkerClick(Marker marker) {

        return false;
    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Binoculars.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}
