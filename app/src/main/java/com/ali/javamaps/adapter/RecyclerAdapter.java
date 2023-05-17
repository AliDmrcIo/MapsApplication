package com.ali.javamaps.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ali.javamaps.databinding.RecyclerRowBinding;
import com.ali.javamaps.model.Place;
import com.ali.javamaps.view.MainActivity;
import com.ali.javamaps.view.MapsActivity;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PlaceHolder> {

    List<Place> placeList; 

    public RecyclerAdapter(List<Place> placeList){
        this.placeList=placeList;

    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new PlaceHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceHolder holder, int position) {
        holder.recyclerRowBinding.recyclerViewtextView.setText(placeList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(holder.itemView.getContext(), MapsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("place",placeList.get(position));
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    public class PlaceHolder extends RecyclerView.ViewHolder {
        RecyclerRowBinding recyclerRowBinding;

        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {

            super(recyclerRowBinding.getRoot());
            this.recyclerRowBinding=recyclerRowBinding;
        }
    }

}
