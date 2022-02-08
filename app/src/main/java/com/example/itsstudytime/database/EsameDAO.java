package com.example.itsstudytime.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.itsstudytime.database.Esame;

import java.util.List;

@Dao
public interface EsameDAO {
    @Query("select * from table_esame")
    List<Esame> getEsami();

    @Insert
    void insertAll(Esame... esami);

    @Delete
    void delete(Esame esame);

    @Update
    void update(Esame esame);


}

