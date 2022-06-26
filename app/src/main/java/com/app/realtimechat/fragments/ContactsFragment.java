package com.app.realtimechat.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.R;
import com.app.realtimechat.entities.Contacts;
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
public class ContactsFragment extends Fragment {

    private View contactsView;
    private RecyclerView contactsRecyclerView;

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth fireAuth;
    private String currentUserId;

    public ContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsRecyclerView = contactsView.findViewById(R.id.contacts_list);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fireAuth = FirebaseAuth.getInstance();
        currentUserId = fireAuth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        return contactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options = new
                FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter =
            new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model) {
                    String userId = getRef(position).getKey();
                    usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if( snapshot.child("userState").hasChild("state")) {
                                String state = snapshot.child("userState").child("state").getValue().toString();

                                if(state.equals("online")) {
                                    holder.onlineIconView.setVisibility(View.VISIBLE);
                                } else if (state.equals("offline")) {
                                    holder.onlineIconView.setVisibility(View.INVISIBLE);
                                }
                            } else {
                                holder.onlineIconView.setVisibility(View.INVISIBLE);
                            }

                            if (snapshot.exists()) {
                                if(snapshot.hasChild("image")) {
                                    String userImage = snapshot.child("image").getValue().toString();
                                    String username = snapshot.child("name").getValue().toString();
                                    String userStatus = snapshot.child("status").getValue().toString();

                                    holder.usernameView.setText(username);
                                    holder.userStatusView.setText(userStatus);
                                    Picasso.get().load(userImage).into(holder.profileImageView);
                                } else {
                                    String username = snapshot.child("name").getValue().toString();
                                    String userStatus = snapshot.child("status").getValue().toString();

                                    holder.usernameView.setText(username);
                                    holder.userStatusView.setText(userStatus);
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
                public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                    ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view);
                    return contactsViewHolder;
                }
            };

        contactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImageView;
        TextView usernameView, userStatusView;
        ImageView onlineIconView;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.users_profile_image);
            usernameView = itemView.findViewById(R.id.user_profile_name);
            userStatusView = itemView.findViewById(R.id.user_status);
            onlineIconView = itemView.findViewById(R.id.user_online_status);
        }
    }
}
