package com.pwc.explore.eyegaze.opencvcolor;


import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;
import com.pwc.explore.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;


public class EyeGazeEventActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
/*    TODO  (1.Temporarily do actually manipulation of eyegaze here.
               2.Add UI changes accordingly
               3.Use Cursor Class appropriately
               4.Need to address Activity Lifecycle)
      Note: The image layout "Test" is temporary since as far
      as I have searched it appears that majority of  OpenCV implementations  uses the cameraBridgeViewBase - produces a preview.
      This preview can be hidden by changing the output of the callback:onCameraFrame.
      The Output produced by the camera is flipped & needs to be addressed.This is an already existing underlying issue in OpenCV*/


    static{ System.loadLibrary( "opencv_java4" );}
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private CameraBridgeViewBase camera;
    private CoordinatorLayout coordinatorLayout;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        coordinatorLayout=findViewById(R.id.eventCoordinatorLayout);
        Snackbar.make(coordinatorLayout,R.string.in_development_note_msg,Snackbar.LENGTH_LONG).show();
        camera = findViewById(R.id.openCVCameraView);
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        camera.setCameraPermissionGranted();
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(this);

        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
//        Log.d(getClass().getName(), Arrays.toString(fileList()));
//        Log.d(getClass().getName(), getFileStreamPath("eyeModel.xml").getAbsolutePath());
//        Log.d(getClass().getName(), getFileStreamPath("faceModel.xml").getAbsolutePath());
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

    }

    /*Iris Detection
    */
    public Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);

        // -- Detect faces
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);

        //Just use the first face detected
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            Rect face = listOfFaces.get(0);
            Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
            Imgproc.ellipse(frame, center, new Size(face.width / 2, face.height / 2), 0, 0, 360,
                    new Scalar(100, 200, 100));
//            Log.d(getClass().getName() + "Face ", " X co-ordinate  is " + center.x + "Y co ordinate" + center.y);
            Mat faceROI = frameGray.submat(face);


            // -- In each face, detect eyes
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);
            List<Rect> listOfEyes = eyes.toList();
            Mat[] eyesROI = new Mat[2];
            try {
                for (int i = 0; i < listOfEyes.size(); i++) {
                    Rect eye = listOfEyes.get(i);
              /*  Point eyeCenter = new Point(face.x + eye.x + eye.width / 2, face.y + eye.y + eye.height / 2);
                int radius = (int) Math.round((eye.width + eye.height) * 0.25);
               Imgproc.circle(frame, eyeCenter, radius, new Scalar(255, 0, 0), 4);
                Log.d(getClass().getName() + " Eyes ", " X co-ordinate  is " + eyeCenter.x + "Y co ordinate" + eyeCenter.y);*/
                    eyesROI[i] = faceROI.submat(eye);
                    Log.d(getClass().getSimpleName() + "Contours", "Reached");
                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat hierarchy = new Mat();
                    Mat cannyOutput = new Mat();
                    Imgproc.Canny(eyesROI[i], cannyOutput, 100, 100 * 2);
                    Imgproc.findContours(cannyOutput,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

                    for (int m = 0; m < contours.size(); m++) {
                        //TODO(Shape detection)
                        Scalar color = new Scalar(255, 0, 0);
                        Point eyePoint = new Point(face.x + eye.x , face.y + eye.y );
                        Log.d(getClass().getSimpleName() +" Contours" + i,contours.size()+"");
                        Imgproc.drawContours(frame, contours, m, color, 1, Imgproc.LINE_8, hierarchy, 0,eyePoint);
                    }

                }

//               Log.d(getClass().getSimpleName() + "Contours", eyesROI[0].empty()+ "");
            } catch (Exception e) {

                Log.e(getClass().getSimpleName(),"Error"+e.getMessage());
            }

        }
        return frame; }



    @Override
    public void onResume()
    {
        camera.enableView();
        super.onResume();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
//        Log.d(getClass().getName(),"On Camera View Started");
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        Log.d(getClass().getName(),"OnCameraFrame triggered");
        return detect(inputFrame.rgba(),faceCascade,eyesCascade);
    }

    @Override
    protected void onDestroy() {
        camera.surfaceDestroyed(camera.getHolder());
        super.onDestroy();
    }
}