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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private DatabaseReference usersReference, contactReference, contactRequestsReference;

    public RequestFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_CONTACTS);
        contactRequestsReference =  FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_REQUEST);
        usersReference = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_USERS);

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
        FirebaseRecyclerAdapter<RequestInfo,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<RequestInfo, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull RequestInfo requestInfo) {

                requestInfo.setReceivedUserUid(getRef(position).getKey());
                Log.i("TAG", requestInfo.getRequestType());
                String requestType = requestInfo.getRequestType();
                usersReference.child(requestInfo.getReceivedUserUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String requestUserName = snapshot.child("name").getValue().toString();
                            String requestUserStatus = snapshot.child("status").getValue().toString();

                            Log.i("TAG3",requestUserName +"/"+requestUserStatus);

                            holder.userName.setText(requestUserName);
                            holder.userStatus.setText(requestUserStatus);
                            if (snapshot.hasChild("image")) {
                                final String requestProfileImage = snapshot.child("image").getValue().toString();
                                Picasso.get().load(requestProfileImage).into(holder.profileImage);
                            }

                            if (requestType.equals("request_receive")){

                                holder.acceptButton.setVisibility(View.VISIBLE);
                                holder.cancelButton.setVisibility(View.VISIBLE);
                                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        acceptContactRequest(currentUserID,requestInfo.getReceivedUserUid());
                                    }
                                });

                                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelContactRequest(currentUserID,requestInfo.getReceivedUserUid());
                                    }
                                });

                            } else if (requestType.equals("request_sent")){
                                holder.acceptButton.setVisibility(View.VISIBLE);
                                holder.acceptButton.setText("Req sent");
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
    private void cancelContactRequest(String senderUid, String receiverUid) {

        contactRequestsReference.child(senderUid)
                .child(receiverUid)
                .child("requestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactRequestsReference.child(receiverUid)
                                    .child(senderUid)
                                    .child("requestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(), "Contact request rejected", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptContactRequest(String senderUid, String receiverUid) {

        contactReference.child(senderUid)
                .child(receiverUid)
                .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            contactReference.child(receiverUid)
                                    .child(senderUid)
                                    .child("Contacts").setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                contactRequestsReference.child(senderUid)
                                                        .child(receiverUid)
                                                        .child("requestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    contactRequestsReference.child(receiverUid)
                                                                            .child(senderUid)
                                                                            .child("requestType").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if (task.isSuccessful()) {
                                                                                        Toast.makeText(getContext(), "New Contact Saved", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }

                    }
                });
    }

}


























