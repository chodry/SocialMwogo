package com.example.chodry.socialmwogo;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    RecyclerView commentsList;
    ImageButton postCommentsBtn;
    EditText commentInputtext;
    String Post_Key, current_user_id, saveCurrentDate, saveCurrentTime, postRandomName;
    DatabaseReference usersRef, postRef;
    FirebaseAuth mAthu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAthu = FirebaseAuth.getInstance();
        current_user_id = mAthu.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        postRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");

        commentsList = (RecyclerView) findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);

        commentInputtext = (EditText) findViewById(R.id.comment_input);
        postCommentsBtn = (ImageButton) findViewById(R.id.post_comment_btn);

        postCommentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                usersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            String username = dataSnapshot.child("username").getValue().toString();
                            ValidateComment(username);
                            commentInputtext.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(

                        Comments.class,
                        R.layout.all_comments_layout,
                        CommentsViewHolder.class,
                        postRef
        ) {
            @Override
            protected void populateViewHolder(CommentsViewHolder viewHolder, Comments model, int position) {

                viewHolder.setUsername(model.getUsername());
                viewHolder.setComments(model.getComments());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
            }
        };

        commentsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public CommentsViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setUsername(String username){

            TextView myusername = (TextView) mView.findViewById(R.id.commment_user_name);
            myusername.setText("@" + username+ "  ");
        }

        public void setComments(String comments){

            TextView mycomment = (TextView) mView.findViewById(R.id.comment_text);
            mycomment.setText(comments);
        }

        public void setDate(String date){

            TextView mydate = (TextView) mView.findViewById(R.id.comment_date);
            mydate.setText("  " + date);

        }

        public void setTime(String time){

            TextView mytime = (TextView) mView.findViewById(R.id.commment_time);
            mytime.setText(time);
        }
    }

    private void ValidateComment(String username) {

        String commenttext = commentInputtext.getText().toString();

        if (TextUtils.isEmpty(commenttext)){
            Toast.makeText(CommentsActivity.this, "Write a comment please", Toast.LENGTH_LONG).show();
        }
        else {

            Calendar callforDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            saveCurrentDate = currentDate.format(callforDate.getTime());

            Calendar callforTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
            saveCurrentTime = currentTime.format(callforTime.getTime());

            postRandomName = current_user_id + saveCurrentDate + saveCurrentTime;

            HashMap commentsMap = new HashMap();
                commentsMap.put("uid", current_user_id);
                commentsMap.put("comments", commenttext);
                commentsMap.put("date", saveCurrentDate);
                commentsMap.put("time", saveCurrentTime);
                commentsMap.put("username", username);

            postRef.child(postRandomName).updateChildren(commentsMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(CommentsActivity.this, "You have added a comment", Toast.LENGTH_SHORT).show();
                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(CommentsActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();

                    }
                }
            });
        }
    }


}
