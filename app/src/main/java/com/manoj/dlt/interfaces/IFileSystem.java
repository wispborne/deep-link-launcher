package com.manoj.dlt.interfaces;

import java.util.List;

public interface IFileSystem
{
    public void write(String key, String value);

    public String read(String key);

    public void clear(String key);

    public void clearAll();

    public List<String> keyList();

    public List<String> values();
}
