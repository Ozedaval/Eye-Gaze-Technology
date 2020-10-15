package com.pwc.commsgaze;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.pwc.commsgaze.databinding.ActivityLoadingBinding;
import com.pwc.commsgaze.initialisation.InitialisationFragment;

import java.util.Arrays;

public class LoadingActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE = 1;
    private Boolean isFirstRun;
    private FragmentManager fragmentManager;
    private LoadingViewModel loadingViewModel;
    private ActivityLoadingBinding binding;
    private Snackbar grantPermissionSnackBar;

    private static final String TAG = "LoadingActivity";
    private Observer<Boolean> firstRunObserver;
    private Observer<Boolean> cameraPermissionObserver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoadingBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        hideSystemUI();
        loadingViewModel = new ViewModelProvider(this).get(LoadingViewModel.class);
        isFirstRun = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.main_first_run_preference_key), true);

        grantPermissionSnackBar=Snackbar.make(view,"Please grant Camera Permission, if you want to use the application.",Snackbar.LENGTH_INDEFINITE);
        grantPermissionSnackBar.show();

        requestCameraPermission();

        if (isFirstRun) {
            fragmentManager = getSupportFragmentManager();
            firstRunObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean isFirstRun) {
                    Log.d(TAG," OnChangedLiveData"+"Changed to "+isFirstRun);
                    Boolean isCameraPermissionGranted = loadingViewModel.getCameraPermissionGranted().getValue();
                    if (!isFirstRun && isCameraPermissionGranted!=null && isCameraPermissionGranted ) {
                        Log.d(TAG, "Initialisation done");
                        removeObservers();
                        setFirstRunCompleted();
                        startMainActivity();
                    }
                }
            };
            loadingViewModel.getIsFirstRun().observe(this,firstRunObserver );

            if (loadingViewModel.getIsFirstRun().getValue() != null && loadingViewModel.getIsFirstRun().getValue()) {
                Log.d(TAG ,"ViewModel LiveData is a " + loadingViewModel.getIsFirstRun().getValue());
                DialogFragment initialisationFragment = new InitialisationFragment();
                initialisationFragment.setCancelable(false);
                initialisationFragment.show(fragmentManager, getString(R.string.init_fragment_tag));
            }


            cameraPermissionObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean cameraPermissionGranted) {
                    if(!loadingViewModel.getIsFirstRun().getValue() && cameraPermissionGranted){
                        removeObservers();
                        setFirstRunCompleted();
                        startMainActivity();
                    }
                }
            };
            loadingViewModel.getCameraPermissionGranted().observe(this,cameraPermissionObserver);

        }
        else{ startMainActivity();}
        Log.d(TAG ,  "isFirstRun is "+isFirstRun+"");

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();

            }else {
                loadingViewModel.cameraPermissionGranted();
                grantPermissionSnackBar.dismiss();
            }
        }
        Log.d(TAG,  "is First Run is "+isFirstRun);
    }

    void startMainActivity(){
        Intent mainActivityIntent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSystemUI();
    }

    /*Hides System UI
     * https://developer.android.com/training/system-ui/immersive.html*/
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    void setFirstRunCompleted(){
        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE).edit();
        sharedPreferencesEditor.putBoolean(getString(R.string.main_first_run_preference_key), false);
        sharedPreferencesEditor.apply();
        Log.d(TAG, "Files present " + Arrays.toString(fileList()));
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.init_fragment_tag));
        if(fragment != null){
            fragmentManager.beginTransaction().remove(fragment).commit();
        }
    }

    void removeObservers(){
        loadingViewModel.getIsFirstRun().removeObserver(firstRunObserver);
        loadingViewModel.getCameraPermissionGranted().removeObserver(cameraPermissionObserver);
    }

    void requestCameraPermission(){ if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
        }
    }}
}