package com.app.realtimechat.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realtimechat.R;
import com.app.realtimechat.adapters.ChatsAdapter;
import com.app.realtimechat.entities.Contacts;
import com.app.realtimechat.utils.Constants;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatsView;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference contactRef;
    private RecyclerView chatsList;
    private ChatsAdapter adapter;

    public ChatsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        contactRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_CONTACTS).child(currentUserID);
        chatsList = privateChatsView.findViewById(R.id.chats_list);
        adapter = new ChatsAdapter(new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef, Contacts.class)
                .build(), privateChatsView.getContext());
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsList.setAdapter(adapter);
        return privateChatsView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onStart() {
        super.onStart();
        chatsList.getRecycledViewPool().clear();
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

}
