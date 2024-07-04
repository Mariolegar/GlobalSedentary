package com.example.globalsedentary.services;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface DispositivoDAO {
    @Insert
    public void agregarDispositivo(Dispositivo dispositivo);
    @Update
    public void actualizarDispositivo(Dispositivo dispositivo);
    @Delete
    public void eliminarDispositivo(Dispositivo dispositivo);
    @Query("select * from dispositivo where userId==:uid and isUploaded==:string")
    public List<Dispositivo> leerDispositivos(String uid, String string);
    @Query("select * from dispositivo where userId==:uid")
    public Dispositivo leerDispositivo(String uid);

    @Query("select * from dispositivo where nombre==:nombre & userId==:uid")
    public Dispositivo buscarNombreDispositivo(String nombre, String uid);
}
