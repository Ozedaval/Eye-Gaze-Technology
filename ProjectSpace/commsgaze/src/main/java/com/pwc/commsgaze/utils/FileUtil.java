package com.pwc.commsgaze.utils;

import android.content.Context;

import java.io.File;

public class FileUtil {
    public static final String CONTENT_RES_HEADER = "content";
    public static final String AUDIO_RES_HEADER = "audio";
    public static final String IMAGE_RES_HEADER = "image";

    public static boolean customFileFormatChecker(String fileName){
        fileName = fileName.contains(".") ? fileName.substring(0,fileName.lastIndexOf(".")):fileName;
        String[] splitName =  fileName.split("_");
        return  splitName.length == 3 && splitName[0].equalsIgnoreCase("content");
    }

    /* https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)*/
    public static boolean hasExternalStoragePrivateData(String fileName, Context context) {
        File file = new File(context.getExternalFilesDir(null),fileName);
        return file.exists();
    }


}
