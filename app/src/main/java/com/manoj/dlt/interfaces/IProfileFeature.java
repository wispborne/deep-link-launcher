package com.manoj.dlt.interfaces;

import com.google.firebase.database.DatabaseReference;

public interface IProfileFeature
{
    public String getUserId();

    public DatabaseReference getCurrentUserFirebaseBaseRef();
}
