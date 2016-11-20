package com.manoj.dlt.interfaces

import com.google.firebase.database.DatabaseReference

interface IProfileFeature {
    val userId: String

    val currentUserFirebaseBaseRef: DatabaseReference
}
