package com.pwc.explore;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import java.util.concurrent.ExecutionException;

/*Fragment which shows the initialisation loading Bar*/
public class InitialisationFragment extends DialogFragment {

    private Initialisation initialisationAsync;
    private MainViewModel mainViewModelProvider;
    private static final String TAG="Initialisation Fragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment=inflater.inflate(R.layout.fragment_initialisation, container, false);
        ProgressBar progressBar = fragment.findViewById(R.id.initialisationProgressBar);
        progressBar.animate();
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initialisationAsync= new Initialisation(requireContext());
        initialisationAsync.execute();
        mainViewModelProvider=new ViewModelProvider(requireActivity())
                .get(MainViewModel.class);
        Log.d(TAG,"onCreate Called");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        try {
            Boolean asyncTaskCompleted=initialisationAsync.get();
            if(asyncTaskCompleted){
                Log.d(TAG, "onResume Async task completed");
                mainViewModelProvider.initialisationDone();
                Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag(requireActivity().getString(R.string.mainActivity_Fragment_Tag));
                FragmentTransaction fragmentTransaction=requireActivity().getSupportFragmentManager().beginTransaction();
                if (fragment != null) {
                    Log.d(TAG," Fragment removed");
                    fragmentTransaction.remove(fragment);
                }
                /*Temporarily here to make a smooth UI transition (Visually)*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onDestroyView();
                    }
                },3000);
            }
            else {
                while (!initialisationAsync.isCancelled()) {
                    initialisationAsync.cancel(true);
                }
                initialisationAsync.execute();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}