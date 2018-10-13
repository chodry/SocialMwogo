package com.example.chodry.socialmwogo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity {

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    RecyclerView postList;
    Toolbar mtoolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    FirebaseAuth mAthu;
    DatabaseReference userRef, postsRef, likesRef;
    CircleImageView navProfileImage;
    TextView navProfileName;
    String currentUserId;
    ImageButton addNewPost;
    Boolean likechecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAthu = FirebaseAuth.getInstance();
        currentUserId = mAthu.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        drawerLayout = (DrawerLayout) findViewById(R.id.drawable_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        addNewPost = (ImageButton) findViewById(R.id.add_new_post_btn);


        mtoolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("Social Mwogo");

        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        navProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        navProfileName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);

        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){

                    if (dataSnapshot.hasChild("full name")){
                        String fullname = dataSnapshot.child("full name").getValue().toString();
                        navProfileName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profile image")){
                        String image = dataSnapshot.child("profile image").getValue().toString();
                        Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(navProfileImage);

                    }
                    else {
                        Toast.makeText(MainActivity.this, "Profile name don't exist.....", Toast.LENGTH_LONG).show();
                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });

        addNewPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToPostActivity();
            }
        });

        DisplayAllUsersPosts();
    }

    private void DisplayAllUsersPosts() {

        Query sortpostsinDecendingorder = postsRef.orderByChild("counter");

        FirebaseRecyclerAdapter<Post,PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Post, PostsViewHolder>(
                        Post.class,
                        R.layout.all_posts_layout,
                        PostsViewHolder.class,
                        sortpostsinDecendingorder
                ) {
            @Override
            protected void populateViewHolder(PostsViewHolder viewHolder, Post model, int position) {

                final String PostKey = getRef(position).getKey();

                viewHolder.setFullname(model.getFullname());
                viewHolder.setTime(model.getTime());
                viewHolder.setDate(model.getDate());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setDescription(model.getDescription());
                viewHolder.setProfileimage(getApplicationContext(), model.getProfileimage());
                viewHolder.setPostimage(getApplicationContext(), model.getPostimage());

                viewHolder.setLikesBtnStatus(PostKey);

                viewHolder.likepostbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        likechecker = true;

                        likesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (likechecker.equals(true)){

                                    if (dataSnapshot.child(PostKey).hasChild(currentUserId)){
                                        likesRef.child(PostKey).child(currentUserId).removeValue();
                                        likechecker = false;
                                    }
                                    else{
                                        likesRef.child(PostKey).child(currentUserId).setValue(true);
                                        likechecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                });

                viewHolder.commenPostbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
                        commentsIntent.putExtra("PostKey", PostKey);
                        startActivity(commentsIntent);
                    }
                });

            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder{

        View mview;

        ImageButton likepostbtn, commenPostbtn;
        TextView displaylikes;
        int countlikes;
        String currentuserid;
        DatabaseReference likesRef;

        public PostsViewHolder(View itemView) {
            super(itemView);
            mview = itemView;

            likepostbtn = (ImageButton) mview.findViewById(R.id.likes_btn);
            commenPostbtn = (ImageButton) mview.findViewById(R.id.comment_btn);
            displaylikes = (TextView) mview.findViewById(R.id.display_no_of_likes);

            likesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentuserid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikesBtnStatus(final String PostKey){
            likesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(PostKey).hasChild(currentuserid)){
                        countlikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likepostbtn.setImageResource(R.drawable.like);
                        displaylikes.setText((Integer.toString(countlikes)+" Likes"));
                    }
                    else{
                        countlikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        likepostbtn.setImageResource(R.drawable.dislike);
                        displaylikes.setText((Integer.toString(countlikes)+" Likes"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        public void setFullname(String fullname){
            TextView username = (TextView) mview.findViewById(R.id.post_profile_username);
            username.setText(fullname);
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView image = (CircleImageView) mview.findViewById(R.id.post_profile_image);
            Picasso.with(ctx).load(profileimage).into(image);

        }

        public void setTime(String time){
            TextView posttime = (TextView) mview.findViewById(R.id.post_time);
            posttime.setText("   " + time);

        }

        public void setDate(String date){
            TextView postdate = (TextView) mview.findViewById(R.id.post_date);
            postdate.setText("   " + date);
        }

        public void setDescription(String description){
            TextView postdescription = (TextView) mview.findViewById(R.id.post_description);
            postdescription.setText(description);
        }

        public void setTitle(String title){
            TextView posttitle = (TextView) mview.findViewById(R.id.post_title);
            posttitle.setText(title);
        }

        public void setPostimage(Context ctx, String postimage){
            ImageView Postimage = (ImageView) mview.findViewById(R.id.post_image);
            Picasso.with(ctx).load(postimage).into(Postimage);

        }
    }

    private void sendUserToPostActivity() {

        Intent postIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(postIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //if user has logged in
        FirebaseUser currentUser = mAthu.getCurrentUser();
        if (currentUser == null){
            sendUserToLoginActivity();
        }
        else {
            checkUserExistance();
        }
    }

    private void checkUserExistance() {

        final String current_user_id = mAthu.getCurrentUser().getUid();
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(current_user_id)){
                    sendusertoSetupActivity();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendusertoSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        //user does not return to the home page
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent LoginIntent = new Intent(MainActivity.this, LoginActivity.class);
        //user does not return to the home page
        LoginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(LoginIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_profile:
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_messages:
                Toast.makeText(this, "message", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                mAthu.signOut();
                sendUserToLoginActivity();
                break;
            case R.id.nav_post:
                sendUserToPostActivity();
                break;
            case R.id.nav_qanda:
                Toast.makeText(this, "Q and A", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
