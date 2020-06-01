package com.bagas.socialmediaapps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    //views
    EditText etEmail, etPassword;
    Button btnMendaftar;

    //progress dialog to display while registrering user
    ProgressDialog progressDialog;

    //Declare an instance FirebaseAuth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //init
        etEmail = findViewById(R.id.et_reg_id);
        etPassword = findViewById(R.id.et_reg_password);
        btnMendaftar = findViewById(R.id.btn_mendaftar);

        //in onCreate method, instalize the FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mendaftaftarkan pengguna...");

        //handle register btn click
        btnMendaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input email, password
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();


                // validate
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Maaf email anda tidak valid");
                    etEmail.setFocusable(true);
                }
                else if (password.length() < 6 ) {
                    //set error and focus to password edittext
                    etPassword.setError("Maaf, password anda kurang dari 6 digit");
                    etPassword.setFocusable(true);
                }
                else {
                    // register the user
                    registerUser(email, password);
                }
            }
        });
    }

    private void registerUser(String email, String password) {
        // email and user is valid, show progress dialog and start registering user
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start register activity
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Selamat, anda berhasil mendaftar", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, ProfileActivity.class));
                            finish();
                        } else {
                            //if sign0in fails, display a message to user
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Maaf, autentikasi tidak berhasil", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //error, dismiss progres dialog and get and show the error message
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, "Maaf, pendaftaran tidak berhasil", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go to preview activity
        return super.onSupportNavigateUp();
    }
}
