package com.example.smartkitchen;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    EditText edtName;
    EditText edtSignupEmail;
    EditText edtSignupPassword;
    EditText edtConfirmPassword;

    Button btnCreateAccount;
    TextView txtLogin;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signup);

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Connecting Java variables with XML views
        edtName = findViewById(R.id.edtName);
        edtSignupEmail = findViewById(R.id.edtSignupEmail);
        edtSignupPassword = findViewById(R.id.edtSignupPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        txtLogin = findViewById(R.id.txtLogin);

        // Create Account button
        btnCreateAccount.setOnClickListener(v -> {

            String name = edtName.getText().toString().trim();
            String email = edtSignupEmail.getText().toString().trim();
            String password = edtSignupPassword.getText().toString().trim();
            String confirmPassword =
                    edtConfirmPassword.getText().toString().trim();

            // -----------------------------
            // 1. CHECK NAME
            // -----------------------------
            if (name.isEmpty()) {

                edtName.setError("Please enter your name");

            }

            // -----------------------------
            // 2. CHECK EMAIL
            // -----------------------------
            else if (email.isEmpty()) {

                edtSignupEmail.setError("Please enter your email");

            }

            // Check proper email format
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                showAlert(
                        "Invalid Email",
                        "Please enter a valid email address.\n\nExample:\nvidhi@gmail.com"
                );

            }

            // -----------------------------
            // 3. CHECK PASSWORD EMPTY
            // -----------------------------
            else if (password.isEmpty()) {

                showAlert(
                        "Password Required",
                        "Please enter a password.\n\n" +
                                "Your password should contain:\n\n" +
                                "• Minimum 8 characters\n" +
                                "• 1 uppercase letter (A-Z)\n" +
                                "• 1 lowercase letter (a-z)\n" +
                                "• 1 number (0-9)\n" +
                                "• 1 special character (@, #, $, !, etc.)"
                );

            }

            // -----------------------------
            // 4. CHECK PASSWORD STRENGTH
            // -----------------------------
            else if (!isStrongPassword(password)) {

                showAlert(
                        "Password Doesn't Meet Requirements",
                        "Your password is not strong enough.\n\n" +
                                "Please make sure it contains:\n\n" +
                                "✓ Minimum 8 characters\n" +
                                "✓ At least 1 uppercase letter (A-Z)\n" +
                                "✓ At least 1 lowercase letter (a-z)\n" +
                                "✓ At least 1 number (0-9)\n" +
                                "✓ At least 1 special character (@, #, $, !, etc.)"
                );

            }

            // -----------------------------
            // 5. CHECK CONFIRM PASSWORD
            // -----------------------------
            else if (confirmPassword.isEmpty()) {

                edtConfirmPassword.setError(
                        "Please confirm your password"
                );

            }

            // -----------------------------
            // 6. CHECK PASSWORD MATCH
            // -----------------------------
            else if (!password.equals(confirmPassword)) {

                showAlert(
                        "Passwords Don't Match",
                        "The password and confirm password are different.\n\n" +
                                "Please enter the same password in both fields."
                );

            }

            // -----------------------------
            // 7. CREATE FIREBASE ACCOUNT
            // -----------------------------
            else {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                                SignupActivity.this,
                                task -> {

                                    if (task.isSuccessful()) {

                                        Toast.makeText(
                                                SignupActivity.this,
                                                "Account created successfully! 💕",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        // Go back to Login
                                        finish();

                                    } else {

                                        String errorMessage =
                                                "Unable to create your account.";

                                        if (task.getException() != null) {

                                            errorMessage =
                                                    task.getException()
                                                            .getMessage();
                                        }

                                        showAlert(
                                                "Signup Failed",
                                                errorMessage
                                        );
                                    }
                                }
                        );
            }
        });

        // Login text
        txtLogin.setOnClickListener(v -> {
            finish();
        });
    }

    // ==========================================
    // PASSWORD STRENGTH CHECK
    // ==========================================

    private boolean isStrongPassword(String password) {

        // Minimum 8 characters
        if (password.length() < 8) {
            return false;
        }

        // At least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // At least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // At least one number
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // At least one special character
        if (!password.matches(".*[@#$%^&+=!].*")) {
            return false;
        }

        return true;
    }

    // ==========================================
    // CUSTOM ALERT DIALOG
    // ==========================================

    private void showAlert(String title, String message) {

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}