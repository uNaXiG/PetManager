package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AlarmSetting extends AppCompatActivity {

    Settings settings;
    TextView title;
    TextView dis_time;

    int cur_hour, cur_min;
    TimePickerDialog timePickerDialog;
    Calendar calendar;

    String hour, min;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting);
        transparentStatusBar();

        settings = (Settings) getApplicationContext();

        // 標題 //
        title = findViewById(R.id.title);
        title.setText("寵物" + settings.Get_Pet_Info().get(settings.Get_Select_Pet()).Name + "的餵食提醒");

        // 顯示當下的時間 //
        dis_time = findViewById(R.id.display_time);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        String str = sdf.format(new Date());
        hour = str.split(":")[0];
        min = str.split(":")[1];
        dis_time.setText(str);
        // 選擇時間 //
        dis_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calendar= Calendar.getInstance();
                cur_hour = calendar.get(Calendar.HOUR_OF_DAY);
                cur_min = calendar.get(Calendar.MINUTE);
                timePickerDialog = new TimePickerDialog(AlarmSetting.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        hour = String.format("%02d", h);
                        min = String.format("%02d", m);
                        dis_time.setText(hour + " : " + min);
                    }
                }, cur_hour, cur_min, false);
                timePickerDialog.show();;
            }
        });

        // 設定餵食 //
        MaterialButton set_alarm = findViewById(R.id.set_alarm);
        set_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
                intent.putExtra(AlarmClock.EXTRA_HOUR, Integer.parseInt(hour));
                intent.putExtra(AlarmClock.EXTRA_MINUTES, Integer.parseInt(min));

                //intent.putExtra(AlarmClock.EXTRA_MESSAGE, title.getText().toString());
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivity(intent);
                }
                else{
                    Toast.makeText(AlarmSetting.this, "您裝置並無可以支援餵食提醒功能的APP", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 取消 //
        MaterialButton cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { finish(); }
        });



    }






    private void transparentStatusBar() {
        //改變狀態列顏色為透明
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
}