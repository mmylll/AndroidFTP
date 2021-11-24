package com.example.androidftpclient;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidftpclient.atv.holder.MyHolder;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt_conn, bt_send,testbt;
    private EditText et_ip, et_port, et_msg;
    private TextView tv_data;

    com.example.androidftpclient.FTPClient FTPClient;
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

        //Root
        TreeNode root = TreeNode.root();

        //Parent
        MyHolder.IconTreeItem nodeItem = new MyHolder.IconTreeItem(R.drawable.ic_arrow_drop_down, "Parent");
        TreeNode parent = new TreeNode(nodeItem).setViewHolder(new MyHolder(getApplicationContext(), true, MyHolder.DEFAULT, MyHolder.DEFAULT));

        //Child
        MyHolder.IconTreeItem childItem = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Child");
        TreeNode child = new TreeNode(childItem).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 25));

        //Sub Child
        MyHolder.IconTreeItem subChildItem = new MyHolder.IconTreeItem(R.drawable.ic_folder, "Sub Child");
        TreeNode subChild = new TreeNode(subChildItem).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 50));

        MyHolder.IconTreeItem fileChildItem = new MyHolder.IconTreeItem(R.drawable.ic_baseline_insert_drive_file_24, "File");
        TreeNode fileChild = new TreeNode(fileChildItem).setViewHolder(new MyHolder(getApplicationContext(), false, R.layout.child, 75));

        subChild.addChild(fileChild);

        //Add sub child.
        child.addChild(subChild);


        //Add child.
        parent.addChildren(child);
        root.addChild(parent);

        //Add AndroidTreeView into view.
        AndroidTreeView tView = new AndroidTreeView(getApplicationContext(), root);
        ((LinearLayout) findViewById(R.id.ll_parent)).addView(tView.getView());



    }

    private void Clickbt() {
        bt_conn.setOnClickListener(this);
        bt_send.setOnClickListener(this);
        testbt.setOnClickListener(this);
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

        testbt = findViewById(R.id.testbt);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.testbt:
//                new ChooserDialog(MainActivity.this)
//                        .withFilter(true, false)
//                        .withStartFile("/data")
//                        // to handle the result(s)
//                        .withChosenListener(new ChooserDialog.Result() {
//                            @Override
//                            public void onChoosePath(String path, File pathFile) {
//                                Toast.makeText(MainActivity.this, "FOLDER: " + path, Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .build()
//                        .show();

//                Intent i4 = new Intent(getApplicationContext(), FileBrowser.class);
//                startActivity(i4);

//                Intent i2 = new Intent(getApplicationContext(), FileChooser.class);
//                i2.putExtra(Constants.SELECTION_MODE, Constants.SELECTION_MODES.SINGLE_SELECTION.ordinal());
//                startActivityForResult(i2, 200);


                break;
            case R.id.bt_conn:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
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
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && data!=null) {
            System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            if (resultCode == RESULT_OK) {
                Uri file = data.getData();
                System.out.println(file.getPath());
            }
        }
    }
}
