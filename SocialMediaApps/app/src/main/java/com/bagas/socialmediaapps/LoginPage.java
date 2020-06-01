package com.bagas.socialmediaapps;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginPage extends AppCompatActivity {

    private EditText etId, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);


    }

    public void mendaftar(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void lupaPassword(View view) {
        Intent i = new Intent(this, ForgotPasswordActivity.class);
        startActivity(i);
    }

    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi keluar aplikasi");
        builder.setIcon(R.drawable.ic_exit_to_app_black_24dp);
        builder.setMessage("Anda yakin ingin keluar aplikasi ? ");
        builder.setCancelable(false);

        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAndRemoveTask();
                finish();
            }
        });

        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}

//In this part (01) we will do the following
//* 1. Add Internet permission (for firebase)
//* 2. Add Register and Login Button in SplashScreen
//* 3. Create RegisterActivity
//* 4. Create Firebase Project and connect app with that
//* 5. Check Google-service.json file to make sure app is connect with firebase
//* 6. User Registration using Email & Password
//* 7. Create Profile Activity
//* 8. Make ProfileActivity Launcher
//* 9. Go to ProfileActivity After Registration/Login
//* 10. Add Logout button
