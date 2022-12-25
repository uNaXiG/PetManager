package com.example.petmanage;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Thread thread;  // send edited profile info
    private Thread thread2; // to get pet profile
    private Thread thread3; // to get pet healthy

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    PrintWriter out;
    BufferedReader in;
    Settings setting;

    LinearLayout layout;
    String pets;
    Bitmap pet_profile;
    ArrayList<Pet> pet_info;
    private final int GALLERY_REQ_CODE = 1000;

    ImageView iv;
    String sel_pet_id;
    String img_base64;

    private DrawerLayout drawerLayout;//滑動選單
    private NavigationView navView;//導航檢視

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        transparentStatusBar(); // 透明化狀態列

        setting = (Settings) getApplicationContext();
        server_ip = setting.Get_IP();
        server_port = setting.Get_Port();

        // 一些滑動選單的設定值 //
        drawerLayout = findViewById(R.id.drawer_layout);
        TextView btn = (TextView) findViewById(R.id.toolbar);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 滑動選單 //
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        navView = findViewById(R.id.nav_view);
        View headerView = navView.getHeaderView(0);
        layout = (LinearLayout) findViewById(R.id.layout);

        // 將多筆寵物的json字串各別取出 //
        pets = setting.Get_pets();
        String[] pet_set;
        try {
            pet_set = pets.split("&");
        }catch (Exception e){
            pet_set = new String[]{""};
            pet_set[0] = pets;
        }
        pet_profile = BitmapFactory.decodeResource(getResources(), R.drawable.show_pet_pic);

        // json decode //
        Gson gson = new Gson();
        pet_info = new ArrayList<Pet>(); // 紀錄每隻寵物資訊

        Set_Visual(pet_set, gson);

        setting.Set_Pet_info(pet_info);



        // 設定名稱
        TextView uname = (TextView) headerView.findViewById(R.id.user_name);
        uname.setText(setting.Get_reg_name());

        // 設定頭像
        ImageView profile = (ImageView) headerView.findViewById(R.id.profile);
        profile.setImageBitmap(setting.get_profile());

        String pets[] = new String[setting.Get_Pet_Info().size()];
        for(int i = 0; i < setting.Get_Pet_Info().size(); i++) pets[i]=setting.Get_Pet_Info().get(i).Name;


        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.item_info:      // 選擇個人資料
                    Intent go_to_personal = new Intent();
                    go_to_personal.setClass(MainActivity.this, EditPersonalInfo.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_personal);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.item_add:    // 新增寵物頁面
                    setting.Set_First(false);
                    Intent go_to_add_pet = new Intent();
                    go_to_add_pet.setClass(MainActivity.this, ReadPetID.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_add_pet);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.item_healthy: // 健康
                    // 初始化寵物名單陣列 //
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                    builder2.setTitle("您要管理哪隻寵物的健康紀錄呢？"); //設置它的標題
                    final int[] x2 = {0};
                    builder2.setSingleChoiceItems(pets,0,new DialogInterface.OnClickListener() {
                        //把它設置為單選的型態
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            x2[0] = i;
                        }
                    });
                    builder2.setNegativeButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setting.Set_Select_Pet(x2[0]);
                            thread3 =  new Thread(RequestToGetHealthy);
                            thread3.start();

                        }
                    });
                    builder2.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    builder2.create().show(); //也是一樣記得創建他並顯示

                    break;
                case R.id.item_analyze:   // 情緒
                    // 初始化寵物名單陣列 //
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setTitle("您要填寫哪隻寵物的情緒分析呢？"); //設置它的標題
                    final int[] x1 = {0};
                    builder1.setSingleChoiceItems(pets,0,new DialogInterface.OnClickListener() {
                        //把它設置為單選的型態
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            x1[0] = i;
                        }
                    });
                    builder1.setNegativeButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setting.Set_Select_Pet(x1[0]);
                            Intent go_to_healthy = new Intent();
                            go_to_healthy.setClass(MainActivity.this, Healthy.class);   // 跳轉到健康頁面
                            startActivity(go_to_healthy);
                            //關閉滑動選單
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                    });
                    builder1.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    builder1.create().show(); //也是一樣記得創建他並顯示
                    break;

                case R.id.item_diary:      // 日記

                    // 初始化寵物名單陣列 //
                    AlertDialog.Builder builder=new AlertDialog.Builder(this);
                    builder.setTitle("您要看哪隻寵物的日記呢？"); //設置它的標題
                    final int[] x = {0};
                    builder.setSingleChoiceItems(pets,0,new DialogInterface.OnClickListener() {
                        //把它設置為單選的型態
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            x[0] = i;
                        }
                    });
                    builder.setNegativeButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            setting.Set_Select_Pet(x[0]);
                            thread2 =  new Thread(RequestToGetDiary);
                            thread2.start();

                        }
                    });
                    builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        return;
                    }
                });
                    builder.create().show(); //也是一樣記得創建他並顯示
                    break;

                case R.id.item_setting:   // 設定
                    Intent go_to_setting = new Intent();
                    go_to_setting.setClass(MainActivity.this, SystemSetting.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_setting);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                    break;

                case R.id.item_contact:   // 寫信

                    break;

                case R.id.item_exit:      // 登出
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
                    setting.set_profile(b);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                    finish();
                    break;

                default: break;
            }



            return true;
        });
    }

    private void Set_Visual(String[] pet_set, Gson gson){
        for(int i = 0; i < pet_set.length; i++){
            Pet pet = gson.fromJson(pet_set[i].toString(), Pet.class);    // 反序列化json成類別
            pet_info.add(pet);    // 加入寵物紀錄

            LinearLayout.LayoutParams layout_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layout_params.setMargins(50,70,50,10);

            LinearLayout first_layout = new LinearLayout(this);
            first_layout.setOrientation(LinearLayout.VERTICAL);
            first_layout.setBackgroundColor(0x80ffffff);
            first_layout.setLayoutParams(layout_params);
            layout.addView(first_layout);

            LinearLayout parent_layout = new LinearLayout(this);
            parent_layout.setOrientation(LinearLayout.HORIZONTAL); //
            parent_layout.setLayoutParams(layout_params);

            first_layout.addView(parent_layout);

            // 加入寵物頭像圖片區域 //
            ImageView imgv = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(450,550);
            params.setMargins(5,18,10,0);
            imgv.setLayoutParams(params);
            imgv.setId(i);
            imgv.setImageBitmap(pet_profile);       // 只差從server取得base64後存進 setting.pet_profile List中，然後根據索引i取出bitmap
            // 修改寵物圖片 //
            imgv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ArrayList<Pet> p = new ArrayList<Pet>(setting.Get_Pet_Info());
                    Pet pet = p.get(setting.Get_Select_Pet());
                    Intent upload = new Intent(Intent.ACTION_PICK);
                    upload.setData((MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                    startActivityForResult(upload, GALLERY_REQ_CODE);
                    int v = view.getId();
                    setting.Set_Select_Pet(v);
                    iv = (ImageView) findViewById(v);
                }
            });
            parent_layout.addView(imgv);

            // 新增一個垂直布局 //
            LinearLayout child_layout = new LinearLayout(this);
            child_layout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams child_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            child_layout.setLayoutParams(child_params);
            parent_layout.addView(child_layout);

            // 逐一將寵物紀錄資料讀出 //
            String[] pet_class = {"晶片           ", "名字           ", "種類           ", "性別           ", "生日           ", "登錄日期   "};
            for(int j = 0; j < 6; j++){
                TextView tx = new TextView(this);
                tx.setTextColor(0xffff8a00);

                LinearLayout.LayoutParams tx_params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                tx_params.setMargins(10,18,0,0);
                tx.setLayoutParams(tx_params);
                tx.setTextSize(16);
                tx.setText(pet_class[j]);
                switch (j){
                    case 0: tx.setText(tx.getText()+pet.PetId);break;
                    case 1: tx.setText(tx.getText()+pet.Name);break;
                    case 2: tx.setText(tx.getText()+pet.Specie);break;
                    case 3: tx.setText(tx.getText()+pet.Gender);break;
                    case 4: tx.setText(tx.getText()+pet.Birthday);break;
                    case 5: tx.setText(tx.getText()+pet.Date.split(" ")[0]);break;
                }
                child_layout.addView(tx);   // 加入寵物資訊
            }

            MaterialButton b = new MaterialButton(this);
            LinearLayout.LayoutParams btn_params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            btn_params.setMargins(80,0,80,20);
            b.setLayoutParams(btn_params);
            b.setBackgroundColor(Color.parseColor("#AAEBD090"));
            b.setTextColor(Color.parseColor("#8B6020"));
            Drawable edit_icon = getResources().getDrawable(R.drawable.edit);
            edit_icon.setBounds(0, 0, edit_icon.getMinimumWidth(), edit_icon.getMinimumHeight());
            b.setCompoundDrawables(null,null,edit_icon,null);
            b.setText("      編    輯");
            b.setId(i);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int v = view.getId();
                    setting.Set_Select_Pet(v);
                }
            });
            first_layout.addView(b);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            iv.setImageURI(data.getData());
            Pet pet = setting.Get_Pet_Info().get(setting.Get_Select_Pet());
            sel_pet_id = pet.PetId;

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData() );
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byte_array = byteArrayOutputStream.toByteArray();
                img_base64 = Base64.encodeToString(byte_array, Base64.NO_WRAP);
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            thread = new Thread(Connection);
            thread.start();
        }
    }

    // 取得健康紀錄的執行續 //
    private Runnable RequestToGetHealthy = new Runnable() {
        @Override
        public void run() {
            try {
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                // 向 server 發送訊息
                out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);
                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("/get_healthy/" + setting.Get_Pet_Info().get(setting.Get_Select_Pet()).PetId);
                String response = "";
                response = in.readLine();

                if(response.split("/")[1].equals("get_healthy_response")){
                    Pet.healthy_title = response.split("/")[2].split("_");
                    Pet.healthy_content = response.split("/")[3].split("_");
                    Pet.healthy_score = response.split("/")[4].split("_");

                    Intent go_to_manage = new Intent();
                    go_to_manage.setClass(MainActivity.this, PetHealthManage.class);   // 跳轉到健康頁面
                    startActivity(go_to_manage);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(response.split("/")[1].equals("empty_healthy")){
                    Intent go_to_manage = new Intent();
                    go_to_manage.setClass(MainActivity.this, PetHealthManage.class);   // 跳轉到健康頁面
                    startActivity(go_to_manage);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
            }
            catch (Exception e){}
        }
    };



    private Runnable RequestToGetDiary = new Runnable() {
        @Override
        public void run() {
            try {
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                // 向 server 發送訊息
                out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);
                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println("/get_diary/" + setting.Get_Pet_Info().get(setting.Get_Select_Pet()).PetId);
                String response = "";
                response = in.readLine();
                if(response.split("/")[1].equals("get_diary_response")){
                    Pet.diary_title = response.split("/")[2].split("_");
                    Pet.diary_content = response.split("/")[3].split("_");
                    Pet.diary_profile = response.split("/", 5)[4].split(",");
                    Intent go_to_diary = new Intent();
                    go_to_diary.setClass(MainActivity.this, PetDiary.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_diary);
                    //關閉滑動選單
                    drawerLayout.closeDrawer(GravityCompat.START);
                }
                else if(response.split("/")[1].equals("empty_diary")){
                    Intent go_to_diary = new Intent();
                    go_to_diary.setClass(MainActivity.this, PetDiary.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_diary);
                }
            }
            catch (Exception e){}
        }
    };

    private Runnable Connection = new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try{
                //輸入 Server 端的 IP
                InetAddress host = InetAddress.getByName(server_ip);
                //建立連線
                clientSocket = new Socket(host, server_port);

                String message = "/edit_pet_img/" + sel_pet_id + "/" + img_base64;
                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);

                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                out.println(message);
                String response = "";   // 伺服器給予的回應字串，用於判定登入是否成功以及身分別
                response = in.readLine();

                if(response.equals("OK")){
                    clientSocket.close();
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