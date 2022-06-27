package com.app.realtimechat.fragments;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.R;
import com.app.realtimechat.entities.ContactRequest;
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

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private View requestFragmentView;
    private RecyclerView myRequestList;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private DatabaseReference usersRef, ContactsRef, contactRequestsReference;

    public RequestFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactRequestsReference =  FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_REQUEST);
        usersRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_USERS);

        myRequestList = requestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        return requestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ContactRequest> options =
                new FirebaseRecyclerOptions.Builder<ContactRequest>()
                        .setQuery(contactRequestsReference.child(currentUserID), ContactRequest.class)
                        .build();
        FirebaseRecyclerAdapter<ContactRequest,RequestViewHolder> adapter = new FirebaseRecyclerAdapter<ContactRequest, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder holder, int position, @NonNull ContactRequest model) {

                String userID = getRef(position).getKey();
                Log.i("TAG", model.getRequestType());
                String requestType = model.getRequestType();
                usersRef.child(userID).addValueEventListener(new ValueEventListener() {
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
}


























