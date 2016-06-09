package com.manoj.dlt.utils;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public abstract class FirebaseChildAddedListener implements ChildEventListener
{
    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) { }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

    @Override
    public void onCancelled(DatabaseError databaseError) { }
}
