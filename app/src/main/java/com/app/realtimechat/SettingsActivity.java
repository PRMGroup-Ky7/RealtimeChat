package com.app.realtimechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 5;
    private CircleImageView editProfileImageView;
    private EditText editUsernameView, editStatusView;
    private Button updateButton;
    private ProgressDialog loadingBar;
    private Toolbar settingsToolbar;
    private String currentUserId;
    private FirebaseAuth fireAuth;
    private DatabaseReference dbRef;
    private StorageReference storageRef;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get id of current user logging in
        fireAuth = FirebaseAuth.getInstance();
        currentUserId = fireAuth.getUid();

        // get realtime database
        dbRef = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        // get all field view
        initField();
        editUsernameView.setVisibility(View.INVISIBLE);

        retrieveUserInformation();

        updateButton.setOnClickListener(v -> forwardUpdateToDatabase());

        editProfileImageView.setOnClickListener(v -> {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        });
    }

    public void initField() {
        editProfileImageView = findViewById(R.id.set_profile_image);

        // set username invisible if update , visible if create profile
        editUsernameView = findViewById(R.id.set_user_name);
        editStatusView = findViewById(R.id.set_profile_status);
        updateButton = findViewById(R.id.update_settings_button);
        settingsToolbar = findViewById(R.id.settings_toolbar);
        loadingBar = new ProgressDialog(this);

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Update Profile");
    }

    public void retrieveUserInformation() {
        dbRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.hasChild("name") && snapshot.hasChild("image")) {

                            String username = snapshot.child("name").getValue().toString();
                            editUsernameView.setText(username);

                            String status = snapshot.child("status").getValue().toString();
                            editStatusView.setText(status);

                            String imageUrl = snapshot.child("image").getValue().toString();
                            Picasso.get().load(imageUrl).into(editProfileImageView);
                        } else if (snapshot.exists() && snapshot.hasChild("name")) {

                            String username = snapshot.child("name").getValue().toString();
                            editUsernameView.setText(username);

                            String status = snapshot.child("status").getValue().toString();
                            editStatusView.setText(status);

                        } else {
                            // new profile
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
            Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(newStatus)) {
            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        } else {
            HashMap<String, Object> profileMap = new HashMap();
            profileMap.put("uid", currentUserId);
            profileMap.put("name", newUsername);
            profileMap.put("status", newStatus);
            saveDisplayName(newUsername);
            dbRef.child("Users").child(currentUserId).updateChildren(profileMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            goToMainActivity();
                        } else {
                            Toast.makeText(SettingsActivity.this, "Update unsucessful", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void goToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Loading image");
                loadingBar.setMessage("Please wait, we are uploading your image");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference imageProfileRef =
                        storageRef
                                .child("Profile Images")
                                .child(currentUserId + ".jpg");

                uploadTask = imageProfileRef.putFile(resultUri);
                uploadTask.addOnProgressListener((OnProgressListener<UploadTask.TaskSnapshot>) snapshot -> {
                    double progress = (snapshot.getBytesTransferred() / snapshot.getTotalByteCount()) * 100;
                    loadingBar.setMessage((int) progress + "% uploading...");
                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageProfileRef.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        dbRef.child("Users").child(currentUserId).child("image")
                                .setValue(downloadUrl.toString())
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Picasso.get().load(downloadUrl.toString()).into(editProfileImageView);
                                        Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                                    } else {
                                        loadingBar.dismiss();
                                        String message = task1.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error uploading image database: " + message, Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(SettingsActivity.this, "Error uploading image database " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void saveDisplayName(String displayName) {
        FirebaseUser user = fireAuth.getCurrentUser();

        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("FlashChat", "User name updated.");
                        }
                    });
        }

    }
}
