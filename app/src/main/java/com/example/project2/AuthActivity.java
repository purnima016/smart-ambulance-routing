package com.example.project2;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;

    // Login Views
    private LinearLayout loginSection, signupSection;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnCreateAccount, btnEmergencySkip;
    private TextView tvSignUp, tvLogin;

    // Signup Views
    private EditText etUsername, etSignupEmail, etSignupPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Initialize Firebase Auth and Database Helper
        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        // Layout sections
        loginSection = findViewById(R.id.loginSection);
        signupSection = findViewById(R.id.signupSection);

        // Login fields
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Signup fields
        etUsername = findViewById(R.id.etUsername);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Toggle links
        tvSignUp = findViewById(R.id.tvSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        // Emergency skip
        btnEmergencySkip = findViewById(R.id.btnEmergencySkip);
    }

    private void setupClickListeners() {
        // Login button
        btnLogin.setOnClickListener(v -> loginUser());

        // Create account button
        btnCreateAccount.setOnClickListener(v -> createAccount());

        // Toggle to Signup
        tvSignUp.setOnClickListener(v -> showSignupSection());

        // Toggle to Login
        tvLogin.setOnClickListener(v -> showLoginSection());

        // Emergency skip
        btnEmergencySkip.setOnClickListener(v -> emergencySkip());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Show loading
        btnLogin.setText("LOGGING IN...");
        btnLogin.setEnabled(false);

        // Firebase login
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Check and create driver profile if needed
                            checkDriverProfile(user);
                            Toast.makeText(AuthActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            goToMainApp();
                        }
                    } else {
                        // Login failed
                        btnLogin.setText("Sign In");
                        btnLogin.setEnabled(true);
                        Toast.makeText(AuthActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createAccount() {
        String username = etUsername.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Driver name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etSignupEmail.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etSignupPassword.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            etSignupPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Show loading
        btnCreateAccount.setText("CREATING ACCOUNT...");
        btnCreateAccount.setEnabled(false);

        // Firebase create account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign up success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save driver details to SQLite database
                            boolean saved = dbHelper.addDriver(
                                    user.getUid(),           // Firebase UID
                                    username,                // Driver name
                                    email,                   // Email
                                    "Not Provided",          // Driver License (default)
                                    "AMB-001",               // Ambulance Number (default)
                                    "Not Provided"           // Phone (default)
                            );

                            if (saved) {
                                Toast.makeText(AuthActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                goToMainApp();
                            } else {
                                btnCreateAccount.setText("Create Account");
                                btnCreateAccount.setEnabled(true);
                                Toast.makeText(AuthActivity.this, "Account created but profile save failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        // Sign up failed
                        btnCreateAccount.setText("Create Account");
                        btnCreateAccount.setEnabled(true);
                        Toast.makeText(AuthActivity.this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkDriverProfile(FirebaseUser user) {
        Driver driver = dbHelper.getDriverByUid(user.getUid());

        if (driver == null) {
            // Driver profile doesn't exist in SQLite, create one
            String defaultUsername = user.getEmail().split("@")[0]; // Use email prefix as username
            dbHelper.addDriver(
                    user.getUid(),
                    defaultUsername,
                    user.getEmail(),
                    "Not Provided",
                    "AMB-001",
                    "Not Provided"
            );
            Toast.makeText(this, "Welcome! Profile created.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Welcome back, " + driver.getUsername() + "!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSignupSection() {
        loginSection.setVisibility(View.GONE);
        signupSection.setVisibility(View.VISIBLE);

        // Clear login fields
        etEmail.setText("");
        etPassword.setText("");
    }

    private void showLoginSection() {
        signupSection.setVisibility(View.GONE);
        loginSection.setVisibility(View.VISIBLE);

        // Clear signup fields
        etUsername.setText("");
        etSignupEmail.setText("");
        etSignupPassword.setText("");
    }

    private void emergencySkip() {
        // Anonymous login for emergency access
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AuthActivity.this, "Emergency access granted!", Toast.LENGTH_SHORT).show();
                        goToMainApp();
                    } else {
                        Toast.makeText(AuthActivity.this, "Emergency access failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void goToMainApp() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            goToMainApp();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database connection
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}