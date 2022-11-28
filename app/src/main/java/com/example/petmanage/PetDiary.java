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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
public class PetDiary extends AppCompatActivity {

    ListView diaryListView;
    Button addDiaryButton;

    String Title[] = {"11/23","11/24","11/25","11/26","11/27"};
    String sec[] = {"我是狗",
            "我是兔子",
            "我是貓",
            "我也是狗",
            "我還是狗"};
    int image[] = {R.drawable.img1,R.drawable.img2,R.drawable.img3,R.drawable.img4,R.drawable.img5};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_diary);

        diaryListView = (ListView) findViewById(R.id.diary_list);
        addDiaryButton = (Button) findViewById(R.id.add_diary_button);

        MyAdapter myAdapter = new MyAdapter(this,Title,sec,image);
        diaryListView.setAdapter(myAdapter);
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