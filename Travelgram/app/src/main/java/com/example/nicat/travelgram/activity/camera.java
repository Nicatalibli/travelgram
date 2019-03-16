package com.example.nicat.travelgram.activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicat.travelgram.BottomNavigationHelper;
import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.controller;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class camera extends AppCompatActivity {
    private BottomNavigationView camera_nav;

    Integer REQUEST_CAMERA=1, SELECT_FILE=0;
    TextView dialogcamera,dialoggallery;
    private StorageReference cStroage;
    private ProgressDialog mProgress;
    String currentPhotoPath;
    StorageReference storageReference;
    Uri photoURI;
    final UUID uuid = UUID.randomUUID();
    FirebaseDatabase firebaseDatabase =FirebaseDatabase.getInstance();
    FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if(isNetworkConnected()==true){
            //tanimlamalar

            degeraktarma();

            Dialog dialog = new Dialog(camera.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_design);
            dialogcamera = dialog.findViewById(R.id.dialogcamera);
            dialoggallery= dialog.findViewById(R.id.dialoggallery);

            //camera izni
            if(Build.VERSION.SDK_INT >= 23){
                requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},3);
            }

            System.out.println("GPSPROVIDERRR"+isGpsConnected());

            dialogcamera.setOnClickListener(new View.OnClickListener() {
                //kameratiklama
                @Override
                public void onClick(View v) {
                        if(isGpsConnected() == true){
                            if(ContextCompat.checkSelfPermission(camera.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(camera.this ,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},1);
                            } else {
                                dispatchTakePictureIntent();
                            }
                        }
                        else if(isGpsConnected() == false){
                            buildAlertMessageNoGps();
                        }
                }
            });
            dialoggallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //selectphototiklama

                    if(isGpsConnected() == true){

                        if(ContextCompat.checkSelfPermission(camera.this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(camera.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                        }else {
                            if(ContextCompat.checkSelfPermission(camera.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                                ActivityCompat.requestPermissions(camera.this ,new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION},1);
                            } else {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);
                            }
                        }

                    }
                    else if(isGpsConnected() == false){

                        buildAlertMessageNoGps();

                    }

                   }});
            dialog.show();


            //navigation
            navigationeffectsilme();
            navigationbar();
            //SECILMISICONUSECMEK
            camera_nav.getMenu().findItem(R.id.nav_camera).setChecked(true);


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

            AlertDialog.Builder builder = new AlertDialog.Builder(camera.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }




    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //upload izni verildiginde ne olsun
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select File"),SELECT_FILE);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==Activity.RESULT_OK) {
            if (requestCode==REQUEST_CAMERA){
                //camera ile fotograf kayit
                //progress dialog
                mProgress.setMessage("Saving Photo");
                mProgress.show();

                final String imagename = "images/"+uuid+".jpg";

                //URINI BITMAPA DONUSTURUB KALITESINI INDIRMEK
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(photoURI);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap cameraimage = BitmapFactory.decodeStream(imageStream);
                cameraimage = getResizedBitmap(cameraimage, 1000);// 400 is for example, replace with desired size



                final StorageReference camerasave = storageReference.child(imagename);
                camerasave.putFile(getImageUri(camera.this,cameraimage)).addOnSuccessListener(camera.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        StorageReference newReference = FirebaseStorage.getInstance().getReference(imagename);
                        newReference.getDownloadUrl().addOnSuccessListener(camera.this, new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                controller.CAMERACAMERACONTROL = 1;
                                controller.CAMERACAMERABINOCULARSSAVELOCATION=1;

                                //tarihkayitetmek
                                Date c = Calendar.getInstance().getTime();
                                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
                                String formattedDate = df.format(c);
                                DatabaseReference datesave = firebaseDatabase.getReference("photo").child(firebaseAuth.getUid()).child(uuid.toString()).child("photodate");
                                datesave.setValue(formattedDate);


                                Intent cameraphotokayit = new Intent(camera.this,Binoculars.class);
                                cameraphotokayit.putExtra("uuid",uuid);
                                cameraphotokayit.putExtra("camerauri",uri);
                                startActivity(cameraphotokayit);
                                mProgress.hide();
                            }
                        });
                    }
                }).addOnFailureListener(camera.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(camera.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    }
                });

                }
            else if(requestCode==SELECT_FILE){
                //selected ile fotograf kayit (intent ile gondermek)
                mProgress.setMessage("Saving Photo");
                mProgress.show();
                Uri selectedImageUri = data.getData();


                controller.CAMERASELECTORCONTROL = 1;
                controller.CAMERASELECTORBINOCULARSSAVELOCATION=1;

                Intent intent = new Intent(camera.this,Binoculars.class);
                intent.putExtra("selectedphoto",selectedImageUri);
                startActivity(intent);
                mProgress.hide();
            }
        }



    }

    private void degeraktarma() {
        camera_nav = (BottomNavigationView) findViewById(R.id.camera_nav);
        cStroage = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(camera.this);
        storageReference = FirebaseStorage.getInstance().getReference();
        mProgress = new ProgressDialog(camera.this);
        firebaseAuth = FirebaseAuth.getInstance();




    }

    private void dispatchTakePictureIntent() {
        //fotograf intenti almak
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.nicat.travelgram.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA);
            }
        }
    }
    private File createImageFile() throws IOException {

        //kamera ile cekilmis fotografi gallerye kayit etmek
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

        return image;
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
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.camera_nav);
        BottomNavigationHelper.disableShiftMode(bottomNavigationView);
    }
    private void navigationbar() {
        camera_nav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.nav_world:
                        finish();
                        startActivity(new Intent(camera.this,MainActivity.class));
                        return true;
                    case R.id.nav_binoculars:
                        finish();
                        startActivity(new Intent(camera.this,Binoculars.class));
                        return true;
                    case R.id.nav_camera:
                        finish();
                        startActivity(new Intent(camera.this,camera.class));
                        return true;
                    case R.id.nav_trend:
                        finish();
                        startActivity(new Intent(camera.this,trend.class));
                        return true;
                    case R.id.nav_profile:
                        finish();
                        startActivity(new Intent(camera.this,profile.class));
                        return true;
                    default:
                        return false;
                }}});

    }
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(camera.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    private boolean isGpsConnected() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return statusOfGPS;
    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }



}
