package com.pwc.commsgaze.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
    public static final String CONTENT_RES_HEADER = "content";
    public static final String AUDIO_RES_HEADER = "audio";
    public static final String IMAGE_RES_HEADER = "image";
    private static final String TAG = "FileUtil";

    /* Content file to be formatted as such : content_topicName_wordName.extension */
    public static boolean isContentFileNameFormatted(String fileName){
        fileName = fileName.contains(".") ? fileName.substring(0,fileName.lastIndexOf(".")):fileName;
        String[] splitName =  fileName.split("_");
        return  splitName.length == 3 && splitName[0].equalsIgnoreCase(CONTENT_RES_HEADER);
    }

    /* https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)*/
    public static boolean hasExternalStoragePrivateData(String fileName, Context context) {
        File file = new File(context.getExternalFilesDir(null),fileName);
        return file.exists();
    }

    /**
     *Writes onto the OutputStream  based on the InputStream values
     * @param in:The Stream which consist of the writable data.
     * @param out:The Stream upon which data is to be written.
     * https://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard*/
    public static void write(InputStream in, FileOutputStream out) throws IOException {
        byte[] buff = new byte[in.available()];

        int read = 0;
        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
        Log.d(TAG, "Done  Copying");
    }


}
