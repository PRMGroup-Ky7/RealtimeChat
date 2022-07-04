package com.app.realtimechat;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.entities.Contacts;
import com.app.realtimechat.entities.Messages;
import com.app.realtimechat.utils.Constants;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter;

    private Button btnInvite;
    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private RecyclerView listContactOnline;
    private Dialog dialog;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupRef, groupMessageKeyRef, contactsRef, requestRef;
    private String currentGroupName, currentUserID, currentUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_USERS);
        groupRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_GROUPS).child(currentGroupName);
        contactsRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_CONTACTS).child(currentUserID);
        requestRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_REQUEST);

        initializeFields();
        getUserInfo();

        sendMessageButton.setOnClickListener(v -> {
            sendMessage();
            userMessageInput.setText("");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });

        btnInvite.setOnClickListener(v -> {
            showInviteContactModal();
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getContactListener();
        getMessageFromGroupListener();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void handleSendRequestJoinGroup(String inviteUserID) {
        Map members = new HashMap();
        members.put(inviteUserID, false);
        groupRef.child("members")
                .updateChildren(members)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Invited", Toast.LENGTH_SHORT).show();
                    }
                });
        Map groups = new HashMap();
        groups.put(currentGroupName, false);
        usersRef.child(inviteUserID)
                .child("groups")
                .updateChildren(groups)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(GroupChatActivity.this, "Updated User", Toast.LENGTH_SHORT).show();
                    }
                });

        Map request = new HashMap();
        request.put("currentGroupName", currentGroupName);
        request.put("requestType", "request_sent_invite_group");
        requestRef
                .child(currentUserID)
                .child(inviteUserID)
                .setValue(request)
                .addOnCompleteListener(task -> {
                    request.put("requestType", "request_receive_invite_group");
                    if (task.isSuccessful()) {
                        requestRef
                                .child(inviteUserID)
                                .child(currentUserID)
                                .setValue(request)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(this, "Sent invite group", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
    }

    private void getContactListener() {
        FirebaseRecyclerOptions options = new
                FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Contacts, GroupChatActivity.ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final GroupChatActivity.ContactsViewHolder holder, int position, @NonNull Contacts model) {
                String userId = getRef(position).getKey();
                usersRef.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (snapshot.child("groups").child(currentGroupName).exists()) {
                                String userImage = snapshot.child("image").getValue().toString();
                                String username = snapshot.child("name").getValue().toString();
                                holder.contactName.setText(username);
                                Picasso.get().load(userImage).into(holder.profileImageView);

                                DataSnapshot ds = snapshot.child("groups").child(currentGroupName);
                                boolean joined = Boolean.parseBoolean(String.valueOf(ds.getValue()));
                                holder.btnInvite.setVisibility(View.GONE);
                                holder.tvRequest.setVisibility(View.VISIBLE);
                                if (joined) {
                                    holder.tvRequest.setText("ADDED");
                                } else {
                                    holder.btnInvite.setText("SENT");
                                }
                            } else {
                                if (snapshot.hasChild("image")) {
                                    String userImage = snapshot.child("image").getValue().toString();
                                    String username = snapshot.child("name").getValue().toString();
                                    holder.contactName.setText(username);
                                    Picasso.get().load(userImage).into(holder.profileImageView);
                                    holder.btnInvite.setOnClickListener(v -> {
                                        handleSendRequestJoinGroup(userId);
                                        holder.btnInvite.setVisibility(View.GONE);
                                        holder.tvRequest.setVisibility(View.VISIBLE);
                                        holder.tvRequest.setText("SENT");
                                    });
                                }
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
            public GroupChatActivity.ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_user_item_dialog, parent, false);
                GroupChatActivity.ContactsViewHolder contactsViewHolder = new GroupChatActivity.ContactsViewHolder(view);
                return contactsViewHolder;
            }
        };
        listContactOnline.setAdapter(adapter);
        adapter.startListening();
    }

    private void showInviteContactModal() {
        Button btnClose = dialog.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void getMessageFromGroupListener() {
        groupRef
                .child("Messages")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            displayMessages(snapshot);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        if (snapshot.exists()) {
                            displayMessages(snapshot);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void initializeFields() {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        btnInvite = findViewById(R.id.btn_invite);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_scroll_view);

        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.custom_invite_dialog);

        listContactOnline = dialog.findViewById(R.id.rcv_online_contact);
        listContactOnline.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please write a message", Toast.LENGTH_SHORT).show();
        } else {
            groupMessageKeyRef = groupRef.child("Messages").child(messageKey);
            Messages messages = new Messages(Constants.getCurrentDatetime(), message, currentUserName, "text");
            Map messageInfoMap = messages.getGroupMessage();
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void displayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext()) {
            String currentDatetime = ((DataSnapshot) iterator.next()).getValue().toString();
            String message = ((DataSnapshot) iterator.next()).getValue().toString();
            String name = ((DataSnapshot) iterator.next()).getValue().toString();
            String type = ((DataSnapshot) iterator.next()).getValue().toString();
            displayTextMessages.append(name + " sent : " + message + "\n" + currentDatetime + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImageView;
        TextView contactName, tvRequest;
        Button btnInvite;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.civ_contact_profile);
            contactName = itemView.findViewById(R.id.tv_name_contact);
            btnInvite = itemView.findViewById(R.id.btn_invite_contact);
            tvRequest = itemView.findViewById(R.id.tv_request);
        }
    }
}
