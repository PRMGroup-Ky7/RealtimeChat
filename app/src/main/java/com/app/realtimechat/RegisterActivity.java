package com.app.realtimechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button createAccountButton;
    private EditText etEmail, etPassword, etName;
    private TextView tvAlreadyHaveAccount;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        initializeFields();

        tvAlreadyHaveAccount.setOnClickListener(v -> sendUserToLoginActivity());

        createAccountButton.setOnClickListener(v -> CreateNewAccount());
    }

    private void initializeFields() {
        createAccountButton = findViewById(R.id.register_button);
        etEmail = findViewById(R.id.register_email);
        etPassword = findViewById(R.id.register_password);
        tvAlreadyHaveAccount = findViewById(R.id.already_have_account_link);

        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void CreateNewAccount() {
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
        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please wait, while we're creating an new account for you");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                sendUserToMainActivity();
                Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
            } else {
                String message = task.getException().toString();
                Toast.makeText(RegisterActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
            loadingBar.dismiss();
        });
    }

}
