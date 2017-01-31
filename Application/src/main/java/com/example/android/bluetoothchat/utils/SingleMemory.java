package com.example.android.bluetoothchat.utils;

import java.util.HashMap;

/**
 * Created by GyungDal on 2017-01-26.
 */

public class SingleMemory {
    private static SingleMemory memory;
    private HashMap<String, String> data;

    public static SingleMemory getInstance(){
        if(memory == null){
            memory = new SingleMemory();
        }
        return memory;
    }

    public String getData(String key){
        try {
            return data.get(key);
        }catch(NullPointerException e){
            return null;
        }
    }

    public void setData(String key, String value){
        data.put(key, value);
    }
}
