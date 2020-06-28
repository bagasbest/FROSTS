package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import com.bagas.socialmediaapps.notification.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class DashboardActivity extends AppCompatActivity {

    //Firebase Auth
    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    String mUID;

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

        checkUserStatus();

        //update token
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(mUID).setValue(mToken);
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
                        case R.id.nav_chat:
                            actionBar.setTitle("Chats");
                            selectedFragment = new ChatListFragment();
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
            mUID = user.getUid();

            //save uid currently signed in user in shared preferences
            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("Current_USERID", mUID);
            editor.apply();
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

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }
}
