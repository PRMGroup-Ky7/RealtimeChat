package com.app.realtimechat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.app.realtimechat.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String visitedUserId, currentUserId,requestState;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;
    private DatabaseReference userReference,contactRequestReference,contactReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_USERS);
        contactRequestReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_REQUEST);
        contactReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_CONTACTS);

        mAuth = FirebaseAuth.getInstance();

        currentUserId=mAuth.getCurrentUser().getUid();
        visitedUserId=getIntent().getStringExtra("visitedUserId");
        requestState = "no_request";
        Log.i("ProfileStuff",visitedUserId);

        userProfileImage            = findViewById(R.id.visit_profile_image);
        userProfileName             = findViewById(R.id.visit_user_name);
        userProfileStatus           = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton    = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        getVisitedUserInfo();

    }

    private void getVisitedUserInfo() {
        userReference.child(visitedUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists()) && (snapshot.hasChild("image"))) {
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();

                } else {
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {
//        contactRequestReference.child(currentUserId).child(visitedUserId).setValue("stuff");
        contactRequestReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild(visitedUserId)){

                    String requestType = snapshot.child(visitedUserId).child("requestType").getValue().toString();

                    if (requestType.equalsIgnoreCase("sent")){
                        requestState = "request_sent";
                    } else if (requestType.equalsIgnoreCase("receive")){
                        requestState = "request_receive";
                    }
                }else {
                    contactReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.hasChild(visitedUserId)){
                                requestState = "friend";
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (requestState) {
                case "no_request" : {
                    Log.i("test","no_request");
                    break;
                }

                case "request_sent" : {
                    Log.i("test","request_sent");
                    break;
                }

                case "request_receive" : {
                    Log.i("test","request_receive");
                    break;
                }

                case "friend" : {
                    Log.i("test","friend");
                    break;
                }
            }
        }
    });

    }
}