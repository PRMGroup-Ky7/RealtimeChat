package com.app.realtimechat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.ChatActivity;
import com.app.realtimechat.R;
import com.app.realtimechat.entities.Contacts;
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
public class ChatsFragment extends Fragment {

    private final String retImage = "default_image";
    private View privateChatsView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference chatsRef, usersRef;
    private RecyclerView chatsList;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        chatsRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_CONTACTS).child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_USERS);
        chatsList = privateChatsView.findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatsViewHolder holder, int position, @NonNull Contacts model) {
                final String usersIDs = getRef(position).getKey();
                final String[] retImage = {"default_image"};

                usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.hasChild("image")) {
                                retImage[0] = snapshot.child("image").getValue().toString();
                                Picasso.get().load(retImage[0]).into(holder.profileImage);
                            }
                            final String retName = snapshot.child("name").getValue().toString();
                            final String retStatus = snapshot.child("status").getValue().toString();

                            holder.userName.setText(retName);
                            holder.userStatus.setText(retStatus);

                            if (snapshot.child("userState").hasChild("state")) {
                                String currentDatetime = snapshot.child("userState").child("currentDatetime").getValue().toString();
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                if (state.equals("online")) {
                                    holder.userStatus.setText("online");
                                } else if (state.equals("offline")) {
                                    holder.userStatus.setText("Last seen: " + currentDatetime);
                                }
                            } else {
                                holder.userStatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(v -> {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("visit_user_id", usersIDs);
                                chatIntent.putExtra("visit_user_name", retName);
                                chatIntent.putExtra("visit_image", retImage[0]);
                                startActivity(chatIntent);
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                return new ChatsViewHolder(view);
            }
        };
        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profileImage;
        TextView userStatus, userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
        }
    }
}
