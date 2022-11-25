package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    Settings settings; // 取得類別物件
    String confirm;

    private Thread thread;

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    PrintWriter out;
    BufferedReader in;

    Button birthday;
    DatePickerDialog.OnDateSetListener  mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        transparentStatusBar();
        settings = (Settings) getApplicationContext();

        server_ip = settings.Get_IP();
        server_port = settings.Get_Port();

        // 生日
        birthday = (Button) findViewById(R.id.birthday);
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDate,year,month,day);
                dialog.getDatePicker().setMaxDate((System.currentTimeMillis()));
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;
                birthday.setText(String.format("%02d", year) + "-" + String.format("%02d", month) + "-" + String.format("%02d", day));
            }
        };

        // 下一步
        MaterialButton submit = (MaterialButton) findViewById(R.id.reg_submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText user_name = (EditText)findViewById(R.id.username);
                settings.Set_reg_name(user_name.getText().toString());

                RadioGroup radioGroup = findViewById(R.id.gender_gp);
                int gender_num = radioGroup.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(gender_num);
                if(radioButton != null) settings.Set_reg_gender(radioButton.getText().toString());

                settings.Set_reg_birthday(birthday.getText().toString());

                EditText user_account = (EditText)findViewById(R.id.user_account);
                settings.Set_reg_account(user_account.getText().toString());

                EditText user_pwd = (EditText)findViewById(R.id.password);
                settings.Set_reg_pwd(user_pwd.getText().toString());

                EditText confirm_pwd = (EditText)findViewById(R.id.confirm_pwd);
                confirm = confirm_pwd.getText().toString();

                EditText user_email = (EditText)findViewById(R.id.email);
                settings.Set_reg_email(user_email.getText().toString());

                EditText user_phone = (EditText)findViewById(R.id.phone);
                settings.Set_reg_phone(user_phone.getText().toString());

                String account = settings.Get_reg_account();
                String pwd = settings.Get_reg_pwd();
                String email = settings.Get_reg_email();
                String phone = settings.Get_reg_phone();

                /*if(!account.matches(".*[a-zA-Z].*" )|| account.length() < 8){
                    Toast.makeText(RegisterActivity.this, "account should be include at least a letter and at least 8 character", Toast.LENGTH_SHORT).show();
                    //pr.setText("login error");
                    return;
                }
                else if(!pwd.matches(".*[a-zA-Z].*") || !pwd.matches(".*[0-9].*")){
                    Toast.makeText(RegisterActivity.this, "password should be include at least a letter and a number", Toast.LENGTH_SHORT).show();
                    //pr.setText("login error");
                    return;
                }
                else if( !pwd.equals(confirm) ){
                    Toast.makeText(RegisterActivity.this, "confirm password is not the same", Toast.LENGTH_SHORT).show();
                    //pr.setText("login error");
                    return;
                }
                else if(!email.matches("^\\w{1,63}@[a-zA-Z0-9]{2,63}\\.[a-zA-Z]{2,63}(\\.[a-zA-Z]{2,63})?$" )){
                    Toast.makeText(RegisterActivity.this, "email format is incorrect ", Toast.LENGTH_SHORT).show();
                    //pr.setText("login error");
                    return;
                }
                else if(!phone.matches("[0][9][0-9]{8}" )){
                    Toast.makeText(RegisterActivity.this, "phone format is incorrect ", Toast.LENGTH_SHORT).show();
                    //pr.setText("login error");
                    return;
                }*/
                InputMethodManager imm = (InputMethodManager) getSystemService(RegisterActivity.INPUT_METHOD_SERVICE); imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                thread = new Thread(Connection);
                thread.start();
            }
        });
        // 返回
        MaterialButton go_back = (MaterialButton) findViewById(R.id.back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private Runnable Connection=new Runnable(){
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
                            .put("Name", settings.Get_reg_name())
                            .put("Gender", settings.Get_reg_gender())
                            .put("Birthday", settings.Get_reg_birthday())
                            .put("Account", settings.Get_reg_account())
                            .put("Password", settings.Get_reg_pwd())
                            .put("Email", settings.Get_reg_email())
                            .put("Phone", settings.Get_reg_phone())
                            .toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String message = "/register/" + res;
                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);
                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println(message);
                String response = "";   // 伺服器給予的回應字串，用於判定登入是否成功以及身分別

                response = in.readLine();
                TextView rej = (TextView) findViewById(R.id.reject_code);
                //pr.setText(response);

                if(response.equals("/register/seccuss")){
                    settings.Set_First(true);
                    settings.Set_pets("");
                    // 通過
                    // 前往新增寵物頁面 //
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(RegisterActivity.this, ReadPetID.class);
                    startActivity(intent);
                }
                if(response.contains("reject")){
                    // 拒絕

                    switch (response.split(("/"))[3]){
                        // 被拒絕的原因 //
                        case "account":
                            rej.setText("Account is exist.");
                            break;
                        case "email":
                            rej.setText("Email is used.");
                            break;
                        case "phone":
                            rej.setText("Phone number is used.");
                            break;
                        default:break;
                    }
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