package com.example.chodry.socialmwogo;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    Button LoginButton;
    EditText UserEmail, UserPassword;
    TextView NeedNweAccount;
    FirebaseAuth mAthu;
    ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAthu = FirebaseAuth.getInstance();

        NeedNweAccount = (TextView) findViewById(R.id.register_link);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        LoginButton = (Button) findViewById(R.id.login_btn);
        loadingbar = new ProgressDialog(this);

        NeedNweAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlowingUsertoLogin();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAthu.getCurrentUser();
        if (currentUser != null){
            sendUsertoMainActivity();
        }
    }

    private void AlowingUsertoLogin() {

        String email1 = UserEmail.getText().toString();
        String password1 = UserPassword.getText().toString();

        if (TextUtils.isEmpty(email1) && TextUtils.isEmpty(password1)){
            Toast.makeText(LoginActivity.this, "Field is empty", Toast.LENGTH_LONG).show();
        }
        else {
            loadingbar.setTitle("Logging into your account...");
            loadingbar.setMessage("Please wait......");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            mAthu.signInWithEmailAndPassword(email1, password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()){
                        sendUsertoMainActivity();
                        loadingbar.dismiss();

                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();

                    }

                }
            });
        }
    }

    private void sendUsertoMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {

        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);

    }
}
