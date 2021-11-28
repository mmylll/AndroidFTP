package com.example.demo10_22tcp;

import android.app.Application;

public class MyDataApp extends Application {

    private FTPClient ftpClient;

    @Override
    public void onCreate()
    {
        ftpClient = new FTPClient();
        super.onCreate();
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }
}
