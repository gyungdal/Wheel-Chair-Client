package com.example.android.bluetoothchat.community;

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


public class getWheel extends AsyncTask<Void, Void, String> {
    private String phone;

    public getWheel(String phone){
        this.phone = phone;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try{
            JSONObject json = new JSONObject(getStringFromUrl("http://gyungdal.iptime.org/test.php?phone="
                    + this.phone));
            String status = json.getString("status");
            if(status.equals("OK"))
                return "";
            else
                return "";
        }catch(Exception e){
            Log.e("AuthManager", e.getMessage());
        }
        return "";
    }


    // getStringFromUrl : 주어진 URL의 문서의 내용을 문자열로 반환
    public String getStringFromUrl(String pUrl){

        BufferedReader bufreader=null;
        HttpURLConnection urlConnection = null;

        StringBuffer page=new StringBuffer(); //읽어온 데이터를 저장할 StringBuffer객체 생성

        try {

            //[Type1]
            /*
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(pUrl));
            InputStream contentStream = response.getEntity().getContent();
            */

            //[Type2]
            URL url= new URL(pUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream contentStream = urlConnection.getInputStream();

            bufreader = new BufferedReader(new InputStreamReader(contentStream,"UTF-8"));
            String line = null;

            //버퍼의 웹문서 소스를 줄단위로 읽어(line), Page에 저장함
            while((line = bufreader.readLine())!=null){
                Log.d("line:",line);
                page.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            //자원해제
            try {
                bufreader.close();
                urlConnection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Log.i("get User Data", page.toString());
        return page.toString();
    }
}
