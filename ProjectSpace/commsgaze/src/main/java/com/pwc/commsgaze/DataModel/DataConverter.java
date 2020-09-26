package com.pwc.commsgaze.DataModel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class DataConverter {

    public static byte[] converImage2ByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0,stream);
        return stream.toByteArray();
    }

    public static Bitmap convertByteArray2Image(byte[] array){
        return BitmapFactory.decodeByteArray(array,0,array.length);

    }

}
