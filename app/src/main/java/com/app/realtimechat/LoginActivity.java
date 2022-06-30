package com.app.realtimechat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.realtimechat.utils.ActivityUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ProgressDialog loadingBar;
    private Button loginButton;
    private EditText etEmail, etPassword;
    private TextView tvNewAccountLink;
    private ActivityUtil activityUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_alt);

        activityUtil = new ActivityUtil(LoginActivity.this);
        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        currentUser = mAuth.getCurrentUser();

        initializeFields();

        loginButton.setOnClickListener(v -> {
            allowUserToLogin();
        });

        tvNewAccountLink.setOnClickListener(v -> {
            activityUtil.switchActivity(LoginActivity.this, RegisterActivity.class);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null) {
            activityUtil.switchActivityWithFlag(LoginActivity.this, MainActivity.class);
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

    private void allowUserToLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (TextUtils.isEmpty(email) | TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return;
        }

        loadingBar.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        activityUtil.switchActivityWithFlag(LoginActivity.this, MainActivity.class);
                        Toast.makeText(LoginActivity.this, R.string.success_login, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.fail_login, Toast.LENGTH_SHORT).show();
                    }
                    loadingBar.dismiss();
                });
    }

}