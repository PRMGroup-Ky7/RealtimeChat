package com.app.realtimechat.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.R;
import com.app.realtimechat.entities.Messages;
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

public class MessagesAdapter extends FirebaseRecyclerAdapter<Messages, MessagesAdapter.MessageViewHolder> {

    private final FirebaseRecyclerOptions options;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(@NonNull FirebaseRecyclerOptions<Messages> options) {
        super(options);
        this.options = options;
    }

    @Override
    protected void onBindViewHolder(@NonNull MessagesAdapter.MessageViewHolder messageViewHolder, int position, @NonNull Messages model) {
        Messages messages = (Messages) options.getSnapshots().get(position);

        String messageSenderId = mAuth.getCurrentUser().getUid();

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("image")) {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).into(messageViewHolder.receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);


        switch (fromMessageType) {
            case "text":
                if (fromUserID.equals(messageSenderId)) {
                    messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

                    messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                    messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                    messageViewHolder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getCurrentDatetime());
                } else {
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                    messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                    messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                    messageViewHolder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getCurrentDatetime());
                }
                break;
            case "image":
                if (fromUserID.equals(messageSenderId)) {
                    messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderPicture);
                } else {
                    messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
                }
                break;
            default:
                if (fromUserID.equals(messageSenderId)) {
                    messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
                    messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);
                    messageViewHolder.itemView.setOnClickListener(v -> {
                        Uri url = Uri.parse(messages.getMessage());
                        Intent intent = new Intent(Intent.ACTION_VIEW, url);
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    });
                } else {
                    messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
                    messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                    Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverPicture);
                }
                break;
        }
    }

    @NonNull
    @Override
    public MessagesAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_messages_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture, messageReceiverPicture;


        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_messsage_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }
}
