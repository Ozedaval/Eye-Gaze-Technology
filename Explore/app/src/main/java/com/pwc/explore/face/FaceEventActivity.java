package com.pwc.explore.face;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.pwc.explore.databinding.ActivityEventUiBinding;
import java.io.IOException;

public class FaceEventActivity extends AppCompatActivity {


    private ActivityEventUiBinding binding;
    private CameraSource cameraSource;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEventUiBinding.inflate(getLayoutInflater());
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


        String apiReady=detector.isOperational()?"ready":"not ready";
        Toast.makeText(getApplicationContext(),"API "+apiReady,Toast.LENGTH_LONG).show();
    }
    public static void scaleNormalOrUp(final View v, final int motion){
        v.post(new Runnable() {
            @Override
            public void run() {
                if(motion==MotionEvent.AXIS_PRESSURE){
                    v.setScaleX(1.10f);
                    v.setScaleY(1.10f);}
                else{
                    v.setScaleX(1);
                    v.setScaleY(1);

                }

            }

        });}


    public static void setText(final TextView v, final String text){
        v.post(new Runnable() {
            @Override
            public void run() {
                v.setText(text);
            }
        });

    }

}
