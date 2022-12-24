package com.example.petmanage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Healthy extends AppCompatActivity {

    Settings settings;

    private Thread thread;
    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket
    PrintWriter out;
    BufferedReader in;

    Spinner spinner;
    String eat_res, pupu_res;
    EditText eat_count, water_count, urine_count;
    TextView title, result;

    private NotificationManager msg_accept;
    private Notification notify_accept;

    private NotificationManager msg_reject;
    private Notification notify_reject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthy);
        transparentStatusBar();

        settings = (Settings) getApplicationContext();

        server_ip = settings.Get_IP();
        server_port = settings.Get_Port();

        // 標題 /
        title = (TextView)findViewById(R.id.health_title);
        title.setText(settings.Get_Pet_Info().get(settings.Get_Select_Pet()).Name + "的健康管理");


        // 吃的 //
        RadioGroup eatgp = (RadioGroup)findViewById(R.id.eat_gp);
        Healthy.RadioGroupListener listener_eat = new RadioGroupListener();
        eatgp.setOnCheckedChangeListener(listener_eat);

        // 份量 //
        eat_count = (EditText) findViewById(R.id.eatnum);

        // 便便 //
        RadioGroup pupugp = (RadioGroup)findViewById(R.id.pupu_gp);
        Healthy.RadioGroupListener listener_pupu = new RadioGroupListener();
        pupugp.setOnCheckedChangeListener(listener_pupu);

        // 水量 //
        water_count = (EditText) findViewById(R.id.water);

        // 尿尿 //
        urine_count = (EditText) findViewById(R.id.urine);

        spinner = (Spinner) findViewById(R.id.spinner_energy);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this,
                        R.array.health_energy,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(spnOnItemSelected);

        result = (TextView) findViewById(R.id.health_result);


        // 送出 //
        Button submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eat_count.getText().toString().isEmpty() || water_count.getText().toString().isEmpty() || urine_count.getText().toString().isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(Healthy.this);
                    builder.setTitle("健康管理");
                    builder.setMessage("若有空白未輸入數值，系統將忽略不計，但會影響一定程度的準確性喔，請問依然要送出嗎？");
                    builder.setIcon(R.drawable.icon);
                    builder.setPositiveButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    builder.setNegativeButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            thread = new Thread(Connection);
                            thread.start();
                        }
                    });
                    builder.create().show();
                }
                else{
                    thread = new Thread(Connection);
                    thread.start();
                }

            }
        });

        // 返回 //
        TextView back = (TextView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish(); }
        });

        //建立通知
        msg_accept = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("web","通知",NotificationManager.IMPORTANCE_HIGH);
            msg_accept.createNotificationChannel(channel);
        }
        Bitmap edit_fail = BitmapFactory.decodeResource(getResources(), R.drawable.error);

        msg_reject = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel("web","通知",NotificationManager.IMPORTANCE_HIGH);
            msg_reject.createNotificationChannel(channel);
        }
        notify_reject = new NotificationCompat.Builder(this,"web")
                .setContentTitle("健康管理")
                .setContentText(settings.Get_Pet_Info().get(settings.Get_Select_Pet()).Name + "今天已經回報過囉！")
                .setSmallIcon(R.mipmap.app_icon)
                .setLargeIcon(edit_fail)
                .build();
    }

    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group,int checkedID)
        {
            // 吃的
            if(checkedID == R.id.wet) eat_res = "wet";
            else if(checkedID == R.id.dry) eat_res = "dry";

            // 便便
            if(checkedID == R.id.hard) pupu_res = "hard";
            else if(checkedID == R.id.soft) pupu_res = "soft";

        }
    }


    private AdapterView.OnItemSelectedListener spnOnItemSelected
            = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String sPos=String.valueOf(pos);
            String sInfo=parent.getItemAtPosition(pos).toString();
        }
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    private Runnable Connection=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());

                settings.Set_reg_date(formatter.format(date));
                String res = "";
                try {
                    res = new JSONObject()
                            .put("PetId", settings.Get_Pet_Info().get(settings.Get_Select_Pet()).PetId)
                            .put("Food", eat_res)
                            .put("Eat", eat_count.getText())
                            .put("Pupu", pupu_res)
                            .put("Water", water_count.getText())
                            .put("Urine", urine_count.getText())
                            .put("Energy", spinner.getSelectedItem().toString())
                            .put("Date", formatter.format(date))
                            .toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String message = "/health/" + res ;
                Log.d("TCP", message);

                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);

                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println(message);
                String response = in.readLine();


                if(response.split("/")[1].equals("health_result")){
                    //show_res.setVisibility(View.VISIBLE);
                    Log.d("Response", "OK");
                    if(response.split("/")[2].equals("health")) { result.setTextColor(Color.argb(255,0,200,32)); }  // 健康給綠色
                    else if(response.split("/")[2].equals("unhealth")) { result.setTextColor(Color.argb(255,200,0,32)); }   // 不健康紅色
                    else if(response.split("/")[2].equals("exist")) {   // 存在
                        msg_reject.notify(1, notify_reject); // 通知
                        return;
                    }
                    result.setText(response.split("/")[3]); // 顯示 建不健康+分數
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