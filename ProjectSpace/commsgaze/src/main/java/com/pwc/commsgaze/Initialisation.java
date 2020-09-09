package com.pwc.commsgaze;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/*An AsyncTask class which is used for initialising intensive background tasks.*/
class Initialisation extends AsyncTask<Void,Void,Boolean> {

    private WeakReference<Context> contextWeakReference;
    private InputStream eyeModelInputStream;
    private InputStream faceModelInputStream;
    private FileOutputStream eyeModelOutputStream;
    private FileOutputStream faceModelOutputStream;
    private static final String TAG="Initialisation";
    private static final String CONTENT_RES_HEADER = "content";
    private ArrayList<InputStream> contentInputStreams;
    private ArrayList<FileOutputStream> contentOutputStreams;
    private ArrayList<File> contentExternalFiles;


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

            /*Loading content Streams*/
            Field[] fields = R.raw.class.getFields();
            contentOutputStreams = new ArrayList<>(fields.length);
            contentInputStreams = new ArrayList<>(fields.length);
            contentExternalFiles = new ArrayList<>(fields.length);

     /*TODO: Based on https://stackoverflow.com/questions/33350250/why-getexternalfilesdirs-doesnt-work-on-some-devices
       I think there might be a problem depending on the device
       Need a fix for majority of the Devices later on*/
            File externalFileDir = context.getExternalFilesDir(null);

            String name;
            int resourceID;
            for(int i=0;i<fields.length;i++){
                if((name = fields[i].getName()).contains(CONTENT_RES_HEADER)) {
                    contentExternalFiles.add( new File(externalFileDir, name));
                    resourceID= fields[i].getInt(null);
                    Log.d(TAG,"rID "+resourceID);
                    contentInputStreams.add(context.getResources().openRawResource(resourceID));
                    contentOutputStreams.add(new FileOutputStream(contentExternalFiles.get(i)));
                }

            }

        } catch (FileNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected Boolean doInBackground(Void... voids) {
        /*TODO: Need to delete xml attached to apk in res/raw*/
        try {
            write(faceModelInputStream,faceModelOutputStream);
            write(eyeModelInputStream,eyeModelOutputStream);

            for(int i=0;i<contentExternalFiles.size();i++){
                write(contentInputStreams.get(i),contentOutputStreams.get(i));
            }

            /*TODO: Remove this once code - review is done, also remove the .jpg or add .jpg extension to all images under res/raw*/
            for(File file:contentExternalFiles) {
                Log.d(TAG,"Abs "+file.getAbsolutePath());
                Log.d(TAG,"Can "+file.getCanonicalPath());
                Log.d(TAG,"fName "+file.getName());
                Log.d(TAG,"External App - Specific Storage has Sample? " + hasExternalStoragePrivatePicture(file.getName()));
            }

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
        Log.d("Initialisation ", "Done  Copying");
    }


    /*https://developer.android.com/reference/android/content/Context.html#getExternalFilesDir(java.lang.String)*/
    boolean hasExternalStoragePrivatePicture(String fileName) {
        Context context = contextWeakReference.get();
        File file = new File(context.getExternalFilesDir(null),fileName);
        return file.exists();
    }
}