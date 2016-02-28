package com.manoj.dlt.features;

import android.content.Context;
import android.content.SharedPreferences;
import com.manoj.dlt.Constants;
import com.manoj.dlt.interfaces.IFileSystem;

public class FileSystem implements IFileSystem
{

    SharedPreferences _preferences;
    SharedPreferences.Editor _editor;

    public FileSystem(Context context)
    {
        _preferences = context.getSharedPreferences(Constants.FILE_SYSTEM_KEY,Context.MODE_PRIVATE);
        _editor = _preferences.edit();
    }

    @Override
    public void write(String key, String value)
    {
        _editor.putString(key,value);
        _editor.commit();
    }

    @Override
    public String read(String key)
    {
        return _preferences.getString(key, null);
    }

    @Override
    public void clear(String key)
    {
        _editor.remove(key);
        _editor.commit();
    }

    @Override
    public void clearAll()
    {
        _editor.clear();
        _editor.commit();
    }
}
