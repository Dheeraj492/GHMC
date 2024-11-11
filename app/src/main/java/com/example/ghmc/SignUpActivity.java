package com.example.ghmc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Ensure this matches your XML layout file name

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        signupEmail = findViewById(R.id.signup_email);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });

        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Login Activity
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void createAccount() {
        String email = signupEmail.getText().toString();
        String password = signupPassword.getText().toString();

        // Check if fields are empty
        if (TextUtils.isEmpty(email)) {
            signupEmail.setError("Email is required.");
            signupEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            signupPassword.setError("Password is required.");
            signupPassword.requestFocus();
            return;
        }

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignUpActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Save login state
                        SharedPreferences preferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("is_logged_in", true);
                        editor.apply();

                        // Redirect to home screen
                        Intent intent = new Intent(SignUpActivity.this, HomeScreenActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // If sign-in fails, display a message to the user
                        Toast.makeText(SignUpActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
