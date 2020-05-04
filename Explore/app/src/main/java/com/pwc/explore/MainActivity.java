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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import 	androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.databinding.ActivityMainBinding;
import com.pwc.explore.face.FaceEventActivity;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_CODE = 1;
    private MainViewModel mainViewModel=null;//Initialising cause of a unusual Code Coverage
    private Boolean isFirstRun;
    private ActivityMainBinding binding;
    private FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            }
        }

        isFirstRun = ( getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.first_run_preference_key), true));

        if (isFirstRun) {
            fragmentTransaction= getSupportFragmentManager().beginTransaction();
            mainViewModel = new ViewModelProvider(this)
                    .get(MainViewModel.class);

            mainViewModel.getIsFirstRun().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (!aBoolean) {
                        Log.d(getClass().getSimpleName()+ " OnChangedLiveData","Changed to "+aBoolean);
                        Snackbar.make(binding.mainCoordinatorLayout,
                                getString(R.string.initialisation_done_msg),
                                Snackbar.LENGTH_LONG).show();
                        Log.d(getClass().getSimpleName(), "Initialisation done");
                        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE).edit();
                        sharedPreferencesEditor.putBoolean(getString(R.string.first_run_preference_key), false);
                        sharedPreferencesEditor.apply();
                        Log.d(getClass().getSimpleName(), "Files present " + Arrays.toString(fileList()));
                        isFirstRun= false;
                        mainViewModel.getIsFirstRun().removeObserver(this);
                    }
                }
            });
        }
        Log.d(getClass().getName() + "isFirstRun is ",  isFirstRun+"");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstRun) {
            if (mainViewModel.getIsFirstRun().getValue() != null && mainViewModel.getIsFirstRun().getValue()) {
                Log.d(getClass().getSimpleName() +" onResume","ViewModel LiveData isa" +mainViewModel.getIsFirstRun().getValue());
                Log.d(getClass().getSimpleName() + " onResume", "Called");

                Fragment fragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.mainActivity_Fragment_Tag));
                if (fragment != null) {
                    fragmentTransaction.remove(fragment);
                }
                DialogFragment initialisationFragment = new InitialisationFragment();
                initialisationFragment.setCancelable(false);
                initialisationFragment.show(fragmentTransaction, getString(R.string.mainActivity_Fragment_Tag));
            }
        }
    }


    public void startFaceEvent(View view) {
        Intent faceEventIntent = new Intent(this, FaceEventActivity.class);
        startActivity(faceEventIntent);
    }


    public void startEyeGazeShape(View view) {
        Intent eyeGazeIntent = new Intent(this, com.pwc.explore.eyegaze.opencvshape.EyeGazeEventActivity.class);
        startActivity(eyeGazeIntent);
    }

    public void startEyeGazeMaxArea(View view) {
        Intent eyeGazeIntent = new Intent(this, com.pwc.explore.eyegaze.opencvmaxarea.EyeGazeEventActivity.class);
        startActivity(eyeGazeIntent);
    }

    public void startEyeGazeBlob(View view) {
        Intent eyeGazeIntent = new Intent(this, com.pwc.explore.eyegaze.opencvblob.EyeGazeEventActivity.class);
        startActivity(eyeGazeIntent);
    }
}

