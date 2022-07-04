package com.app.realtimechat.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.realtimechat.GroupChatActivity;
import com.app.realtimechat.R;
import com.app.realtimechat.utils.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {

    private final ArrayList<String> groups = new ArrayList<>();
    private View groupFragmentView;
    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private DatabaseReference groupRef, rootRef;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    public GroupsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);
        groupRef = FirebaseDatabase.getInstance().getReference().child(Constants.CHILD_GROUPS);
        rootRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        initializeFields();
        retrieveAndDisplayGroups();
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String currentGroupName = parent.getItemAtPosition(position).toString();
            Intent groupChatIntent = new Intent(getContext(), GroupChatActivity.class);
            groupChatIntent.putExtra("groupName", currentGroupName);
            startActivity(groupChatIntent);
        });
        return groupFragmentView;
    }

    private void initializeFields() {
        listView = groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, groups);
        listView.setAdapter(arrayAdapter);
    }


    private void retrieveAndDisplayGroups() {
        rootRef.child(Constants.CHILD_USERS)
                .child(currentUser.getUid())
                .child("groups")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Set<String> set = new HashSet<>();
                            Iterator getGroups = snapshot.getChildren().iterator();
                            while (getGroups.hasNext()) {
                                DataSnapshot ds = (DataSnapshot) getGroups.next();
                                String name = ds.getKey();
                                boolean isGroup = Boolean.parseBoolean(String.valueOf(ds.getValue()));
                                if (isGroup) {
                                    set.add(name);
                                }
                            }
                            groups.clear();
                            groups.addAll(set);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
