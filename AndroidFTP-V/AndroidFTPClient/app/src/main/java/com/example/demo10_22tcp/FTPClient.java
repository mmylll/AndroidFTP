package com.example.demo10_22tcp;

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
    public PrintWriter ctrlOutput;// 控制输出用的流
    public BufferedReader ctrlInput;// 控制输入用的流
    private ServerSocket serverDataSocket;
    private String dir = "/data/FTPClient";

    public FTPClient(Handler handler) {
        this.handler = handler;
    }

    public FTPClient(){

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

    public void conntcp(String ip, int port) throws IOException {
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
//            byte[] byte1 = new byte[1024];
//            int i = inputStream.read(byte1);
//            byte[] byte2 = new byte[i];
//            System.arraycopy(byte1, 0, byte2, 0, i);
//            String s = new String(byte2, "UTF-8");
            String s = ctrlInput.readLine();
            message = new Message();
            message.what = 1;//接收
            message.obj = s;
            handler.sendMessage(message);
            if(s.contains("Passive Mode")){
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
                        throw new IOException(
                                "FTPClient received bad data link information: "
                                        + s);
                    }
                }

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
        String cmd = "PORT "; // PORT存放用PORT命令传递数据的变量
        int i;
        Socket dataSocket = null;// 传送数据用Socket
        try {
// 得到自己的地址
            byte[] address = InetAddress.getLocalHost().getAddress();
// 用适当的端口号构造服务器
            serverDataSocket = new ServerSocket(0,1);
// 准备传送PORT命令用的数据
            for (i = 0; i < 4; ++i)
                cmd = cmd + (address[i] & 0xff) + ",";
            cmd = cmd + (((serverDataSocket.getLocalPort()) / 256) & 0xff) + "," + (serverDataSocket.getLocalPort() & 0xff);
// 利用控制用的流传送PORT命令
//            ctrlOutput.println(cmd);
//            ctrlOutput.flush();
            send(cmd);
// 向服务器发送处理对象命令(LIST,RETR,及STOR)
//            ctrlOutput.println(ctrlcmd);
//            ctrlOutput.flush();
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

    public void doGet() {
        String fileName = "";
        String loafile="";
        BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));
        try {
            int n;
            byte[] buff = new byte[1024];
// 指定服务器上的文件名
            System.out.println("远程文件名");
            fileName = lineread.readLine();
// 在客户端上准备接收用的文件
            System.out.println("本地文件");
            loafile=lineread.readLine();
            File local=new File(loafile);
            FileOutputStream outfile = new FileOutputStream(local);
//// 构造传输文件用的数据流
//            Socket dataSocket = dataConnectionPort("RETR " + fileName);
            send("RETR " + fileName);
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
// doPut方法
// 向服务器发送文件

    public void doPut() {
        String fileName = "";
        BufferedReader lineread = new BufferedReader(new InputStreamReader(System.in));
        try {
            int n;
            byte[] buff = new byte[1024];
            FileInputStream sendfile = null;
// 指定文件名
            System.out.println("本地文件");
            fileName = lineread.readLine();
// 准备读出客户端上的文件
//BufferedInputStream dataInput = new BufferedInputStream(new FileInputStream(fileName));
            try {

                sendfile = new FileInputStream(fileName);
            } catch (Exception e) {
                System.out.println("文件不存在");
                return;
            }
            System.out.println("远程文件");
            String lonfile=lineread.readLine();
// 准备发送数据的流
//            Socket dataSocket = dataConnectionPort("STOR " + lonfile);
            send("STOR " + lonfile);
            OutputStream outstr = dataSocket.getOutputStream();
            while ((n = sendfile.read(buff)) > 0) {
                outstr.write(buff, 0, n);
            }
            dataSocket.close();
            sendfile.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
