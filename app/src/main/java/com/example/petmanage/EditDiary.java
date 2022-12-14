package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class EditDiary extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_diary);
        transparentStatusBar();

        TextView title = (TextView) findViewById(R.id.title);   // 顯示標題
        title.setText(Pet.diary_title[Pet.Select_Diary]);

        byte[] decodedString = Base64.decode(Pet.diary_profile[Pet.Select_Diary], Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); // 日記圖片

        ImageView show_img = (ImageView) findViewById(R.id.img);    // 顯示圖片
        show_img.setImageBitmap(decodedByte);

        TextView content = (TextView) findViewById(R.id.content);   // 顯示內容
        content.setText(Pet.diary_content[Pet.Select_Diary].replace("***", "\n"));

        TextView back  = (TextView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }



    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}