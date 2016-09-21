package com.manoj.dlt.features;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.manoj.dlt.Constants;
import com.manoj.dlt.DbConstants;
import com.manoj.dlt.interfaces.IProfileFeature;
import com.manoj.dlt.utils.Utilities;

import java.util.UUID;

public class ProfileFeature implements IProfileFeature
{
    private static ProfileFeature _instance;
    private FileSystem _fileSystem;
    private String _userId;

    private ProfileFeature(Context context)
    {
        _fileSystem = Utilities.getOneTimeStore(context);
        _userId = _fileSystem.read(Constants.USER_ID_KEY);
        if(_userId == null)
        {
            _userId = generateUserId();
            _fileSystem.write(Constants.USER_ID_KEY, _userId);
        }
        Log.d("profile","user id = "+_userId);
    }

    public static ProfileFeature getInstance(Context context)
    {
        if(_instance == null)
        {
            _instance = new ProfileFeature(context);
        }
        return _instance;
    }

    @Override
    public String getUserId()
    {
        return _userId;
    }

    @Override
    public DatabaseReference getCurrentUserFirebaseBaseRef()
    {
        DatabaseReference baseUserRef = Constants.getFirebaseUserRef();
        return baseUserRef.child(_userId);
    }

    private String generateUserId()
    {
        //TODO: better implementation
        String rand = UUID.randomUUID().toString();
        return rand.substring(0,5);
    }
}
