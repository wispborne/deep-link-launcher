package com.manoj.dlt.interfaces;

public interface IFileSystem
{
    public void write(String key, String value);

    public String read(String key);

    public void clear(String key);

    public void clearAll();
}
