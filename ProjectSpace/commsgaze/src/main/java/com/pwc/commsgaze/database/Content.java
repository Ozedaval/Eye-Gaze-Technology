package com.pwc.commsgaze.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "content_table")
public class Content {
    @PrimaryKey()
    @ColumnInfo(name = "word")
    @NonNull String word;
    @ColumnInfo(name = "image_path")
    String imageDirPath;
    @ColumnInfo(name = "audio_path")
    String audioDirPath;
    @ColumnInfo(name = "topic")
    String topic;


    public Content(@NonNull String word, String imageDirPath, String audioDirPath, String topic){
        this.word = word;
        this.imageDirPath = imageDirPath;
        this.audioDirPath = audioDirPath;
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }



    public String getWord() {
        return word;
    }

    public String getImageDirPath() {
        return imageDirPath; }

    public String getAudioDirPath() {
        return audioDirPath;
    }



    public void setWord(String word){
        this.word = word;
    }

    public void setImageDirPath(String imageDirPath){
        this.imageDirPath = imageDirPath;
    }

    public void setAudioDirPath(String audioDirPath) {
        this.audioDirPath = audioDirPath;}


    @Override
    public String toString() {
        return "Content{" +
                "word='" + word + '\'' +
                ", imageDirPath='" + imageDirPath + '\'' +
                ", audioDirPath='" + audioDirPath + '\'' +
                ", topic='" + topic + '\'' +
                '}';
    }
}
