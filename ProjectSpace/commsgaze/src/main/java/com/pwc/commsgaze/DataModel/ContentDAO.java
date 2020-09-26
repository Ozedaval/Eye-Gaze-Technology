package com.pwc.commsgaze.DataModel;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// Data Access Object
@Dao
public interface ContentDAO {
    @Query("Select * from Contents")
    List<Content> getAllWords();

    @Query("DELETE FROM Contents")
    public void deleteAll();

    @Insert
    void insertWord(Content words);

    @Update
    void updateWord(Content words);

    @Delete
    void deleteWord(Content words);





}
