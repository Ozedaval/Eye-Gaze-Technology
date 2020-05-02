package com.pwc.explore.eyegaze.opencvcolor;


import android.util.Log;
import com.pwc.explore.DetectionListener;
import com.pwc.explore.Direction;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.List;


public class Detect {

    private DetectionListener dl;

    Detect(DetectionListener dl){
        this.dl=dl;
    }

    private void sendDetection(Direction direction){
        dl.move(direction);
    }


    /*Iris Detection
   Face & Eye detection is inspired from:
   https://github.com/opencv/opencv/blob/master/samples/java/tutorial_code/objectDetection/cascade_classifier/ObjectDetectionDemo.java
   Ideology of finding max area used by contour:
   https://stackoverflow.com/questions/31504366/opencv-for-java-houghcircles-finding-all-the-wrong-circles
   */
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {

        Mat frameGray = new Mat();

        /*Creating a Grayscale version of the Image*/
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        /*Increasing contrast & brightness of the image appropriately*/
        Imgproc.equalizeHist(frameGray, frameGray);

        /*Detecting faces*/
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);

        /*Using the First Detected Face*/
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            Rect face = listOfFaces.get(0);
            /*Log.d(getClass().getName() + "Face ", " X co-ordinate  is " + center.x + "Y co ordinate" + center.y);*/

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame,face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);


            /*Detecting Eyes of the face*/
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);
            List<Rect> listOfEyes = eyes.toList();
            Mat[] eyesROI = new Mat[2];
            Rect[] eyesBoundary=new Rect[2];
            Point[] irisCenters=new Point[2];
            Log.d(getClass().getSimpleName(),"face.x= "+face.x+"face.y = "+face.y);
            try {
                for (int i = 0; i < listOfEyes.size(); i++) { //Just get the first 2 detected eyes
                    Rect eye = listOfEyes.get(i);

                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x=face.x+eye.x;
                    eye.y=face.y+eye.y;

                    /*Cropping an eye Image*/
                    eyesROI[i] = frame.submat(eye);
                    eyesBoundary[i]=eye;

                    /*Point eyeCenter = new Point(face.x + eye.x + eye.width / 2f, face.y + eye.y + eye.height / 2f);
                    int radiusEye = (int) Math.round((eye.width + eye.height) * 0.25);
                    Log.d("Detect" + " Eyes ", " X co-ordinate  is " + eyeCenter.x + "Y co ordinate" + eyeCenter.y);
                    Log.d("Detect" + " Eyes ", " X co-ordinate  is " + eye.x + "Y co ordinate" + eye.y);*/

                    /*Displaying boundary of the detected eye*/
                    Imgproc.rectangle(frame,eye,new Scalar(10, 0, 255));
                    /*TODO Iris-Color Detection & Estimate Gaze - Call sendDirection(Direction direction)*/

                } }catch (Exception e) {

                Log.e(getClass().getSimpleName(),"Error "+e.getMessage());
            }
        }
        return frame; }
}
