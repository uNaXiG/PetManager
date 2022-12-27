package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class PetHealthManage extends AppCompatActivity {

    Settings setting;

    String[] title, content, score;
    LinearLayout layout;

    public  static int sel_idx;
    public static String sel_title, sel_date, sel_remark;

    private Thread thread;
    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    PrintWriter out;
    BufferedReader in;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_health_manage);
        transparentStatusBar();

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        layout = (LinearLayout) findViewById(R.id.layout);

        // 標題 //
        TextView view_title = (TextView) findViewById(R.id.title_text);
        view_title.setText(setting.Get_Pet_Info().get(setting.Get_Select_Pet()).Name + " 的健康管理");

        sel_idx = 0;
        sel_title = "";
        sel_date = "";
        sel_remark = "";
        try{

            title = Pet.healthy_title.clone();

            content = Pet.healthy_content.clone();

            score = Pet.healthy_score.clone();

            if(title.length > 0){
                LinearLayout before = (LinearLayout)findViewById(R.id.before_layout);
                before.setVisibility(View.INVISIBLE);
            }
            else{
                LinearLayout before = (LinearLayout)findViewById(R.id.before_layout);
                before.setVisibility(View.VISIBLE);
            }

            Set_Visual();
        }
        catch (Exception ex){
        }


        // 返回 //
        TextView back = (TextView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Pet.healthy_title = new String[0];
                Pet.healthy_content = new String[0];
                Pet.healthy_score = new String[0];
                finish();
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
            first_layout.setBackgroundColor(0x99ffffff);
            first_layout.setLayoutParams(layout_params);
            first_layout.setId(i);
            layout.addView(first_layout);

            int score_int = Integer.parseInt(score[i]);
            // 標題
            TextView tx = new TextView(this);

            if(score_int > 50) tx.setTextColor(0xff00bd90);
            else tx.setTextColor(0xffdd4040);

            LinearLayout.LayoutParams tx_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tx_params.setMargins(20,30,10,30);
            tx.setLayoutParams(tx_params);
            tx.setTextSize(22);
            tx.setText(title[i] + " 的健康紀錄");
            first_layout.addView(tx);

            // 內容
            TextView tx1 = new TextView(this);

            if(score_int > 50) tx1.setTextColor(0xff00bd90);
            else tx1.setTextColor(0xffdd4040);

            LinearLayout.LayoutParams tx1_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tx1_params.setMargins(20,10,10,0);
            tx1.setLayoutParams(tx1_params);
            tx1.setTextSize(18);

            tx1.setText(content[i].replace("===", "\t\t\t\t").replace("***", "\n"));
            first_layout.addView(tx1);

            // 分數
            TextView tx2 = new TextView(this);

            if(score_int > 50) tx2.setTextColor(0xff00bd90);
            else tx2.setTextColor(0xffdd4040);
            LinearLayout.LayoutParams tx2_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tx2_params.setMargins(0,5,20,5);
            tx2.setLayoutParams(tx2_params);
            tx2.setTextSize(22);
            tx2.setGravity(Gravity.RIGHT);
            tx2.setText("評估分數：" + score[i] + "  ");
            first_layout.addView(tx2);


            //test
            MaterialButton b = new MaterialButton(this);
            LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btn_params.setMargins(80,0,80,20);
            b.setLayoutParams(btn_params);
            b.setBackgroundColor(Color.parseColor("#00BD90"));
            b.setTextColor(Color.parseColor("#ff000000"));
            b.setText("編輯就醫紀錄");
            b.setId(i);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int v = view.getId();
                    //Toast.makeText(PetHealthManage.this, "select : " + title[v] + " " + setting.Get_Pet_Info().get(setting.Get_Select_Pet()).PetId , Toast.LENGTH_SHORT).show();
                    sel_idx = v;
                    sel_title = "編輯" + setting.Get_Pet_Info().get(setting.Get_Select_Pet()).Name + "的就醫紀錄";
                    sel_date = title[sel_idx];
                    sel_remark = Pet.healthy_medical[sel_idx].equals("<empty>")? "" : Pet.healthy_medical[v];
                    Intent go_to_Medical = new Intent();
                    go_to_Medical.setClass(PetHealthManage.this, MedicalRecord.class);
                    startActivity(go_to_Medical);
                }
            });
            first_layout.addView(b);
        }
    }

    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}