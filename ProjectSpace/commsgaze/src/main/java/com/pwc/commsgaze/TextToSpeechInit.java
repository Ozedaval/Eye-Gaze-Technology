package com.pwc.commsgaze;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.Locale;


public class TextToSpeechInit extends AsyncTask<Void,Void, TextToSpeech> {


    private Initialisation initialisationAsync;
    private TextToSpeech textToSpeech;
    private WeakReference<Context> contextWeakReference;
    private static final String TAG="TextToSpeechInit";

    TextToSpeechInit(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {

    }



    @Override
    protected TextToSpeech doInBackground(Void... voids) {
        Context context = contextWeakReference.get();
        textToSpeech = new TextToSpeech(context,new TextToSpeechListener());
        return textToSpeech;
    }


    @Override
    protected void onPostExecute(TextToSpeech textToSpeech) {
        Log.d(TAG,"Done with background activity");
        initialisationAsync = new Initialisation(contextWeakReference.get(),textToSpeech);
        initialisationAsync.execute();
        contextWeakReference.clear();
    }
    public Initialisation getInitialisationAsync() {
        return initialisationAsync;
    }

    class TextToSpeechListener implements TextToSpeech.OnInitListener{

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
    }}
