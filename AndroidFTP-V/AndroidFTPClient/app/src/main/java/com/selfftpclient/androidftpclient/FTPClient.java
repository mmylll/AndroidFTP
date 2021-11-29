package com.selfftpclient.androidftpclient;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class FTPClient {
    Socket ctrlSocket;
    Socket dataSocket;
    InputStream inputStream;
    OutputStream outputStream;
    Message message;
    Handler handler;
    public PrintWriter ctrlOutput;// 控制输出流
    public BufferedReader ctrlInput;// 控制输入流
    private ServerSocket serverDataSocket;
    private String dir;
    private String ip;
    private String remoteDir = "/storage/emulated/0/Android/data/com.example.androidftpserver/files/Server";

    public FTPClient(Handler handler) {
        this.handler = handler;
    }

    public FTPClient(){

    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setRemoteDir(String remoteDir){
        this.remoteDir = remoteDir;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public BufferedReader getCtrlInput() {
        return ctrlInput;
    }

    public PrintWriter getCtrlOutput() {
        return ctrlOutput;
    }

    public String getRemoteDir() {
        return remoteDir;
    }

    public void connftp(String ip, int port) throws IOException {
        this.ip = ip;
        ctrlSocket = new Socket(ip, port);
        inputStream = ctrlSocket.getInputStream();
        outputStream = ctrlSocket.getOutputStream();
        ctrlOutput = new PrintWriter(outputStream);
        ctrlInput = new BufferedReader(new InputStreamReader(inputStream));
        message = new Message();
        message.what = 3;//连接成功
        handler.sendMessage(message);
        accept();
    }

    private void accept() throws IOException {
        while (true) {
            byte[] byte1 = new byte[1024];
            int i = inputStream.read(byte1);
            byte[] byte2 = new byte[i];
            System.arraycopy(byte1, 0, byte2, 0, i);
            String s = new String(byte2, "UTF-8");
            message = new Message();
            message.what = 1;//接收
            message.obj = s;
            handler.sendMessage(message);
            if(s.contains("Passive Mode")){
//                int opening = s.indexOf('(');
//                int closing = s.indexOf(')', opening + 1);
//                if (closing > 0) {
//                    String dataLink = s.substring(opening + 1, closing);
//                    StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
//                    try {
//                        String passHost = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
//                                + tokenizer.nextToken() + "." + tokenizer.nextToken();
//                        int passPort = Integer.parseInt(tokenizer.nextToken()) * 256
//                                + Integer.parseInt(tokenizer.nextToken());
//                        dataSocket = new Socket(passHost,passPort);
//
//                        System.out.println("dataSocket链接成功");
//                    } catch (Exception e) {
//                        throw new IOException(
//                                "FTPClient received bad data link information: "
//                                        + s);
//                    }
//                }
                dataConnectionPASV(s);
//                dataConnectionPort();
            }
        }
    }

    public void send(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = s.getBytes();
                    outputStream.flush();
                    outputStream.write(bytes);
                    message = new Message();
                    message.what = 2;//发送
                    message.obj = s;
                    handler.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public Socket dataConnectionPort() {
        String cmd = "PORT ";
        int i;
        try {
            System.out.println( InetAddress.getLocalHost().getAddress()+"----------------------------");
            byte[] address = InetAddress.getLocalHost().getAddress();
// 用适当的端口号构造服务器
            serverDataSocket = new ServerSocket(0,1);
// 准备传送PORT命令用的数据
            for (i = 0; i < 4; ++i)
                cmd = cmd + (address[i] & 0xff) + ",";
            cmd = cmd + (((serverDataSocket.getLocalPort()) / 256) & 0xff) + "," + (serverDataSocket.getLocalPort() & 0xff);
            send(cmd);
// 接受与服务器的连接
            while(true) {
                dataSocket = serverDataSocket.accept();
                System.out.println(dataSocket.isConnected());
                break;
            }

            serverDataSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        return dataSocket;
    }

    public Socket dataConnectionPASV(String s) throws IOException{
        int opening = s.indexOf('(');
        int closing = s.indexOf(')', opening + 1);
        if (closing > 0) {
            String dataLink = s.substring(opening + 1, closing);
            StringTokenizer tokenizer = new StringTokenizer(dataLink, ",");
            try {
                String passHost = tokenizer.nextToken() + "." + tokenizer.nextToken() + "."
                        + tokenizer.nextToken() + "." + tokenizer.nextToken();
                int passPort = Integer.parseInt(tokenizer.nextToken()) * 256
                        + Integer.parseInt(tokenizer.nextToken());
                dataSocket = new Socket(passHost,passPort);

                System.out.println("dataSocket链接成功");
            } catch (Exception e) {
                throw new IOException("FTPClient received bad data link information: "+ s);
            }
        }
        return dataSocket;
    }

    public void doRETR(String filename) {
        String fileName = filename;
        String loafile=filename;
        try {
            int n;
            byte[] buff = new byte[1024];
// 在客户端上准备接收用的文件
            File local=new File(dir + "/" + loafile);
            System.out.println(local.getPath()+"---------------------");
            FileOutputStream outfile = new FileOutputStream(local);
// 构造传输文件用的数据流
            send("RETR " + remoteDir + "/" + fileName);
            BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
// 接收来自服务器的数据，写入本地文件
            while ((n = dataInput.read(buff)) > 0) {
                outfile.write(buff, 0, n);
            }
            dataSocket.close();
            outfile.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
//STOR命令
// 向服务器发送文件
    public void doSTOR(String filename) {
        String fileName = filename;
        try {
            int n;
            byte[] buff = new byte[1024];
            FileInputStream sendfile = null;
            try {

                sendfile = new FileInputStream(dir + "/" + fileName);
            } catch (Exception e) {
                System.out.println("文件不存在");
                return;
            }
            System.out.println("远程文件");
            String lonfile=filename;
            send("STOR " + remoteDir + "/" + lonfile);
            OutputStream outstr = dataSocket.getOutputStream();
            while ((n = sendfile.read(buff)) > 0) {
                outstr.write(buff, 0, n);
            }
            outstr.flush();
            dataSocket.close();
            sendfile.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void disConnect(){
        try{
            ctrlSocket.close();
            dataSocket.close();
            inputStream.close();
            outputStream.close();
            ctrlInput.close();
            ctrlOutput.close();
            send("QUIT");
        }catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }
}