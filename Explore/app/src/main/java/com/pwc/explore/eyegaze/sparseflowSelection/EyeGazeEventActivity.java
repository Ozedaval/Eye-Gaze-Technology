package com.pwc.explore.eyegaze.sparseflowSelection;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.MainActivity;
import com.pwc.explore.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;


public class EyeGazeEventActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, ItemAdapter.OnItemListener{
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
    private CameraBridgeViewBase camera;
    private CoordinatorLayout coordinatorLayout;
    private TextView eyegazeTextView;
    private Detect detect;
    private static final String TAG="EyeGazeEventActivity";
    ItemAdapter itemAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        coordinatorLayout=findViewById(R.id.eyeGazeCoordinatorLayout);
        eyegazeTextView=findViewById(R.id.eyeGazeTextView);
        camera = findViewById(R.id.javaCameraView);

        Snackbar.make(coordinatorLayout,R.string.in_development_note_msg,Snackbar.LENGTH_LONG).show();

        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        camera.setCameraPermissionGranted();
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(this);

        detect=new Detect();
        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        /*Log.d(TAG, Arrays.toString(fileList()));
        Log.d(TAG, getFileStreamPath("eyeModel.xml").getAbsolutePath());
        Log.d(TAG, getFileStreamPath("faceModel.xml").getAbsolutePath());*/
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        detect.screenHeight = displayMetrics.heightPixels;
        detect.screenWidth = displayMetrics.widthPixels;

        double screenX = detect.screenXY[0];
        double screenY = detect.screenXY[1];

        Bitmap btm = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.icon);

        List<String> itemList = new ArrayList<>();
        for(int i=0; i<3; i++){
            itemList.add("element"+(i+1)+"");
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        final ItemAdapter itemAdapter = new ItemAdapter(itemList,EyeGazeEventActivity.this, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(EyeGazeEventActivity.this,3);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.itemAdapter = itemAdapter;
        detect.itemAdapter = itemAdapter;




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
       eyegazeTextView.post(new Runnable() {
           @Override
           public void run() {

               eyegazeTextView.setText(detect.getDirection()+"");
           }
       });
        return detect.detect(inputFrame.rgba(),faceCascade,eyesCascade);
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

    @Override
    public void onItemClick(int position) {

    }
}