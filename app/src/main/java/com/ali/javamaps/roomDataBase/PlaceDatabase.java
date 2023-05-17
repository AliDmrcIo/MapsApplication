package com.ali.javamaps.roomDataBase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.ali.javamaps.model.Place;

//Burası database'in teknik kısmı. Burada database'i inşa ettik, o böyle somut ama herşeyin aslı olan yapıyı DATABASE'i burada inşa ettik

@Database(entities = {Place.class},version = 1)//bu versiyonun yazılma sebebi, entitylerde bir değişiklik yapılacağı zaman versiyonu artırmak zorunda kalabiliriz.
public abstract class PlaceDatabase extends RoomDatabase {//burası RoomDatabase'den extends olan bir abstract olmalı ve burası database'in kendisi. Direkt database'i yarattığmız kısım. Yaratılma kısmı şu şekilde: @Database yazcaz. sonra önceden oluşturdudğumuz entity'i vericez. Sonar versiyonu vericez. Sonra Abstract bir class altında abstract bir method oluşturup yine önceden oluşturduğumuz Dao'yu çağırıcaz.

    public abstract PlaceDao placeDao();

}
