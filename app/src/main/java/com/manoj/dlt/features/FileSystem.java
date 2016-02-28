package com.manoj.dlt.features;

import android.content.Context;
import android.content.SharedPreferences;
import com.manoj.dlt.Constants;
import com.manoj.dlt.interfaces.IFileSystem;

import java.util.ArrayList;
import java.util.List;

public class FileSystem implements IFileSystem
{

    private SharedPreferences _preferences;
    private SharedPreferences.Editor _editor;

    public FileSystem(Context context,String key)
    {
        _preferences = context.getSharedPreferences(key,Context.MODE_PRIVATE);
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

    @Override
    public List<String> keyList()
    {
        return new ArrayList<String>(_preferences.getAll().keySet());
    }

    @Override
    public List<String> values()
    {
        List<String> values = new ArrayList<String>();
        for(String key: keyList())
        {
            String value = read(key);
            values.add(value);
        }
        return values;
    }
}
