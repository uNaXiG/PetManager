package com.example.petmanage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

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
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ReadPetID extends AppCompatActivity {

    Settings settings;
    private Thread thread;

    String server_ip;   // 伺服器IP
    int server_port;       // port number
    private Socket clientSocket;    //客戶端的socket

    boolean isReading = false;  // 判定是否正要掃描

    PrintWriter out;
    BufferedReader in;

    EditText in_pet_id;
    EditText in_pet_name;
    String gender="";
    Spinner spinner;

    Button birthday;
    DatePickerDialog.OnDateSetListener  mDate;

    MaterialButton submit;
    TextView rej;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_read_pet_id);

        transparentStatusBar();
        settings = (Settings) getApplicationContext();

        server_ip = settings.Get_IP();
        server_port = settings.Get_Port();

        TextView sys_txt = (TextView) findViewById(R.id.show_text); // 顯示系統提示的字樣
        ImageView btn = (ImageView) findViewById(R.id.show_reader); // 啟動掃描器按鈕

        in_pet_id = (EditText)findViewById(R.id.pet_id);    // 寵物ID
        in_pet_name = (EditText)findViewById(R.id.pet_name);    // 寵物name
        spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(this,
                        R.array.petspecies_array,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0, false);
        spinner.setOnItemSelectedListener(spnOnItemSelected);

        RadioGroup radioGroup = (RadioGroup)findViewById(R.id.pet_gender);
        RadioGroupListener listener =new RadioGroupListener();
        radioGroup.setOnCheckedChangeListener(listener);
        rej = (TextView) findViewById(R.id.reject_code);
        // 按下掃描器時 //
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isReading){
                    AlertDialog.Builder builder = new AlertDialog.Builder(ReadPetID.this);
                    builder.setTitle("RFID reader");
                    builder.setMessage("In development...");
                    builder.create().show();

                    btn.setImageDrawable(getResources().getDrawable(R.drawable.reader_img));    // 切換圖片
                    sys_txt.setText("Please scan your pet chip !");     // 切換提示
                    isReading = true;
                    Scan_Rfid();    // 執行掃瞄晶片的功能
                }
                else{
                    btn.setImageDrawable(getResources().getDrawable(R.drawable.unread_img));    // 切換圖片
                    sys_txt.setText("Please click to start your setting :)");   // 切換提示
                    isReading = false;
                }
            }
        });

        // 生日
        birthday = (Button) findViewById(R.id.birthday);
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(ReadPetID.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDate,year,month,day);
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


        // 送出
        submit = (MaterialButton) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if(in_pet_id.length() != 15){
                    Toast.makeText(ReadPetID.this, "pet ID format is incorrect ", Toast.LENGTH_SHORT).show();
                    return;
                }*/
                if("".equals(in_pet_id.getEditableText().toString())){
                    Toast.makeText(ReadPetID.this, "Pet ID have not been filled!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if("".equals(in_pet_name.getEditableText().toString())){
                    Toast.makeText(ReadPetID.this, "Pet name have not been filled!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if("Select Pet Species".equals(spinner.getSelectedItem().toString())){
                    Toast.makeText(ReadPetID.this, "Pet species have not been selected!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if("".equals(gender)){
                    Toast.makeText(ReadPetID.this, "Pet gender have not been selected!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if("Select Birthday".equals(birthday.getText().toString())){
                    Toast.makeText(ReadPetID.this, "Pet birthday have not been selected!", Toast.LENGTH_SHORT).show();
                    return;
                }
                /*if("".equals(in_pet_id.getEditableText().toString()) || "".equals(in_pet_name.getEditableText().toString())|| "Select Pet Species".equals(spinner.getSelectedItem().toString())|| "".equals(gender)|| "Select Birthday".equals(birthday.getText().toString())){
                    Toast.makeText(ReadPetID.this, "There are items that have not been filled!", Toast.LENGTH_SHORT).show();
                    return;
                }*/
                else{
                    InputMethodManager imm = (InputMethodManager) getSystemService(RegisterActivity.INPUT_METHOD_SERVICE); imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    thread = new Thread(Connection);
                    thread.start();
                }
            }
        });

        // 返回
        MaterialButton go_back = (MaterialButton) findViewById(R.id.back);
        go_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settings.Set_Add(false);
                finish();
            }
        });

    }

    class RadioGroupListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup group,int checkedID)
        {
            if(checkedID == R.id.male){
                gender="Boy";
            }
            else if(checkedID == R.id.female){
                gender="Girl";
            }
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
            //
        }
    };

        private void Scan_Rfid(){
        //todo
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
                //in_pet_id = (EditText)findViewById(R.id.pet_id);    // 寵物ID
                //in_pet_name = (EditText)findViewById(R.id.pet_name);    // 寵物name
                //EditText in_pet_specie = (EditText)findViewById(R.id.pet_specie);    // 寵物種類
                //EditText in_pet_gender = (EditText)findViewById(R.id.pet_gender);    // 寵物性別???咪咪幫幫忙

                SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());

                settings.Set_reg_date(formatter.format(date));
                String res = "";
                try {
                    res = new JSONObject()
                            .put("PetId", in_pet_id.getText().toString())
                            .put("Owner", settings.Get_reg_account())
                            .put("Name", in_pet_name.getText().toString())
                            .put("Specie", spinner.getSelectedItem().toString())
                            .put("Gender", gender)
                            .put("Birthday", birthday.getText().toString())
                            .put("Date", formatter.format(date))
                            .toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String message = "/add_pet/" + res ;
                if(settings.Get_First()) {
                    message += "/first";
                }
                else message += "/non_first";
                Log.d("TCP", message);

                // 向 server 發送訊息
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),true);
                // 接收 server 發來的訊息
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.println(message);
                String response = in.readLine();    // server回覆是否允許加入

                // 讓新用戶加入一個新寵物ID //
                if(response.contains("seccuss") && settings.Get_First()){
                    settings.Set_Add(true);
                    settings.Set_First(false);
                    settings.Set_pets(res);
                    // 通過
                    // 前往應用程式主頁面 //
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ReadPetID.this, MainActivity.class);
                    startActivity(intent);
                }
                else if(response.contains("seccuss") && !settings.Get_First()){
                    settings.Set_Add(true);
                    String pets = settings.Get_pets() + "&" + response.split("/")[3];
                    settings.Set_pets(pets);
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ReadPetID.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(response.contains("reject")){
                    settings.Set_Add(false);
                    // 拒絕
                    rej.setText("Pet ID is exist !");
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