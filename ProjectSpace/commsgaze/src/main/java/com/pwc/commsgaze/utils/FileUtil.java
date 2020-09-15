package com.pwc.commsgaze.utils;

public class FileUtil {
    public static final String CONTENT_RES_HEADER = "content";
    public static boolean customFileFormatChecker(String fileName){
        fileName = fileName.contains(".") ? fileName.substring(0,fileName.lastIndexOf(".")):fileName;
        String[] splitName =  fileName.split("_");
        return  splitName.length == 3 && splitName[0].equalsIgnoreCase("content");
    }
}
