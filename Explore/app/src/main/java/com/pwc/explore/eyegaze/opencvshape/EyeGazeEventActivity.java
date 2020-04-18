package com.pwc.explore.eyegaze.opencvshape;


import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pwc.explore.R;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.Arrays;
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


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        camera = findViewById(R.id.javaCameraView);
        camera.setVisibility(SurfaceView.VISIBLE);
        camera.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        camera.setCameraPermissionGranted();
        camera.disableFpsMeter();
        camera.setCvCameraViewListener(this);

        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        Log.d(getClass().getName(), Arrays.toString(fileList()));
        Log.d(getClass().getName(), getFileStreamPath("eyeModel.xml").getAbsolutePath());
        Log.d(getClass().getName(), getFileStreamPath("faceModel.xml").getAbsolutePath());
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

    }

    /*References for detect function:
    https://github.com/opencv/opencv/blob/master/samples/java/tutorial_code/objectDetection/cascade_classifier/ObjectDetectionDemo.java
    */
    public Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade){
        Mat frameGray = new Mat();
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(frameGray, frameGray);

        // -- Detect faces
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);

        List<Rect> listOfFaces = faces.toList();
        for (Rect face : listOfFaces) {
            Point center = new Point(face.x + face.width / 2, face.y + face.height / 2);
            Imgproc.ellipse(frame, center, new Size(face.width / 2, face.height / 2), 0, 0, 360,
                    new Scalar(100, 200, 100));
            Log.d(getClass().getName() +"Face "," X co-ordinate  is "+center.x +"Y co ordinate" + center.y );

            Mat faceROI = frameGray.submat(face);

            // -- In each face, detect eyes
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);

            List<Rect> listOfEyes = eyes.toList();
            for (Rect eye : listOfEyes) {
                Point eyeCenter = new Point(face.x + eye.x + eye.width / 2, face.y + eye.y + eye.height / 2);
                int radius = (int) Math.round((eye.width + eye.height) * 0.25);
                Imgproc.circle(frame, eyeCenter, radius, new Scalar(255, 0, 0), 4);
                Log.d(getClass().getName() +"Eyes "," X co-ordinate  is "+eyeCenter.x +"Y co ordinate" + eyeCenter.y );

            }
        }
        return frame;}

    @Override
    public void onResume()
    {
        camera.enableView();
        super.onResume();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(getClass().getName(),"On Camera View Started");
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Log.d(getClass().getName(),"OnCameraFrame triggered");
        return detect(inputFrame.rgba(),faceCascade,eyesCascade);
    }
}