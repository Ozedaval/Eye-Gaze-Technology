package com.pwc.explore;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import java.util.concurrent.ExecutionException;


public class InitialisationFragment extends DialogFragment {
    private  Initialisation initialisationAsync = null;
    private MainViewModel mainViewModelProvider;

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


        Log.d(getTag()+ " onCreate","Called");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getTag(), "onResume");
        try {
            Boolean asyncTaskCompleted=initialisationAsync.get();
            if(asyncTaskCompleted){
                Log.d(getTag(), "onResume Async task completed");
                mainViewModelProvider.initialisationDone();

                Log.d(getTag()+ "Fragment  ","removed");
                Fragment fragment = requireActivity().getSupportFragmentManager().findFragmentByTag(requireActivity().getString(R.string.mainActivity_Fragment_Tag));
                FragmentTransaction fragmentTransaction=requireActivity().getSupportFragmentManager().beginTransaction();
                if (fragment != null) {
                    fragmentTransaction.remove(fragment);

                }
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
