package com.example.petmanage;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class Pet {
    public String PetId;
    public String Owner;
    public String Name;
    public String Specie;
    public String Gender;
    public String Birthday;
    public String Date;

    public static String[] diary_title;
    public static String[] diary_content;
    public static String[] diary_profile;

    public static String[] healthy_title;
    public static String[] healthy_content;
    public static String[] healthy_score;

    public static int Select_Diary;
    public Pet(){ }

    public Pet(String PetId, String Owner, String Name, String Specie, String Gender, String Birthday, String Date){
        this.PetId = PetId;
        this.Owner = Owner;
        this.Name = Name;
        this.Specie = Specie;
        this.Gender = Gender;
        this.Birthday = Birthday;
        this.Date = Date;
    }

}
