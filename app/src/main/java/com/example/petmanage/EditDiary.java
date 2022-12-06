package com.example.petmanage;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

public class EditDiary extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_diary);

        EditText edi = (EditText) findViewById(R.id.test);
        edi.setText(Pet.diary_content[Pet.Select_Diary]);
    }
}