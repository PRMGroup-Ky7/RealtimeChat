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

public class MainActivity extends AppCompatActivity {

    private final ActivityUtil aUtil = new ActivityUtil(this);
    private boolean clicked = false;
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter mTabsAccessorAdapter;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference rootRef;
    private FloatingActionButton fabMain, fabFriend, fabCreateGroup, fabSetting, fabLogout;
    private Animation rotateOpen, rotateClose, fromBottom, toBottom;
    private TextView tvFriend, tvGroup, tvSetting, tvLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        String name = user.getDisplayName() != null ? user.getDisplayName() : "";
        getSupportActionBar().setTitle("Welcome " + name + " !");

        myViewPager = findViewById(R.id.main_tabs_pager);
        mTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

        initFab();

        fabMain.setOnClickListener(v -> {
            handleFabMain();
        });

        fabFriend.setOnClickListener(v -> {
            aUtil.switchActivity(MainActivity.this, FindFriendsActivity.class);
        });

        fabCreateGroup.setOnClickListener(v -> {
            requestNewGroup();
        });

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
        setVisibility(clicked);
        setAnimation(clicked);
        setClickable(clicked);
        clicked = !clicked;
    }

    private void setVisibility(boolean clicked) {
        if (!clicked) {
            fabFriend.setVisibility(View.VISIBLE);
            fabCreateGroup.setVisibility(View.VISIBLE);
            fabSetting.setVisibility(View.VISIBLE);
            fabLogout.setVisibility(View.VISIBLE);

            tvFriend.setVisibility(View.VISIBLE);
            tvGroup.setVisibility(View.VISIBLE);
            tvSetting.setVisibility(View.VISIBLE);
            tvLogout.setVisibility(View.VISIBLE);

        } else {
            fabFriend.setVisibility(View.INVISIBLE);
            fabCreateGroup.setVisibility(View.INVISIBLE);
            fabSetting.setVisibility(View.INVISIBLE);
            fabLogout.setVisibility(View.INVISIBLE);

            tvFriend.setVisibility(View.INVISIBLE);
            tvGroup.setVisibility(View.INVISIBLE);
            tvSetting.setVisibility(View.INVISIBLE);
            tvLogout.setVisibility(View.INVISIBLE);

        }
    }

    private void setAnimation(boolean clicked) {
        if (!clicked) {
            fabFriend.startAnimation(fromBottom);
            fabCreateGroup.startAnimation(fromBottom);
            fabSetting.startAnimation(fromBottom);
            fabLogout.startAnimation(fromBottom);

            tvFriend.startAnimation(fromBottom);
            tvGroup.startAnimation(fromBottom);
            tvSetting.startAnimation(fromBottom);
            tvLogout.startAnimation(fromBottom);

            fabMain.startAnimation(rotateOpen);

        } else {
            fabFriend.startAnimation(toBottom);
            fabCreateGroup.startAnimation(toBottom);
            fabSetting.startAnimation(toBottom);
            fabLogout.startAnimation(toBottom);

            tvFriend.startAnimation(toBottom);
            tvGroup.startAnimation(toBottom);
            tvSetting.startAnimation(toBottom);
            tvLogout.startAnimation(toBottom);

            fabMain.startAnimation(rotateClose);
        }
    }

    private void setClickable(boolean clicked) {
        if (!clicked) {
            fabFriend.setClickable(true);
            fabCreateGroup.setClickable(true);
            fabSetting.setClickable(true);
            fabLogout.setClickable(true);
        } else {
            fabFriend.setClickable(false);
            fabCreateGroup.setClickable(false);
            fabSetting.setClickable(false);
            fabLogout.setClickable(false);
        }
    }


    private void initFab() {
        tvFriend = findViewById(R.id.tv_friend);
        tvSetting = findViewById(R.id.tv_setting);
        tvGroup = findViewById(R.id.tv_group);
        tvLogout = findViewById(R.id.tv_logout);

        fabMain = findViewById(R.id.fab_main);
        fabFriend = findViewById(R.id.fab_find_friend);
        fabSetting = findViewById(R.id.fab_settings);
        fabCreateGroup = findViewById(R.id.fab_crate_group);
        fabLogout = findViewById(R.id.fab_logout);

        rotateOpen = AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim);
        rotateClose = AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim);
        fromBottom = AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim);
        toBottom = AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
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
        rootRef.child(Constants.CHILD_GROUPS)
                .child(groupName)
                .setValue("")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, groupName + " is created successfully.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error occurred while creating group.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (user != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (user != null) {
            updateUserStatus("offline");
        }
    }

    private void updateUserStatus(String state) {
        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("currentDatetime", Constants.getCurrentDatetime());
        onlineStateMap.put("state", state);

        user = mAuth.getCurrentUser();
        if (user != null) {
            rootRef.child(Constants.CHILD_USERS)
                    .child(user.getUid())
                    .child("userState")
                    .updateChildren(onlineStateMap);
        }
    }

}
