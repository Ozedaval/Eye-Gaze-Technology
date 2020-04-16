package com.pwc.explore;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import static android.content.Context.MODE_PRIVATE;

class Initialisation extends AsyncTask<Void,Void,Void> {
    private WeakReference<MutableLiveData<Boolean>> initialisationLiveDataWeakReference;
    private WeakReference<Context> contextWeakReference;
    private InputStream eyeModelInputStream;
    private InputStream faceModelInputStream;
    private FileOutputStream eyeModelOutputStream;
    private  FileOutputStream faceModelOutputStream;


    @Override
    protected void onPreExecute() {
        Context context=contextWeakReference.get();
        eyeModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
        faceModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);

       try {
            eyeModelOutputStream=context.openFileOutput("eyeModel.xml", MODE_PRIVATE);
            faceModelOutputStream=context.openFileOutput("faceModel.xml", MODE_PRIVATE);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
//https://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard
    void write(InputStream in, FileOutputStream out) throws IOException {
        byte[] buff = new byte[1024 * 1024 * 2]; //2MB file
        int read = 0;

        try {
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
        } finally {
            in.close();
            out.close();
        }
        Log.d("Initialisation ", "Done  Copying");
    }


    Initialisation(Context context,MutableLiveData<Boolean> initialisation){
        initialisationLiveDataWeakReference =new WeakReference<>(initialisation);
        contextWeakReference=new WeakReference<>(context);
    }

    //https://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard

    @Override
    protected Void doInBackground(Void... voids) {
        //TODO 1( Need to delete xml attached to apk in res/raw)
        try {
            write(faceModelInputStream,faceModelOutputStream);
            write(eyeModelInputStream,eyeModelOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
    @Override
    protected void onPostExecute(Void aVoid) {
        contextWeakReference.clear();
        initialisationLiveDataWeakReference.get().setValue(true);
        initialisationLiveDataWeakReference.clear();
    }



}