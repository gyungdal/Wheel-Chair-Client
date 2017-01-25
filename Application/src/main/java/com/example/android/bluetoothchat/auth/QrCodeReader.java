package com.example.android.bluetoothchat.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.zxing.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by GyungDal on 2017-01-12.
 */
public

class QrCodeReader extends Activity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    private final String TAG = "Qrcode";
    private final String regex = "^([0-9a-fA-f][0-9a-fA-f]:){5}([0-9a-fA-f][0-9a-fA-f])$";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(rawResult.getText());
        if(matcher.find()) {
            //Toast.makeText(getApplicationContext(), rawResult.getText(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.putExtra("QR_CODE", rawResult.getText());
            setResult(0, intent);
            finish();
        }else{
            Toast.makeText(getApplicationContext(), "NOT FOUND", Toast.LENGTH_SHORT).show();
        }
        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }
}