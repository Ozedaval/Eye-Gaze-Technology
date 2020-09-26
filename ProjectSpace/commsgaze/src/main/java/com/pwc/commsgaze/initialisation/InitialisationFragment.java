package com.pwc.commsgaze.initialisation;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.pwc.commsgaze.MainViewModel;
import com.pwc.commsgaze.R;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Stack;

import static com.pwc.commsgaze.utils.FileUtil.CONTENT_RES_HEADER;
import static com.pwc.commsgaze.utils.FileUtil.customFileFormatChecker;

/*Fragment which shows the initialisation loading Bar*/
public class InitialisationFragment extends DialogFragment {


    private TextToSpeech textToSpeech;
    private MainViewModel mainViewModel;
    private static final String TAG = "Initialisation Fragment";
    private Initialisation initialisationAsync;
    private Stack<File> contentAudioExternalFileStack = new Stack<>();
    private boolean isInitialisationAsyncDone;
    private boolean isTTSConversationDone;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_initialisation, container, false);
        ProgressBar progressBar = fragment.findViewById(R.id.initialisationProgressBar);
        progressBar.animate();
        return fragment;
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final InitialisationFragment initialisationFragment = this;
        textToSpeech = new TextToSpeech(requireContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    System.out.println("onInit");

                    textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Log.d(TAG, "onStart : " + s);
                        }

                        @Override
                        public void onDone(String s) {
                            Log.d(TAG, "onDone : " + s);
                            if (!contentAudioExternalFileStack.empty()) {
                                File audioFile = contentAudioExternalFileStack.pop();
                                textToSpeech.synthesizeToFile(parseTextFromFileName(audioFile), null, audioFile, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                            } else {
                                isTTSConversationDone=true;
                                Log.d(TAG,"TTS onDone initAsyncDone? "+ isInitialisationAsyncDone);
                                if(isInitialisationAsyncDone) {
                                    if(getActivity()!= null)
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                closeFragment();
                                            }
                                        });
                                }
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "onError : " + s);
                        }

                        @Override
                        public void onAudioAvailable(String utteranceId, byte[] audio) {
                            Log.d(TAG, "on Audio Available : " + audio.length);
                        }
                    });

                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "This Language is not supported");
                    }
                } else {
                    Log.e(TAG, "Initialisation Failed!");
                }

                if(getContext()!= null){
                    File externalFileDir = getContext().getExternalFilesDir(null);
                    Field[] fields = R.raw.class.getFields();
                    String name;
                    String audioName;
                    for(Field field:fields){
                        if((name = field.getName()).contains(CONTENT_RES_HEADER)) {
                            /*Looking for only content related files*/
                            if (customFileFormatChecker(name)) {
                                name = name.substring(name.indexOf("_"));
                                audioName = "audio" + name;
                                Log.d(TAG, "Audio File Name  " + audioName);
                                contentAudioExternalFileStack.add(new File(externalFileDir, audioName + ".wav"));
                            } else {
                                throw new AssertionError("Starter File: " + name + " is not formatted correctly in accordance to the custom format we are using!");
                            }
                        }
                    }
                }
                File firstAudioFile = contentAudioExternalFileStack.pop();
                textToSpeech.synthesizeToFile(parseTextFromFileName(firstAudioFile),null,firstAudioFile,TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
            }
        });


        initialisationAsync  = new Initialisation(initialisationFragment);
        initialisationAsync.execute();

        mainViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);
        Log.d(TAG, "onCreate Called");
    }

    /* Assuming that file name is in this format "contentType_topicName_contentName.extension */
    private String parseTextFromFileName(File file){
        String  text = file.getName();
        text = text.contains(".") ? text.substring(0,text.lastIndexOf(".")):text;
        return  text.split("_")[2];
    }


    void closeFragment(){
        textToSpeech.shutdown();
        textToSpeech = null;
        Log.d(TAG," closeFragment called");
        /*Temporarily here to make a smooth UI transition (Visually) */
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initialisationAsync.cancel(true);
                mainViewModel.initialisationDone();
            }
        }, 2000);
    }

    void setInitialisationAsyncDone(){
        isInitialisationAsyncDone = true;
        if(isTTSConversationDone){
            closeFragment();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initialisationAsync.cancel(true);
    }

}