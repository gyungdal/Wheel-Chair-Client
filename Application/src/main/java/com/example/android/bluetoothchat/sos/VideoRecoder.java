package com.example.android.bluetoothchat.sos;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by GyungDal on 2017-01-21.
 */

public class VideoRecoder {
    private String videoPath =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "sos.mp4";
    private final String photoPath =
            Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator + "sos.png";

    public String getVideoPath(){
        return this.videoPath;
    }

    public String getPhotoPath(){
        return this.photoPath;
    }

    public Uri getVideoUri() throws IOException {
        File file = new File(videoPath);
        if(file.isFile())
            file.delete();
        if(!file.canWrite())
            file.createNewFile();

        Uri output = Uri.fromFile(file);
        return output;
    }

    public void saveThumbnail() {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            Bitmap output = retriever.getFrameAtTime(1000000);
            FileOutputStream fos = new FileOutputStream(photoPath);
            output.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        }catch(Exception e){
            Log.e("MP4 -> BMP", e.getMessage());
        }
    }

}
