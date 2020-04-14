package com.pwc.explore;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.pwc.explore.databinding.ActivityMainBinding;
import com.pwc.explore.eyegaze.EyeGazeEventActivity;
import com.pwc.explore.face.FaceEventActivity;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    //TODO(Make Initialisation into a
    //     separate fragment/activity (Loading screen) to prevent users clicking UI components)

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

        boolean isFirstRun=getPreferences(Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.first_run_preference_key),true);

        if(isFirstRun){
            Toast.makeText(this, R.string.hang_on_msg,Toast.LENGTH_LONG).show();
            MutableLiveData<Boolean> initialisation = new MutableLiveData<>();

            final Observer<Boolean> initialisationObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(@Nullable final Boolean newValue) {
                    binding.eyeGazeControllerButton.setEnabled(true);
                    binding.facialControllerButton.setEnabled(true);
                    SharedPreferences.Editor sharedPreferencesEditor=getSharedPreferences(getString(R.string.main_preference_key),Context.MODE_PRIVATE).edit();
                    sharedPreferencesEditor.putBoolean(getString(R.string.first_run_preference_key),true);
                    sharedPreferencesEditor.apply();
                }
            };
            initialisation.observe(this,initialisationObserver);
            new Initialisation(initialisation).execute();
        }
    }


    public void startFaceEvent(View view) {
        Intent faceEventIntent=new Intent(this, FaceEventActivity.class);
        startActivity(faceEventIntent);

    }

    public void startEyeGazeEvent(View view) {
        Toast.makeText(this,"Note: Feature is still in Development",Toast.LENGTH_LONG).show();
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
}

 class Initialisation extends AsyncTask<Void,Void,Void>{
    private WeakReference<MutableLiveData<Boolean> > mutableLiveDataWeakReference;

    Initialisation(MutableLiveData<Boolean> initialisation){
        mutableLiveDataWeakReference=new WeakReference<>(initialisation);
    }
     @Override
     protected Void doInBackground(Void... voids) {
//            getResources().getXml( R.raw.haarcascade_eye_tree_eyeglasses).t
         //TODO 1( Need to copy XML file attached to the apk into android internal files system
         return null;

     }
    @Override
    protected void onPostExecute(Void aVoid) {
        mutableLiveDataWeakReference.get().setValue(true);
    }



}
