package com.selfftpclient.androidftpclient;

import android.app.Application;

import com.selfftpclient.androidftpclient.FTPClient;

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
