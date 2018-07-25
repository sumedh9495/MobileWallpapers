package com.thecoolprogrammer.mobilewallpapers.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.thecoolprogrammer.mobilewallpapers.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        Intent intent  =new Intent(MainActivity.this,HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
