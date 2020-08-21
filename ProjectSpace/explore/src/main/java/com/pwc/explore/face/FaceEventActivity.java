package com.pwc.explore.face;


import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;
import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.databinding.ActivityEventUiBinding;
import java.io.IOException;

/* Handles face tracking Activity*/
public class FaceEventActivity extends AppCompatActivity {
    /*TODO(1.Need to address Activity Lifecycle)*/

    private ActivityEventUiBinding binding;
    private CameraSource cameraSource;
    private FaceDetector detector;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEventUiBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

        detector = new FaceDetector.Builder(getApplicationContext())
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
        Snackbar.make(binding.eventUICoordinatorLayout,"API is " + apiReady+ "!",Snackbar.LENGTH_LONG).show();
    }


    @Override
    protected void onDestroy() {
        detector.release();
        cameraSource.release();
        super.onDestroy();
    }


    /**
     * Sets the view object - scaled up or normal depending on the user's action
     * @param v: View object upon which Scaling or return to normal state occurs
     * @param motion : User's action*/
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
        });
    }


    /**
     * Sets the TextView to the given text
     * @param text: The text which needs to be set.
     * @param v: The TextView which needs to be set to the text. */
    public static void setText(final TextView v, final String text){
        v.post(new Runnable() {
            @Override
            public void run() {
                v.setText(text);
            }
        });
    }

}