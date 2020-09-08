package com.pwc.explore.eyegaze.opencvsparseflowui;



import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.Direction;
import com.pwc.explore.R;
import com.pwc.explore.databinding.ActivityEventUiBinding;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import static android.view.View.VISIBLE;


public class EyeGazeEventActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
/*    TODO  (  1.Update existing UI & Link up RecyclerView UI
               3.Use Cursor Class appropriately
               4.Need to address Activity Lifecycle
               )
      Note: The image layout "screen" is temporary since as far
      as I have searched it appears that majority of  OpenCV implementations  uses the cameraBridgeViewBase - produces a preview.
      This preview can be hidden by changing the output of the callback:onCameraFrame.
      The Output produced by the camera is flipped & needs to be addressed.This is an already existing underlying issue in OpenCV*/


    static{ System.loadLibrary( "opencv_java4" );}
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private ActivityEventUiBinding binding;
    private Detect detect;
    private static final String TAG="EyeGazeEventActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding=ActivityEventUiBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);
        Snackbar.make(binding.eventUICoordinatorLayout,R.string.in_development_note_msg,Snackbar.LENGTH_LONG).show();

        binding.reCalibrateButton.setVisibility(VISIBLE);
        binding.openCVCameraView.setVisibility(VISIBLE);
        binding.openCVCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        binding.openCVCameraView.setCameraPermissionGranted();
        binding.openCVCameraView.disableFpsMeter();
        binding.openCVCameraView.setCvCameraViewListener(this);

        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        binding.openCVCameraView.bringToFront();

        /*Log.d(TAG, Arrays.toString(fileList()));
        Log.d(TAG, getFileStreamPath("eyeModel.xml").getAbsolutePath());
        Log.d(TAG, getFileStreamPath("faceModel.xml").getAbsolutePath());*/
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

        detect=new Detect(faceCascade,eyesCascade);
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
        /* TODO make use of abstraction*/
        binding.linearLayoutLeft.post(new Runnable() {
            @Override
            public void run() {
                if(detect.getDirection()== Direction.LEFT){
                    scaleNormalOrUp(binding.linearLayoutLeft,MotionEvent.AXIS_PRESSURE);
                    binding.leftTextView.setText("Viewing Left");
                }
                else{
                    scaleNormalOrUp(binding.linearLayoutLeft,MotionEvent.ACTION_BUTTON_RELEASE);
                    binding.leftTextView.setText("Left");
                }
            }
        });

        binding.linearLayoutRight.post(new Runnable() {
            @Override
            public void run() {
                if(detect.getDirection()== Direction.RIGHT){
                    scaleNormalOrUp(binding.linearLayoutRight,MotionEvent.AXIS_PRESSURE);
                    binding.rightTextView.setText("Viewing Right");
                }
                else{
                    scaleNormalOrUp(binding.linearLayoutRight,MotionEvent.ACTION_BUTTON_RELEASE);
                    binding.rightTextView.setText("Right");
                }
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

    public void reCalibrate(View view) {
        detect=new Detect(faceCascade,eyesCascade);
        System.gc();
    }

    public static void scaleNormalOrUp(final View v, final int motion){
        v.post(new Runnable() {
            @Override
            public void run() {
                if(motion== MotionEvent.AXIS_PRESSURE){
                    v.setScaleX(1.10f);
                    v.setScaleY(1.10f);}
                else{
                    v.setScaleX(1);
                    v.setScaleY(1);

                }

            }

        });}



}