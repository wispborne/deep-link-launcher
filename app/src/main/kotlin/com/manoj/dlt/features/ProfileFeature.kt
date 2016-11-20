package com.manoj.dlt.features

import android.content.Context
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.manoj.dlt.Constants
import com.manoj.dlt.interfaces.IProfileFeature
import com.manoj.dlt.utils.Utilities
import java.util.*

class ProfileFeature private constructor(context: Context) : IProfileFeature {
    private val fileSystem = Utilities.getOneTimeStore(context)

    override var userId = fileSystem.read(Constants.USER_ID_KEY) ?: generateUserIdAndSave()

    override val currentUserFirebaseBaseRef: DatabaseReference
        get() = Constants.firebaseUserRef.child(userId)

    private fun generateUserIdAndSave(): String {
        val generatedUserId = generateUserId()
        fileSystem.write(Constants.USER_ID_KEY, generatedUserId)
        Log.d("profile", "user id = " + generatedUserId)
        return generatedUserId
    }

    private fun generateUserId(): String {
        //TODO: better implementation
        val rand = UUID.randomUUID().toString()
        return rand.substring(0, 5)
    }

    companion object {
        private var instance: ProfileFeature? = null

        fun getInstance(context: Context): ProfileFeature {
            if (instance == null) {
                instance = ProfileFeature(context)
            }
            return instance as ProfileFeature
        }
    }
}
