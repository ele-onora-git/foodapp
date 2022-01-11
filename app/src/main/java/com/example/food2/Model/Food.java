package com.example.food2.Model;

public class Food {
    private String Name, CO2, FoodID, Image;

    public Food(){}

    public Food(String name, String co2, String foodId, String image){
        Name = name;
        CO2 = co2;
        FoodID = foodId;
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCO2() {
        return CO2;
    }

    public void setCO2(String CO2) {
        this.CO2 = CO2;
    }

    public String getFoodID() {
        return FoodID;
    }

    public void setFoodID(String foodId) {
        FoodID = foodId;
    }

    public String getImage(){return Image;}

    public void setImage(String image){ Image = image;}
}
