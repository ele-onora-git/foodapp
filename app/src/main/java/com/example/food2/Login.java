package com.example.food2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food2.Common.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Login extends AppCompatActivity {

    private TextView registerNewAcc, forgotPassword;
    private EditText inputEmail, inputPassword;
    private Button login;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        registerNewAcc = findViewById(R.id.registerNewAcc);
        login = findViewById(R.id.btnLogin);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        progressDialog=new ProgressDialog(this);
        forgotPassword = findViewById(R.id.forgotPassword);
        mAuth = FirebaseAuth.getInstance();

        registerNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
            }
        });
    }


    private void userLogin() {
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        if(email.isEmpty()){
            inputEmail.setError("email is required");
            inputEmail.requestFocus();
            return;
        }

        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            inputEmail.setError("enter valid email");
            inputEmail.requestFocus();
            return;
        }

        else if (password.isEmpty()|| password.length()<6){
            inputPassword.setError("enter valid password");
            inputPassword.requestFocus();
            return;
        }

        else {
            progressDialog.setMessage("please wait...");
            progressDialog.setTitle("login");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    // email verification
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user.isEmailVerified()){
                        progressDialog.dismiss();
                        // redirect to user profile
                        startActivity(new Intent(Login.this, Diary.class));
                        Common.currentUser = user;
                    } else {
                        progressDialog.dismiss();
                        user.sendEmailVerification();
                        Toast.makeText(Login.this, "check your email to verify your account", Toast.LENGTH_SHORT).show();
                    }
                } else{
                    progressDialog.dismiss();
                    Toast.makeText(Login.this, "failed to login, check your credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}