package com.example.chodry.socialmwogo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Toolbar mtoolbar;
    EditText postTitle, postdescription;
    Button updatepostbtn;
    ImageButton selectpostimage;
    final static int gallery_pick = 1;
    Uri ImageUri;
    String title;
    String description;
    StorageReference postImageRef;
    String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    DatabaseReference usersRef, postRef;
    FirebaseAuth mAthu;
    ProgressDialog loadingbar;
    long countPosts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAthu = FirebaseAuth.getInstance();
        current_user_id = mAthu.getCurrentUser().getUid();

        postImageRef = FirebaseStorage.getInstance().getReference();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        selectpostimage = (ImageButton) findViewById(R.id.select_post_image);
        updatepostbtn = (Button) findViewById(R.id.update_post_btn);
        postTitle = (EditText) findViewById(R.id.post_title);
        postdescription = (EditText) findViewById(R.id.post_description);

        loadingbar = new ProgressDialog(this);

        mtoolbar = (Toolbar) findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        selectpostimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
            }
        });

        updatepostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validatePostInfo();
            }
        });

    }

    private void validatePostInfo() {
        title = postTitle.getText().toString();
        description = postdescription.getText().toString();

        if (ImageUri ==null){
            Toast.makeText(PostActivity.this, "Please select an image.....", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(title)){
            Toast.makeText(PostActivity.this, "Please input your title.....", Toast.LENGTH_LONG).show();
        }
        else if (TextUtils.isEmpty(description)){
            Toast.makeText(PostActivity.this, "Please input your description....", Toast.LENGTH_LONG).show();
        }
        else {

            loadingbar.setTitle("Adding New Post");
            loadingbar.setMessage("Please wait while your post is being added......");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            storingImageToFirebaseStorage();
        }
    }

    private void storingImageToFirebaseStorage() {

        Calendar callforDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(callforDate.getTime());

        Calendar callforTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(callforTime.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filepath = postImageRef.child("post images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()){

                    downloadUrl = task.getResult().getDownloadUrl().toString();
                    Toast.makeText(PostActivity.this, "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                    
                    savingPostInforToDatabase();

                }else {
                    String message = task.getException().toString();
                    Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void savingPostInforToDatabase() {

        postRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    countPosts = dataSnapshot.getChildrenCount();
                }
                else {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){

                    String userfullname = dataSnapshot.child("full name").getValue().toString();
                    String userProfileImage = dataSnapshot.child("profile image").getValue().toString();

                    HashMap postsMap = new HashMap();
                        postsMap.put("uid", current_user_id);
                        postsMap.put("date", saveCurrentDate);
                        postsMap.put("time", saveCurrentTime);
                        postsMap.put("title", title);
                        postsMap.put("description", description);
                        postsMap.put("postimage", downloadUrl);
                        postsMap.put("profileimage", userProfileImage);
                        postsMap.put("fullname", userfullname);
                        postsMap.put("counter", countPosts);

                    postRef.child(current_user_id + postRandomName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()){

                                sendUserToMainActivity();
                                Toast.makeText(PostActivity.this, "New Post is updated successfully", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                            }
                            else {

                                String message = task.getException().toString();
                                Toast.makeText(PostActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                                loadingbar.dismiss();


                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery() {

        Intent galleryintent = new Intent();
        galleryintent.setAction(Intent.ACTION_GET_CONTENT);
        galleryintent.setType("image/*");
        startActivityForResult(galleryintent, gallery_pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == gallery_pick && resultCode == RESULT_OK && data!=null){

            ImageUri = data.getData();
            selectpostimage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home){
            sendUserToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PostActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
