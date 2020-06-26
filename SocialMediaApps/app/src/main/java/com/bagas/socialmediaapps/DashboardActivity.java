package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    //Firebase Auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Actionbar and it's title
        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");


        firebaseAuth = FirebaseAuth.getInstance();

        BottomNavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment trannsaction default on start
        actionBar.setTitle("Home");
        getSupportFragmentManager().beginTransaction().replace(R.id.content,
                new HomeFragment()).commit();
    }

    BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                           selectedFragment = new HomeFragment();
                           break;
                        case R.id.nav_Profile:
                            actionBar.setTitle("Profile");
                            selectedFragment = new ProfileFragment();
                            break;
                        case R.id.nav_user:
                            actionBar.setTitle("User");
                            selectedFragment = new UserFragment();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.content,
                            selectedFragment).commit();

                    return true;
                }
            };

    private void checkUserStatus() {
        //get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null) {
            //user is signed in stay here
            //tvShow.setText(user.getEmail());

        } else {
            //uset not signed
            startActivity(new Intent(this, LoginPage.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }


}
