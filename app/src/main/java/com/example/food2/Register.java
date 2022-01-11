package com.example.food2;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.food2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {

    private TextView alreadyHaveAcc;
    private Button register;
    private EditText inputName, inputEmail, inputPassword, inputConfirmPassword;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    FirebaseDatabase fDatabase;
    DatabaseReference firebaseDatbase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fDatabase = FirebaseDatabase.getInstance();
        firebaseDatbase = fDatabase.getReference("Users");

        mAuth = FirebaseAuth.getInstance();

        alreadyHaveAcc = findViewById(R.id.alreadyHaveAcc);

        register = findViewById(R.id.btnRegister);

        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        progressDialog=new ProgressDialog(this);

        alreadyHaveAcc.setOnClickListener(v -> startActivity(new Intent(Register.this, Login.class)));

        register.setOnClickListener(v -> registerUser());

    }

    private void registerUser() {
        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        if(name.isEmpty()){
            inputName.setError("name is required");
            inputName.requestFocus(); // refocus on the field
            return;
        }
        else if(email.isEmpty()){
            inputName.setError("email is required");
            inputName.requestFocus();
            return;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            inputEmail.setError("enter valid email");
            inputEmail.requestFocus();
            return;
        }
        else if (password.isEmpty()|| password.length()<6){
            inputPassword.setError("enter valid password");
            inputPassword.requestFocus();
            return;
        }
        else if (!password.equals(confirmPassword)){
            inputConfirmPassword.setError("passwords don't match");
            inputConfirmPassword.requestFocus();
            return;
        }
        else {
            progressDialog.setMessage("please wait...");
            progressDialog.setTitle("registration");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        // create user object that will store info into the database
                        User user = new User(name, email);
                        Log.d(TAG, "check: name " + name);
                        Log.d(TAG, "check: email " + email);
                        // send the object to the real time database
                        // addOnCompleteListener checks if the data has been inserted before
                        firebaseDatbase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(user).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()){
                                progressDialog.dismiss();
                                Toast.makeText(Register.this,
                                        "registered successfully", Toast.LENGTH_LONG).show();
                                // redirect to login layout for email verification
                                startActivity(new Intent(Register.this, Login.class));
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(Register.this,
                                        "failed to register, try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Register.this,
                                "failed to register, try again", Toast.LENGTH_LONG).show();
                    }
                });

    }

}