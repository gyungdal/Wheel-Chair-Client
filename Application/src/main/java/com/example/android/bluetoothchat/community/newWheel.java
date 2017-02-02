package com.example.android.bluetoothchat.community;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static java.lang.Boolean.TRUE;
import static java.lang.Boolean.FALSE;

/**
 * Created by GyungSik on 2017-02-01.
 */

public class newWheel extends AsyncTask<Void, Void, Boolean> {
    private final String serverUrl = "http://gayangcodezero.iptime.org:8080/newWheel.php?";
    private String phone, companyName, userName, address;
    public newWheel(String phone, String companyName, String userName, String address){
        this.phone = phone;
        this.companyName = companyName;
        this.userName = userName;
        this.address = address;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try{
            String url = serverUrl + "phone=" + phone +
                    "&company_name=" + companyName +
                    "&user_name=" + userName +
                    "&device_address=" + address;
            Log.i("new wheel", url);
            String text = getText(url);
            if(text.isEmpty() | text.equals("[]"))
                return FALSE;
            return new JSONObject(text).getString("result").equals("ok");
        }catch(Exception e){
            Log.e("new Wheel", e.getMessage());
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
