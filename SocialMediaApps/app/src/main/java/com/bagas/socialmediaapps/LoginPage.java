package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class LoginPage extends AppCompatActivity {

    FirebaseAuth mAuth;

    private EditText etId, etPassword, etGetEmailForgotPassword;
    private Button btnLogin, btnConfirm, btnDismiss;
    Dialog dialog;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        etId = findViewById(R.id.et_id);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

    }



    public void mendaftar(View view) {
        Intent i = new Intent(this, RegisterActivity.class);
        startActivity(i);
    }

    public void lupaPassword(View view) {
        dialog = new Dialog(this);
        showDialogForgotPassword();
    }

    private void showDialogForgotPassword() {
        dialog.setContentView(R.layout.forgot_password);
        btnConfirm = dialog.findViewById(R.id.btn_konfirmasi_email);
        btnDismiss = dialog.findViewById(R.id.btn_dismiss_email);
        etGetEmailForgotPassword = dialog.findViewById(R.id.et_forgot_pw);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ForgotPasswordResetEmail();
            }
        });

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void ForgotPasswordResetEmail() {

        String email = etGetEmailForgotPassword.getText().toString().trim();


        // validate
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etGetEmailForgotPassword.setError("Maaf email anda tidak valid");
            etGetEmailForgotPassword.setFocusable(true);
        } else {

            progressDialog();
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                dialog.dismiss();
                                Toast.makeText(LoginPage.this, "Email anda terkonfirmasi, silahkan cek email anda untuk ubah kata sandi", Toast.LENGTH_SHORT).show();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(LoginPage.this, "Email anda tidak terkonfirmasi, cek koneksi internet anda", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(LoginPage.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void progressDialog() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
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
