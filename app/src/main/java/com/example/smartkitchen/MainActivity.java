package com.example.smartkitchen;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    // Login screen components
    EditText edtEmail;
    EditText edtPassword;
    Button btnLogin;
    TextView txtSignup;

    // Firebase Authentication
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect Java file with XML layout
        setContentView(R.layout.activity_main);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Find UI elements
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtSignup = findViewById(R.id.txtSignup);

        // Login button
        btnLogin.setOnClickListener(v -> {

            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            // Check email
            if (email.isEmpty()) {

                edtEmail.setError("Please enter your email");

            }

            // Check password
            else if (password.isEmpty()) {

                edtPassword.setError("Please enter your password");

            }

            // Firebase login
            else {

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, task -> {

                            if (task.isSuccessful()) {

                                Intent intent =
                                        new Intent(MainActivity.this, HomeActivity.class);

                                startActivity(intent);

                                finish();

                            } else {

                                // Login failed
                                String errorMessage =
                                        "Login failed. Please check your email and password.";

                                if (task.getException() != null) {
                                    errorMessage =
                                            task.getException().getMessage();
                                }

                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Login Failed ❌")
                                        .setMessage(errorMessage)
                                        .setPositiveButton("OK", null)
                                        .show();
                            }
                        });
            }
        });

        // Signup button
        txtSignup.setOnClickListener(v -> {

            Intent intent =
                    new Intent(MainActivity.this, SignupActivity.class);

            startActivity(intent);

        });
    }
}
