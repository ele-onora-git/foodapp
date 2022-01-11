package com.example.food2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.food2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Profile extends AppCompatActivity {

    private TextView nameTxt;
    private Button signOutBtn;

    FirebaseDatabase database;
    DatabaseReference userDb;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    String userEmail;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        nameTxt = findViewById(R.id.nameTxt);
        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent signOut = new Intent(Profile.this, Login.class);
                signOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                signOut.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signOut);
            }
        });

        userEmail = firebaseUser.getEmail();

        //initialise Firebase
        database = FirebaseDatabase.getInstance();
        userDb = database.getReference("Users");
        userDb.orderByChild("email").equalTo(userEmail).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                User userName = snapshot.getValue(User.class);
                name = userName.name;
                nameTxt.setText(name);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}