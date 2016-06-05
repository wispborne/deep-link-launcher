package com.manoj.dlt.services;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.manoj.dlt.utils.Utilities;

import java.util.Map;

public class FCMMessagingService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification() != null) {
            Log.d("FCM", "fcm messsage noti body = " + remoteMessage.getNotification().getBody());
        }
        Log.d("FCM","fcm messsage data printing ");
        for(Map.Entry<String,String> entry: remoteMessage.getData().entrySet())
        {
            Log.d("FCM","key = "+entry.getKey()+" , value = "+entry.getValue());
        }
        String deepLink = remoteMessage.getData().get("deep_link");
        Utilities.checkAndFireDeepLink(deepLink, getApplicationContext());
    }
}
