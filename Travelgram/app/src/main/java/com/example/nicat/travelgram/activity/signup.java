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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nicat.travelgram.R;
import com.example.nicat.travelgram.Model.user;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbb20.CountryCodePicker;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class signup extends AppCompatActivity implements View.OnClickListener {

    //View Tanimlama
    EditText signamesurname,sigusername,siggmail,sigpassword;
    CountryCodePicker sigcountry;
    Button sigsignbutton;
    TextView sigsignin;
    ImageView sigaddprofilepicture;
    //DEGER TANIMLAMA
    Integer SELECT_FILE=0;
    Uri pickedimageuri;
    //Kutuphane Tanimlama
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    //controller
    int imagecontroller  = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        if(isNetworkConnected()==true){
            //tanimlamalar
            hideactionbar();
            degerakarma();
            if(firebaseAuth.getCurrentUser() != null){
                finish();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
            sigsignbutton.setOnClickListener(signup.this);
            sigsignin.setOnClickListener(this);
            sigaddprofilepicture.setOnClickListener(this);

            sigcountry.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
                @Override
                public void onCountrySelected() {
                    Toast.makeText(signup.this, "Updated " + sigcountry.getSelectedCountryName(), Toast.LENGTH_SHORT).show();
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

            AlertDialog.Builder builder = new AlertDialog.Builder(signup.this);
            builder.setCancelable(false).setMessage("You have not internet connection.").setPositiveButton("Check internet connection", dialogClickListener).show();
        }
        else{
            Toast.makeText(getApplicationContext(),"You have internet connection prolblem",Toast.LENGTH_SHORT).show();
        }




    }

    public void degerakarma(){
        //view deger aktarma
        signamesurname = (EditText) findViewById(R.id.signamesurname);
        sigusername = (EditText) findViewById(R.id.sigusername);
        siggmail = (EditText) findViewById(R.id.siggmail);
        sigpassword = (EditText) findViewById(R.id.sigpassword);
        sigcountry = (CountryCodePicker) findViewById(R.id.sigcountry);
        sigsignin = (TextView) findViewById(R.id.sigsignin);
        sigsignbutton = (Button) findViewById(R.id.sigsignbutton);
        sigaddprofilepicture = (ImageView) findViewById(R.id.sigaddprofilepicture);
        //kutuphane tanimlama
        progressDialog = new ProgressDialog(this);
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }


    private void registerUser(){
        //alinicak veriler
        final String namesurname = signamesurname.getText().toString().trim();
        final String username    = sigusername.getText().toString().trim();
        final String gmail       = siggmail.getText().toString().trim();
        String password    = sigpassword.getText().toString().trim();
        final String country     = sigcountry.getSelectedCountryName().toString().trim();


        //tum veri girisleri kontrol
        if(TextUtils.isEmpty(namesurname)){
            Toast.makeText(this,"Please enter your name and surname.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this,"Please enter your username.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(gmail)){
            Toast.makeText(this,"Please enter your mail.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Please enter your password.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(country)){
            Toast.makeText(this,"Please enter your country.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(imagecontroller == 0){
            Toast.makeText(this,"Please choose profile picture.",Toast.LENGTH_SHORT).show();
            return;
        }

        //progress dialog
        progressDialog.setMessage("Registering User");
        progressDialog.show();
        //firebase kayit
        firebaseAuth.createUserWithEmailAndPassword(gmail,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //verileri kayit etmek
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("user").child(firebaseAuth.getUid());
                        myRef.setValue(new user(namesurname,country,username,gmail));
                        myRef.child("userid").setValue(firebaseAuth.getUid());
                        myRef.child("photocounter").setValue("0");

                        firebasefotografkayit();


                    }
                    else {
                        progressDialog.hide();
                        Toast.makeText(signup.this,"Could not register. Please try again",Toast.LENGTH_SHORT);
                    }

            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view == sigsignbutton){
            //selectphototiklama



                    registerUser();




        }
        if(view == sigsignin){
            Intent hesabimvar = new Intent(getApplicationContext(),loginpage.class);
            startActivity(hesabimvar);
        }
        if(view == sigaddprofilepicture) {

            //selectphototiklama
            if(ContextCompat.checkSelfPermission(signup.this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(signup.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED )
            {
                ActivityCompat.requestPermissions(signup.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            }else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select File"),SELECT_FILE);
            }}

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
        if(resultCode==Activity.RESULT_OK) {
            if(requestCode==SELECT_FILE){
                //selected ile fotograf kayit
                pickedimageuri = data.getData();

                sigaddprofilepicture.setImageURI(pickedimageuri);
                imagecontroller = 1;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    public void firebasefotografkayit(){

        final String imagename = "profilepictures/" + firebaseAuth.getUid() +".jpg";

        //URINI BITMAPA DONUSTURUB KALITESINI INDIRMEK
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(pickedimageuri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        selectedImage = getResizedBitmap(selectedImage, 1000);// 400 is for example, replace with desired size



        StorageReference selectedsave = storageReference.child("profilepictures/").child(firebaseAuth.getUid()).child("profilepictures.jpg");


        selectedsave.putFile(getImageUri(signup.this,selectedImage)).addOnSuccessListener(signup.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageReference newReference = FirebaseStorage.getInstance().getReference().child("profilepictures/").child(firebaseAuth.getUid()).child("profilepictures.jpg");
                newReference.getDownloadUrl().addOnSuccessListener(signup.this, new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //PHOTO URL KAYIT ETMEK
                        String downloadurl = uri.toString();
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference().child("user").child(firebaseAuth.getUid()).child("profilepicture");
                        myRef.setValue(downloadurl);

                        progressDialog.hide();
                        Toast.makeText(signup.this,"Registered Successfully",Toast.LENGTH_SHORT);
                        startActivity(new Intent(signup.this,MainActivity.class));

                    }
                });
            }
        }).addOnFailureListener(signup.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(signup.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        });
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
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(signup.this.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

}





