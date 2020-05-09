
package com.pwc.explore.eyegaze.opencvsparseflow;


import android.util.Log;
import com.pwc.explore.DetectionListener;
import com.pwc.explore.Direction;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Detect {

    private DetectionListener detectionListener;
    private boolean isFirstPairOfIrisFound;
    private SimpleBlobDetector simpleBlobDetector;
    private SparseOpticalFlowDetector sparseOpticalFlowDetector;
    private static final String TAG="Detect";
    private boolean needCalibration;
    private int frameCount;
    private static final int FRAME_CALIBRATION_RATE=90;

    Detect(DetectionListener dl) {
        this.detectionListener = dl;
        simpleBlobDetector = SimpleBlobDetector.create();
        /*By Default isFirstPairOfIrisFound & needCalibration is false*/
        sparseOpticalFlowDetector = new SparseOpticalFlowDetector(new Size(30,30 ),2);
    }


    private void sendDetection(Direction direction) {
        detectionListener.move(direction);
    }


    /*Iris Detection*/
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        /*Thread.dumpStack();*/

        calculateNeedCalibration(false);
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

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);

            Set<Integer> blobID=new HashSet<>();
            if (!isFirstPairOfIrisFound||needCalibration) {

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
                List<Rect> listOfEyes = eyes.toList();
                Mat[] eyesROI = new Mat[listOfEyes.size()];

                for (int i = 0; i < listOfEyes.size(); i++) { //Just get the first 2 detected eyes
                    Rect eye = listOfEyes.get(i);

                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x + eye.x;
                    eye.y = face.y + eye.y;

                    /*Cropping an eye Image*/
                    eyesROI[i] = frame.submat(eye);

                    /*Displaying boundary of the detected eye*/
                    Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));

                    /*Iris Detection via Blob Detection*/
                    Mat eyeROICanny = new Mat();
                    Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 3);

                    MatOfKeyPoint blobs = new MatOfKeyPoint();
                    simpleBlobDetector.detect(eyeROICanny, blobs);
                    /*Log.d(TAG+ " Number of blobs ", blobs.toArray().length + "");*/

                    /*Finding Iris*/
                    KeyPoint[] blobsArray = blobs.toArray();
                    if (blobsArray.length != 0) {
                        Point blobCentre = blobsArray[0].pt;
                        blobCentre.x = blobCentre.x + eye.x;
                        blobCentre.y = blobCentre.y + eye.y;
                        Imgproc.circle(frame, blobCentre, 2, new Scalar(255, 0, 0), 4);
                        Log.d(TAG,"Height "+eye.height+"Width "+eye.width);
                        Log.d(TAG,"Iris Centre X"+blobCentre.x+"Iris Centre Y"+blobCentre.y);
                        float irisRadius=2;//TODO(Need to find a value dependent on the size of the eye )
                        blobID.add(i);
                        sparseOpticalFlowDetector.resetSparseOpticalFlow();
                        sparseOpticalFlowDetector.setROIPoints(i,getIrisSparsePoint(irisRadius,blobCentre));
                    }
                }
            }

            /*sparseOpticalFlow Initiator/Calibration Alternator*/
            if ((!isFirstPairOfIrisFound||needCalibration) && blobID.size()==2) {
                calculateNeedCalibration(true);
                isFirstPairOfIrisFound = true;
            }

            if (isFirstPairOfIrisFound) {
                HashMap<Integer,Point[]> prevPoints=sparseOpticalFlowDetector.getROIPoints();// For ease of debugging
                Log.d(TAG,"Eye A Previous Points: "+ Arrays.toString(prevPoints.get(0))+"  Eye B Previous Points: "+Arrays.toString(prevPoints.get(1)));
                sparseOpticalFlowDetector.predictPoints(frameGray);
                HashMap<Integer,Point[]> predictionsMap=sparseOpticalFlowDetector.getROIPoints();
                Log.d(TAG,"Eye A Predicted Points: "+ Arrays.toString(predictionsMap.get(0))+"  Eye B Predicted Points: "+Arrays.toString(predictionsMap.get(1)));
                Point[][][] irisPredictedSparsePointss= new Point[][][]
                        {{predictionsMap.get(0)},{predictionsMap.get(1)}};
                for (Point[][] irisPredictedSparsePoints : irisPredictedSparsePointss) {
                    for (Point[] points : irisPredictedSparsePoints) {
                        if (points != null) {
                            for (Point point : points) {
                                Imgproc.circle(frame, point, 3, new Scalar(255, 0, 0));
                            }
                        }
                    }
                }
            }
        }
        return frame;
    }

    /*Helper function to re-calibrate Optical Flow by using iris detection after N frames & when eyes are detected again*/
    private void calculateNeedCalibration(boolean calibrationIrisIdentified){
        if(!calibrationIrisIdentified){
            frameCount++;
        }
        if(frameCount>FRAME_CALIBRATION_RATE){
            if(calibrationIrisIdentified){
                frameCount=0;
                needCalibration=false;
            }else{
                needCalibration= true;
            }
        }

    }

    private Point[] getIrisSparsePoint(float irisRadius, Point irisCentre){
        Point[] sparsePoints= new Point[5];
        sparsePoints[0]=irisCentre;
        sparsePoints[1]=new Point(irisCentre.x+irisRadius,irisCentre.y);
        sparsePoints[2]=new Point(irisCentre.x-irisRadius,irisCentre.y);
        sparsePoints[3]=new Point(irisCentre.x,irisCentre.y+irisRadius);
        sparsePoints[4]=new Point(irisCentre.x+irisRadius,irisCentre.y-irisRadius);
        return sparsePoints;
    }
}