package com.example.globalsedentary.services;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RegistroDAO{
    @Insert
    public void agregarRegistro(Registro registro);
    @Update
    public void actualizarRegistro(Registro registro);
    @Delete
    public void eliminarRegistro(Registro registro);
    @Query("select * from registro")
    public List<Registro> leerRegistros();
    @Query("select * from registro where registro_id==:registro_id")
    public Registro leerRegistro(int registro_id);
    @Query("select * from registro where isUploaded==:string")
    public List<Registro> leerRegistroBool(String string);
}