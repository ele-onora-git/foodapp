package com.example.food2.Model;

import com.google.firebase.firestore.FieldValue;

import java.util.Map;

public class Entries {

    private String Email, FoodId, FoodName, Quantity, CO2, Level, Colour;
    private long Time;

    public Entries() {}

    public String getFoodId() {
        return FoodId;
    }

    public void setFoodId(String foodId) {
        FoodId = foodId;
    }

    public String getFoodName() {
        return FoodName;
    }

    public void setFoodName(String foodName) {
        FoodName = foodName;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public String getCO2() {
        return CO2;
    }

    public void setCO2(String CO2) { this.CO2 = CO2; }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) { Email = email; }

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) { Level = level; }

    public long getTime(){return Time;}

    public void setTime(long time) { Time = time;}

    public String getColour(){return Colour;}

    public void setColour(String colour) { Colour = colour;}

    public Entries(String email, String foodId, String foodName, String quantity, String co2, long time, String level, String colour) {
        Email = email;
        FoodId = foodId;
        FoodName = foodName;
        Quantity = quantity;
        CO2 = co2;
        Time = time;
        Level = level;
        Colour = colour;
    }
}