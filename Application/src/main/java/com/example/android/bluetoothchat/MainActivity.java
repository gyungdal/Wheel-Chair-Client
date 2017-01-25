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
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.android.bluetoothchat.fragment.BluetoothChatFragment;
import com.example.android.bluetoothchat.fragment.SosFragment;
import com.example.android.bluetoothchat.fragment.StatusFragment;
import com.example.android.common.logger.Log;
import com.example.android.weater.GpsInfo;
import com.example.android.weater.ParsingWeatherInfo;

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

    public static final String TAG = "MainActivity";
    private final String[] Permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission_group.STORAGE,
            Manifest.permission_group.CONTACTS,
            Manifest.permission_group.SMS,
            Manifest.permission_group.CAMERA};
    // Whether the Log Fragment is currently shown
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ActionBar bar = getActionBar();
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
        for(int i = 0;i<Permissions.length;i++)
            requestPermission(i, Permissions[i]);

    }


    private void requestPermission(int i, String permission){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    permission)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{permission}, i);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        return super.onPrepareOptionsMenu(menu);
    }

    private void testGPS(){
        GpsInfo gps = new GpsInfo(MainActivity.this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            Log.i("GPS", "위도: " + latitude + ", 경도: " + longitude);
            ParsingWeatherInfo weather =
                    new ParsingWeatherInfo(String.valueOf(latitude), String.valueOf(longitude));
            String[] info = {""};
            try {
                info = weather.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            /*Toast.makeText(
                    getApplicationContext(),
                    "당신의 위치 - \n위도: " + latitude + "\n경도: " + longitude,
                    Toast.LENGTH_LONG).show();*/
            Toast.makeText(MainActivity.this, "온도 : " + info[0] +
                    "\n습도 : " + info[1] + "\n풍속 : " + info[2], Toast.LENGTH_SHORT).show();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}
