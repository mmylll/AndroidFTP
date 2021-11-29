package com.selfftpclient.androidftpclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_conn, bt_send;
    private EditText et_ip, et_port, et_msg;
    private TextView tv_data;
    private Button bt_user, bt_pass,bt_mode,bt_type,bt_stru,bt_pattern,bt_download,bt_upload,bt_disconn;
    private EditText et_username, et_pass,et_download,et_upload;
    private Spinner sp_mode,sp_type,sp_stru,sp_pattern;

    com.selfftpclient.androidftpclient.FTPClient FTPClient;
    Message message;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    tv_data.append("接收消息：" + msg.obj.toString() + "\n");
                    break;
                case 2:
                    tv_data.append("发送消息：" + msg.obj.toString() + "\n");
                    break;
                case 3:
                    Toast.makeText(MainActivity.this, "FTP连接成功！", Toast.LENGTH_SHORT).show();
                    bt_send.setEnabled(true);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Clickbt();
        FTPClient = new FTPClient(handler);
    }

    private void Clickbt() {
        bt_conn.setOnClickListener(this);
        bt_send.setOnClickListener(this);
        bt_user.setOnClickListener(this);
        bt_pass.setOnClickListener(this);
        bt_mode.setOnClickListener(this);
        bt_type.setOnClickListener(this);
        bt_stru.setOnClickListener(this);
        bt_pattern.setOnClickListener(this);
        bt_download.setOnClickListener(this);
        bt_upload.setOnClickListener(this);
        bt_disconn.setOnClickListener(this);
    }

    private void initView() {
        bt_conn = findViewById(R.id.bt_conn);
        bt_send = findViewById(R.id.bt_send);
        bt_send.setEnabled(false);
        et_ip = findViewById(R.id.et_ip);
        et_port = findViewById(R.id.et_port);
        et_msg = findViewById(R.id.et_msg);
        tv_data = findViewById(R.id.tv_data);
        tv_data.setMovementMethod(ScrollingMovementMethod.getInstance());
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
        bt_download = findViewById(R.id.btDownload);
        bt_upload = findViewById(R.id.btUpload);
        et_download = findViewById(R.id.editDownload);
        et_upload = findViewById(R.id.editUpload);
        bt_disconn = findViewById(R.id.bt_disconn);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_conn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (Build.VERSION.SDK_INT >= 23) {
                                int REQUEST_CODE_CONTACT = 101;
                                String[] permissions = {
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                //Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
                                //验证是否许可权限
                                for (String str : permissions) {
                                    if (MainActivity.this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                                        //申请权限
                                        MainActivity.this.requestPermissions(permissions, REQUEST_CODE_CONTACT);
                                        return;
                                    }
                                }
                            }
                            String path = getExternalFilesDir("Client").getPath();
                            System.out.println(path + "--------------------");
                            FTPClient.setDir(path);
                            FTPClient.connftp(et_ip.getText().toString(), Integer.parseInt(et_port.getText().toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.bt_send:
                FTPClient.send(et_msg.getText().toString());
                break;
            case R.id.btUser:
                FTPClient.send("USER "+et_username.getText().toString());
                break;
            case R.id.btPass:
                FTPClient.send("PASS " +et_pass.getText().toString());
                break;
            case R.id.btPattern:
                if(sp_pattern.getSelectedItem().toString().equals("PORT")){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            FTPClient.dataConnectionPort();
                        }
                    }).start();
                }else if(sp_pattern.getSelectedItem().toString().equals("PASV")){
                    FTPClient.send("PASV");
                }
                break;

            case R.id.btType:
                if(sp_type.getSelectedItem().toString().equals("Ascii")){
                    FTPClient.send("TYPE A");
                }else if(sp_type.getSelectedItem().toString().equals("Binary")){
                    FTPClient.send("TYPE B");
                }
                break;
            case R.id.btDownload:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FTPClient.doRETR(et_download.getText().toString());
                    }
                }).start();
                break;

            case R.id.btUpload:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FTPClient.doSTOR(et_upload.getText().toString());
                    }
                }).start();
                break;

            case R.id.bt_disconn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        FTPClient.disConnect();
                    }
                }).start();
                break;
        }
    }
}