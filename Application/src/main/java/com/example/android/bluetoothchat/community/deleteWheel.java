package com.example.android.bluetoothchat.community;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static java.lang.Boolean.FALSE;

/**
 * Created by GyungSik on 2017-02-01.
 */

public class deleteWheel extends AsyncTask<Void, Void, Boolean> {
    private String serverUrl = "http://gayangcodezero.iptime.org:8080/deleteWheel.php?phone=";
    private String phone, address;

    public deleteWheel(String phone, String address){
        this.phone = phone;
        this.address = address;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        try{
            Log.i("delete wheel", serverUrl + phone + "&address=" + address);
            String text = getText(serverUrl + phone + "&address=" + address);
            Log.i("delete response", text);
            if(text.isEmpty())
                return FALSE;
            return new JSONObject(text).getString("result").equals("ok");
        }catch(Exception e){
            Log.e("AuthManager", e.getMessage());
        }
        return FALSE;
    }


    private String getText(String connUrl) throws IOException {
        String fullString = "";
        URL url = new URL(connUrl);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            fullString += line;
        }
        reader.close();
        return fullString;
    }
}
