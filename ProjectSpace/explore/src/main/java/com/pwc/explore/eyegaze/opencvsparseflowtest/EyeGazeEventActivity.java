package com.pwc.explore.eyegaze.opencvsparseflowtest;



import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.R;
import com.pwc.explore.RealTimeTest;
import com.pwc.explore.databinding.ActivityEventTestBinding;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

/* Handles Gaze tracking Activity*/
public class EyeGazeEventActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
/*    TODO  (  1.Update existing UI & Link up RecyclerView UI
               3.Use Cursor Class appropriately
               4.Need to address Activity Lifecycle)
      Note: The image layout "screen" is temporary;Since as far
      as I have searched it appears that majority of  OpenCV implementations  uses the cameraBridgeViewBase - produces a preview.
      This preview can be hidden by changing the output of the callback:onCameraFrame.
      The Output produced by the camera is flipped & needs to be addressed.This is an already existing underlying issue in OpenCV*/


    static{ System.loadLibrary( "opencv_java4" );}
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private Detect detect;
    private static final String TAG="EyeGazeEventActivity";
    private ActivityEventTestBinding binding;
    private RealTimeTest realTimeTest;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEventTestBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

        Snackbar.make(binding.eventTestCoordinatorLayout,R.string.in_development_note_msg,Snackbar.LENGTH_LONG).show();
        binding.openCVCameraView.setVisibility(SurfaceView.VISIBLE);
        binding.openCVCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        binding.openCVCameraView.setCameraPermissionGranted();
        binding.openCVCameraView.disableFpsMeter();
        binding.openCVCameraView.setCvCameraViewListener(this);
        binding.reCalibrateButton.setVisibility(View.VISIBLE);

        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        /*Log.d(TAG, Arrays.toString(fileList()));
        Log.d(TAG, getFileStreamPath("eyeModel.xml").getAbsolutePath());
        Log.d(TAG, getFileStreamPath("faceModel.xml").getAbsolutePath());*/
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

        detect = new Detect(faceCascade,eyesCascade);
        realTimeTest = new RealTimeTest();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        /*Log.d(TAG,"On Camera View Started");*/
    }


    @Override
    public void onCameraViewStopped() {
    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        binding.eventTextView.post(new Runnable() {
            @Override
            public void run() {
                binding.eventTextView.setText(detect.getDirection()+"");
            }
        });
        return detect.detect(inputFrame.rgba());
    }


    @Override
    public void onResume()
    {  super.onResume();
        binding.openCVCameraView.enableView();
    }


    @Override
    protected void onDestroy() {
        binding.openCVCameraView.surfaceDestroyed(binding.openCVCameraView.getHolder());
        super.onDestroy();
    }

    /**
     * To reset the tracking */
    public void reCalibrate(View view) {
        detect = new Detect(faceCascade,eyesCascade);
        System.gc();
    }

    public void leftButton(View view) {
        realTimeTest.setNumOfTry(realTimeTest.getNumOfTry() + 1);
        if (binding.eventTextView.getText().toString().equals("LEFT"))
            realTimeTest.setNumOfCorrect(realTimeTest.getNumOfCorrect() + 1);
        realTimeTest.setAccuracy();
        binding.calculatedAccTextView.setText(realTimeTest.getAccuracy() +" %");
        if (realTimeTest.getNumOfTry() % 100 == 0) {
            realTimeTest.saveResult(this, false);
            realTimeTest.saveResult(this, true);
        }
    }

    public void rightButton(View view) {
        realTimeTest.setNumOfTry(realTimeTest.getNumOfTry() + 1);
        if (binding.eventTextView.getText().toString().equals("RIGHT"))
            realTimeTest.setNumOfCorrect(realTimeTest.getNumOfCorrect() + 1);
        realTimeTest.setAccuracy();
        binding.calculatedAccTextView.setText(realTimeTest.getAccuracy() +" %");
        if (realTimeTest.getNumOfTry() % 100 == 0) {
            realTimeTest.saveResult(this, false);
            realTimeTest.saveResult(this, true);
        }
    }

    public void neutralButton(View view) {
        realTimeTest.setNumOfTry(realTimeTest.getNumOfTry() + 1);
        if (binding.eventTextView.getText().toString().equals("NEUTRAL"))
            realTimeTest.setNumOfCorrect(realTimeTest.getNumOfCorrect() + 1);
        realTimeTest.setAccuracy();
        binding.calculatedAccTextView.setText(realTimeTest.getAccuracy() +" %");
        if (realTimeTest.getNumOfTry() % 100 == 0) {
            realTimeTest.saveResult(this, false);
            realTimeTest.saveResult(this, true);
        }
    }
}