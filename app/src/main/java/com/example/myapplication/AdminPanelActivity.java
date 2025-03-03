package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        ListView userListView = findViewById(R.id.userListView);
        List<String> pendingUsers = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pendingUsers);
        userListView.setAdapter(adapter);

        loadPendingUsers(pendingUsers, adapter);
    }

    private void loadPendingUsers(List<String> pendingUsers, ArrayAdapter<String> adapter) {
        db.collection("users").whereEqualTo("approved", false).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    pendingUsers.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String email = doc.getString("email");
                        String userId = doc.getId();
                        pendingUsers.add(email);
                        addApproveRejectButtons(userId, email);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show());
    }

    private void addApproveRejectButtons(final String userId, final String email) {
        View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
        Button approveButton = view.findViewById(android.R.id.text1);
        Button rejectButton = view.findViewById(android.R.id.text2);

        approveButton.setText("Approve");
        rejectButton.setText("Reject");

        approveButton.setOnClickListener(v -> approveUser(userId, email));
        rejectButton.setOnClickListener(v -> rejectUser(userId, email));
    }

    private void approveUser(String userId, String email) {
        db.collection("users").document(userId).update("approved", true)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, email + " approved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error approving user", Toast.LENGTH_SHORT).show());
    }

    private void rejectUser(String userId, String email) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> Toast.makeText(this, email + " rejected", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error rejecting user", Toast.LENGTH_SHORT).show());
    }
}