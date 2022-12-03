package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class PetDiary extends AppCompatActivity {

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket
    private Thread thread;
    int lock = 0;

    PrintWriter out;
    BufferedReader in;
    Settings setting;

    ListView diaryListView;
    Button addDiaryButton;

    ShapeableImageView pet_icon;
    TextView pet_name;

    String[] title;
    String[] contant;
    int image[] = {R.drawable.giwawa, R.drawable.giwawa};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_diary);

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        pet_name = (TextView) findViewById(R.id.pet_name);
        pet_name.setText("【" + setting.Get_Pet_Info().get(0).PetId + "】" + setting.Get_Pet_Info().get(0).Name + " 的日記本");

        // 讀取伺服器資料 //
        thread = new Thread(Connection);
        thread.start();
        while (lock == 0) continue;

        //pet_name.setText("unlock");

        /*MyAdapter myAdapter = new MyAdapter(this, title, contant, image);
        diaryListView.setAdapter(myAdapter);
        diaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title_str = title[(int) id];
                Toast.makeText(PetDiary.this, title_str, Toast.LENGTH_SHORT).show();
            }
        });*/

        diaryListView = (ListView) findViewById(R.id.diary_list);
        addDiaryButton = (Button) findViewById(R.id.add_diary_btn);


        addDiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent write_diary = new Intent();
                write_diary.setClass(PetDiary.this, WriteDiary.class);
                startActivity(write_diary);
            }
        });




        TextView back = (TextView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        transparentStatusBar();
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

                String message  ="/get_diary/" + setting.Get_Pet_Info().get(0).PetId;
                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);

                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println(message);
                String response = "";   // 伺服器給予的回應字串，用於判定登入是否成功以及身分別
                response = in.readLine();

                if(response.split("/")[1].equals("get_diary_response")){
                    title = response.split("/")[2].split(",").clone();
                    contant = response.split("/")[3].split(",").clone();
                    lock = 2;
                    clientSocket.close();
                }
                else if(response.split("/")[1].equals("empty_diary")){

                }

            }
            catch(Exception e){
                e.printStackTrace();
                Log.e("text","Socket連線="+e.toString());
                finish();    //當斷線時自動關閉 Socket
            }
        }
    };

    class MyAdapter extends ArrayAdapter<String>{

        Context context;
        String rTitle[];
        String rsec[];
        int rimage[];

        MyAdapter (Context context,String title[],String sec[],int image[]){
            super(context,R.layout.diary_listview,R.id.date,title);
            this.context = context;
            this.rTitle = title;
            this.rsec = sec;
            this.rimage = image;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View row = layoutInflater.inflate(R.layout.diary_listview,parent,false);
            ImageView images = row.findViewById(R.id.diary_image);
            TextView textView = row.findViewById(R.id.date);
            TextView textView1 = row.findViewById(R.id.diary_content);

            images.setImageResource(rimage[position]);
            textView.setText(rTitle[position]);
            textView1.setText(rsec[position]);

            return row;
        }
    }


    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}