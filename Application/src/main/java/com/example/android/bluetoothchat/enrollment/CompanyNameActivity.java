package com.example.android.bluetoothchat.enrollment;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.bluetoothchat.R;

/**
 * Created by GyungDal on 2017-01-30.
 */

public class CompanyNameActivity extends Activity{
    private EditText companyName;
    private Button nextButton, noCompanyButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // If the Android version is lower than Jellybean, use this call to hide
        // the status bar.
        final String adr = getIntent().getStringExtra("address");
        final String name = getIntent().getStringExtra("name");
        if(adr.isEmpty() | name.isEmpty()){
            Toast.makeText(getApplicationContext(), "인증 실패", Toast.LENGTH_SHORT).show();
            CompanyNameActivity.this.finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
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
        setContentView(R.layout.activity_new_company);
        nextButton = (Button)findViewById(R.id.next_button);
        companyName = (EditText)findViewById(R.id.company_name);
        noCompanyButton = (Button)findViewById(R.id.none_company_button);
        noCompanyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
                intent.putExtra("user_name", name);
                intent.putExtra("address", adr);
                intent.putExtra("company_name", "기관 없음ㅌ");
                startActivity(intent);
                CompanyNameActivity.this.finish();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(companyName.getText().toString().trim().isEmpty()){
                    Toast.makeText(getApplicationContext(), "기관명을 입력해주세요", Toast.LENGTH_SHORT).show();
                }else{
                    Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
                    intent.putExtra("user_name", name);
                    intent.putExtra("address", adr);
                    intent.putExtra("company_name", companyName.getText().toString());
                    startActivity(intent);
                    CompanyNameActivity.this.finish();
                }
            }
        });

    }

}
