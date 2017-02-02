package com.example.android.bluetoothchat.community;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static java.lang.Boolean.FALSE;

/**
 * Created by GyungSik on 2017-02-01.
 */

public class sendMessage extends AsyncTask<Void, Void, Boolean> {
    private final String serverUrl = "http://gayangcodezero.iptime.org:8080/writeMessage.php?";
    private String phone, companyName, userName, message;

    public sendMessage(String phone, String companyName, String userName, String message) {
        this.phone = phone;
        this.companyName = companyName;
        this.userName = userName;
        this.message = message;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            String url = serverUrl + "phone=" + phone +
                    "&company_name=" + companyName +
                    "&user_name=" + userName +
                    "&message=" + message;
            Log.i("message_url", url);
            String text = getText(url);
            Log.i("message result", text);
            if (text.isEmpty() | text.equals("[]"))
                return FALSE;
            return new JSONObject(text).getString("result").equals("ok");
        } catch (Exception e) {
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