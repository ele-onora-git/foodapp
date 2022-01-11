package com.example.food2;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.food2.Interface.ItemClickListener;
import com.example.food2.Model.Category;
import com.example.food2.ViewHolder.CategoryListViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


public class CategoryList extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference categoryList;
    RecyclerView recyclerCategoryList;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Category, CategoryListViewHolder> adapter;

    long passDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_list);

        //initialise Firebase
        database = FirebaseDatabase.getInstance();
        categoryList = database.getReference("Category");
        categoryList.keepSynced(true);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //load category list
        recyclerCategoryList = (RecyclerView) findViewById(R.id.recyclerCategoryList);
        recyclerCategoryList.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerCategoryList.setLayoutManager(layoutManager);

        if (getIntent() != null)
            passDate = getIntent().getLongExtra("passDate", passDate);
        if (passDate!= 0){
            Log.d(TAG, "passDate no zero from CategoryList " + passDate);
            loadCategoryList(); }
        if(passDate == 0){
            Log.d(TAG, "passDate zero from CategoryList " + passDate);
            passDate = System.currentTimeMillis()/1000;
            loadCategoryList();
        }
    }


    private void loadCategoryList() {
        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>().
                        setQuery(categoryList, Category.class).
                        build();
        adapter = new FirebaseRecyclerAdapter<Category,
                CategoryListViewHolder>(options) {
            @Override
            public void onBindViewHolder(@NonNull CategoryListViewHolder holder,
                                         int position, @NonNull Category model) {
                holder.categoryName.setText(model.getName());
                // insert image
                Picasso.with(getBaseContext()).load(model.getImage()).fit().centerCrop().into(holder.categoryImageView);

                // final Category clickItem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        // get CategoryId and send to new Activity
                        Intent foodList = new Intent(CategoryList.this, FoodList.class);
                        // since CategoryId is key, we only need the key of this item
                        foodList.putExtra("CategoryId", adapter.getRef(position).getKey());
                        foodList.putExtra("passDate", passDate);
                        startActivity(foodList);
                    }
                });
            }

            @NonNull
            @Override
            public CategoryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.category_item, parent, false);
                return new CategoryListViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerCategoryList.setAdapter(adapter);
    }
}
