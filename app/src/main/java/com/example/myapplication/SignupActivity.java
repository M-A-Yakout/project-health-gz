package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            saveUserDataToFirestore(email);
                            Toast.makeText(SignupActivity.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Sign-up failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void saveUserDataToFirestore(String email) {
        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).set(
                new User(email, false) // اجعل القيمة الافتراضية لـ "approved" هي false
        ).addOnFailureListener(e -> {
            Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    class User {
        String email;
        boolean approved;

        public User(String email, boolean approved) {
            this.email = email;
            this.approved = approved;
        }

        // Empty constructor required for Firestore
        public User() {
        }
    }
}