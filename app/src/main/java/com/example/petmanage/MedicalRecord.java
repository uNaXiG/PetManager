package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MedicalRecord extends AppCompatActivity {

    Settings setting;
    private Thread thread;
    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    PrintWriter out;
    BufferedReader in;

    EditText remark;


    private NotificationManager manager;
    private Notification notification1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);
        transparentStatusBar();

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();


        // 標題 //
        TextView show_title = (TextView) findViewById(R.id.medical_title);
        show_title.setText(PetHealthManage.sel_title);

        // 日期 //
        String display_date = "日期 : " + PetHealthManage.sel_date;
        TextView show_date = (TextView) findViewById(R.id.medical_date);
        show_date.setText(display_date.replace("-", " / "));

        // 備註 //
        String display_remark = PetHealthManage.sel_remark;
        remark = (EditText) findViewById(R.id.medical_remark);
        remark.setText(display_remark.replace("***", "\n"));

        // 保存 //
        Button save = (Button) findViewById(R.id.save_btn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thread = new Thread(SendMedical);
                thread.start();
            }
        });

        // 返回 //
        Button back = (Button) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PetHealthManage.sel_title = "";
                PetHealthManage.sel_date = "";
                PetHealthManage.sel_remark = "";
                finish();
            }
        });

        //建立通知
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("web","通知",NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.check);
        notification1 = new NotificationCompat.Builder(MedicalRecord.this,"web")
                .setContentTitle("就醫紀錄")
                .setContentText("編輯成功")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(bitmap)
                .build();
    }

    private Runnable SendMedical = new Runnable() {
        @Override
        public void run() {

            try{
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                // 向 server 發送訊息
                out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);
                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("/write_medical/" + setting.Get_Pet_Info().get(setting.Get_Select_Pet()).PetId + "/" + PetHealthManage.sel_date + "/" + remark.getText().toString().replace("\n", "***"));
                String response = "";
                response = in.readLine();

                if(response.split("/")[1].equals("success")){
                    Pet.healthy_medical[PetHealthManage.sel_idx] = remark.getText().toString();
                    manager.notify(1,notification1); // 通知
                    finish();
                }

            }
            catch (Exception ex){ finish();}

        }
    };

    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}