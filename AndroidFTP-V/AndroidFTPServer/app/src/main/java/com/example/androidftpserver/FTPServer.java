package com.example.androidftpserver;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
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
    private String absolutePath = null;//绝对路径
    private String relativePath = null;//相对路径
    private final static Random random = new Random();//随机数
    BufferedReader br;
    PrintWriter pw;
    String clientIp = null;//记录客户端IP
    String username = "not logged in";//用户名
    String password = "";
    boolean loginStatus = false;
    boolean isRunning = false;
    final String LOGIN_WARNING = "530 Please log in with USER and PASS first.";
    String str = "";//命令内容字符串
    int port_high = 0;
    int port_low = 0;
    String remoteIp = "";//接收文件的IP地址
    private String remotePath = "/storage/emulated/0/Android/data/com.selfftpclient.androidftpclient/files/Client";
    private String type;


    public FTPServer(Handler handler) {
        this.handler = handler;
        type = "B";
    }

    public void setAbsolutePath(String path){
        this.absolutePath = path;
        this.relativePath = path;
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

                doUSER(command);
            }
// PASS命令
            else if(command.toUpperCase().startsWith("PASS")){

                doPASS(command);
            }
// QUIT命令
            else if(command.toUpperCase().startsWith("QUIT")){

                doQuit(command);
            }

//PORT命令，主动模式传输数据
            else if(command.toUpperCase().startsWith("PORT")){

                doPORT(command);
            }
//PASV命令，被动模式传输数据
            else if(command.toUpperCase().startsWith("PASV")) {

                doPASV(command);
            }
//RETR命令
            else if(command.toUpperCase().startsWith("RETR")){

                doRETR(command);
            }
//STOR命令
            else if(command.toUpperCase().startsWith("STOR")){

                doSTOR(command);
            }
            else if(command.toUpperCase().startsWith("TYPE")){

                doType(command);
            }
// 输入非法命令
            else{

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
        loginStatus = false;
    }

    public void doPASS(String command){
        logger.info("(not logged in) ("+clientIp+")> "+command);
        password = command.substring(4).trim();
        if(username.equals("test") && password.equals("test")){
            pw.println("230 Logged on"); pw.flush();
            logger.info("("+username+") ("+clientIp+")> 230 Logged on");
            logger.info("客户端 "+clientIp+" 通过 "+username+"用户登录");
            loginStatus = true;
        } else{
            pw.println("530 Login or password incorrect!");
            pw.flush();
            logger.info("(not logged in) ("+clientIp+")> 530 Login or password incorrect!");
            username = "not logged in";
        }
    }

    public void doPORT(String command){
        logger.info("("+username+") ("+clientIp+")> "+command); if(loginStatus){
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
                } catch (ConnectException e) {
                    send("425 Can't open data connection.");
                    logger.info("("+username+") ("+clientIp+")> 425 Can't open data connection.");
                    System.out.println(e.getMessage());
                    for(StackTraceElement ste : e.getStackTrace()){
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
        if(loginStatus){
            ServerSocket serverSocket = null;
            while( true ){
//获取服务器空闲端口
                port_high = 1 + random.nextInt(20);
                port_low = 100 + random.nextInt(1000);
                try {
//服务器绑定端口
                    serverSocket = new ServerSocket(port_high * 256 + port_low);
                    System.out.println(port_high * 256 + port_low);
                    break;
                } catch (IOException e) {
                    continue;
                }
            }
            logger.info("用户"+clientIp+"："+username+"执行PASV命令");
            InetAddress inetAddress = null;
            try {
                inetAddress = InetAddress.getLocalHost();
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            }
//            pw.println("227 Entering Passive Mode ("+inetAddress.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");
//            pw.flush();
            send("227 Entering Passive Mode ("+inetAddress.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");
            while (true) {
                try {
                    dataSocket = serverSocket.accept();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            logger.info("("+username+") ("+clientIp+")> 227 Entering Passive Mode ("+inetAddress.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");

        } else{
            send(LOGIN_WARNING);
            logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
        }
    }

    public void doRETR(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        if(loginStatus){
            str = command.substring(4).trim();
            if("".equals(str)){
                send("501 Syntax error");
                logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
            } else {
                try {
                    send("150 Opening data channel for file transfer.");
                    logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
                    RandomAccessFile outfile = null;
                    OutputStream dataOutputStream = null;
                    try {
                        outfile = new RandomAccessFile(str,"r");
                        dataOutputStream = dataSocket.getOutputStream();
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
                    byte byteBuffer[]= new byte[1024];
                    int length;
                    try{
                        while((length = outfile.read(byteBuffer)) != -1){
                            dataOutputStream.write(byteBuffer, 0, length);
                        }
                        dataOutputStream.close();
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
        if(loginStatus){
            str = command.substring(4).trim();
            if("".equals(str)){
                send("501 Syntax error");
                logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
            } else {
                try {
                    send("150 Opening data channel for file transfer.");
                    logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
                    RandomAccessFile infile = null;
                    InputStream dataInputStream = null;
                    try {
                        infile = new RandomAccessFile(str,"rw");
                        dataInputStream = dataSocket.getInputStream();
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
                    byte byteBuffer[] = new byte[1024];
                    int length;
                    try{
                        while((length =dataInputStream.read(byteBuffer) )!= -1){
                            infile.write(byteBuffer, 0, length);
                        }
                        dataInputStream.close();
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

    public void errorCommand(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        send("500 Syntax error, command unrecognized.");
        logger.info("("+username+") ("+clientIp+")> 500 Syntax error, command unrecognized.");
    }

    public void doType(String command){
        String[] strings = command.split("\\s+");
        if(strings[1].equals("A")){
            type = "A";
            send("200 Enter Type Asicc");
        }else if(strings[1].equals("B")){
            type = "B";
            send("200 Enter Type Binary");
        }
    }

    public void doQuit(String command){
        logger.info("("+username+") ("+clientIp+")> "+command);
        isRunning = false;
        send("221 Goodbye");
        try {
        dataSocket.close();
        ctrlSocket.close();
        logger.info("("+username+") ("+clientIp+")> 221 Goodbye");
            Thread.currentThread();
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            for(StackTraceElement ste : e.getStackTrace()){
                System.out.println(ste.toString());
            }
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }
}