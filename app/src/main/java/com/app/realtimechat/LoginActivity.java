package com.app.realtimechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private ProgressDialog loadingBar;
    private Button loginButton;
    private EditText etEmail, etPassword;
    private TextView tvNewAccountLink;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_alt);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        initializeFields();

        loginButton.setOnClickListener(v -> {
            try {
                allowUserToLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        tvNewAccountLink.setOnClickListener(v -> {
            sendUserToRegisterActivity();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            sendUserToMainActivity();
        }
    }

    private void initializeFields() {
        loginButton = findViewById(R.id.login_button);
        etEmail = findViewById(R.id.login_email);
        etPassword = findViewById(R.id.login_password);
        tvNewAccountLink = findViewById(R.id.need_new_account_link);

        loadingBar = new ProgressDialog(this);
        loadingBar.setTitle("Login");
        loadingBar.setMessage("Please wait while we are checking your credentials");
        loadingBar.setCanceledOnTouchOutside(false);
    }

    private void allowUserToLogin() throws IOException {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() | password.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return;
        }
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendUserToMainActivity();
                Toast.makeText(LoginActivity.this, "Logged in Successful...", Toast.LENGTH_SHORT).show();
            } else {
                String message = task.getException().toString();
                Toast.makeText(LoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
            }
            loadingBar.dismiss();
        });

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

}