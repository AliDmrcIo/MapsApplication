package com.ali.javamaps.roomDataBase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.ali.javamaps.model.Place;


@Database(entities = {Place.class},version = 1).
public abstract class PlaceDatabase extends RoomDatabase {

    public abstract PlaceDao placeDao();

}
