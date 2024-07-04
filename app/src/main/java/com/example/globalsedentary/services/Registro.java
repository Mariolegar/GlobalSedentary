package com.example.globalsedentary.services;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity(tableName = "registro")
public class Registro {
    @ColumnInfo(name="registro_id")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "fechaRegistro")
    String FechaRegistro;
    @ColumnInfo(name = "humedad")
    String Humedad;
    @ColumnInfo(name = "temperatura")
    String Temperatura;
    @ColumnInfo(name = "dispositivo_id")
    int Dispositivo;
    @ColumnInfo(name = "isUploaded", defaultValue = "false")
    String IsUploaded;
    public Registro() {}

    public Registro(String fechaRegistro, String temperatura, String humedad, int dispositivo, String isUploaded) {
        FechaRegistro = fechaRegistro;
        Temperatura = temperatura;
        Humedad = humedad;
        Dispositivo = dispositivo;
        IsUploaded = isUploaded;
        this.id = 0;
    }

    public String getFechaRegistro() {
        return FechaRegistro;
    }

    public void setFechaRegistro(String fechaRegistro) {
        FechaRegistro = fechaRegistro;
    }

    public String getTemperatura() {
        return Temperatura;
    }

    public void setTemperatura(String temperatura) {
        Temperatura = temperatura;
    }

    public String getHumedad() {
        return Humedad;
    }

    public void setHumedad(String humedad) {
        Humedad = humedad;
    }

    public int getDispositivo() {
        return Dispositivo;
    }

    public void setDispositivo(int dispositivo) {
        Dispositivo = dispositivo;
    }

    public int getId()  { return id; }
    public void setIsUploaded(String isUploaded) {
        IsUploaded = isUploaded;
    }
    public String getIsUploaded() { return IsUploaded; }
}
