package com.pwc.commsgaze;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static android.content.ContentValues.TAG;
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
    private ArrayList<File> contentPicExternalFiles;
    private TextToSpeech textToSpeech;
    private ArrayList<File> contentAudioExternalFiles;





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

            /*Loading content Streams/Files*/
            Field[] fields = R.raw.class.getFields();
            contentOutputStreams = new ArrayList<>(fields.length);
            contentInputStreams = new ArrayList<>(fields.length);
            contentPicExternalFiles = new ArrayList<>(fields.length);
            contentAudioExternalFiles = new ArrayList<>(fields.length);

     /*TODO: Based on https://stackoverflow.com/questions/33350250/why-getexternalfilesdirs-doesnt-work-on-some-devices
       I think there might be a problem depending on the device
       Need a fix for majority of the Devices later on*/
            File externalFileDir = context.getExternalFilesDir(null);


            textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        System.out.println("onInit");
                        int result = textToSpeech.setLanguage(Locale.US);
                        if (result == TextToSpeech.LANG_MISSING_DATA ||
                                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.e(TAG, "This Language is not supported");
                        }

                    } else
                        Log.e(TAG, "Initialisation Failed!");
                }
            });

            String name;
            int resourceID;
            String picName;
            String audioName;
            for(int i=0;i<fields.length;i++){
                if((name = fields[i].getName()).contains(CONTENT_RES_HEADER)) {
                    /*Looking for only content related files*/
                    if (customFileFormatChecker(name)){
                        name = name.substring(name.indexOf("_"));
                        picName = "picture" + name;
                        Log.d(TAG, "Picture File Name  " + picName);
                        contentPicExternalFiles.add(new File(externalFileDir, picName));
                        audioName = "audio" + name;
                        Log.d(TAG, "Audio File Name  " + audioName);
                        contentAudioExternalFiles.add(new File(externalFileDir, audioName));
                        resourceID = fields[i].getInt(null);
                        Log.d(TAG, "rID " + resourceID);
                        contentInputStreams.add(context.getResources().openRawResource(resourceID));
                        contentOutputStreams.add(new FileOutputStream(contentPicExternalFiles.get(i)));
                    }
                    else{
                        throw new AssertionError("Starter File: "+ name +" is not formatted correctly in accordance to the custom format we are using!");
                    }
                }
            }
        } catch (FileNotFoundException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /*Assuming that file name is in this format "contentType_topicName_contentName.extension*/
    private void convertTextToAudio() {
        String text;
        for (File file : contentAudioExternalFiles) {
            text = file.getName();
            text = text.contains(".") ? text.substring(0,text.lastIndexOf(".")):text;
            text = text.split("_")[2];
            textToSpeech.synthesizeToFile(text,null,file,this.hashCode()+"");
        }
    }

    private boolean customFileFormatChecker(String fileName){
        fileName = fileName.contains(".") ? fileName.substring(0,fileName.lastIndexOf(".")):fileName;
        String[] splitName =  fileName.split("_");
        return  splitName.length == 3 && splitName[0].equalsIgnoreCase("content");
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        /*TODO: Need to delete xml and content data, which will attached to apk*/
        try {



            write(faceModelInputStream,faceModelOutputStream);
            write(eyeModelInputStream,eyeModelOutputStream);

            for(int i = 0; i< contentPicExternalFiles.size(); i++){
                write(contentInputStreams.get(i),contentOutputStreams.get(i));
            }

            Log.d(TAG,"Text to Audio is called");
        /*    while(!isTextToSpeechEngineInitialised){

            }*/

            convertTextToAudio();




            /*TODO: Remove this once code - review is done*/
            for(File file: contentPicExternalFiles) {
                Log.d(TAG,"Abs "+file.getAbsolutePath());
                Log.d(TAG,"Can "+file.getCanonicalPath());
                Log.d(TAG,"fName "+file.getName());
                Log.d(TAG,"External App - Specific Storage has Picture? " + hasExternalStoragePrivateData(file.getName()));
            }

            for(File file: contentAudioExternalFiles) {
                Log.d(TAG,"Abs "+file.getAbsolutePath());
                Log.d(TAG,"Can "+file.getCanonicalPath());
                Log.d(TAG,"fName "+file.getName());
                Log.d(TAG,"External App - Specific Storage has Audio? " + hasExternalStoragePrivateData(file.getName()));
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
    boolean hasExternalStoragePrivateData(String fileName) {
        Context context = contextWeakReference.get();
        File file = new File(context.getExternalFilesDir(null),fileName);
        return file.exists();
    }
}