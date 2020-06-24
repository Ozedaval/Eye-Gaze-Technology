package com.pwc.explore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/*Allows selection to other Activities which show different workings towards the final outcome*/
public class Miscellaneous extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miscellaneous);
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


    public void startCalibrationSelection(View view){
        Intent eyeGazeIntent = new Intent(this, com.pwc.explore.eyegaze.sparseflowSelection.EyeGazeEventActivity.class);
        startActivity(eyeGazeIntent);
    }
}
