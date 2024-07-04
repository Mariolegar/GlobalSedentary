package com.example.globalsedentary.services;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Registro.class, Dispositivo.class, User.class},version = 12)
public abstract class RegistroDatabase  extends RoomDatabase {
    public abstract RegistroDAO leerRegistroDAO();
    public abstract DispositivoDAO leerDispositivoDAO();
    public abstract UserDao leerUserDAO();
}
