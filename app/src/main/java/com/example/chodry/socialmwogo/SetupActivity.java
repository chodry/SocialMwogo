package com.example.chodry.socialmwogo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    Button saveinfobtn;
    EditText UserName, fullname, countryName;
    CircleImageView profileimage;

    FirebaseAuth mAuth;
    DatabaseReference usersRef;
    StorageReference userprofileRef;
    String current_user_id;
    ProgressDialog loadingbar;

    final static int gallery_pick = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        UserName = (EditText) findViewById(R.id.setup_username);
        fullname = (EditText) findViewById(R.id.setup_full_name);
        countryName = (EditText) findViewById(R.id.setup_country);
        saveinfobtn = (Button) findViewById(R.id.setup_info_btn);
        profileimage = (CircleImageView) findViewById(R.id.setup_profile_image);

        loadingbar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
        userprofileRef = FirebaseStorage.getInstance().getReference().child("profile images");

        saveinfobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAccountsetupInfo();
            }
        });

        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //opening the gallery
                Intent galleryintent = new Intent();
                galleryintent.setAction(Intent.ACTION_GET_CONTENT);
                galleryintent.setType("image/*");
                startActivityForResult(galleryintent, gallery_pick);

            }
        });
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("profile image")){
                        String image  = dataSnapshot.child("profile image").getValue().toString();
                        Picasso.with(SetupActivity.this).load(image).placeholder(R.drawable.profile).into(profileimage);
                    }else {
                        Toast.makeText(SetupActivity.this, "Please select profile image first.....", Toast.LENGTH_LONG).show();


                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //loading the image
        if (requestCode == gallery_pick && resultCode == RESULT_OK && data!=null){
            Uri imageuri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }
        if (requestCode== CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode==RESULT_OK){

                loadingbar.setTitle("Profile Image");
                loadingbar.setMessage("Please wait while we are updating your profile image......");
                loadingbar.show();
                loadingbar.setCanceledOnTouchOutside(true);

                Uri resultUri = result.getUri();

                StorageReference filepath = userprofileRef.child(current_user_id + ".jpg");
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SetupActivity.this, "Your image is saved", Toast.LENGTH_SHORT).show();

                            final  String downloadUri = task.getResult().getDownloadUrl().toString();
                            usersRef.child("profile image").setValue(downloadUri).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        Intent selfInent = new Intent(SetupActivity.this, SetupActivity.class);
                                        startActivity(selfInent);
                                        Toast.makeText(SetupActivity.this, "Your image is saved in the database", Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();
                                    }
                                    else {
                                        String message = task.getException().toString();
                                        Toast.makeText(SetupActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                                        loadingbar.dismiss();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            else {
                Toast.makeText(SetupActivity.this,"Image can't be cropped, Try again", Toast.LENGTH_SHORT).show();
                loadingbar.dismiss();
            }
        }
    }

    private void saveAccountsetupInfo() {

        String username = UserName.getText().toString();
        String full_name = fullname.getText().toString();
        String country = countryName.getText().toString();

        if(TextUtils.isEmpty(username) && TextUtils.isEmpty(full_name) && TextUtils.isEmpty(country)){
            Toast.makeText(SetupActivity.this, "Fields missing", Toast.LENGTH_LONG).show();
        }else {
            loadingbar.setTitle("Saving Information...");
            loadingbar.setMessage("Please wait......");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("full name", full_name);
            userMap.put("country", country);
            userMap.put("status", "status");
            userMap.put("gender", "none");
            userMap.put("dob", "");
            userMap.put("relationship", "married ");
            usersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        senuserToMainAcivity();
                        Toast.makeText(SetupActivity.this, "Your account is created", Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();
                    }
                    else {

                        String message = task.getException().toString();
                        Toast.makeText(SetupActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();

                    }
                }
            });
        }
    }

    private void senuserToMainAcivity() {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
