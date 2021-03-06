package com.example.android.bluetoothchat.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothchat.MainActivity;
import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.community.deleteWheel;
import com.example.android.bluetoothchat.utils.SingleMemory;
import com.example.android.common.logger.Log;
import com.example.android.weater.GpsInfo;
import com.example.android.weater.ParsingWeatherInfo;

import org.w3c.dom.Text;

import java.util.concurrent.ExecutionException;

/**
 * Created by GyungDal on 2017-01-25.
 */

public class StatusFragment extends Fragment {
    private TextView humi, temp;
    private Button reEnroll;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        humi = (TextView)view.findViewById(R.id.humiText);
        temp = (TextView) view.findViewById(R.id.tempText);
        reEnroll = (Button)view.findViewById(R.id.re_enroll_device_button);
        reEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.myDialog));
                builder.setCancelable(false);
                builder.setMessage("기기를 재등록 하시겠습니까?");
                builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deleteWheel deleteWheel = new deleteWheel(SingleMemory.getInstance().getData("phone"),
                                SingleMemory.getInstance().getData("address"));
                        try {
                            if (!deleteWheel.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get())
                                Toast.makeText(getContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
                            ((Activity)getContext()).finish();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.show();
            }
        });
        Pair<String, String> data = getWeather();
        if(data != null){
            temp.setText(data.first);
            humi.setText(data.second);
        }
    }


    private Pair<String, String> getWeather(){
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
            if(info[0].contains("."))
                info[0] = info[0].substring(0, info[0].indexOf('.'));
            return new Pair<String, String>(info[0] + "℃", info[1] + "%");
            /*Toast.makeText(getContext(), "온도 : " + info[0] +
                    "\n습도 : " + info[1] + "\n풍속 : " + info[2], Toast.LENGTH_SHORT).show();*/
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
        return null;
    }
}
