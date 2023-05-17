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

    List<Place> placeList; //daha önce burası ArrayList olarak oluyordu ama şimdi bize liste olarak verildiği için liste olarak yaratabiliriz.

    public RecyclerAdapter(List<Place> placeList){//hemen bir önceki satırın constructor'ı. Bunu aldıktan sonra diğer yerlerde(aşağıda ki 3 implemente edilmiş fonksiyonlar sanırım) kullanmam daha kolay olacak.
        this.placeList=placeList;

    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false); //parent.getContext yapmamızın sebbi bir aktivitenin içerisinde olmadığımızdan RecyclerAdapter.this diye alamıyoruz ama parent ile alabiliyoruz

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
        RecyclerRowBinding recyclerRowBinding;//Aslında construcor oluştururken yaptığımız şeyleri yapıyoruz. Burada bir class içerisinde değişken tanımladım gibi düşün, aşağıda da onun constructor'unu this.recycler=recycler falan diye eşitliyorum  burada RecyclerView için oluşturduğum xml'i bir parametre olarak tanımlıyorum

        public PlaceHolder(RecyclerRowBinding recyclerRowBinding) {

            super(recyclerRowBinding.getRoot());//benden bir görünüm istiyor ve ben de o görünümün önceden burada yazan itemView değil, bu şekilde getRoot olacağını söylüyorum
            this.recyclerRowBinding=recyclerRowBinding;
        }
    }

}
