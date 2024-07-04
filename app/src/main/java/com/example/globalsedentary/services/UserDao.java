package com.example.globalsedentary.services;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao

public interface UserDao {
    @Insert
    public void agregarUser(User user);
    @Query("select * from user where user_id==:uid")
    public User leerUser(String uid);
    @Query("SELECT * FROM user WHERE email = :email AND password = :password")
    public LiveData<User> leerUserEmailPassword(String email, String password);
    @Update
    public void update(User user);
}
