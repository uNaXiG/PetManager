package com.example.petmanage;

public class Pet {
    public String PetId;
    public String Owner;
    public String Name;
    public String Specie;
    public String Gender;
    public String Birthday;
    public String Date;

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
