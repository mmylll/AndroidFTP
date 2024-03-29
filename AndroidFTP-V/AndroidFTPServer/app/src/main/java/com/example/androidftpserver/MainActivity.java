package com.example.androidftpserver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;



public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_conn, bt_send;
    private EditText et_port, et_msg;
    private TextView tv_data, tv_ip;

    private String path;

    com.example.androidftpserver.FTPServer FTPServer;
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
                    Toast.makeText(MainActivity.this, "TCP连接成功！", Toast.LENGTH_SHORT).show();
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
        tv_ip.setText(GetLocalIP());
        FTPServer = new FTPServer(handler);
    }

    private void Clickbt() {
        bt_conn.setOnClickListener(this);
        bt_send.setOnClickListener(this);
    }

    private void initView() {
        bt_conn = findViewById(R.id.bt_conn);
        bt_send = findViewById(R.id.bt_send);
        bt_send.setEnabled(false);
        tv_ip = findViewById(R.id.tv_ip);
        et_port = findViewById(R.id.et_port);
        et_msg = findViewById(R.id.et_msg);
        tv_data = findViewById(R.id.tv_data);
        tv_data.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private String GetLocalIP() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        if (ipAddress == 0) return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
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
                                    } else {
                                        //这里就是权限打开之后自己要操作的逻辑
                                    }
                                }
                            }
                            String path = getExternalFilesDir("Server").getPath();
                            System.out.println(path + "----------------------------------");
                            FTPServer.setAbsolutePath(path);
                            FTPServer.connectftp(Integer.parseInt(et_port.getText().toString()));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case R.id.bt_send:
                FTPServer.send(et_msg.getText().toString());
                break;
        }
    }
}