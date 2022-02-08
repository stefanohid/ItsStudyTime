package com.example.itsstudytime.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.itsstudytime.database.Esame;
import com.example.itsstudytime.database.EsameDAO;

@Database(entities = {Esame.class}, version = 2)
public abstract class EsameDB extends RoomDatabase {
    public abstract EsameDAO esameDAO();
}





