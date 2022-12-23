package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

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

    Settings setting;

    Button addDiaryButton;

    ShapeableImageView pet_icon;
    TextView pet_name;

    LinearLayout layout;
    String[] title, content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_diary);
        transparentStatusBar();

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        pet_name = (TextView) findViewById(R.id.pet_name);

        addDiaryButton = (Button) findViewById(R.id.add_diary_btn);
        TextView back = (TextView) findViewById(R.id.back);

        try {
            title = Pet.diary_title.clone();

            content = Pet.diary_content.clone();

            layout = (LinearLayout) findViewById(R.id.layout);

            pet_name.setText(setting.Get_Pet_Info().get(setting.Get_Select_Pet()).PetId + " -- " + setting.Get_Pet_Info().get(setting.Get_Select_Pet()).Name + " 的日記本");

            Set_Visual();
        }
        catch (Exception ex){

            pet_name.setText("目前這邊沒有任何日記喔~ 點擊下方按鈕新增");

        }
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // clear pet array //
                Pet.diary_title = new String[0];
                Pet.diary_content = new String[0];
                Pet.diary_profile = new String[0];
                finish();
            }

        });
        addDiaryButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent write_diary = new Intent();
                write_diary.setClass(PetDiary.this, WriteDiary.class);
                startActivity(write_diary);
            }

        });

    }

    private void Set_Visual(){
        for(int i = 0; i < title.length; i++){

            LinearLayout.LayoutParams layout_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout_params.setMargins(50,70,50,10);

            LinearLayout first_layout = new LinearLayout(this);
            first_layout.setOrientation(LinearLayout.VERTICAL);
            first_layout.setBackgroundColor(0x80ffffff);
            first_layout.setLayoutParams(layout_params);
            first_layout.setId(i);
            int finalI = i;
            first_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(PetDiary.this, String.valueOf(finalI), Toast.LENGTH_SHORT).show();
                    Pet.Select_Diary = finalI;
                    Intent intent = new Intent();
                    intent.setClass(PetDiary.this, EditDiary.class);
                    startActivity(intent);
                }
            });

            layout.addView(first_layout);

            // 標題
            TextView tx = new TextView(this);
            tx.setTextColor(0xffff8a00);
            LinearLayout.LayoutParams tx_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tx_params.setMargins(10,30,10,30);
            tx.setLayoutParams(tx_params);
            tx.setTextSize(22);
            tx.setText(title[i]);
            first_layout.addView(tx);

            // 內容
            TextView tx1 = new TextView(this);
            tx1.setTextColor(0xffff8a00);
            LinearLayout.LayoutParams tx1_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tx1_params.setMargins(20,10,10,10);
            tx1.setLayoutParams(tx1_params);
            tx1.setTextSize(18);
            char[] char_of_content = content[i].toCharArray();
            String part_of_content = "";

            for(int j = 0; j < char_of_content.length; j++){
                if(String.valueOf(char_of_content[j]).equals("*") || j >= 20) break;
                part_of_content += String.valueOf(char_of_content[j]);
            }

            tx1.setText(part_of_content + "......");
            first_layout.addView(tx1);

        }
    }

    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}