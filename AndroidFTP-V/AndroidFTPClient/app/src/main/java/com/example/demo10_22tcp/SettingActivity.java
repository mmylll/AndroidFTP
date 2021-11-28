package com.example.demo10_22tcp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    FTPClient FTPClient;
    public PrintWriter ctrlOutput;// 控制输出用的流
    public BufferedReader ctrlInput;// 控制输入用的流


    private Button bt_user, bt_pass,bt_mode,bt_type,bt_stru,bt_pattern;
    private EditText et_username, et_pass;
    private Spinner sp_mode,sp_type,sp_stru,sp_pattern;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initView();
        Clickbt();
        MyDataApp appState = ((MyDataApp)getApplicationContext());
        FTPClient = appState.getFtpClient();


    }

    private void Clickbt() {
        bt_user.setOnClickListener(this);
        bt_pass.setOnClickListener(this);
        bt_mode.setOnClickListener(this);
        bt_type.setOnClickListener(this);
        bt_stru.setOnClickListener(this);
        bt_pattern.setOnClickListener(this);
    }

    private void initView() {
        bt_user = findViewById(R.id.btUser);
        bt_pass = findViewById(R.id.btPass);
        bt_mode = findViewById(R.id.btMode);
        bt_type = findViewById(R.id.btType);
        bt_stru = findViewById(R.id.btStru);
        bt_pattern = findViewById(R.id.btPattern);
        et_username = findViewById(R.id.editUsername);
        et_pass = findViewById(R.id.editPassword);
        sp_mode = findViewById(R.id.spMode);
        sp_type = findViewById(R.id.spType);
        sp_stru = findViewById(R.id.spStru);
        sp_pattern = findViewById(R.id.spPattern);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btUser:

        }
    }
}