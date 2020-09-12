package com.pwc.commsgaze;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.pwc.commsgaze.databinding.ActivityMainBinding;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final int PERMISSION_REQUEST_CODE = 1;
    private MainViewModel mainViewModel;
    private Boolean isFirstRun;
    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private static final String TAG = "MainActivity";



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
        isFirstRun = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.main_first_run_preference_key), true);

        if (isFirstRun) {
            fragmentManager = getSupportFragmentManager();
            mainViewModel = new ViewModelProvider(this)
                    .get(MainViewModel.class);

            mainViewModel.getIsFirstRun().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (!aBoolean) {
                        Log.d(TAG," OnChangedLiveData"+"Changed to "+aBoolean);
                        Snackbar.make(binding.mainCoordinatorLayout,
                                getString(R.string.main_initialisation_done_msg),
                                Snackbar.LENGTH_LONG).show();
                        Log.d(TAG, "Initialisation done");
                        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE).edit();
                        sharedPreferencesEditor.putBoolean(getString(R.string.main_first_run_preference_key), false);
                        sharedPreferencesEditor.apply();
                        Log.d(TAG, "Files present " + Arrays.toString(fileList()));
                        isFirstRun= false;
                        mainViewModel.getIsFirstRun().removeObserver(this);
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.init_fragment_tag));
                        if(fragment != null){
                            fragmentManager.beginTransaction().remove(fragment).commit();
                        }
                    }
                }
            });
            if (mainViewModel.getIsFirstRun().getValue() != null && mainViewModel.getIsFirstRun().getValue()) {
                Log.d(TAG ," onResume "+"ViewModel LiveData isa" + mainViewModel.getIsFirstRun().getValue());
                Log.d(TAG , "on Resume Called");

                DialogFragment initialisationFragment = new InitialisationFragment();
                initialisationFragment.setCancelable(false);
                initialisationFragment.show(fragmentManager, getString(R.string.init_fragment_tag));
            }
        }
        Log.d(TAG ,  "isFirstRun is "+isFirstRun+"");


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();
            }
        }
        Log.d(getClass().getName() + "isFirstRun is ",  isFirstRun+"");
    }



}