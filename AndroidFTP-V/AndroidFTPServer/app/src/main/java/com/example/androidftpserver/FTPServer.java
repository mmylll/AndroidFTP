package com.example.androidftpserver;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.logging.Logger;

public class FTPServer {
    private Socket ctrlSocket;
    private ServerSocket serverSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    Message message;
    Handler handler;

    private Socket dataSocket;
    private Logger logger;//日志对象
    private String dir = null;//绝对路径
    private String pdir = null;//相对路径
    private final static Random generator = new Random();//随机数
    BufferedReader br;
    PrintWriter pw;
    String clientIp = null;//记录客户端IP
    String username = "not logged in";//用户名
    String password = "";
    boolean loginStuts = false;
    boolean isRunning = false;
    final String LOGIN_WARNING = "530 Please log in with USER and PASS first.";
    String str = "";//命令内容字符串
    int port_high = 0;
    int port_low = 0;
    String remoteIp = "";//接收文件的IP地址
    private String remotePath = "/storage/emulated/0/Android/data/com.selfftpclient.androidftpclient/files/Client";


    public FTPServer(Handler handler) {
        this.handler = handler;
    }

    public void setDir(String path){
        this.dir = path;
        this.pdir = path;
    }

    public void connectftp(int port) throws IOException {
        logger = Logger.getLogger("com");
        serverSocket = new ServerSocket(port);
        logger.info("准备连接");
        while (true){
            ctrlSocket = serverSocket.accept();
            if (ctrlSocket != null){
                System.out.println("连接成功");
                clientIp = ctrlSocket.getInetAddress().toString().substring(1);
                inputStream = ctrlSocket.getInputStream();
                outputStream = ctrlSocket.getOutputStream();
                br = new BufferedReader(new InputStreamReader(inputStream));
                pw = new PrintWriter(outputStream);
                message = new Message();
                message.what = 3;//连接成功
                handler.sendMessage(message);
                accept();
            }
        }
    }

