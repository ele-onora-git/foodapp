package com.example.food2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.example.food2.Common.Common;
import com.example.food2.Model.Entries;
import com.example.food2.Model.Food;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;


import java.text.DecimalFormat;


public class FoodDetail extends AppCompatActivity {

    DecimalFormat df = new DecimalFormat("0.0");

    TextView foodDetailName, foodDetailCO2, foodDetailComparison, numberTotal, co2Level, kmValue;
    ImageView imgFood;
    CollapsingToolbarLayout collapsingToolbarLayout;
    NumberPicker numberPickerKg, numberPickerGramm, numberPickerServings;

    FloatingActionButton btnAdd;

    String foodId="";
    long passDate;

    FirebaseDatabase database;
    DatabaseReference foods;
    FirebaseFirestore db;

    Food currentFood;

    DatabaseReference entryDbRef;
    int totalGrammOfFood;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        //Firebase retrieve food data
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");
        foods.keepSynced(true);

        db = FirebaseFirestore.getInstance();

        //Firebase food to diary
        // entryDbRef = FirebaseDatabase.getInstance().getReference().child("Entries");

        //Init view
        numberPickerKg = findViewById(R.id.numberPickerKg);
        numberPickerGramm = findViewById(R.id.numberPickerGramm);
        numberPickerServings = findViewById(R.id.numberPickerServings);

        foodDetailName = findViewById(R.id.foodDetailName);
        foodDetailCO2 = findViewById(R.id.foodDetailCO2);
        foodDetailComparison = findViewById(R.id.foodDetailComparison);
        imgFood = findViewById(R.id.imgFood);
        kmValue = findViewById(R.id.kmValue);
        kmValue.setText("0.0");
        numberTotal = findViewById(R.id.numberTotal);

        collapsingToolbarLayout = findViewById(R.id.collapsing);


        // level: low, medium, high, very high
        co2Level = findViewById(R.id.co2Level);

        btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToDiary();

                Intent loadDiary = new Intent(FoodDetail.this, Diary.class);
                loadDiary.putExtra("passDate", passDate);
                startActivity(loadDiary);
            }
        });

        if(getIntent() != null){
            foodId = getIntent().getStringExtra("FoodID");
            Log.d(TAG, "getStringExtra FoodID " + foodId);
            passDate = getIntent().getLongExtra("passDate", passDate);}
        if(!foodId.isEmpty() && passDate != 0){
            Log.d(TAG, "second if statement " + "working");
            getDetailFood(foodId);
        }

        // number pickers set up
        numberPickerKg.setMaxValue(10);
        numberPickerKg.setMinValue(0);
        numberPickerKg.setValue(0);

        numberPickerGramm.setMaxValue(100);
        numberPickerGramm.setMinValue(0);
        numberPickerGramm.setValue(0);

        numberPickerServings.setMaxValue(10);
        numberPickerServings.setMinValue(1);
        numberPickerServings.setValue(1);

        numberPickerKg.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                Log.i(TAG, "onValueChanged" + newVal);
                calculate();
                calculateCar();
            }
        });

        numberPickerGramm.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calculate();
                calculateCar();
            }
        });

        numberPickerServings.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                calculate();
                calculateCar();
            }
        });
    }

    public void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentFood = snapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).fit().centerCrop().into(imgFood);

                collapsingToolbarLayout.setTitle(currentFood.getName());

                foodDetailCO2.setText(currentFood.getCO2());

                foodDetailName.setText(currentFood.getName());

                if (Double.parseDouble(currentFood.getCO2()) < 3.8){
                    co2Level.setText("low");
                    co2Level.setTextColor(Color.parseColor("#FF7CB342"));
                } else if(Double.parseDouble(currentFood.getCO2()) < 7.6 && Double.parseDouble(currentFood.getCO2()) >= 3.8){
                    co2Level.setText("medium");
                    co2Level.setTextColor(Color.parseColor("#FFFFC107"));
                } else if(Double.parseDouble(currentFood.getCO2()) < 11.4 && Double.parseDouble(currentFood.getCO2()) >= 7.6){
                    co2Level.setText("high");
                    co2Level.setTextColor(Color.parseColor("#FFE53935"));
                } else if(Double.parseDouble(currentFood.getCO2()) >= 11.4){
                    co2Level.setText("very high");
                    co2Level.setTextColor(Color.parseColor("#FFB71C1C"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void calculate(){
        int valueKg = numberPickerKg.getValue();
        int valueG = numberPickerGramm.getValue();
        int valueServings = numberPickerServings.getValue();
        float total;

        String co2PerG = (String) foodDetailCO2.getText();
        Float floatValCo2PerG = Float.valueOf(co2PerG);

        totalGrammOfFood = (valueKg*100 + valueG) * valueServings;
        total = totalGrammOfFood * floatValCo2PerG;
        numberTotal.setText(df.format(total));

    }

    public void calculateCar(){
        double totalEmissions = Double.parseDouble(numberTotal.getText().toString());
        double value = totalEmissions/127;
        kmValue.setText(df.format(value));
    }

    private void addToDiary() {
        String Email = Common.currentUser.getEmail();
        String FoodId = foodId;
        String FoodName = currentFood.getName();
        String Quantity = String.valueOf(totalGrammOfFood);
        String CO2 = String.valueOf(numberTotal.getText());
        Long Time = passDate;
        String Level = String.valueOf(co2Level.getText());
        String Colour = String.format("#%06X", (0xFFFFFF & co2Level.getCurrentTextColor()));

        // Long Time = System.currentTimeMillis()/1000;
        //FieldValue Time = FieldValue.serverTimestamp();
        Log.d(TAG, "timestamp value " + Time);


        Entries entries = new Entries(Email, FoodId, FoodName, Quantity, CO2, Time, Level, Colour);
        // primary code is the timestamp
        db.collection("entries").document().set(entries, SetOptions.merge());


        // entryDbRef.child(String.valueOf(System.currentTimeMillis())).setValue(entries);
        Toast.makeText(FoodDetail.this, "entry added", Toast.LENGTH_SHORT).show();
    }
}