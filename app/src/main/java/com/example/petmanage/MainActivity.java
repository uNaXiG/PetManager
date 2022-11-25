package com.example.petmanage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity {

    Settings setting;
    LinearLayout layout;
    String pets;
    ArrayList<Pet> pet_info;
    private final int GALLERY_REQ_CODE = 1000;

    Thread thread;

    ImageView iv;
    Button btn;

    private DrawerLayout drawerLayout;//滑動選單
    private NavigationView navView;//導航檢視

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        transparentStatusBar(); // 透明化狀態列

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
        setting = (Settings) getApplicationContext();
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

        // json decode //
        Gson gson = new Gson();
        pet_info = new ArrayList<Pet>(); // 紀錄每隻寵物資訊

        Set_Visual(pet_set, gson);

        setting.Set_Pet_info(pet_info);

        // 設定名稱
        TextView uname = (TextView) headerView.findViewById(R.id.user_name);
        uname.setText(setting.Get_reg_name());

        navView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.item_info:      // 選擇個人資料
                    Intent go_to_personal = new Intent();
                    go_to_personal.setClass(MainActivity.this, EditPersonalInfo.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_personal);
                    break;

                case R.id.item_add:    // 新增寵物頁面
                    setting.Set_First(false);
                    Intent go_to_add_pet = new Intent();
                    go_to_add_pet.setClass(MainActivity.this, ReadPetID.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_add_pet);
                    break;

                case R.id.item_analyze:   // 情緒

                    break;

                case R.id.item_diary:      // 日記

                    break;

                case R.id.item_setting:   // 設定
                    Intent go_to_setting = new Intent();
                    go_to_setting.setClass(MainActivity.this, SystemSetting.class);   // 跳轉到編輯資料頁面
                    startActivity(go_to_setting);
                    break;

                case R.id.item_contact:   // 寫信

                    break;

                case R.id.item_exit:      // 登出
                    finish();
                    break;

                default: break;
            }
            //關閉滑動選單
            drawerLayout.closeDrawer(GravityCompat.START);
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
            imgv.setImageResource(R.drawable.show_pet_pic);
            imgv.setId(i);
            int finalI = i;
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
        }
    }

    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}