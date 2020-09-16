package com.pwc.commsgaze.DataModel;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "Contents")
public class Content {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "Content")
    String word;
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    byte [] image;
    @ColumnInfo()


    public int getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public byte[] getImage() {
        return image;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setWord(String word){
        this.word = word;
    }

    public void setImage(byte[] image){
        this.image = image;
    }
}
