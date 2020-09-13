package com.pwc.explore;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import static android.content.Context.MODE_PRIVATE;

/*An AsyncTask class which is used for initialising intensive background tasks.*/
class Initialisation extends AsyncTask<Void,Void,Boolean> {

    private WeakReference<Context> contextWeakReference;
    private InputStream eyeModelInputStream;
    private InputStream faceModelInputStream;
    private FileOutputStream eyeModelOutputStream;
    private FileOutputStream faceModelOutputStream;
    private static final String TAG="Initialisation";


    Initialisation(Context context){
        contextWeakReference=new WeakReference<>(context);
    }


    @Override
    protected void onPreExecute() {
        Context context=contextWeakReference.get();
        Log.d(TAG," ProgressBar Done setup for progress bar");
        eyeModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
        faceModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
        try {
            eyeModelOutputStream=context.openFileOutput("eyeModel.xml", MODE_PRIVATE);
            faceModelOutputStream=context.openFileOutput("faceModel.xml", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        /*TODO 1( Need to delete xml attached to apk in res/raw)*/
        try {
            write(faceModelInputStream,faceModelOutputStream);
            write(eyeModelInputStream,eyeModelOutputStream);
            return true;
        } catch (IOException  e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean bool) {
        contextWeakReference.clear();
        Log.d(TAG ," onPostExecute Called & "+getStatus()+bool);
    }


    /**
     *Writes onto the OutputStream  based on the InputStream values
     * @param in:The Stream which consist of the writable data.
     * @param out:The Stream upon which data is to be written.
     * Based on https://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard*/
    private void write(InputStream in, FileOutputStream out) throws IOException {
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
}