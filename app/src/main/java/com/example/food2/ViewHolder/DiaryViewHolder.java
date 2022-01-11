package com.example.food2.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.food2.Interface.ItemClickListener;
import com.example.food2.R;

public class DiaryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView diaryItemName, diaryItemTotalGrammOfFood, diaryTotalCo2PerItem;

    private ItemClickListener itemClickListener;

    public DiaryViewHolder(@NonNull View itemView) {
        super(itemView);

        diaryItemName = itemView.findViewById(R.id.diaryItemName);
        diaryItemTotalGrammOfFood = itemView.findViewById(R.id.diaryItemTotalGrammOfFood);
        diaryTotalCo2PerItem = itemView.findViewById(R.id.diaryTotalCo2PerItem);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAbsoluteAdapterPosition(), false);
    }
}
