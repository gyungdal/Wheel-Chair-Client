package com.example.android.bluetoothchat.community;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by GyungDal on 2017-01-30.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.example.android.bluetoothchat.utils.SingleMemory;


public class getWheel extends AsyncTask<Void, Void, String> {
    private String serverUrl = "http://gayangcodezero.iptime.org:8080/getMyWheel.php?phone=";
    private String phone;

    public getWheel(String phone){
        this.phone = phone;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try{
            String text = getText(serverUrl + phone);
            if(text.equals("[]"))
                return "";
            JSONObject json = new JSONArray(text).getJSONObject(0);
            SingleMemory.getInstance().setData("user_name", json.getString("user_name"));
            SingleMemory.getInstance().setData("company_name", json.getString("company_name"));
            SingleMemory.getInstance().setData("address", json.getString("device_address"));
            return json.getString("device_address");
        }catch(Exception e){
            Log.e("AuthManager", e.getMessage());
        }
        return "";
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
