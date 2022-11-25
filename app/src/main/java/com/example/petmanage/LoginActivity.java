package com.example.petmanage;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {


    Settings settings;     // 全域變數(類別)
    String server_ip;   // 伺服器IP
    int server_port;    // 伺服器port
    private Thread thread;
    private Socket clientSocket;    //客戶端的socket
    PrintWriter out;
    BufferedReader in;

    private String in_name = "";   // 使用者所輸入的名稱
    private String in_pwd = "";    // 密碼
    private String response = "";   // 伺服器給予的回應字串，用於判定登入是否成功以及身分別

    public static String user_name = "";    // 將使用者名稱帶到主畫面
    TextView pr;

    private NotificationManager manager;
    private Notification notification1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        transparentStatusBar();

        settings = (Settings) getApplicationContext();
        TextView username = (TextView) findViewById(R.id.username);
        TextView pwd = (TextView) findViewById(R.id.password);

        MaterialButton login_btn = (MaterialButton) findViewById(R.id.loginbtn);
        pr = (TextView) findViewById(R.id.print);

        server_ip = settings.Get_IP();  // 取得伺服器IP
        server_port = settings.Get_Port();  // 取得伺服器port

        //建立通知
        manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("web","通知",NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.check);
        notification1 = new NotificationCompat.Builder(LoginActivity.this,"web")
                .setContentTitle("登入通知")
                .setContentText("您已成功登入")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(bitmap)
                .build();

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pr.setText("");
                in_name = username.getText().toString();
                in_pwd = pwd.getText().toString();
                if(in_name.length() < 8 || in_pwd.length() < 8){
                    Toast.makeText(LoginActivity.this, "Account(or password) length should be at least 8 character !", Toast.LENGTH_SHORT).show();
                    pr.setText("login error");
                    return;
                }
                if(!Character.isLetter(in_name.charAt(0))){
                    Toast.makeText(LoginActivity.this, "Account first character must be a letter", Toast.LENGTH_SHORT).show();
                    pr.setText("login error");
                    return;
                }
                if(!in_pwd.matches(".*[a-z].*")){
                    Toast.makeText(LoginActivity.this, "password should be include at least a letter", Toast.LENGTH_SHORT).show();
                    pr.setText("login error");
                    return;
                }
                thread=new Thread(Connection);
                thread.start();
            }
        });

        // 註冊用戶按下 //
        MaterialButton register_user = (MaterialButton) findViewById(R.id.reg_user);
        register_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 載入註冊頁面
                username.setText("");
                pwd.setText("");
                Intent intent = new Intent();
                intent.setClass(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        // 忘記密碼按下 //
        TextView forget_pwd = (TextView) findViewById((R.id.forgetpwd));
        forget_pwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Forget Password !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //連結socket伺服器做傳送與接收
    private Runnable Connection=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);
                String message = "/login/" + in_name + "/" + in_pwd;
                Log.d("TCP", message);

                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);
                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println(message);
                response = in.readLine();

                if(response != ""){
                    if(response.split("/")[1].equals("accept")){
                        pr.setText("Connection...");
                        user_name = in_name;
                        settings.Set_reg_account(user_name);
                        manager.notify(1,notification1); // 通知

                        settings.Set_reg_name(response.split("/")[3]);
                        settings.Set_reg_gender(response.split("/")[4]);
                        settings.Set_reg_birthday(response.split("/")[5]);
                        settings.Set_reg_account(response.split("/")[6]);
                        settings.Set_reg_pwd(response.split("/")[7]);
                        settings.Set_reg_email(response.split("/")[8]);
                        settings.Set_reg_phone(response.split("/")[9]);
                        settings.Set_reg_date(response.split("/")[10]);
                        settings.Set_Uid(response.split("/")[11]);

                        if(response.split("/")[2].equals("empty_user") ){
                            // 判定尚未加入寵物資料
                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this, ReadPetID.class);   // 跳轉到輸入晶片頁面
                            startActivity(intent);
                        }
                        else if( response.split("/")[2].equals("normal") ){
                            // 已加入過寵物紀錄
                            settings.Set_pets(response.split("/")[12]);
                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this, MainActivity.class);     // 跳轉到應用程式主介面

                            startActivity(intent);
                        }

                    }
                    else{
                        pr.setText("Login failed...");
                        response = "";
                    }
                }
            }
            catch(Exception e){
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}