    private void accept() throws IOException {
        isRunning = true;
        while (isRunning) {
            byte[] byte1 = new byte[1024];
            int a = inputStream.read(byte1);
            byte[] byte2 = new byte[a];
            System.arraycopy(byte1, 0, byte2, 0, a);
            String command = new String(byte2, "UTF-8");
            message = new Message();
            message.what = 1;//接收
            message.obj = command;
            handler.sendMessage(message);
// USER命令
            if(command.toUpperCase().startsWith("USER")){
//                logger.info("(not logged in) ("+clientIp+")> "+command);
//                username = command.substring(4).trim();
//                if("".equals(username)){
//                    pw.println("501 Syntax error"); pw.flush();
//                    logger.info("(not logged in) ("+clientIp+")> 501 Syntax error");
//                    username = "not logged in";
//                } else{
//                    pw.println("331 Password required for " + username);
//                    pw.flush();
//                    logger.info("(not logged in) ("+clientIp+")> 331 Password required for " + username);
//                }
//                loginStuts = false;
                doUSER(command);
            }
// PASS命令
            else if(command.toUpperCase().startsWith("PASS")){
//                logger.info("(not logged in) ("+clientIp+")> "+command);
//                password = command.substring(4).trim();
//                if(username.equals("root") && password.equals("root")){
//                    pw.println("230 Logged on"); pw.flush();
//                    logger.info("("+username+") ("+clientIp+")> 230 Logged on");
//                    logger.info("客户端 "+clientIp+" 通过 "+username+"用户登录");
//                    loginStuts = true;
//                } else{
//                    pw.println("530 Login or password incorrect!");
//                    pw.flush();
//                    logger.info("(not logged in) ("+clientIp+")> 530 Login or password incorrect!");
//                    username = "not logged in";
//                }
                doPASS(command);
            }
// PWD命令
            else if(command.toUpperCase().startsWith("PWD")){
//                File folder = new File("/data");
//                send("("+folder.isDirectory()+folder.isFile()+" "+folder.exists()+")");
//                System.out.println("("+folder.isDirectory()+folder.isFile()+" "+folder.exists()+")"+"-------------------");
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    logger.info("用户"+clientIp+"："+username+"执行PWD命令");
////                    pw.println("257 "+pdir+" is current directory");
////                    pw.flush();
//                    send("257 "+pdir+" is current directory");
//                    logger.info("("+username+") ("+clientIp+")> 257 "+pdir+" is current directory");
//                } else{
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doPWD(command);
            }
//// CWD命令
            else if(command.toUpperCase().startsWith("CWD")){
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    str = command.substring(3).trim();
//                    if("".equals(str)){
//                        pw.println("250 Broken client detected, missing argument to CWD. "+pdir+" is current directory.");
//                        pw.flush();
//                        logger.info("("+username+") ("+clientIp+")> 250 Broken client detected, missing argument to CWD. "+pdir+" is current directory.");
//                    }
//                    else{
////判断目录是否存在
//                        String tmpDir = dir + "/" + str;
//                        File file = new File(tmpDir);
//                        if(file.exists()){//目录存在
//                            dir = dir + "/" + str;
//                            if("/".equals(pdir)){
//                                pdir = pdir + str;
//                            } else{
//                                pdir = pdir + "/" + str;
//                            }
//                            logger.info("用户"+clientIp+"："+username+"执行CWD命令");
//                            pw.println("250 CWD successful. "+pdir +"is current directory");
//                            pw.flush();
//                            logger.info("("+username+") ("+clientIp+")> 250 CWD successful."+pdir+" is current directory");
//                        } else{
//                            //目录不存在
//                            pw.println("550 CWD failed. "+pdir+": directory not found.");
//                            pw.flush();
//                            logger.info("("+username+") ("+clientIp+")> 550 CWD failed. "+pdir+": directory not found.");
//                        }
//                    }
//                } else{
//                    pw.println(LOGIN_WARNING); pw.flush();
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doCWD(command);
            }
// QUIT命令
            else if(command.toUpperCase().startsWith("QUIT")){
                logger.info("("+username+") ("+clientIp+")> "+command);
                isRunning = false;
                send("221 Goodbye");
                dataSocket.close();
                ctrlSocket.close();
                logger.info("("+username+") ("+clientIp+")> 221 Goodbye");
                try {
                    Thread.currentThread();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                }
            }

//PORT命令，主动模式传输数据
            else if(command.toUpperCase().startsWith("PORT")){
//                logger.info("("+username+") ("+clientIp+")> "+command); if(loginStuts){
//                    try {
//                        str = command.substring(4).trim();
//                        port_low = Integer.parseInt(str.substring(str.lastIndexOf(",")+1));
//                        port_high = Integer.parseInt(str.substring(0, str.lastIndexOf(",")).substring(str.substring(0, str.lastIndexOf(",")).lastIndexOf(",")+1));
//                        String str1 = str.substring(0, str.substring(0, str.lastIndexOf(",")).lastIndexOf(","));
//                        remoteIp = str1.replace(",", ".");
//                        try {
////实例化主动模式下的socket
//                            dataSocket = new Socket(remoteIp,port_high * 256 + port_low);
//                            logger.info("用户"+clientIp+"："+username+"执行PORT命令");
////                            pw.println("200 port command successful");
////                            pw.flush();
//                            send("200 port command successful");
//                            logger.info("("+username+") ("+clientIp+")> 200 port command successful");
//                        } catch (ConnectException ce) {
////                            pw.println("425 Can't open data connection.");
////                            pw.flush();
//                            send("425 Can't open data connection.");
//                            logger.info("("+username+") ("+clientIp+")> 425 Can't open data connection.");
//                            System.out.println(ce.getMessage());
//                            for(StackTraceElement ste : ce.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        } catch (UnknownHostException e) {
//                            System.out.println(e.getMessage());
//                            for(StackTraceElement ste : e.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        } catch (IOException e) {
//                            System.out.println(e.getMessage());
//                            for(StackTraceElement ste : e.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        }
//                    } catch (NumberFormatException e) {
////                        pw.println("503 Bad sequence of commands.");
////                        pw.flush();
//                        send("503 Bad sequence of commands.");
//                        logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
//                        System.out.println(e.getMessage());
//                        for(StackTraceElement ste : e.getStackTrace()){
//                            System.out.println(ste.toString());
//                        }
//                    }
//                } else{
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doPORT(command);
            }
//PASV命令，被动模式传输数据
            else if(command.toUpperCase().startsWith("PASV")) {
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    ServerSocket ss = null;
//                    while( true ){
////获取服务器空闲端口
//                        port_high = 1 + generator.nextInt(20);
//                        port_low = 100 + generator.nextInt(1000);
//                        try {
////服务器绑定端口
//                            ss = new ServerSocket(port_high * 256 + port_low);
//                            System.out.println("***********************");
//                            System.out.println(port_high * 256 + port_low);
//                            break;
//                        } catch (IOException e) {
//                            continue;
//                        }
//                    }
//                    logger.info("用户"+clientIp+"："+username+"执行PASV命令");
//                    InetAddress i = null;
//                    try {
//                        i = InetAddress.getLocalHost();
//                    } catch (UnknownHostException e1) {
//                        e1.printStackTrace();
//                    }
//                    pw.println("227 Entering Passive Mode ("+i.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");
//                    pw.flush();
//                    while (true) {
//                        try {
//                            dataSocket = ss.accept();
//                            System.out.println(dataSocket.isConnected()+"----------------------");
//                            break;
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    logger.info("("+username+") ("+clientIp+")> 227 Entering Passive Mode ("+i.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");
//
//                } else{
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doPASV(command);
            }
//RETR命令
            else if(command.toUpperCase().startsWith("RETR")){
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    str = command.substring(4).trim();
//                    if("".equals(str)){
////                        pw.println("501 Syntax error");
////                        pw.flush();
//                        send("501 Syntax error");
//                        logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
//                    } else {
//                        try {
////                            pw.println("150 Opening data channel for file transfer.");
////                            pw.flush();
//                            send("150 Opening data channel for file transfer.");
//                            logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
//                            RandomAccessFile outfile = null;
//                            OutputStream outsocket = null;
//                            try {
////创建从中读取和向其中写入（可选）的随机访问文件流，该文件具有指定名称
//                                outfile = new RandomAccessFile(str,"r");
//                                System.out.println(str+"///////////////");
//                                outsocket = dataSocket.getOutputStream();
//                            } catch (FileNotFoundException e) {
//                                System.out.println(e.getMessage());
//                                for(StackTraceElement ste : e.getStackTrace()){
//                                    System.out.println(ste.toString());
//                                }
//                            } catch (IOException e) {
//                                System.out.println(e.getMessage());
//                                for(StackTraceElement ste : e.getStackTrace()){
//                                    System.out.println(ste.toString());
//                                }
//                            }
//                            byte bytebuffer[]= new byte[1024]; int length;
//                            try{
//                                while((length = outfile.read(bytebuffer)) != -1){
//                                    outsocket.write(bytebuffer, 0, length);
//                                }
//                                outsocket.close();
//                                outfile.close();
////                                dataSocket.close();
//                            } catch(IOException e){
//                                System.out.println(e.getMessage());
//                                for(StackTraceElement ste : e.getStackTrace()){
//                                    System.out.println(ste.toString());
//                                }
//                            }
//                            logger.info("用户"+clientIp+"："+username+"执行RETR命令");
////                            pw.println("226 Transfer OK");
////                            pw.flush();
//                            send("226 Transfer OK");
//                            logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
//                        } catch (Exception e){
////                            pw.println("503 Bad sequence of commands.");
////                            pw.flush();
//                            send("503 Bad sequence of commands.");
//                            logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
//                            System.out.println(e.getMessage());
//                            for(StackTraceElement ste : e.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        }
//                    }
//                } else{
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doRETR(command);
            }
//STOR命令
            else if(command.toUpperCase().startsWith("STOR")){
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    str = command.substring(4).trim();
//                    if("".equals(str)){
////                        pw.println("501 Syntax error");
////                        pw.flush();
//                        send("501 Syntax error");
//                        logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
//                    } else {
//                        try {
////                            pw.println("150 Opening data channel for file transfer.");
////                            pw.flush();
//                            send("150 Opening data channel for file transfer.");
//                            logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
//                            RandomAccessFile infile = null;
//                            InputStream insocket = null;
//                            try {
//                                infile = new RandomAccessFile(str,"rw");
//                                insocket = dataSocket.getInputStream();
//                            } catch (FileNotFoundException e) {
//                                System.out.println(e.getMessage());
//                                for(StackTraceElement ste : e.getStackTrace()){
//                                    System.out.println(ste.toString());
//                                }
//                            } catch (IOException e) {
//                                System.out.println(e.getMessage());
//                                for(StackTraceElement ste : e.getStackTrace()){
//                                    System.out.println(ste.toString());
//                                }
//                            }
//                            byte bytebuffer[] = new byte[1024]; int length;
//                            try{
//                                while((length =insocket.read(bytebuffer) )!= -1){
//                                    infile.write(bytebuffer, 0, length);
//                                }
//                                insocket.close();
//                                infile.close();
////                                dataSocket.close();
//                            } catch(IOException e){
//                                System.out.println(e.getMessage());
//                                for(StackTraceElement ste : e.getStackTrace()){
//                                    System.out.println(ste.toString());
//                                }
//                            }
//                            logger.info("用户"+clientIp+"："+username+"执行STOR命令");
////                            pw.println("226 Transfer OK");
////                            pw.flush();
//                            send("226 Transfer OK");
//                            logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
//                        } catch (Exception e){
////                            pw.println("503 Bad sequence of commands.");
////                            pw.flush();
//                            send("503 Bad sequence of commands.");
//                            logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
//                            System.out.println(e.getMessage());
//                            for(StackTraceElement ste : e.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        }
//                    }
//                } else {
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doSTOR(command);
            }
//NLST命令
            else if(command.toUpperCase().startsWith("NLST")) {
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    try {
////                        pw.println("150 Opening data channel for directory list.");
////                        pw.flush();
//                        send("150 Opening data channel for directory list.");
//                        System.out.println(username+password);
//                        logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for directory list.");
//                        PrintWriter pwr = null;
//                        try {
//                            pwr= new PrintWriter(dataSocket.getOutputStream(),true);
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                        File file = new File(dir);
//                        String[] dirstructure = new String[10];
//                        dirstructure= file.list();
//                        for(int i=0;i<dirstructure.length;i++){
//                            pwr.println(dirstructure[i]);
//                        }
//                        //                            dataSocket.close();
//                        pwr.close();
//                        logger.info("用户"+clientIp+"："+username+"执行NLST命令");
////                        pw.println("226 Transfer OK");
////                        pw.flush();
//                        send("226 Transfer OK");
//                        logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
//                    } catch (Exception e){
////                        pw.println("503 Bad sequence of commands.");
////                        pw.flush();
//                        send("503 Bad sequence of commands.");
//                        logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
//                        System.out.println(e.getMessage());
//                        for(StackTraceElement ste : e.getStackTrace()){
//                            System.out.println(ste.toString());
//                        }
//                    }
//
//                }else{
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doNLST(command);
            }
//LIST命令
            else if(command.toUpperCase().startsWith("LIST")) {
//                logger.info("("+username+") ("+clientIp+")> "+command);
//                if(loginStuts){
//                    try{
////                        pw.println("150 Opening data channel for directory list.");
////                        pw.flush();
//                        send("150 Opening data channel for directory list.");
//                        logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for directory list.");
//                        PrintWriter pwr = null;
//                        try {
//                            pwr= new PrintWriter(dataSocket.getOutputStream(),true);
//                        } catch (IOException e) {
//                            System.out.println(e.getMessage());
//                            for(StackTraceElement ste : e.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        }
//                        FtpUtil.getDetailList(pwr, dir);
//                        try {
//                            dataSocket.close();
//                            pwr.close();
//                        } catch (IOException e) {
//                            System.out.println(e.getMessage());
//                            for(StackTraceElement ste : e.getStackTrace()){
//                                System.out.println(ste.toString());
//                            }
//                        }
//                        logger.info("用户"+clientIp+"："+username+"执行LIST命令");
////                        pw.println("226 Transfer OK");
////                        pw.flush();
//                        send("226 Transfer OK");
//                        logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
//                    } catch (Exception e){
////                        pw.println("503 Bad sequence of commands.");
////                        pw.flush();
//                        send("503 Bad sequence of commands.");
//                        logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
//                        System.out.println(e.getMessage());
//                        for(StackTraceElement ste : e.getStackTrace()){
//                            System.out.println(ste.toString());
//                        }
//                    }
//                } else {
////                    pw.println(LOGIN_WARNING);
////                    pw.flush();
//                    send(LOGIN_WARNING);
//                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
//                }
                doLIST(command);
            }
// 输入非法命令
            else{
//                logger.info("("+username+") ("+clientIp+")> "+command);
////                pw.println("500 Syntax error, command unrecognized.");
////                pw.flush();
//                send("500 Syntax error, command unrecognized.");
//                logger.info("("+username+") ("+clientIp+")> 500 Syntax error, command unrecognized.");
                errorCommand(command);
            }
        }
        try {
            logger.info("("+username+") ("+clientIp+")> disconnected.");
            logger.info("用户"+clientIp+"："+username+"退出"); br.close();
            ctrlSocket.close();
            pw.close();
            if(null != dataSocket){
                dataSocket.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            for(StackTraceElement ste : e.getStackTrace()){
                System.out.println(ste.toString());
            }
        }
        }


    public void send(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] bytes = s.getBytes();
                    outputStream.write(bytes);
                    outputStream.flush();
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

    public void doUSER(String command){
        logger.info("(not logged in) ("+clientIp+")> "+command);
        username = command.substring(4).trim();
        if("".equals(username)){
            pw.println("501 Syntax error"); pw.flush();
            logger.info("(not logged in) ("+clientIp+")> 501 Syntax error");
            username = "not logged in";
        } else{
            pw.println("331 Password required for " + username);
            pw.flush();
            logger.info("(not logged in) ("+clientIp+")> 331 Password required for " + username);
        }
        loginStuts = false;
    }

    public void doPASS(String command){
        logger.info("(not logged in) ("+clientIp+")> "+command);
        password = command.substring(4).trim();
        if(username.equals("root") && password.equals("root")){
            pw.println("230 Logged on"); pw.flush();
            logger.info("("+username+") ("+clientIp+")> 230 Logged on");
            logger.info("客户端 "+clientIp+" 通过 "+username+"用户登录");
            loginStuts = true;
        } else{
            pw.println("530 Login or password incorrect!");
            pw.flush();
            logger.info("(not logged in) ("+clientIp+")> 530 Login or password incorrect!");
            username = "not logged in";
        }
    }

    public void doPWD(String command){
        File folder = new File(dir);
        send("("+folder.isDirectory()+folder.isFile()+" "+folder.exists()+")");
        System.out.println("("+folder.isDirectory()+folder.isFile()+" "+folder.exists()+")"+"-------------------");
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            logger.info("用户"+clientIp+"："+username+"执行PWD命令");

            send("257 "+pdir+" is current directory");
            logger.info("("+username+") ("+clientIp+")> 257 "+pdir+" is current directory");
        } else{
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doCWD(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            str = command.substring(3).trim();
            if("".equals(str)){
                pw.println("250 Broken client detected, missing argument to CWD. "+pdir+" is current directory.");
                pw.flush();
                logger.info("("+username+") ("+clientIp+")> 250 Broken client detected, missing argument to CWD. "+pdir+" is current directory.");
            }
            else{
//判断目录是否存在
                String tmpDir = dir + "/" + str;
                File file = new File(tmpDir);
                if(file.exists()){//目录存在
                    dir = dir + "/" + str;
                    if("/".equals(pdir)){
                        pdir = pdir + str;
                    } else{
                        pdir = pdir + "/" + str;
                    }
                    logger.info("用户"+clientIp+"："+username+"执行CWD命令");
                    pw.println("250 CWD successful. "+pdir +"is current directory");
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> 250 CWD successful."+pdir+" is current directory");
                } else{
                    //目录不存在
                    pw.println("550 CWD failed. "+pdir+": directory not found.");
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> 550 CWD failed. "+pdir+": directory not found.");
                }
            }
        } else{
            pw.println(LOGIN_WARNING); pw.flush();
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doPORT(String command){
        logger.info("("+username+") ("+clientIp+")> "+command); if(loginStuts){
            try {
                str = command.substring(4).trim();
                port_low = Integer.parseInt(str.substring(str.lastIndexOf(",")+1));
                port_high = Integer.parseInt(str.substring(0, str.lastIndexOf(",")).substring(str.substring(0, str.lastIndexOf(",")).lastIndexOf(",")+1));
                String str1 = str.substring(0, str.substring(0, str.lastIndexOf(",")).lastIndexOf(","));
                remoteIp = str1.replace(",", ".");
                try {
//实例化主动模式下的socket
                    dataSocket = new Socket(remoteIp,port_high * 256 + port_low);
                    logger.info("用户"+clientIp+"："+username+"执行PORT命令");
                    send("200 port command successful");
                    logger.info("("+username+") ("+clientIp+")> 200 port command successful");
                } catch (ConnectException ce) {
                    send("425 Can't open data connection.");
                    logger.info("("+username+") ("+clientIp+")> 425 Can't open data connection.");
                    System.out.println(ce.getMessage());
                    for(StackTraceElement ste : ce.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                } catch (UnknownHostException e) {
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                }
            } catch (NumberFormatException e) {
                send("503 Bad sequence of commands.");
                logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                System.out.println(e.getMessage());
                for(StackTraceElement ste : e.getStackTrace()){
                    System.out.println(ste.toString());
                }
            }
        } else{
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doPASV(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            ServerSocket ss = null;
            while( true ){
//获取服务器空闲端口
                port_high = 1 + generator.nextInt(20);
                port_low = 100 + generator.nextInt(1000);
                try {
//服务器绑定端口
                    ss = new ServerSocket(port_high * 256 + port_low);
                    System.out.println(port_high * 256 + port_low);
                    break;
                } catch (IOException e) {
                    continue;
                }
            }
            logger.info("用户"+clientIp+"："+username+"执行PASV命令");
            InetAddress i = null;
            try {
                i = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
            pw.println("227 Entering Passive Mode ("+i.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");
            pw.flush();
            while (true) {
                try {
                    dataSocket = ss.accept();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logger.info("("+username+") ("+clientIp+")> 227 Entering Passive Mode ("+i.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");

        } else{
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doRETR(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            str = command.substring(4).trim();
            if("".equals(str)){
                send("501 Syntax error");
                logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
            } else {
                try {
                    send("150 Opening data channel for file transfer.");
                    logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
                    RandomAccessFile outfile = null;
                    OutputStream outsocket = null;
                    try {
                        outfile = new RandomAccessFile(str,"r");
                        outsocket = dataSocket.getOutputStream();
                    } catch (FileNotFoundException e) {
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }
                    byte bytebuffer[]= new byte[1024]; int length;
                    try{
                        while((length = outfile.read(bytebuffer)) != -1){
                            outsocket.write(bytebuffer, 0, length);
                        }
                        outsocket.close();
                        outfile.close();
                    } catch(IOException e){
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }
                    logger.info("用户"+clientIp+"："+username+"执行RETR命令");
                    send("226 Transfer OK");
                    logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
                } catch (Exception e){
                    send("503 Bad sequence of commands.");
                    logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                }
            }
        } else{
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doSTOR(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            str = command.substring(4).trim();
            if("".equals(str)){
                send("501 Syntax error");
                logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
            } else {
                try {
                    send("150 Opening data channel for file transfer.");
                    logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
                    RandomAccessFile infile = null;
                    InputStream insocket = null;
                    try {
                        infile = new RandomAccessFile(str,"rw");
                        insocket = dataSocket.getInputStream();
                    } catch (FileNotFoundException e) {
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }
                    byte bytebuffer[] = new byte[1024]; int length;
                    try{
                        while((length =insocket.read(bytebuffer) )!= -1){
                            infile.write(bytebuffer, 0, length);
                        }
                        insocket.close();
                        infile.close();
//                                dataSocket.close();
                    } catch(IOException e){
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }
                    logger.info("用户"+clientIp+"："+username+"执行STOR命令");
                    send("226 Transfer OK");
                    logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
                } catch (Exception e){
                    send("503 Bad sequence of commands.");
                    logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                }
            }
        } else {
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doNLST(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            try {
                send("150 Opening data channel for directory list.");
                System.out.println(username+password);
                logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for directory list.");
                PrintWriter pwr = null;
                try {
                    pwr= new PrintWriter(dataSocket.getOutputStream(),true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                File file = new File(dir);
                String[] dirstructure = new String[10];
                dirstructure= file.list();
                for(int i=0;i<dirstructure.length;i++){
                    pwr.println(dirstructure[i]);
                }
                //                            dataSocket.close();
                pwr.close();
                logger.info("用户"+clientIp+"："+username+"执行NLST命令");
                send("226 Transfer OK");
                logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
            } catch (Exception e){
                send("503 Bad sequence of commands.");
                logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                System.out.println(e.getMessage());
                for(StackTraceElement ste : e.getStackTrace()){
                    System.out.println(ste.toString());
                }
            }

        }else{
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doLIST(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStuts){
            try{
                send("150 Opening data channel for directory list.");
                logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for directory list.");
                PrintWriter pwr = null;
                try {
                    pwr= new PrintWriter(dataSocket.getOutputStream(),true);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                }
                FtpUtil.getDetailList(pwr, dir);
                try {
                    dataSocket.close();
                    pwr.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
                        System.out.println(ste.toString());
                    }
                }
                logger.info("用户"+clientIp+"："+username+"执行LIST命令");
                send("226 Transfer OK");
                logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
            } catch (Exception e){
                send("503 Bad sequence of commands.");
                logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                System.out.println(e.getMessage());
                for(StackTraceElement ste : e.getStackTrace()){
                    System.out.println(ste.toString());
                }
            }
        } else {
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void errorCommand(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        send("500 Syntax error, command unrecognized.");
        logger.info("("+username+") ("+clientIp+")> 500 Syntax error, command unrecognized.");
    }

}
