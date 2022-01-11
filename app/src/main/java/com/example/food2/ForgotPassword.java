package com.example.food2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private EditText inputEmail;
    private Button resetPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        inputEmail =  findViewById(R.id.inputEmail);
        resetPassword = findViewById(R.id.resetPwdBtn);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = inputEmail.getText().toString().trim();

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

        else {
            progressDialog.setMessage("please wait...");
            progressDialog.setTitle("resetting password");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ForgotPassword.this, "check your email to reset your password", Toast.LENGTH_SHORT).show();
                    progressDialog.hide();
                    Intent backToLogin = new Intent(ForgotPassword.this, Login.class);
                    startActivity(backToLogin);
                } else{
                    Toast.makeText(ForgotPassword.this, "oops, try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}