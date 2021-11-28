package com.example.androidftpclient;

import android.app.Activity;
import android.app.Application;

import com.example.androidftpclient.ui.login.LoginActivity;

import java.util.HashMap;
import java.util.Map;

public class MyDataApp extends Application {

    private FTPClient ftpClient;

    @Override
    public void onCreate()
    {
        ftpClient = new FTPClient("a");
        super.onCreate();
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }
}
