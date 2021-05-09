package com.example.chatbook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.chatbook.Activity.MainActivity;
import com.example.chatbook.Activity.PhoneNumberActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        Thread thread = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(1000);
                }catch (Exception e){
                    e.printStackTrace();

                }finally {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if(currentUser!=null) {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(SplashActivity.this, PhoneNumberActivity.class);
                        startActivity(intent);
                    }
                }
                super.run();
            }
        };thread.start();
    }
}