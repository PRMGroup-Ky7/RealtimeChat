package com.app.realtimechat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.adapters.MessageAdapter;
import com.app.realtimechat.entities.Messages;
import com.app.realtimechat.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private final List<Messages> messagesList = new ArrayList<>();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/YYYY - hh:mm a");
    private final LocalDateTime now = LocalDateTime.now();
    private String checker = "";
    private String myUrl = "";
    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private ImageButton sendMessageButton, sendFileButton;
    private EditText messageInputText;
    private LinearLayoutManager mLinearLayoutManager;
    private MessageAdapter mMessageAdapter;
    private RecyclerView userMessagesList;
    private Uri fileUri;
    private StorageTask uploadTask;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        intializeControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(v -> sendMessage());

        displayLastSeen();

        sendFileButton.setOnClickListener(v -> {
            CharSequence[] options = new CharSequence[]{
                    "Image", "PDF File", "MS Word Files"
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
            builder.setTitle("Select the File");
            builder.setItems(options, (dialog, i) -> {
                switch (i) {
                    case 0: {
                        checker = "image";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select image"), 438);
                        break;
                    }
                    case 1: {
                        checker = "pdf";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/pdf");
                        startActivityForResult(Intent.createChooser(intent, "Select PDF file"), 438);
                        break;
                    }
                    case 2: {
                        checker = "docx";
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        intent.setType("application/msword");
                        startActivityForResult(Intent.createChooser(intent, "Select MS Word file"), 438);
                        break;
                    }
                }
            });
            builder.show();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        rootRef.child(Constants.CHILD_MESSAGES).child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        mMessageAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void intializeControllers() {
        chatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageButton = findViewById(R.id.send_message_btn);
        sendFileButton = findViewById(R.id.send_files_btn);
        messageInputText = findViewById(R.id.input_message);

        mMessageAdapter = new MessageAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list_of_users);

        mLinearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(mLinearLayoutManager);
        userMessagesList.setAdapter(mMessageAdapter);
        loadingBar = new ProgressDialog(this);
    }

    private void sendMessage() {
        String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Please type a message.", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("currentDatetime", dtf.format(now));

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ChatActivity.this, "Message Sent.", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().getMessage();
                    Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
                messageInputText.setText("");

            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait, uploading your image.");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri = data.getData();
            if (!checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + checker);

                uploadTask = filePath.putFile(fileUri);
                uploadTask.addOnProgressListener((OnProgressListener<UploadTask.TaskSnapshot>) snapshot -> {
                    double progress = (snapshot.getBytesTransferred() / snapshot.getTotalByteCount()) * 100;
                    loadingBar.setMessage((int) progress + "% uploading...");
                }).continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", downloadUri.toString());
                        messageTextBody.put("name", fileUri.getLastPathSegment());
                        messageTextBody.put("type", checker);
                        messageTextBody.put("from", messageSenderID);
                        messageTextBody.put("to", messageReceiverID);
                        messageTextBody.put("messageID", messagePushID);
                        messageTextBody.put("currentDatetime", dtf.format(now));

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                        rootRef.updateChildren(messageBodyDetails);
                        loadingBar.dismiss();
                    }
                });
            } else if (checker.equals("image")) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages")
                        .child(messageSenderID).child(messageReceiverID).push();

                final String messagePushID = userMessageKeyRef.getKey();
                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }).addOnCompleteListener((OnCompleteListener<Uri>) task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        myUrl = downloadUri.toString();
                        Map messageTextBody = new HashMap();
                        messageTextBody.put("message", myUrl);
                        messageTextBody.put("name", fileUri.getLastPathSegment());
                        messageTextBody.put("type", checker);
                        messageTextBody.put("from", messageSenderID);
                        messageTextBody.put("to", messageReceiverID);
                        messageTextBody.put("messageID", messagePushID);
                        messageTextBody.put("currentDatetime", dtf.format(now));

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                loadingBar.dismiss();
                                Toast.makeText(ChatActivity.this, "Message Sent.", Toast.LENGTH_SHORT).show();
                            } else {
                                loadingBar.dismiss();
                                String message = task1.getException().getMessage();
                                Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            }
                            messageInputText.setText("");
                        });

                    }
                });
            } else {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLastSeen() {
        rootRef.child(Constants.CHILD_USERS).child(messageReceiverID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child("userState").hasChild("state")) {
                            String currentDatetime = dataSnapshot.child("userState").child("currentDatetime").getValue().toString();
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            } else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + currentDatetime);
                            }
                        } else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}

