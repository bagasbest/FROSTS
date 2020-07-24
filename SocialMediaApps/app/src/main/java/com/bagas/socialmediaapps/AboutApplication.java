package com.bagas.socialmediaapps;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class AboutApplication extends AppCompatActivity {

    ActionBar actionBar;
    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_application);

        actionBar = getSupportActionBar();
        actionBar.setTitle("About Hottalk");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        logo = findViewById(R.id.logo_app);

        Glide.with(this).load(R.drawable.icon_fire).into(logo);

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}
