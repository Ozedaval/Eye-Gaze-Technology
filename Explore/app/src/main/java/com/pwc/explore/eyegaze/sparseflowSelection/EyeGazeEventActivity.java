package com.pwc.explore.eyegaze.sparseflowSelection;


import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.internal.VisibilityAwareImageButton;
import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
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
    int calibrated = 0;
    double []leftTop = new double[2]; // left top
    double[] rightBottom = new double[2];
    double[] middle = new double[2];
    boolean calibration = false;
    ArrayList<Double> cb_eyeX_li = new ArrayList<>();
    ArrayList<Double> cb_eyeY_li = new ArrayList<>();
    ImageView aimImage;
    Runnable runnable;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        coordinatorLayout=findViewById(R.id.eventCoordinatorLayout);
        eyegazeTextView=findViewById(R.id.eventTextView);
        camera = findViewById(R.id.openCVCameraView);
        leftTop[0]=-1; leftTop[1]=-1;
        rightBottom[0]=-1; rightBottom[1]=-1;
        middle[0]=-1; middle[1]=-1;

        Snackbar.make(coordinatorLayout,R.string.in_development_note_msg,Snackbar.LENGTH_LONG).show();

        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        camera.setCameraPermissionGranted();
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(this);
        aimImage = (ImageView) findViewById(R.id.aim);
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

        List<String> itemList = new ArrayList<>();
        for(int i=0; i<2; i++){
            itemList.add("element"+(i+1)+"");
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        final ItemAdapter itemAdapter = new ItemAdapter(itemList,EyeGazeEventActivity.this, this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(EyeGazeEventActivity.this,2);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setLayoutManager(gridLayoutManager);
        this.itemAdapter = itemAdapter;
        detect.SCREEN_HEIGHT = itemAdapter.itemHeight;
        detect.SCREEN_WIDTH = itemAdapter.itemWidth;

        detect.itemAdapter = itemAdapter;


    }

    public void calibrate(View view){
        AlertDialog.Builder builder
                = new AlertDialog
                .Builder(EyeGazeEventActivity.this);
        // Set the message show for the Alert time
        builder.setMessage("Click OK to proceed the calibration. Please look at the aim placed on the middle of the screen" +
                "until the aim is disappeared");

        // Set Alert Title
        builder.setTitle("Calibration");
        builder.setCancelable(false);
        builder.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       calibration = true;
                       aimImage.setVisibility(View.VISIBLE);
                        for(int i = 0 ; i < 15000 ; i++){
                            cb_eyeX_li.add(detect.cb_eyeX);
                            cb_eyeY_li.add(detect.cb_eyeY);
                        }
                        cb_eyeX_li = filter_outliers(cb_eyeX_li); cb_eyeY_li = filter_outliers(cb_eyeY_li);
                        // average of X, Y -> Center of the rectangle
                        detect.averageX = sum(cb_eyeX_li) / cb_eyeX_li.size();
                        detect.averageY = sum(cb_eyeY_li) / cb_eyeY_li.size();
                    }
                });
        builder.setNegativeButton(
                "NO",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();

    }


    static ArrayList<Double> filter_outliers(ArrayList<Double> list){
        int[] IQR = get_IQR(list);
        for(int i = 0; i < list.size(); i++){
            if(list.get(i) < IQR[0] - 1.5*IQR[2] || (list.get(i) > IQR[1] + 1.5*IQR[2])){
                list.remove(i);
            }
        }
        return list;
    }
    static int[] get_IQR(ArrayList<Double> coordinates){
        // get IQR
        Collections.sort(coordinates);
        int mid_index = median(coordinates, 0, coordinates.size());
        // Median of first half
        double Q1 = coordinates.get(median(coordinates,0,mid_index));
        double Q3 = coordinates.get(median(coordinates, mid_index+1, coordinates.size()));

        int[] result = {(int)Q1,(int)Q3,(int)(Q3-Q1)};
        return result;
    }

    static int median(ArrayList<Double> a, int l, int r){
        int n = r - 1 + 1;
        n = ((n+1) / 2) -1;
        return n+1;
    }

    static int sum(ArrayList<Double> a){
        double sum = 0;
        for(int i = 0 ; i < a.size() ; i++){
            sum += a.get(i);
        } return (int) sum;
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

                if( detect.cb_eyeX > detect.averageX){
                    itemAdapter.select(0); // consider that it's mirror image
                }else{
                    itemAdapter.select(1);
                }
            }
        });
        if(calibration){
        aimImage.postDelayed(new Runnable() {
            @Override
            public void run() {
                aimImage.setVisibility(View.INVISIBLE);
                calibration =false;
            }
        },3000);}
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