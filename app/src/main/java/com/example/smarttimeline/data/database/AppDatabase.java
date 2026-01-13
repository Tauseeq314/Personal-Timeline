package com.example.smarttimeline.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.smarttimeline.data.dao.PostDao;
import com.example.smarttimeline.data.entity.Post;

@Database(entities = {Post.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract PostDao postDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "smarttimeline_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}