package cn.maddie.androidtcpserver;
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
    Socket ctrlSocket;
    ServerSocket serverSocket;
    InputStream inputStream;
    OutputStream outputStream;
    Message message;
    Handler handler;

    private Socket dataSocket;
    private Logger logger;//日志对象
    private String dir;//绝对路径
    private String pdir = "/Data";//相对路径
    private final static Random generator = new Random();//随机数
    BufferedReader br;
    PrintWriter pw;
    String clientIp = null;//记录客户端IP
    String username = "not logged in";//用户名
    String password = "";// 口令
    boolean loginStuts = false;//登录状态
    final String LOGIN_WARNING = "530 Please log in with USER and PASS first.";
    String str = "";//命令内容字符串
    int port_high = 0;
    int port_low = 0;
    String retr_ip = "";//接收文件的IP地址


    public FTPServer(Handler handler) {
        this.handler = handler;
    }

    public void conntcp( int port) throws IOException {
        logger = Logger.getLogger("com");
        serverSocket = new ServerSocket(port);
        System.out.println("?????????????");
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
        boolean b = true;
        while (b) {
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
            } //end USER
// PASS命令
            else if(command.toUpperCase().startsWith("PASS")){
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
            } //end PASS
// PWD命令
            else if(command.toUpperCase().startsWith("PWD")){
                logger.info("("+username+") ("+clientIp+")> "+command);
                if(loginStuts){
                    logger.info("用户"+clientIp+"："+username+"执行PWD命令");
                    pw.println("257 "+pdir+" is current directory");
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> 257 "+pdir+" is current directory");
                } else{
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            } //end PWD
// CWD命令
            else if(command.toUpperCase().startsWith("CWD")){
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
            } //end CWD
// QUIT命令
            else if(command.toUpperCase().startsWith("QUIT")){
                logger.info("("+username+") ("+clientIp+")> "+command);
                b = false;
                pw.println("221 Goodbye"); pw.flush();
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
            } //end QUIT
            /*
             * 传输参数命令
             */
//PORT命令，主动模式传输数据
            else if(command.toUpperCase().startsWith("PORT")){
                logger.info("("+username+") ("+clientIp+")> "+command); if(loginStuts){
                    try {
                        str = command.substring(4).trim();
                        port_low = Integer.parseInt(str.substring(str.lastIndexOf(",")+1));
                        port_high = Integer.parseInt(str.substring(0, str.lastIndexOf(",")).substring(str.substring(0, str.lastIndexOf(",")).lastIndexOf(",")+1));
                        String str1 = str.substring(0, str.substring(0, str.lastIndexOf(",")).lastIndexOf(","));
                        retr_ip = str1.replace(",", ".");
                        try {
//实例化主动模式下的socket
                            dataSocket = new Socket(retr_ip,port_high * 256 + port_low);
                            logger.info("用户"+clientIp+"："+username+"执行PORT命令"); pw.println("200 port command successful");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 200 port command successful");
                        } catch (ConnectException ce) {
                            pw.println("425 Can't open data connection.");
                            pw.flush();
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
                        pw.println("503 Bad sequence of commands.");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }
                } else{
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            } //end PORT
//PASV命令，被动模式传输数据
            else if(command.toUpperCase().startsWith("PASV")) {
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
                    try {
                        dataSocket = ss.accept();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    logger.info("("+username+") ("+clientIp+")> 227 Entering Passive Mode ("+i.getHostAddress().replace(".", ",")+","+port_high+","+port_low+")");

                } else{
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            } //end PASV
//RETR命令
            else if(command.toUpperCase().startsWith("RETR")){
                logger.info("("+username+") ("+clientIp+")> "+command);
                if(loginStuts){
                    str = command.substring(4).trim();
                    if("".equals(str)){
                        pw.println("501 Syntax error");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
                    } else {
                        try {
                            pw.println("150 Opening data channel for file transfer.");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
                            RandomAccessFile outfile = null;
                            OutputStream outsocket = null;
                            try {
//创建从中读取和向其中写入（可选）的随机访问文件流，该文件具有指定名称
                                outfile = new RandomAccessFile(dir+"/"+str,"r");
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
//                                dataSocket.close();
                            } catch(IOException e){
                                System.out.println(e.getMessage());
                                for(StackTraceElement ste : e.getStackTrace()){
                                    System.out.println(ste.toString());
                                }
                            }
                            logger.info("用户"+clientIp+"："+username+"执行RETR命令");
                            pw.println("226 Transfer OK");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
                        } catch (Exception e){
                            pw.println("503 Bad sequence of commands.");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                            System.out.println(e.getMessage());
                            for(StackTraceElement ste : e.getStackTrace()){
                                System.out.println(ste.toString());
                            }
                        }
                    }
                } else{
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            }//end RETR
//STOR命令
            else if(command.toUpperCase().startsWith("STOR")){
                logger.info("("+username+") ("+clientIp+")> "+command);
                if(loginStuts){
                    str = command.substring(4).trim();
                    if("".equals(str)){
                        pw.println("501 Syntax error");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 501 Syntax error");
                    } else {
                        try {
                            pw.println("150 Opening data channel for file transfer.");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 150 Opening data channel for file transfer.");
                            RandomAccessFile infile = null;
                            InputStream insocket = null;
                            try {
                                infile = new RandomAccessFile(dir+"/"+str,"rw");
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
                            pw.println("226 Transfer OK");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
                        } catch (Exception e){
                            pw.println("503 Bad sequence of commands.");
                            pw.flush();
                            logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                            System.out.println(e.getMessage());
                            for(StackTraceElement ste : e.getStackTrace()){
                                System.out.println(ste.toString());
                            }
                        }
                    }
                } else {
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            } //end STOR
//NLST命令
            else if(command.toUpperCase().startsWith("NLST")) {
                logger.info("("+username+") ("+clientIp+")> "+command);
                if(loginStuts){
                    try {
                        pw.println("150 Opening data channel for directory list.");
                        pw.flush();
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
                        pw.println("226 Transfer OK");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
                    } catch (Exception e){
                        pw.println("503 Bad sequence of commands.");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }

                }else{
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            } //end NLST
//LIST命令
            else if(command.toUpperCase().startsWith("LIST")) {
                logger.info("("+username+") ("+clientIp+")> "+command);
                if(loginStuts){
                    try{
                        pw.println("150 Opening data channel for directory list.");
                        pw.flush();
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
                        pw.println("226 Transfer OK");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 226 Transfer OK");
                    } catch (Exception e){
                        pw.println("503 Bad sequence of commands.");
                        pw.flush();
                        logger.info("("+username+") ("+clientIp+")> 503 Bad sequence of commands.");
                        System.out.println(e.getMessage());
                        for(StackTraceElement ste : e.getStackTrace()){
                            System.out.println(ste.toString());
                        }
                    }
                } else {
                    pw.println(LOGIN_WARNING);
                    pw.flush();
                    logger.info("("+username+") ("+clientIp+")> "+LOGIN_WARNING);
                }
            } //end LIST
// 输入非法命令
            else{
                logger.info("("+username+") ("+clientIp+")> "+command);
                pw.println("500 Syntax error, command unrecognized.");
                pw.flush();
                logger.info("("+username+") ("+clientIp+")> 500 Syntax error, command unrecognized.");
            }
        } //end while
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

}
