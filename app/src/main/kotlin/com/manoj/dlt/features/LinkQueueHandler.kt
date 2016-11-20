package com.manoj.dlt.features

import android.content.Context
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.manoj.dlt.Constants
import com.manoj.dlt.DbConstants
import com.manoj.dlt.utils.FirebaseChildAddedListener
import com.manoj.dlt.utils.Utilities

class LinkQueueHandler private constructor(private val context: Context) {
    private var isProcessing: Boolean = false
    private val queueListener: ChildEventListener
    private val queueReference: DatabaseReference

    init {
        isProcessing = false
        queueReference = ProfileFeature.getInstance(context).currentUserFirebaseBaseRef.child(DbConstants.LINK_QUEUE)
        queueListener = createQueueListener()
    }

    fun runQueueListener() {
        if (isProcessing) {
            //Already attached listener on queue. do nothing
            return
        } else if (Constants.isFirebaseAvailable(context)) {
            queueReference.addChildEventListener(queueListener)
            isProcessing = true
        }
    }

    fun stopQueueListener() {
        if (isProcessing) {
            queueReference.removeEventListener(queueListener)
            isProcessing = false
        }
    }

    private fun createQueueListener() = object : FirebaseChildAddedListener() {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String) {
            val qId = dataSnapshot.key
            val deepLink = dataSnapshot.value.toString()
            Utilities.checkAndFireDeepLink(deepLink, context)
            Utilities.logLinkViaWeb(deepLink, ProfileFeature.getInstance(context).userId, context)
            queueReference.child(qId).setValue(null)
        }
    }

    companion object {
        private var queueHandler: LinkQueueHandler? = null

        fun getInstance(context: Context): LinkQueueHandler {
            if (queueHandler == null) {
                queueHandler = LinkQueueHandler(context)
            }

            return queueHandler as LinkQueueHandler
        }
    }
}
