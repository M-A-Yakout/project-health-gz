package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            checkUserApproval();
                        } else {
                            Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void checkUserApproval() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(auth.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Get the "approved" field from the document
                    Boolean isApproved = documentSnapshot.getBoolean("approved");

                    // Check if the user is approved
                    if (isApproved != null && isApproved) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        // Handle cases where "approved" is null or false
                        if (isApproved == null) {
                            Toast.makeText(this, "Your account status is not determined yet", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Your account is not approved yet", Toast.LENGTH_SHORT).show();
                        }
                        FirebaseAuth.getInstance().signOut(); // Log out the user if not approved
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking approval: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}