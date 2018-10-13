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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText userEmail, Userpassword, UserComfirmPassword;
    Button createAccount;
    FirebaseAuth mAthu;
    ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAthu = FirebaseAuth.getInstance();

        userEmail = (EditText) findViewById(R.id.register_email);
        Userpassword = (EditText) findViewById(R.id.register_password);
        UserComfirmPassword = (EditText) findViewById(R.id.register_confirm_password);
        createAccount = (Button) findViewById(R.id.register_create_account);
        loadingbar = new ProgressDialog(this);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
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

    private void sendUsertoMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void createNewAccount() {

        String email = userEmail.getText().toString();
        String password = Userpassword.getText().toString();
        String confirmpassword = UserComfirmPassword.getText().toString();

        if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(confirmpassword)){
            Toast.makeText(this, "Field is missing!!", Toast.LENGTH_LONG).show();
        }
        else if(!password.equals(confirmpassword)){
            Toast.makeText(this, "Your passwords don't match", Toast.LENGTH_LONG).show();

        }
        else {
            loadingbar.setTitle("Crating New Account...");
            loadingbar.setMessage("Please wait......");
            loadingbar.show();
            loadingbar.setCanceledOnTouchOutside(true);

            mAthu.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        sendUserToSetupActivity();
                        loadingbar.dismiss();

                    }else{
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error occured: " + message, Toast.LENGTH_LONG).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }

    private void sendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
}
