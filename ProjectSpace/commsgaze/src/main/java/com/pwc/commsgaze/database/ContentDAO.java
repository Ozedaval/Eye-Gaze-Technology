package com.pwc.commsgaze.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;


@Dao
public interface ContentDAO {

    @Query("SELECT * FROM content_table")
    LiveData<List<Content>> getAllContent();


    @Query("DELETE FROM content_table")
    public void deleteAll();

    @Insert
    void insertContent(Content content);

    @Update
    void updateContent(Content content);

    @Delete
    void deleteContent(Content content);





}
