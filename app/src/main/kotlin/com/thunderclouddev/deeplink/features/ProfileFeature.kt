package com.thunderclouddev.deeplink.features

import android.content.Context
import android.util.Log
import com.thunderclouddev.deeplink.Constants
import com.thunderclouddev.deeplink.interfaces.IProfileFeature
import com.thunderclouddev.deeplink.utils.Utilities
import java.util.*

class ProfileFeature private constructor(context: Context) : IProfileFeature {
    private val fileSystem = Utilities.getOneTimeStore(context)

    override var userId = fileSystem.read(Constants.USER_ID_KEY) ?: generateUserIdAndSave()

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
