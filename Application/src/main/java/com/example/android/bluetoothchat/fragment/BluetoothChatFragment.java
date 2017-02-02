/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.bluetoothchat.fragment;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Camera;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothchat.BluetoothChatService;
import com.example.android.bluetoothchat.Constants;
import com.example.android.bluetoothchat.DeviceListActivity;
import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.auth.AuthManager;
import com.example.android.bluetoothchat.auth.QrCodeReader;
import com.example.android.bluetoothchat.sos.VideoRecoder;
import com.example.android.bluetoothchat.utils.DBManager;
import com.example.android.bluetoothchat.utils.Joystick;
import com.example.android.bluetoothchat.utils.SignalBuilder;
import com.example.android.bluetoothchat.utils.SingleMemory;
import com.example.android.common.logger.Log;
import com.example.android.weater.GpsInfo;
import com.example.android.weater.ParsingWeatherInfo;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class BluetoothChatFragment extends Fragment {
    long startTime, endTime, time;
    private Camera mCamera;
    private MediaRecorder mMediaRecorder;
    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private Class<?> mClss;
    private static final String TAG = "BluetoothChatFragment";
    private VideoRecoder videoRecoder;
    // Intent request codes
    private final int REQUEST_QRCODE_READ = 0;
    private final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private final int REQUEST_ENABLE_BT = 3;
    private final int REQUEST_SELECT_CONTACT = 4;
    private final int REQUEST_VIDEO_RECORD = 5;

    // Layout Views
    private Joystick joystick;
    private TextView endNotice, startNotice, timeView;
    private ImageView stampView;
    private Button endButton, startButton;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;


    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    private void setRemoteState(boolean state){
        if(state){
            timeView.setVisibility(View.VISIBLE);
            endButton.setVisibility(View.VISIBLE);
            endNotice.setVisibility(View.VISIBLE);
            joystick.setVisibility(View.VISIBLE);
            stampView.setVisibility(View.INVISIBLE);
            startButton.setVisibility(View.INVISIBLE);
            startNotice.setVisibility(View.INVISIBLE);
            startButton.setOnClickListener(null);
            endButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupChat();
                }
            });
        }else{
            timeView.setVisibility(View.INVISIBLE);
            endButton.setVisibility(View.INVISIBLE);
            endNotice.setVisibility(View.INVISIBLE);
            joystick.setVisibility(View.INVISIBLE);
            stampView.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.VISIBLE);
            startNotice.setVisibility(View.VISIBLE);
            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    // Otherwise, setup the chat session
                    } else if (mChatService == null) {
                        setupChat();
                    }else{
                        String address = SingleMemory.getInstance().getData("address");
                        if(address == null)
                            ((Activity)getContext()).finish();
                        else
                            connectDevice(address);
                    }
                }
            });
            endButton.setOnClickListener(null);
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        timeView = (TextView)view.findViewById(R.id.timeView);
        startButton = (Button)view.findViewById(R.id.startButton);
        startNotice = (TextView)view.findViewById(R.id.startNotice);
        stampView = (ImageView)view.findViewById(R.id.stampView);
        endButton = (Button)view.findViewById(R.id.endButton);
        endNotice = (TextView) view.findViewById(R.id.endNotice);
        joystick = (Joystick) view.findViewById(R.id.joystick);
        setRemoteState(false);
        joystick.setOnJoystickMoveListener(new Joystick.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
            byte[] motion = new byte[17];
            //init
            Arrays.fill(motion, (byte)0x00);
            if(power > 99)
                power = 99;
            byte[] times = SignalBuilder.shortToBytes((short) 1);

            android.util.Log.i(TAG, "Ang : " + angle + ", Pow : " + power);
            switch(direction) {
                case Joystick.BOTTOM: {
                    android.util.Log.i(TAG, "BOTTOM");
                    motion[0] = 2;
                    motion[1] = (byte) power;
                    motion[2] = times[0];
                    motion[3] = times[1];
                    break;
                }
                case Joystick.LEFT: {
                    android.util.Log.i(TAG, "LEFT");
                    motion[4] = 1;
                    motion[5] = (byte) power;
                    motion[6] = times[0];
                    motion[7] = times[1];
                    break;
                }
                case Joystick.FRONT: {
                    android.util.Log.i(TAG, "FRONT");
                    motion[0] = 1;
                    motion[1] = (byte) power;
                    motion[2] = times[0];
                    motion[3] = times[1];
                    break;
                }
                case Joystick.RIGHT: {
                    android.util.Log.i(TAG, "RIGHT");
                    motion[4] = 2;
                    motion[5] = (byte) power;
                    motion[6] = times[0];
                    motion[7] = times[1];
                    break;
                }
                case Joystick.BOTTOM_LEFT: {
                    android.util.Log.i(TAG, "BOTTOM LEFT");
                    motion[8] = 1;
                    motion[9] = (byte) power;
                    motion[11] = times[0];
                    motion[12] = times[1];
                    break;
                }
                case Joystick.FRONT_RIGHT:{
                    android.util.Log.i(TAG, "FRONT RIGHT");
                    motion[8] = 2;
                    motion[9] = (byte) power;
                    motion[11] = times[0];
                    motion[12] = times[1];
                    break;
                }
                case Joystick.LEFT_FRONT: {
                    android.util.Log.i(TAG, "FRONT LEFT");
                    motion[8] = 3;
                    motion[9] = (byte) power;
                    motion[11] = times[0];
                    motion[12] = times[1];
                    break;
                }
                case Joystick.RIGHT_BOTTOM: {
                    android.util.Log.i(TAG, "RIGHT BOTTOM");
                    motion[8] = 4;
                    motion[9] = (byte) power;
                    motion[11] = times[0];
                    motion[12] = times[1];
                    break;
                }
                default:
                    android.util.Log.i(TAG, "CENTER");
                    break;
            }
            byte[] signal = SignalBuilder.move(motion);
            sendMessage(signal);
            }
        }, Joystick.DEFAULT_LOOP_INTERVAL);
    }

    /**
     * Set up the UI and background operations for chat.
     */
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getActivity(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable.
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if(message[1] == 100)
            startTime = System.currentTimeMillis();

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            mChatService.write(message);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    setRemoteState(false);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            setRemoteState(true);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    switch(readBuf[1]){
                        //TODO : 코드가 작동을 하지 않을 경우에는 랜덤으로 500~1000사이값으로...
                        case 101 :
                            endTime = System.currentTimeMillis();
                            time = endTime - startTime;
                            Log.i("TIME","TIME : " + time + "ms");
                            timeView.setText("TIME : " + time + "ms");
                            break;
                    }
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_QRCODE_READ :{

                break;
            }
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_SELECT_CONTACT :
                Cursor cursor = getContext().getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                cursor.moveToFirst();
                String name = cursor.getString(0);        //0은 이름을 얻어옵니다.
                String number = cursor.getString(1);   //1은 번호를 받아옵니다.
                cursor.close();
                DBManager dbManager = new DBManager(getContext());
                Log.i("name", name);
                Log.i("number", number);
                Toast.makeText(getContext(), "name : " + name + "\nnumber : " + number,
                        Toast.LENGTH_SHORT).show();
                dbManager.insertNumber(name, number);
                break;
            case REQUEST_VIDEO_RECORD :{
                videoRecoder.saveThumbnail();
                break;
            }
            default:
                Log.i("REQEST CODE", String.valueOf(requestCode));
                break;


        }
    }

    /**
     * Establish connection with other divice
     *
     * @param data   An {@link Intent} with {@link DeviceListActivity#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }


    private void connectDevice(String address) {
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
            case R.id.gps :{
                testGPS();
                return true;
            }
            case R.id.qrcode :{
                return true;
            }
            case R.id.SOSTEST: {
                videoRecoder = new VideoRecoder();
                try {
                    Uri uri = videoRecoder.getVideoUri();
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_VIDEO_RECORD);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            case R.id.SOS :{
/*
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("SOS 전화부");
                final DBManager dbManager = new DBManager(getContext());
                String[] list;
                if(!dbManager.selectNumber().isEmpty()) {
                    ArrayList<String> numbers = dbManager.selectNumber();
                    list = new String[numbers.size()];
                    Log.i("LIST SIZE", String.valueOf(list.length));
                    for(int i = 0;i<numbers.size();i++)
                        list[i] = numbers.get(i);
                }else {
                    list = new String[1];
                    list[0] = "비어있음";
                }
                final int len = list.length;
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        getContext(),
                        android.R.layout.test_list_item);
                for(String temp : list)
                    adapter.add(temp);

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String strName = adapter.getItem(i);
                        AlertDialog.Builder innBuilder = new AlertDialog.Builder(
                                getContext());
                        innBuilder.setMessage(strName + "\n번호를 삭제하시겠습니까?");
                        innBuilder.setTitle("WARRING!");
                        innBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dbManager.deleteNumber(strName);
                                dialog.dismiss();
                            }
                        });
                        innBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        innBuilder.setNeutralButton("그 외", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                AlertDialog.Builder inn = new AlertDialog.Builder(
                                        getContext());
                                inn.setMessage(strName + " SOS SMS/CALL");
                                inn.setTitle("문자/전화");
                                inn.setPositiveButton("전화", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        Intent callIntent = new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+strName.trim()));
                                        startActivity(callIntent);
                                        dialog.dismiss();
                                    }
                                });
                                inn.setNegativeButton("문자", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri smsUri = Uri.parse("tel:" + strName.trim());
                                        Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
                                        intent.putExtra("address", strName.trim());
                                        intent.putExtra("sms_body", "TEST SEND MESSAGE!");
                                        intent.setType("vnd.android-dir/mms-sms");
                                        startActivity(intent);
                                        dialogInterface.dismiss();
                                    }
                                });
                                inn.show();
                            }
                        });
                        innBuilder.show();
                    }
                });
                builder.setNeutralButton("추가", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(len < 3){
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            startActivityForResult(intent, REQUEST_SELECT_CONTACT);
                        }else{
                            Toast.makeText(getContext(), "최대 3개 까지만 등록 가능합니다.", Toast.LENGTH_SHORT).show();
                        }
                        dialogInterface.dismiss();
                    }
                });
                builder.setPositiveButton("전송", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();*/
                return true;
            }
        }
        return false;
    }



    private void testGPS(){
        GpsInfo gps = new GpsInfo(getContext());
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
            Toast.makeText(getContext(), "온도 : " + info[0] +
                    "\n습도 : " + info[1] + "\n풍속 : " + info[2], Toast.LENGTH_SHORT).show();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
    }


}
