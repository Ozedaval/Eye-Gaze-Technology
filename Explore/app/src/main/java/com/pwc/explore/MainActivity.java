package com.pwc.explore;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.pwc.explore.databinding.ActivityMainBinding;
import com.pwc.explore.eyegaze.opencvshape.EyeGazeEventActivity;
import com.pwc.explore.face.FaceEventActivity;


import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
/*    TODO(1.Make Initialisation into a
         separate fragment/activity (Loading screen) to prevent users clicking UI components
         2.Need to address Activity Lifecycle)*/

    private final int PERMISSION_REQUEST_CODE=1;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding= ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CODE);
            }
        }


        boolean isFirstRun=getSharedPreferences(getString(R.string.main_preference_key),Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.first_run_preference_key),true);
        Log.d(getClass().getName(),"isFirstRun is " +  isFirstRun);

        if(isFirstRun){
            toggleClickButton(binding.facialControllerButton);
            toggleClickButton(binding.eyeGazeControllerButton);
            Toast.makeText(this, R.string.hang_on_msg,Toast.LENGTH_LONG).show();
            MutableLiveData<Boolean> initialisation = new MutableLiveData<>();

            Observer<Boolean> initialisationObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable final Boolean newValue) {
                    if(newValue!=null){
                        Log.d(getClass().getName(),"Initialisation done"+newValue);
                        toggleClickButton(binding.eyeGazeControllerButton);
                        toggleClickButton(binding.facialControllerButton);
                        SharedPreferences.Editor sharedPreferencesEditor=getSharedPreferences(getString(R.string.main_preference_key),Context.MODE_PRIVATE).edit();
                        sharedPreferencesEditor.putBoolean(getString(R.string.first_run_preference_key),!newValue);
                        sharedPreferencesEditor.apply();
                        Toast.makeText(getApplicationContext(), R.string.intialisation_done_msg,Toast.LENGTH_SHORT).show();
                        Log.d(getClass().getName(),"Files present "+ Arrays.toString(fileList()));

                    }
                }
            };

            initialisation.observe(this,initialisationObserver);
            new Initialisation(this,initialisation).execute();
        }
    }


    public void startFaceEvent(View view) {
        Intent faceEventIntent=new Intent(this, FaceEventActivity.class);
        startActivity(faceEventIntent);

    }

    public void startEyeGazeEvent(View view) {
        Toast.makeText(this, R.string.in_development_note_msg,Toast.LENGTH_LONG).show();
        Intent eyeGazeIntent=new Intent(this, EyeGazeEventActivity.class);
        startActivity(eyeGazeIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_REQUEST_CODE){
            if(!(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)){
                finish();

            }
        }
    }

    public void toggleClickButton(Button button){
        boolean isVisible=button.getVisibility()==View.VISIBLE;
        int prospectiveVisibility=isVisible?View.GONE:View.VISIBLE;
        button.setFocusableInTouchMode(!isVisible);
        button.setVisibility(prospectiveVisibility);}
}
