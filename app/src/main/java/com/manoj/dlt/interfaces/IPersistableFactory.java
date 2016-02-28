package com.manoj.dlt.interfaces;

public interface IPersistableFactory<T extends IPersistable>
{
    public T fromJson(String jsonString);
}

