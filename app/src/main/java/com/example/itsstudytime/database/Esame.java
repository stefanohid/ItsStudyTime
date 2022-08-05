package com.example.itsstudytime.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.itsstudytime.dates.TimestampConverter;

import java.io.Serializable;

@Entity(tableName = "table_esame")
public class Esame implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "nome_esame")
    private String nome;

    @ColumnInfo(name = "data_esame")
    private String data;

    @ColumnInfo(name = "voto_esame")
    private int voto;

    @ColumnInfo(name = "isLode")
    private boolean lode;

    @ColumnInfo(name = "isPrevious")
    private boolean isPrevious;

    @ColumnInfo(name = "paused_activity")
    @TypeConverters({TimestampConverter.class})
    public String pauseActivity;

    @ColumnInfo(name = "study_time")
    public Long studyTime;

    public Esame(String nome, String data) {
        this.nome = nome;
        this.data = data;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVoto() {
        return voto;
    }

    public void setVoto(int voto) {
        this.voto = voto;
    }

    public boolean isLode() {
        return lode;
    }

    public void setLode(boolean lode) {
        this.lode = lode;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isPrevious() {
        return isPrevious;
    }

    public void setPrevious(boolean previous) {
        isPrevious = previous;
    }

    public String getPauseActivity() {
        return pauseActivity;
    }

    public void setPauseActivity(String pauseActivity) {
        this.pauseActivity = pauseActivity;
    }

    public Long getStudyTime() {
        return studyTime;
    }

    public void setStudyTime(Long studyTime) {
        this.studyTime = studyTime;
    }
}
