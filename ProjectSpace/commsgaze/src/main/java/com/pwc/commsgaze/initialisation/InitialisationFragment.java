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

import com.pwc.commsgaze.LoadingViewModel;
import com.pwc.commsgaze.MainViewModel;
import com.pwc.commsgaze.R;
import com.pwc.commsgaze.StorageViewModel;
import com.pwc.commsgaze.database.Content;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import static com.pwc.commsgaze.utils.FileUtil.AUDIO_RES_HEADER;
import static com.pwc.commsgaze.utils.FileUtil.CONTENT_RES_HEADER;
import static com.pwc.commsgaze.utils.FileUtil.IMAGE_RES_HEADER;
import static com.pwc.commsgaze.utils.FileUtil.isContentFileNameFormatted;

/*Fragment which shows the initialisation loading Bar*/
public class InitialisationFragment extends DialogFragment {


    private TextToSpeech textToSpeech;
    private LoadingViewModel loadingViewModel;
    private static final String TAG = "Initialisation Fragment";
    private Initialisation initialisationAsync;
    private Stack<File> contentAudioExternalFileStack = new Stack<>();
    private boolean isInitialisationAsyncDone;
    private boolean isTTSConversationDone;
    private StorageViewModel storageViewModel;



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
        storageViewModel = new ViewModelProvider(this).get(StorageViewModel.class);
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
                                                initialiseDatabase();
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
                    String word;
                    String audioName;
                    for(Field field:fields){
                        if((word = field.getName()).contains(CONTENT_RES_HEADER)) {
                            /*Looking for only content related files*/
                            if (isContentFileNameFormatted(word)) {
                                word = word.substring(word.indexOf("_"));

                                audioName = AUDIO_RES_HEADER + word;
                                word = word.substring(word.lastIndexOf("_"));
                                Log.d(TAG, "Audio File Name  " + audioName);
                                File audioFile =  new File(externalFileDir, audioName + ".wav");
                                contentAudioExternalFileStack.add(audioFile);
                            } else {
                                throw new AssertionError("Starter File: " + word + " is not formatted correctly in accordance to the custom format we are using!");
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

     loadingViewModel = new ViewModelProvider(requireActivity())
                .get(LoadingViewModel.class);
        Log.d(TAG, "onCreate Called");
    }

    /* Assuming that file name is in this format "contentType_topicName_contentName.extension */
    private String parseTextFromFileName(File file){
        String  text = file.getName();
        text = text.contains(".") ? text.substring(0,text.lastIndexOf(".")):text;
        return  text.split("_")[2];
    }

    void initialiseDatabase(){
        File externalFileDir = getContext().getExternalFilesDir(null);
        if(externalFileDir != null){
            File[] files = externalFileDir.listFiles();
            Log.d(TAG,"Files present " + Arrays.toString(files));
            String fileName;
            HashMap<String,ArrayList<String>> filePathMap = new HashMap<>();
            for(File file:files){
                /*topicWordLabel - _topic_word*/
                if((fileName = file.getName()).contains(AUDIO_RES_HEADER)||fileName.contains(IMAGE_RES_HEADER)){
                    String topicWordLabel = fileName.substring(fileName.indexOf("_")+1);
                    if(topicWordLabel.contains(".")){
                        topicWordLabel = topicWordLabel.substring(0,topicWordLabel.lastIndexOf("."));
                    }
                    if(filePathMap.containsKey(topicWordLabel)){
                        filePathMap.get(topicWordLabel).add(file.getAbsolutePath());
                    }
                    else {
                        ArrayList<String> filePathList = new ArrayList<>();
                        filePathList.add(file.getAbsolutePath());
                        filePathMap.put(topicWordLabel,filePathList);
                    }

                    Log.d(TAG,"topic Word Label " + topicWordLabel);
                }
            }

            for(Map.Entry<String,ArrayList<String>> entry:filePathMap.entrySet()){
                /*Only inserting Content with those which have audio and image*/
                if(entry.getValue().size()==2){
                    String word = entry.getValue().get(0);
                    String topic = word.substring(word.indexOf("_")+1,word.lastIndexOf("_"));
                    String imgPath = word.contains(IMAGE_RES_HEADER)? word: entry.getValue().get(1);
                    String audioPath = word.contains(AUDIO_RES_HEADER)? word : entry.getValue().get(1);
                    word = word.substring(word.lastIndexOf("_")+1);
                    Content content = new Content(word,imgPath,audioPath,topic);
                    Log.d(TAG,"Content being added " + content.toString());
                    storageViewModel.insertContent(content);
                }
                else{
                    Log.e(TAG,entry.toString()+"is not being added");
                }
            }

            Log.d(TAG,"fileNameMap " + filePathMap.toString());
        }
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
                loadingViewModel.initialisationDone();
            }
        }, 2000);
    }

    void setInitialisationAsyncDone(){
        isInitialisationAsyncDone = true;
        if(isTTSConversationDone){
            initialiseDatabase();
            closeFragment();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initialisationAsync.cancel(true);
    }

}