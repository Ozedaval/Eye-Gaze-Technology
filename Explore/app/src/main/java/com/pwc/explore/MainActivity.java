package com.pwc.explore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.pwc.explore.face.FaceActivity;

public class MainActivity extends AppCompatActivity {
    private final int PERMISSION_REQUEST_CODE=1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CODE);
            }
        }

    }

    public void startFaceEvent(View view) {
        Intent faceEventIntent=new Intent(this, FaceActivity.class);
        startActivity(faceEventIntent);
    }

    public void startEyeGazeEvent(View view) {
        Toast.makeText(this,"Still in Development",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_REQUEST_CODE){
            if(!(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)){
                finish();

            }

        }    }
}
