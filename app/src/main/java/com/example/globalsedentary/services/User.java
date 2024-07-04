package com.example.globalsedentary.services;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class User {
    @ColumnInfo(name="user_id")
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name="email")
    public String email;

    @ColumnInfo(name="password")
    public String password;
    @ColumnInfo(name="isloggedin")
    public boolean isLoggedIn;
    public User() {}
    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.isLoggedIn = isLoggedIn;
        this.id = 0;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }


}
