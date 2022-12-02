package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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
import java.io.PrintWriter;
import java.net.Socket;

public class PetDiary extends AppCompatActivity {

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    PrintWriter out;
    BufferedReader in;
    Settings setting;

    ListView diaryListView;
    Button addDiaryButton;

    ShapeableImageView pet_icon;
    TextView pet_name;
    String title[] = {"【2022-11-25】哭啊","【2022-11-26】喜哩靠","【2022-11-28】好想贏韓國","【2022-11-29】世足"};
    String contant[] = {"我想拿A",
            "現在放棄，寒假就開始了",
            "怎樣都要畢業吧",
            "C羅攜鑼去西螺吸螺XD"};

    int image[] = {R.drawable.giwawa,R.drawable.peko_img,R.drawable.bg_img,R.drawable.show_pet_pic};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_diary);

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        pet_name = (TextView) findViewById(R.id.pet_name);
        pet_name.setText("【" + setting.Get_Pet_Info().get(0).PetId + "】" + setting.Get_Pet_Info().get(0).Name + " 的日記本");

        diaryListView = (ListView) findViewById(R.id.diary_list);
        addDiaryButton = (Button) findViewById(R.id.add_diary_button);

        MyAdapter myAdapter = new MyAdapter(this, title, contant, image);
        diaryListView.setAdapter(myAdapter);
        diaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String title_str = title[(int) id];
                Toast.makeText(PetDiary.this, title_str, Toast.LENGTH_SHORT).show();
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