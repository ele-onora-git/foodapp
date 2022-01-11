package com.example.food2;

import android.app.Application;
import android.content.Intent;

import com.example.food2.Common.Common;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;

public class Home extends Application {

    @Override
    public void onCreate() {
        super.onCreate();


        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null){
            Intent intent = new Intent(Home.this, Diary.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Common.currentUser = firebaseUser;
        }
    }
}
