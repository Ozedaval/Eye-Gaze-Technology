package com.pwc.commsgaze.database;


import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Content.class, version = 1, exportSchema = false)
public abstract class ContentDatabase extends RoomDatabase {
    private static ContentDatabase contentDB = null;
    public abstract ContentDAO contentDAO();

    public static synchronized ContentDatabase getDBInstance(Context context){
        if(contentDB == null){
            contentDB = Room.databaseBuilder(context.getApplicationContext(), ContentDatabase.class, "content_database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // We are not considering migrations for the interim
                    .build();
        }
        return contentDB;
    }
}
