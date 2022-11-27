package com.example.petmanage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class EditPersonalInfo extends AppCompatActivity {

    private Thread thread;

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    PrintWriter out;
    BufferedReader in;
    Settings setting;

    ShapeableImageView pic;
    EditText uname;
    EditText account;
    EditText email;
    MaterialButton save_btn;
    boolean need_save = false;
    int edit_account = 0;
    int edit_email = 0;
    int edit_pic = 0;

    private final int GALLERY_REQ_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_personal_info);
        transparentStatusBar();

        setting = (Settings) getApplicationContext();

        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        // 頭像 //
        pic = (ShapeableImageView) findViewById(R.id.pic);
        // 改圖片 //
        pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, GALLERY_REQ_CODE);
            }
        });
        pic.setImageBitmap(setting.get_profile());

        // 登入天數 //
        TextView login_days = (TextView) findViewById(R.id.login_days);
        login_days.setText("開發中"); // 開發中

        // 登記數量 //
        TextView pets_count = (TextView) findViewById(R.id.pets_count);
        String[] pets = setting.Get_pets().split("&");
        pets_count.setText(pets.length + " 隻");

        // 註冊日期 //
        TextView reg_date = (TextView) findViewById(R.id.reg_date);
        reg_date.setText(setting.Get_reg_date());

        // 名稱 //
        uname = (EditText) findViewById(R.id.user_name);
        uname.setText(setting.Get_reg_name());

        // 帳號 //
        account = (EditText) findViewById(R.id.user_account);
        account.setText(setting.Get_reg_account());

        // 信箱 //
        email = (EditText) findViewById(R.id.user_email);
        email.setText(setting.Get_reg_email());

        // 性別 //
        TextView gender = (TextView) findViewById(R.id.user_gender);
        if(setting.Get_reg_gender().equals("Male"))gender.setText("男");
        else gender.setText("女");


        // 生日 //
        TextView birthday = (TextView) findViewById(R.id.user_birthday);
        birthday.setText(setting.Get_reg_birthday());

        // 按下儲存 //
        save_btn = (MaterialButton) findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!uname.getText().toString().equals(setting.Get_reg_name())) need_save = true;
                if(!account.getText().toString().equals(setting.Get_reg_account())) {
                    edit_account = 1;
                    need_save = true;
                }
                if(!email.getText().toString().equals(setting.Get_reg_email())) {
                    edit_email = 1;
                    need_save = true;
                }
                if(need_save){  // 需要保存
                    // 送伺服器 //
                    thread = new Thread(Connection);
                    thread.start();
                }
                else{
                    finish();
                }
            }
        });

        //建立通知
        msg_accept = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("web","通知",NotificationManager.IMPORTANCE_HIGH);
            msg_accept.createNotificationChannel(channel);
        }
        Bitmap edit_success = BitmapFactory.decodeResource(getResources(), R.drawable.check);
        Bitmap edit_fail = BitmapFactory.decodeResource(getResources(), R.drawable.error);
        notify_accept = new NotificationCompat.Builder(this,"web")
                .setContentTitle("修改成功")
                .setContentText("若有修改帳號，下次請用新帳號登入！")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(edit_success)
                .build();

        msg_reject = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("web","通知",NotificationManager.IMPORTANCE_HIGH);
            msg_reject.createNotificationChannel(channel);
        }
        notify_reject = new NotificationCompat.Builder(this,"web")
                .setContentTitle("修改失敗")
                .setContentText("原因：帳號或信箱已被使用！")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(edit_fail)
                .build();

    }

    String encoded_img;
    boolean edited_img = false;
    TextView tv;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            pic.setImageURI(uri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri );
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byte_array = byteArrayOutputStream.toByteArray();
                encoded_img = Base64.encodeToString(byte_array, Base64.NO_WRAP);
                edit_pic = 1;
                need_save = true;
                setting.set_profile(bitmap);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private NotificationManager msg_accept;
    private Notification notify_accept;

    private NotificationManager msg_reject;
    private Notification notify_reject;
    String Edit = "";

    private Runnable Connection=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                String edited_info = String.valueOf(edit_account) + String.valueOf(edit_email) + String.valueOf(edit_pic);
                String name = uname.getText().toString();
                String acc = account.getText().toString();
                String mail = email.getText().toString();
                String uid = setting.Get_Uid();
                String req = name + "/" + acc + "/" + mail + "/" + uid + "/" + edited_info + "/" + encoded_img;
                String message = "/edit/" + req;
                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);

                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println(message);
                String response = "";   // 伺服器給予的回應字串，用於判定登入是否成功以及身分別
                response = in.readLine();


                if(response.contains("reject")){
                    // 否決
                    msg_reject.notify(1, notify_reject); // 通知
                    clientSocket.close();
                }
                else if(response.contains("seccuss")){
                    // 通過
                    msg_accept.notify(1, notify_accept); // 通知

                    setting.Set_reg_name(uname.getText().toString());
                    setting.Set_reg_account(account.getText().toString());
                    setting.Set_reg_email(email.getText().toString());

                    need_save = false;
                    edit_account = 0;
                    edit_email = 0;
                    edit_pic = 0;
                    clientSocket.close();
                    finish();
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