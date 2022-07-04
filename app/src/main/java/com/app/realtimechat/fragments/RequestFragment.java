package com.app.realtimechat.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.R;
import com.app.realtimechat.entities.RequestInfo;
import com.app.realtimechat.utils.Constants;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private DatabaseReference usersReference, contactReference, contactRequestsReference, groupRef;

    public RequestFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_CONTACTS);
        contactRequestsReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_REQUEST);
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_USERS);
        groupRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_GROUPS);

        myRequestList = requestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<RequestInfo> options =
                new FirebaseRecyclerOptions.Builder<RequestInfo>()
                        .setQuery(contactRequestsReference.child(currentUserID), RequestInfo.class)
                        .build();
        FirebaseRecyclerAdapter<RequestInfo, RequestViewHolder> adapter = new FirebaseRecyclerAdapter<RequestInfo, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull RequestInfo requestInfo) {

                requestInfo.setReceivedUserUid(getRef(position).getKey());
                String requestType = requestInfo.getRequestType();
                String currentGroupName = requestInfo.getCurrentGroupName();

                usersReference.child(requestInfo.getReceivedUserUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String requestUserName = snapshot.child("name").getValue().toString();
                            String requestUserStatus = snapshot.child("status").getValue().toString();

                            Log.i("TAG3", requestUserName + "/" + requestUserStatus);

                            holder.userName.setText(requestUserName);
                            holder.userStatus.setText(requestUserStatus);
                            if (snapshot.hasChild("image")) {
                                final String requestProfileImage = snapshot.child("image").getValue().toString();
                                Picasso.get().load(requestProfileImage).into(holder.profileImage);
                            }

                            if (requestType.equals("request_receive")) {

                                holder.acceptButton.setVisibility(View.VISIBLE);
                                holder.cancelButton.setVisibility(View.VISIBLE);
                                holder.acceptButton.setOnClickListener(v -> acceptContactRequest(currentUserID, requestInfo.getReceivedUserUid()));

                                holder.cancelButton.setOnClickListener(v -> cancelContactRequest(currentUserID, requestInfo.getReceivedUserUid(), 1));

                            } else if (requestType.equals("request_sent")) {
                                holder.acceptButton.setVisibility(View.VISIBLE);
                                holder.acceptButton.setText("Req sent");

                                holder.itemView.setOnClickListener(v -> {
                                    CharSequence[] options1 = new CharSequence[]
                                            {
                                                    "Cancel Chat Request"
                                            };

                                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
                                    builder.setTitle("Already Sent Request");
                                    builder.setItems(options1, (dialog, i) -> {
                                        if (i == 0) {
                                            cancelContactRequest(currentUserID, requestInfo.getReceivedUserUid(), 0);
                                        }
                                    });
                                    builder.show();
                                });
                            }

                            if (requestType.equals("request_sent_invite_group")) {
                                holder.userStatus.setText("You sent invite group to " + requestUserName);
                                holder.acceptButton.setVisibility(View.GONE);
                                holder.cancelButton.setVisibility(View.VISIBLE);
                                holder.cancelButton.setOnClickListener(v -> cancelJoinGroup(currentUserID, requestInfo.getReceivedUserUid(), currentGroupName));

                            } else if (requestType.equals("request_receive_invite_group")) {
                                holder.userStatus.setText(requestUserName + " invite you join group ?");
                                holder.acceptButton.setVisibility(View.VISIBLE);
                                holder.acceptButton.setOnClickListener(v -> joinGroup(currentUserID, requestInfo.getReceivedUserUid(), currentGroupName));
                                holder.cancelButton.setVisibility(View.VISIBLE);
                                holder.cancelButton.setOnClickListener(v -> cancelJoinGroup(currentUserID, requestInfo.getReceivedUserUid(), currentGroupName));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            private FirebaseRecyclerAdapter<RequestInfo, RequestViewHolder> getFirebaseRecyclerAdapter() {
                return this;
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                RequestViewHolder viewHolder = new RequestViewHolder(view);
                return viewHolder;
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    private void joinGroup(String senderUid, String receiverUid, String groupName) {
        Map user = new HashMap();
        user.put(groupName, true);

        usersReference.child(senderUid)
                .child("groups")
                .updateChildren(user)
                .addOnCompleteListener(task -> {
                    Map group = new HashMap();
                    group.put(senderUid, true);

                    groupRef.child(groupName)
                            .child("members")
                            .updateChildren(group)
                            .addOnCompleteListener(task1 -> {
                                contactRequestsReference.child(senderUid)
                                        .child(receiverUid)
                                        .removeValue()
                                        .addOnCompleteListener(task2 -> {
                                            if (task.isSuccessful()) {
                                                contactRequestsReference.child(receiverUid)
                                                        .child(senderUid)
                                                        .removeValue()
                                                        .addOnCompleteListener(task3 -> {
                                                            if (task3.isSuccessful()) {
                                                                Toast.makeText(getContext(), "Welcome to " + groupName, Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                            });
                });
    }

    private void cancelJoinGroup(String senderUid, String receiverUid, String group) {
        contactRequestsReference.child(senderUid)
                .child(receiverUid)
                .removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactRequestsReference.child(receiverUid)
                                .child(senderUid)
                                .removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {

                                    }
                                });
                    }
                });
    }

    private void cancelContactRequest(String senderUid, String receiverUid, int mode) {

        contactRequestsReference.child(senderUid)
                .child(receiverUid)
                .child("requestType").removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactRequestsReference.child(receiverUid)
                                .child(senderUid)
                                .child("requestType").removeValue().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        if (mode == 1) {
                                            Toast.makeText(getContext(), "Contact request rejected", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Contact request cancelled", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                    }
                });
    }

    private void acceptContactRequest(String senderUid, String receiverUid) {

        contactReference.child(senderUid)
                .child(receiverUid)
                .child("Contacts").setValue("Saved").addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        contactReference.child(receiverUid)
                                .child(senderUid)
                                .child("Contacts").setValue("Saved").addOnCompleteListener(task12 -> {
                                    if (task12.isSuccessful()) {
                                        contactRequestsReference.child(senderUid)
                                                .child(receiverUid)
                                                .child("requestType").removeValue().addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        contactRequestsReference.child(receiverUid)
                                                                .child(senderUid)
                                                                .child("requestType").removeValue().addOnCompleteListener(task11 -> {
                                                                    if (task11.isSuccessful()) {
                                                                        Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }

                });
    }

    private class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_btn);
            cancelButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }

}


























