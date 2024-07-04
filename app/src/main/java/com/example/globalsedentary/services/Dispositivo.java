package com.example.globalsedentary.services;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "dispositivo")
public class Dispositivo {
    @ColumnInfo(name="dispositivo_id")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "fechaRegistro")
    String FechaRegistro;
    @ColumnInfo(name = "nombre")
    String Nombre;
    @ColumnInfo(name = "userId")
    String UserId;
    @ColumnInfo(name = "isUploaded", defaultValue = "false")
    String IsUploaded;
    public Dispositivo() {}

    public Dispositivo(String fechaRegistro, String nombre, String userId) {
        FechaRegistro = fechaRegistro;
        Nombre = nombre;
        UserId = userId;
        this.id = 0;
    }

    public String getFechaRegistro() {
        return FechaRegistro;
    }
    public void setFechaRegistro(String fechaRegistro) {
        FechaRegistro = fechaRegistro;
    }
    public String getNombre() {
        return Nombre;
    }
    public void setNombre(String nombre) {
        Nombre = nombre;
    }
    public int getId()  { return id; }
    public void setUserId(String userId) {
        UserId = userId;
    }
    public String getUserId() { return UserId; }

    public void setIsUploaded(String isUploaded) {
        IsUploaded = isUploaded;
    }
    public String getIsUploaded() { return IsUploaded; }

}
