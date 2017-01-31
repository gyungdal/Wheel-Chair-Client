package com.example.android.bluetoothchat.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.sos.VideoRecoder;
import com.example.android.bluetoothchat.utils.DBManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by GyungDal on 2017-01-25.
 */

public class SosFragment extends Fragment {
    private final int REQUEST_VIDEO_RECORD  = 1;
    private ImageButton sosButton;
    private VideoRecoder videoRecoder;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sos, container, false);
    }

    private void sendMessage(String number, String path) {
        File file = new File(path);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.addCategory("android.intent.category.DEFAULT");
        sendIntent.putExtra("address", number);
        sendIntent.putExtra("exit_on_sent", true);
        sendIntent.putExtra("subject", "TEST MMS");
        sendIntent.putExtra("sms_body", "MMS 테스트입니다.");
        sendIntent.putExtra(Intent.EXTRA_STREAM, file.toURI());
        sendIntent.setType("image/png");
        startActivity(sendIntent);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_VIDEO_RECORD: {
                videoRecoder.saveThumbnail();
                ArrayList<HashMap<String, String>> lists = new DBManager(getContext()).selectNumber();
                ArrayList<String> list = new ArrayList<>();
                for(HashMap<String, String> info : lists){
                    list.add(info.get("item2"));
                }
                for(String number : list){
                    sendMessage(number, videoRecoder.getPhotoPath());
                }
                break;
            }
        }
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        sosButton = (ImageButton)view.findViewById(R.id.sos_button);
        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoRecoder = new VideoRecoder();
                try {
                    Uri uri = videoRecoder.getVideoUri();
                    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent, REQUEST_VIDEO_RECORD);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}

