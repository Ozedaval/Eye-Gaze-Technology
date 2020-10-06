package com.pwc.commsgaze.initialisation;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pwc.commsgaze.R;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.pwc.commsgaze.utils.FileUtil.CONTENT_RES_HEADER;
import static com.pwc.commsgaze.utils.FileUtil.IMAGE_RES_HEADER;
import static com.pwc.commsgaze.utils.FileUtil.isContentFileNameFormatted;
import static com.pwc.commsgaze.utils.FileUtil.hasExternalStoragePrivateData;
import static com.pwc.commsgaze.utils.FileUtil.write;

/*An AsyncTask class which is used for initialising intensive background tasks.
 * TODO change to java.util.concurrent or Kotlin co-routines as Asynctasks are recently being deprecated*/
class Initialisation extends AsyncTask<Void,Void,Boolean> {

    private WeakReference<InitialisationFragment> initialisationFragmentWeakReference;
    private InputStream eyeModelInputStream;
    private InputStream faceModelInputStream;
    private FileOutputStream eyeModelOutputStream;
    private FileOutputStream faceModelOutputStream;
    private static final String TAG="Initialisation";

    private ArrayList<InputStream> contentInputStreams;
    private ArrayList<FileOutputStream> contentOutputStreams;
    private ArrayList<File> contentImgExternalFiles;




    Initialisation(InitialisationFragment initialisationFragment){
        initialisationFragmentWeakReference = new WeakReference<>(initialisationFragment);
    }


    @Override
    protected void onPreExecute() {
        Context context = initialisationFragmentWeakReference.get().requireContext();
        Log.d(TAG," ProgressBar Done setup for progress bar");
        eyeModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
        faceModelInputStream = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt);
        try {
            eyeModelOutputStream = context.openFileOutput("eyeModel.xml", MODE_PRIVATE);
            faceModelOutputStream = context.openFileOutput("faceModel.xml", MODE_PRIVATE);

            /*Loading content Streams/Files*/
            Field[] fields = R.raw.class.getFields();
            contentOutputStreams = new ArrayList<>(fields.length);
            contentInputStreams = new ArrayList<>(fields.length);
            contentImgExternalFiles = new ArrayList<>(fields.length);



     /*TODO: Based on https://stackoverflow.com/questions/33350250/why-getexternalfilesdirs-doesnt-work-on-some-devices
       I think there might be a problem depending on the device
       Need a fix for majority of the Devices later on*/

            File externalFileDir = context.getExternalFilesDir(null);

            String fileName;
            int resourceID;
            for(int i=0;i<fields.length;i++){
                if((fileName= fields[i].getName()).contains(CONTENT_RES_HEADER)) {
                    /*Looking for only content related files*/
                    if (isContentFileNameFormatted(fileName)){
                        String word = fileName.substring(fileName.indexOf("_"));
                        String imgName = IMAGE_RES_HEADER + word;
                        Log.d(TAG, "Image File Name  " + imgName);

                        contentImgExternalFiles.add(new File(externalFileDir, imgName));
                        resourceID = fields[i].getInt(null);
                        Log.d(TAG, "rID " + resourceID);
                        contentInputStreams.add(context.getResources().openRawResource(resourceID));
                        contentOutputStreams.add(new FileOutputStream(contentImgExternalFiles.get(i)));

                    }
                    else{
                        throw new AssertionError("Starter Content File: "+ fileName +" is not formatted correctly in accordance to the custom format we are using!");
                    }
                }
            }
        } catch (FileNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected Boolean doInBackground(Void... voids) {
        try {

            write(faceModelInputStream,faceModelOutputStream);
            write(eyeModelInputStream,eyeModelOutputStream);

            for(int i = 0; i< contentImgExternalFiles.size(); i++){
                write(contentInputStreams.get(i),contentOutputStreams.get(i));
            }

            /*For Debugging*/
            for(File file: contentImgExternalFiles) {
                Log.d(TAG,"Abs "+file.getAbsolutePath());
                Log.d(TAG,"Can "+file.getCanonicalPath());
                Log.d(TAG,"fName "+file.getName());
                Log.d(TAG,"External App - Specific Storage has Picture? " + hasExternalStoragePrivateData(file.getName(),initialisationFragmentWeakReference.get().requireContext()));
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    protected void onPostExecute(Boolean bool) {
        initialisationFragmentWeakReference.get().setInitialisationAsyncDone();
        initialisationFragmentWeakReference.clear();
        Log.d(TAG ," onPostExecute Called & "+ getStatus()+bool);
    }


}