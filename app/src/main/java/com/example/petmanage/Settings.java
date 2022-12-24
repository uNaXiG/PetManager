package com.example.petmanage;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;

import java.util.ArrayList;

public class Settings extends Application {

    private String server_ip = "192.168.1.103";   // 伺服器IP
    private int port = 8080;     // 伺服器port

    private String reg_name;
    private String reg_gender;
    private String reg_birthday;
    private String reg_account;
    private String reg_pwd;
    private String reg_email;
    private String reg_phone;
    private String pets;
    private String reg_date;
    private String UID;
    public ArrayList<Bitmap> pet_profile;

    private boolean add_pet_first = true;
    private boolean isAdd = false;

    public void Set_First(boolean bool){this.add_pet_first = bool;}
    public boolean Get_First(){return this.add_pet_first; }

    public void Set_Add(boolean bool){this.isAdd = bool;}
    public boolean Get_Add(){return this.isAdd; }

    private Bitmap profile;
    public void set_profile(Bitmap b){ this.profile = b; }
    public Bitmap get_profile() { return  this.profile; }


    private int pet_id;
    private ArrayList<Pet> pet_info;



    public String Get_IP(){
        return this.server_ip;
    }

    public int Get_Port(){
        return this.port;
    }

    public void Set_reg_name(String name){
        reg_name = name;
    }

    public void Set_reg_gender(String gender){
        reg_gender = gender;
    }

    public void Set_reg_birthday(String birthday){reg_birthday = birthday;}

    public void Set_reg_account(String account){
        reg_account = account;
    }

    public void Set_reg_pwd(String pwd){
        reg_pwd = pwd;
    }

    public void Set_reg_email(String email){
        reg_email = email;
    }

    public void Set_reg_phone(String phone){
        reg_phone = phone;
    }

    public void Set_reg_date(String date){
        reg_date = date;
    }

    public void Set_Uid(String uid) { UID = uid;}

    public void Set_pets(String pets) {this.pets = pets; }

    public void Set_Select_Pet(int idx){ this.pet_id = idx; }

    public void Set_Pet_info(ArrayList<Pet> pet_info){ this.pet_info = pet_info; }

    public String Get_reg_name(){
        return this.reg_name;
    }

    public String Get_reg_gender(){
        return this.reg_gender;
    }

    public String Get_reg_birthday(){
        return this.reg_birthday;
    }

    public String Get_reg_account(){
        return this.reg_account;
    }

    public String Get_reg_pwd(){
        return this.reg_pwd;
    }

    public String Get_reg_email(){
        return this.reg_email;
    }

    public String Get_reg_phone(){
        return this.reg_phone;
    }

    public String Get_Uid(){ return this.UID; }

    public String Get_reg_date(){
        return this.reg_date;
    }

    public String Get_pets() { return this.pets; }

    public int Get_Select_Pet(){ return this.pet_id; }

    public ArrayList<Pet> Get_Pet_Info(){ return this.pet_info; }
}
