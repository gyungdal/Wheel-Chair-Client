package com.example.android.bluetoothchat.enrollment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.community.newWheel;

import java.util.concurrent.ExecutionException;

/**
 * Created by GyungDal on 2017-01-30.
 */

public class FinishActivity extends Activity {
    private Button end;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        String userName = getIntent().getStringExtra("user_name");
        String address = getIntent().getStringExtra("address");
        String companyName = getIntent().getStringExtra("company_name");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ColorDrawable colorDrawable = new ColorDrawable();
            colorDrawable.setColor(0xff01a032);
            final ActionBar bar = getActionBar();
            bar.setBackgroundDrawable(colorDrawable);
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(0xff01a032);
        }
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else{
            getActionBar().hide();
        }
        if(getPhoneNumber().isEmpty()){
            Toast.makeText(getApplicationContext(), "개통되지 않은 단말은 사용이 불가능 합니다."
                , Toast.LENGTH_SHORT).show();
            finish();
        }
        try {
            if(!new newWheel(getPhoneNumber(), companyName, userName, address).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get())
                finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_new_finish);
        end = (Button)findViewById(R.id.end_button);
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                FinishActivity.this.finish();
            }
        });
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
}
