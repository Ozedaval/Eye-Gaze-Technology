package com.pwc.commsgaze;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        Context context = contextWeakReference.get();
        Log.d(TAG," ProgressBar Done setup for progress bar");
        eyeModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
        faceModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
        try {
            eyeModelOutputStream = context.openFileOutput("eyeModel.xml", MODE_PRIVATE);
            faceModelOutputStream = context.openFileOutput("faceModel.xml", MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected Boolean doInBackground(Void... voids) {
        /*TODO (Need to delete xml attached to apk in res/raw)*/
        try {
            write(faceModelInputStream,faceModelOutputStream);
            write(eyeModelInputStream,eyeModelOutputStream);
            createExternalStoragePrivateFile();
            Log.d(TAG,"External App - Specific Storage  has Sample?" + hasExternalStoragePrivatePicture());
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
     * https://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard*/
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

    /**
     * TODO Add JavaDocs later
     * https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)*/
    void createExternalStoragePrivateFile() {
        Context context = contextWeakReference.get();
        File file = new File(context.getExternalFilesDir(null), "Sample.jpg");

        try {
            InputStream in = context.getResources().openRawResource(R.raw.sample);
            OutputStream out = new FileOutputStream(file);
            byte[] data = new byte[in.available()];
            in.read(data);
            out.write(data);
            in.close();
            out.close();
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }

    /*https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)*/
    boolean hasExternalStoragePrivatePicture() {
        // Get path for the file on external storage.  If external
        // storage is not currently mounted this will fail.
        Context context = contextWeakReference.get();
        File file = new File(context.getExternalFilesDir(null), "Sample.jpg");
        return file.exists();
    }
}