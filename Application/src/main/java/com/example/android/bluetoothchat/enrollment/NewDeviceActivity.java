package com.example.android.bluetoothchat.enrollment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.auth.AuthManager;
import com.example.android.bluetoothchat.auth.QrCodeReader;
import com.example.android.common.logger.Log;

import java.util.concurrent.ExecutionException;

import static java.security.AccessController.getContext;

/**
 * Created by GyungDal on 2017-01-30.
 */

public class NewDeviceActivity extends Activity{
    private final int REQUEST_QRCODE_READ = 0;
    private ImageButton qrcodeButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorDrawable colorDrawable = new ColorDrawable();
            colorDrawable.setColor(0xff01a032);
            final ActionBar bar = getActionBar();
            bar.setBackgroundDrawable(colorDrawable);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xff01a032);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
            getActionBar().hide();
        }
        setContentView(R.layout.activity_new_devices);
        qrcodeButton = (ImageButton) findViewById(R.id.qrButton);
        qrcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qrcode();
            }
        });
    }
    private void qrcode(){
        try {
            Intent intent = new Intent(getApplicationContext(), QrCodeReader.class);
            startActivityForResult(intent, 0);
        } catch (Exception e) {
            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    @SuppressLint("HardwareIds")
    private String getPhoneNumber(){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                getSystemService(Context.TELEPHONY_SERVICE);
        if(mTelephonyMgr.getLine1Number().isEmpty() | mTelephonyMgr.getLine1Number().trim().isEmpty()){
            return "";
        }else
            return mTelephonyMgr.getLine1Number();
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_QRCODE_READ: {
                if (data != null) {
                    String contents = data.getStringExtra("QR_CODE");
                    Log.i("QRCODE RESULT", contents);
                    //위의 contents 값에 scan result가 들어온다.
                    if (getPhoneNumber().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "개통되지 않은 단말입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AuthManager auth = new AuthManager(getPhoneNumber(), contents);
                    boolean canUse = true;
                    /*try {
                        canUse = auth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                        canUse = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }*/
                    if (!canUse) {
                        Toast.makeText(getApplicationContext(), "이미 등록된 기기거나 네트워크 상황이 좋지 못합니다.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), contents, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), UserNameActivity.class);
                        intent.putExtra("address", contents);
                        startActivity(intent);
                        NewDeviceActivity.this.finish();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "FAIL...", Toast.LENGTH_SHORT).show();
                }
                break;
            }

        }
    }
}
