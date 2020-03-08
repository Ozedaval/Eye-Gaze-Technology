package com.pwc.eyegaze;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.pwc.eyegaze.databinding.ActivityMainBinding;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CameraSource cameraSource;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);



        FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
               .setProminentFaceOnly(true)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();


        detector.setProcessor(
                new LargestFaceFocusingProcessor(
                        detector,
                        new FaceTracker(binding)));

        try {
            cameraSource = new CameraSource.Builder(getApplicationContext(),detector)
                    .setRequestedPreviewSize(640, 480)
                    .setFacing(CameraSource.CAMERA_FACING_FRONT)
                    .setRequestedFps(30.0f)
                    .build()
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }


//        SparseArray<Face> faces = detector.detect(frame);
//        Toast.makeText(getApplicationContext(),faces.valueAt(0)+"",Toast.LENGTH_LONG).show();

        String apiReady=detector.isOperational()?"ready":"not ready";
        Toast.makeText(getApplicationContext(),"API "+apiReady,Toast.LENGTH_LONG).show();
    }
    public static void scaleNormalOrUp(View v, int motion){
        if(motion==MotionEvent.ACTION_DOWN||motion==MotionEvent.ACTION_BUTTON_PRESS||motion==MotionEvent.ACTION_DOWN||motion==MotionEvent.ACTION_BUTTON_PRESS||motion==MotionEvent.AXIS_PRESSURE){
            v.setScaleX(1.10f);
            v.setScaleY(1.10f);}
        else{
            v.setScaleX(1);
            v.setScaleY(1);

        }

    }
}
