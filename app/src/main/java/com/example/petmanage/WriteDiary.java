package com.example.petmanage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WriteDiary extends AppCompatActivity {

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket
    private Thread thread;

    PrintWriter out;
    BufferedReader in;
    Settings setting;

    ImageView pet_profile;
    int is_changed_img = 0;
    String encoded_img;
    TextView cur_date;
    EditText title_in;
    EditText content_in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);
        transparentStatusBar();

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        title_in = (EditText) findViewById(R.id.title_in);
        content_in = (EditText)findViewById(R.id.content_in);
        // disable newline key
        title_in.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            }
        });

        // 圖片
        pet_profile = (ImageView) findViewById(R.id.diary_img);
        pet_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        });

        cur_date = (TextView) findViewById(R.id.current_date);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        cur_date.setText("【"+formatter.format(date) + "】");

        // 返回
        MaterialButton back  = (MaterialButton) findViewById(R.id.cancel_btn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // 保存
        MaterialButton save = (MaterialButton)findViewById(R.id.save_btn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(is_changed_img == 0) return;
                thread = new Thread(Connection);
                thread.start();
            }
        });

        //
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            Uri uri = data.getData();
            pet_profile.setImageURI(uri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri );
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byte_array = byteArrayOutputStream.toByteArray();
                encoded_img = Base64.encodeToString(byte_array, Base64.NO_WRAP);
                is_changed_img = 1;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable Connection = new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                String res = "";
                try {
                    res = new JSONObject()
                            .put("PetId", setting.Get_Pet_Info().get(0).PetId)
                            .put("Date", cur_date.getText())
                            .put("Title", title_in.getText())
                            .put("Content", content_in.getText())
                            .toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String message  = "/new_diary/" + res;
                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);

                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println(message);
                String response = "";   // 伺服器給予的回應字串，用於判定登入是否成功以及身分別
                response = in.readLine();

                if(response.split("/")[1].equals("diary_add_success")){

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = new Date(System.currentTimeMillis());
                    out.println("/diary_pic/"+ setting.Get_Pet_Info().get(0).PetId + "/" + formatter.format(date) + "/" + encoded_img);

                    out.println("/get_diary/" + setting.Get_Pet_Info().get(0).PetId);
                    response = in.readLine();
                    if(response.split("/")[1].equals("get_diary_response")){
                        Pet.diary_title = response.split("/")[2].split(",");
                        Pet.diary_content = response.split("/")[3].split(",");
                        Pet.diary_profile = response.split("/", 5)[4].split(",");
                    }
                    Intent back = new Intent();
                    back.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    back.setClass(WriteDiary.this, PetDiary.class);
                    startActivity(back);
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