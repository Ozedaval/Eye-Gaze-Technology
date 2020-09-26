package com.pwc.commsgaze.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "content_table")
public class Content {
    @PrimaryKey(autoGenerate = true)
    int id;
    @ColumnInfo(name = "word")
    String word;
    @ColumnInfo(name = "image_path")
    String imageDirPath;
    @ColumnInfo(name = "audio_path")
    String audioDirPath;



    public int getId() {
        return id;
    }

    public String getWord() {
        return word;
    }

    public String getImage() {
        return imageDirPath; }

    public String getAudio() {
        return audioDirPath;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setWord(String word){
        this.word = word;
    }

    public void setImage(String imageDirPath){
        this.imageDirPath = imageDirPath;
    }

    public void setAudio(String audioDirPath) {this.audioDirPath = audioDirPath;}
}
