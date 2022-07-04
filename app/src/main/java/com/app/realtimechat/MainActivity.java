package com.app.realtimechat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.app.realtimechat.adapters.TabsAccessorAdapter;
import com.app.realtimechat.utils.ActivityUtil;
import com.app.realtimechat.utils.Constants;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityUtil aUtil;
    private FloatingActionButton[] fabArray;
    private TextView[] tvArray;
    private boolean isClicked = false;
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference rootRef, userRef;
    private FloatingActionButton fabMain, fabFriend, fabCreateGroup, fabSetting, fabLogout;
    private Animation rotateOpen, rotateClose, fromBottom, toBottom;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();
        aUtil = new ActivityUtil(this);


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Welcome");

        myViewPager = findViewById(R.id.main_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        initFab();

        fabMain.setOnClickListener(v -> handleFabMain());

        fabFriend.setOnClickListener(v -> {
            aUtil.switchActivity(MainActivity.this, FindFriendsActivity.class);
        });

        fabCreateGroup.setOnClickListener(v -> requestNewGroup());

        fabSetting.setOnClickListener(v -> {
            aUtil.switchActivity(MainActivity.this, SettingsActivity.class);
        });

        fabLogout.setOnClickListener(v -> {
            updateUserStatus("offline");
            mAuth.signOut();
            aUtil.switchActivityWithFlag(MainActivity.this, LoginActivity.class);
        });
    }

    private void handleFabMain() {
        for (FloatingActionButton fab : fabArray) {
            for (TextView textView : tvArray) {
                if (!isClicked) {
                    fab.setVisibility(View.VISIBLE);
                    fab.startAnimation(fromBottom);
                    textView.startAnimation(fromBottom);
                    fab.setClickable(true);
                    fabMain.setImageDrawable(getDrawable(R.drawable.ic_xmark_solid));
                    fabMain.startAnimation(rotateOpen);
                } else {
                    fab.setVisibility(View.INVISIBLE);
                    fab.startAnimation(toBottom);
                    textView.startAnimation(toBottom);
                    fab.setClickable(false);
                    fabMain.setImageDrawable(getDrawable(R.drawable.ic_bars_solid));
                    fabMain.startAnimation(rotateClose);
                }
            }
        }
        isClicked = !isClicked;
    }

    private void initFab() {
        fabMain = findViewById(R.id.fab_main);
        fabFriend = findViewById(R.id.fab_find_friend);
        fabSetting = findViewById(R.id.fab_settings);
        fabCreateGroup = findViewById(R.id.fab_crate_group);
        fabLogout = findViewById(R.id.fab_logout);

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);

        fabArray = new FloatingActionButton[]{
                fabFriend, fabCreateGroup, fabSetting, fabLogout
        };

        tvArray = new TextView[]{
                findViewById(R.id.tv_friend),
                findViewById(R.id.tv_group),
                findViewById(R.id.tv_setting),
                findViewById(R.id.tv_logout)
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser == null) {
            aUtil.switchActivityWithFlag(this, LoginActivity.class);
        } else {
            verifyUserExist();
        }
        updateUserStatus("online");
    }

    private void verifyUserExist() {
        String currentUserID = mAuth.getCurrentUser().getUid();
        rootRef.child(Constants.CHILD_USERS)
                .child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!(dataSnapshot.child("name").exists())) {
                            aUtil.switchActivityWithFlag(MainActivity.this, SettingsActivity.class);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g PRM Group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String groupName = groupNameField.getText().toString();
            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(MainActivity.this, "Please write Group Name..", Toast.LENGTH_SHORT).show();
            } else {
                createNewGroup(groupName);
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, which) -> dialogInterface.cancel());

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        Map owner = new HashMap();
        owner.put("owner", currentUser.getUid());

        Map members = new HashMap();
        members.put(currentUser.getUid(), true);
        owner.put("members", members);

        rootRef.child(Constants.CHILD_GROUPS)
                .child(groupName)
                .setValue(owner)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, groupName + " is created successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error occurred while creating group.", Toast.LENGTH_SHORT).show();
                    }
                });

        Map groups = new HashMap();
        groups.put(groupName, true);
        rootRef.child(Constants.CHILD_USERS)
                .child(currentUser.getUid())
                .child("groups")
                .updateChildren(groups)
                .addOnCompleteListener(task -> {
                    Toast.makeText(this, "Updated children", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    private void updateUserStatus(String state) {
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("currentDatetime", Constants.getCurrentDatetime());
        onlineStateMap.put("state", state);

        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            rootRef.child(Constants.CHILD_USERS)
                    .child(currentUser.getUid())
                    .child("userState")
                    .updateChildren(onlineStateMap);
        }
    }

}
