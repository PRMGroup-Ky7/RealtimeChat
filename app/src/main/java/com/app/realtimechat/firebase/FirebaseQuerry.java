package com.app.realtimechat.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseQuerry {

    private final DatabaseReference rootRef;

    public FirebaseQuerry(DatabaseReference rootRef) {
        this.rootRef = rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();
    }
}
