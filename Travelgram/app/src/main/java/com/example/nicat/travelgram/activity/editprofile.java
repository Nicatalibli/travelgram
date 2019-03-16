package com.example.nicat.travelgram.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nicat.travelgram.R;
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
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class editprofile extends AppCompatActivity {

    //Request Controller
    Integer SELECT_FILE=0;
    int selectedphotocontrol = 0;

    //Dialog
    ProgressDialog progressDialog;

    //View tanimlamalar
    CircularImageView editpr_userprofile;
    EditText editpr_username;
    EditText editpr_fullname;
    EditText editpr_email;
    EditText editpr_country;
    Button editpr_save;

    //firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private StorageReference storageReference;
    Uri selectedImageUri;


    String userprofilelink,username,fullname,email,country;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);
        if(isNetworkConnected()==true){
            //tanimlamalar
            hideacitonbar();
            degeraktarma();

            kullaniciverilerinicagirmak();

            //fotograf yuklemek
            editpr_userprofile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //selectphototiklama
                    if(ContextCompat.checkSelfPermission(editprofile.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(editprofile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
                    {
                        ActivityCompat.requestPermissions(editprofile.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    }else {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(editprofile.this);
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
            if(requestCode==SELECT_FILE){
                selectedImageUri = data.getData();
                editpr_userprofile.setImageURI(selectedImageUri);

            }
        }



    }


    private void kullaniciverilerinicagirmak() {
        //nameplain text tanimlama
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userprofilelink= dataSnapshot.child("profilepicture").getValue(String.class);
                username       = dataSnapshot.child("username").getValue(String.class);
                fullname       = dataSnapshot.child("namesurname").getValue(String.class);
                email          = dataSnapshot.child("gmail").getValue(String.class);
                country        = dataSnapshot.child("country").getValue(String.class);

                Picasso.get().load(userprofilelink).into(editpr_userprofile);
                editpr_username.setText(""+username);
                editpr_fullname.setText(""+fullname);
                editpr_email.setText(""+email);
                editpr_country.setText(""+country);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
            }
        };
        databaseReference.addValueEventListener(postListener);

    }
    public void editprsaveclick(View view){
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        //verileri kayit etme
        databaseReference.child("username").setValue(editpr_username.getText().toString());
        databaseReference.child("namesurname").setValue(editpr_fullname.getText().toString());
        databaseReference.child("gmail").setValue(editpr_email.getText().toString());
        databaseReference.child("country").setValue(editpr_country.getText().toString());

        if(selectedImageUri != null){
            selectedphotocontrol = 1;
            firebasefotografkayit();
        }

        if(selectedphotocontrol == 0){
            Toast.makeText(editprofile.this,"Update is succesfully",Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(editprofile.this,profile.class);
            startActivity(intent);

        }




    }
    public void firebasefotografkayit(){

        //URINI BITMAPA DONUSTURUB KALITESINI INDIRMEK
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(selectedImageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        selectedImage = getResizedBitmap(selectedImage, 1000);// 400 is for example, replace with desired size



        StorageReference selectedsave = storageReference.child("profilepictures/").child(mAuth.getUid()).child("profilepictures.jpg");


        selectedsave.putFile(getImageUri(editprofile.this,selectedImage)).addOnSuccessListener(editprofile.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageReference newReference = FirebaseStorage.getInstance().getReference().child("profilepictures/").child(mAuth.getUid()).child("profilepictures.jpg");
                newReference.getDownloadUrl().addOnSuccessListener(editprofile.this, new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //PHOTO URL KAYIT ETMEK
                        String downloadurl = uri.toString();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference().child("user").child(mAuth.getUid()).child("profilepicture");
                        myRef.setValue(downloadurl);

                        Toast.makeText(editprofile.this,"Update is succesfully",Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(editprofile.this,profile.class);
                        startActivity(intent);
                    }
                });
            }
        }).addOnFailureListener(editprofile.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(editprofile.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
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


    private void degeraktarma() {
        //VIEW Aktarmalar
        editpr_userprofile = (CircularImageView) findViewById(R.id.editpr_userpofile);
        editpr_username = (EditText) findViewById(R.id.editpr_username);
        editpr_fullname = (EditText) findViewById(R.id.editpr_fullname);
        editpr_email = (EditText) findViewById(R.id.editpr_email);
        editpr_country = (EditText) findViewById(R.id.editpr_country);
        editpr_save = (Button) findViewById(R.id.editpr_save);

        //dialog
        progressDialog = new ProgressDialog(editprofile.this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("user").child(mAuth.getUid());
        storageReference = FirebaseStorage.getInstance().getReference();


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
        ConnectivityManager cm = (ConnectivityManager) getSystemService(editprofile.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }
}
