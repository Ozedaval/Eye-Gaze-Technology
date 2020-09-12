package com.pwc.commsgaze;

import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

/*Fragment which shows the initialisation loading Bar*/
public class InitialisationFragment extends DialogFragment {


    private TextToSpeech textToSpeech;
    private MainViewModel mainViewModel;
    private static final String TAG = "Initialisation Fragment";
    private Initialisation initialisationAsync;



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
                    initialisationAsync  = new Initialisation(initialisationFragment,textToSpeech);
                    initialisationAsync.execute();
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA ||
                            result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e(TAG, "This Language is not supported");
                    }
                } else
                    Log.e(TAG, "Initialisation Failed!");
            }
        });
        mainViewModel = new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);
        Log.d(TAG, "onCreate Called");
    }


    public void closeFragment(){
        mainViewModel.initialisationDone();
        final Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag(requireActivity().getString(R.string.main_fragment_tag));
        final FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        if (fragment != null) {
            Log.d(TAG, " Fragment removed");

            /*Temporarily here to make a smooth UI transition (Visually) */
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    fragmentTransaction.remove(fragment).commit();
                }
            }, 6000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initialisationAsync.cancel(true);
    }

}