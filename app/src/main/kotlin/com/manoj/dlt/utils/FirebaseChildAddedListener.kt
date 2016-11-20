package com.manoj.dlt.utils

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

abstract class FirebaseChildAddedListener : ChildEventListener {
    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String) {
    }

    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
    }

    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String) {
    }

    override fun onCancelled(databaseError: DatabaseError) {
    }
}
