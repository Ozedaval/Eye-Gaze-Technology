package com.pwc.explore.eyegaze.opencvshape;


import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.DetectionListener;
import com.pwc.explore.Direction;
import com.pwc.explore.R;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;


public class EyeGazeEventActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, DetectionListener {
/*    TODO  (  1.Update existing UI & Link up RecyclerView UI
               3.Use Cursor Class appropriately
               4.Need to address Activity Lifecycle
               5.Use ViewBinding once UI is finalised)
      Note: The image layout "screen" is temporary since as far
      as I have searched it appears that majority of  OpenCV implementations  uses the cameraBridgeViewBase - produces a preview.
      This preview can be hidden by changing the output of the callback:onCameraFrame.
      The Output produced by the camera is flipped & needs to be addressed.This is an already existing underlying issue in OpenCV*/


    static{ System.loadLibrary( "opencv_java4" );}
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private CameraBridgeViewBase camera;
    private CoordinatorLayout coordinatorLayout;
    private TextView eyegazeTextView;
    private Detect detect;
    private SurfaceView surfaceView;

    private static final String TAG="EyeGazeEventActivity";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        coordinatorLayout=findViewById(R.id.eventCoordinatorLayout);
        eyegazeTextView=findViewById(R.id.eventTextView);
        camera = findViewById(R.id.openCVCameraView);

        Snackbar.make(coordinatorLayout,R.string.in_development_note_msg,Snackbar.LENGTH_LONG).show();
        camera.setCameraPermissionGranted();
        camera.setCvCameraViewListener( this);


        detect=new Detect(this);
        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        /*Log.d(TAG, Arrays.toString(fileList()));
        Log.d(TAG, getFileStreamPath("eyeModel.xml").getAbsolutePath());
        Log.d(TAG, getFileStreamPath("faceModel.xml").getAbsolutePath());*/
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());
    }


    @Override
    public void move(Direction direction) {
        Log.d(TAG,"Direction is "+direction.name());
        eyegazeTextView.setText(direction.name()+" ");
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
        /*Log.d(TAG,"Columns : "+   inputFrame.rgba().cols()+ " Rows :"+
                inputFrame.rgba().rows()  );*/
       return detect.detect(inputFrame.rgba(), faceCascade, eyesCascade);
    }

    @Override
    public void onResume()
    {  super.onResume();
        camera.enableView();
    }

    @Override
    protected void onDestroy() {
        camera.surfaceDestroyed(camera.getHolder());
        super.onDestroy();
    }
}