package com.ali.javamaps.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.ali.javamaps.R;
import com.ali.javamaps.adapter.RecyclerAdapter;
import com.ali.javamaps.databinding.ActivityMainBinding;
import com.ali.javamaps.model.Place;
import com.ali.javamaps.roomDataBase.PlaceDao;
import com.ali.javamaps.roomDataBase.PlaceDatabase;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable=new CompositeDisposable();
    PlaceDatabase db;
    PlaceDao placeDao;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view=binding.getRoot();
        setContentView(view);



        db=Room.databaseBuilder(getApplicationContext(),PlaceDatabase.class,"Places").build();
        placeDao=db.placeDao();

        compositeDisposable.add(placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(MainActivity.this::handleResponse));


    }

    private void handleResponse(List<Place> placeList) {//getAll() methodunu yukarıda kullandığımda ve o da PlaceDao'da Flowable döndürdüğünden ve o da List of Place döndürdüğünden eninde sonunda benim o list of place'i almam gerekecek o yüzden burada parantezler arasına yazıyorum. yazmazsam zaten hata veriyor.

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));// PlaceDao'da ki getAll methodu bana Query işlemi, yani sorgu işlemi SELECT * FROM işlemi yapıyordu, burada da, o işlem sonucunda çıkan çıktıları recyclerView'da liste olarak göstermeyi yaptık. alt alta bunları koyacağımızı söylemiş oluyoruz
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(placeList);
        binding.recyclerView.setAdapter(recyclerAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//menüyü bağlıyorum

        MenuInflater menuInflater=getMenuInflater();

        menuInflater.inflate(R.menu.menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent=new Intent(MainActivity.this, MapsActivity.class);
        intent.putExtra("info","new");//burdan gidiyorsak intent ile şunu yap, recyclerRow'dan geliyorsa kullanıcı şunu yap demek için bunlara bir keyValue gibi bir şey verdik. Bunları da mapsActivity'de yapıcaz
        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}