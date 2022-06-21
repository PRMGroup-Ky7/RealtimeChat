package com.app.realtimechat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText editUsernameView, editStatusView;
    private Button updateButton;
    private ProgressBar loadingBar;
    private Toolbar settingsToolbar;

    private String currentUserId;
    private FirebaseAuth fireAuth;
    private DatabaseReference dbRef;
    private StorageReference profileImageRef;

    private static final int GALLERY_PICK = 5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get id of current user logging in
        fireAuth = FirebaseAuth.getInstance();
        currentUserId = fireAuth.getUid();

        // get realtime database
        dbRef = FirebaseDatabase.getInstance().getReference();
        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile images");

        // get all field view
        initField();

        retrieveUserInformation();

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_PICK);
            }
        });
    }

    public void initField() {
        profileImageView = findViewById(R.id.set_profile_image);

        // set username invisible if update , visible if create profile
        editUsernameView = findViewById(R.id.set_user_name);
        editUsernameView.setVisibility(View.INVISIBLE);

        editStatusView = findViewById(R.id.set_profile_status);
        updateButton = findViewById(R.id.update_settings_button);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        //setSupportActionBar(settingsToolbar);

        getSupportActionBar().setTitle("Update Profile");
    }

    public void retrieveUserInformation() {
        dbRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            String username = snapshot.child("name").getValue().toString();
                            String status = snapshot.child("status").getValue().toString();

                            editUsernameView.setText(username);
                            editStatusView.setText(status);

                            if(snapshot.hasChild("image")) {

                            }

                        } else {
                            editUsernameView.setVisibility(View.VISIBLE);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void forwardUpdateToDatabase() {
        String newUsername = editUsernameView.getText().toString();
        String newStatus = editStatusView.getText().toString();

        if (TextUtils.isEmpty(newUsername)) {

        } else if (TextUtils.isEmpty(newStatus)) {

        } else {
            HashMap<String, Object> profileMap = new HashMap();
            profileMap.put("uid", currentUserId);
            profileMap.put("username", newUsername);
            profileMap.put("status", newStatus);
            dbRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {

                            } else {

                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
