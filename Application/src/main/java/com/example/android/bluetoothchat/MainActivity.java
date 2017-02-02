/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.example.android.bluetoothchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.android.bluetoothchat.community.deleteWheel;
import com.example.android.bluetoothchat.community.getWheel;
import com.example.android.bluetoothchat.enrollment.NewDeviceActivity;
import com.example.android.bluetoothchat.enrollment.UserNameActivity;
import com.example.android.bluetoothchat.fragment.BluetoothChatFragment;
import com.example.android.bluetoothchat.fragment.SosFragment;
import com.example.android.bluetoothchat.fragment.SosManagerFragment;
import com.example.android.bluetoothchat.fragment.StatusFragment;
import com.example.android.bluetoothchat.utils.SingleMemory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
@TargetApi(Build.VERSION_CODES.M)
public class MainActivity extends FragmentActivity {
    int PERMISSION_ALL = 1;
    public static final String TAG = "MainActivity";
    private final String[] Permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA};
    // Whether the Log Fragment is currently shown
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!hasPermissions(MainActivity.this, Permissions)){
            Log.i("Permission", "Not permission");
            startActivity(new Intent(MainActivity.this, PermissionRequestActivity.class));
            MainActivity.this.finish();
        }else {
            if (getPhoneNumber().isEmpty()) {
                Toast.makeText(getApplicationContext(), "개통되지 않은 단말입니다!", Toast.LENGTH_SHORT).show();
                finish();
            }
            SingleMemory.getInstance().setData("phone", getPhoneNumber());
            getWheel wheel = new getWheel(getPhoneNumber());
            try {
                String address = wheel.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
                if (address.trim().isEmpty()) {
                    startActivity(new Intent(MainActivity.this, NewDeviceActivity.class));
                    MainActivity.this.finish();
                }
                SingleMemory.getInstance().setData("phone", getPhoneNumber());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            ColorDrawable colorDrawable = new ColorDrawable();
            colorDrawable.setColor(0xff01a032);
            final ActionBar bar = getActionBar();
            bar.setBackgroundDrawable(colorDrawable);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(0xff01a032);
            }
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
            bar.addTab(bar.newTab().setText("상태").setTabListener(new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    Fragment fragment = new StatusFragment();
                    transaction.replace(R.id.sample_content_fragment, fragment);
                    transaction.commit();
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }
            }));
            bar.addTab(bar.newTab().setText("원격 조종").setTabListener(new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    Fragment fragment = new BluetoothChatFragment();
                    transaction.replace(R.id.sample_content_fragment, fragment);
                    transaction.commit();
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }
            }), 1, false);
            bar.addTab(bar.newTab().setText("SOS").setTabListener(new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    Fragment fragment = new SosFragment();
                    transaction.replace(R.id.sample_content_fragment, fragment);
                    transaction.commit();
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }
            }), 2, false);
            bar.addTab(bar.newTab().setText("SOS 관리").setTabListener(new ActionBar.TabListener() {
                @Override
                public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    Fragment fragment = new SosManagerFragment();
                    transaction.replace(R.id.sample_content_fragment, fragment);
                    transaction.commit();
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction ft) {

                }
            }), 3, false);
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
    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
