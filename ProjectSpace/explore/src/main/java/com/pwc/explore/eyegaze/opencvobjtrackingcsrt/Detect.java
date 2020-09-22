package com.pwc.explore.eyegaze.opencvobjtrackingcsrt;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.pwc.explore.Direction;
import com.pwc.explore.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.MatOfRect2d;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.features2d.Params;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.tracking.MultiTracker;
import org.opencv.tracking.TrackerCSRT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.NEUTRAL;
import static com.pwc.explore.Direction.RIGHT;
import static com.pwc.explore.Direction.UNKNOWN;

/* Does the Detection and tracking of the Iris*/
public class Detect {

    private boolean isFirstPairOfIrisFound;
    private SimpleBlobDetector simpleBlobDetector;
    private static final String TAG = "Detect";
    private boolean needCalibration;
    private int frameCount;
    private static final int FRAME_CALIBRATION_RATE = 90;
    private GazeEstimator gazeEstimator;
    private Direction direction;
    private Direction prevDirection;
    private DetectionSmoother faceDetectionSmoother;
    private Rect prevFace;
    private static final float FACE_MOVEMENT_THRESHOLD=0.1f;
    private GazeStatus currentGazeStatus;
    private Queue<Boolean> isNeutralQueue;
    private static final int STABLE_NEUTRAL_QUEUE_THRESHOLD = 2;
    private TrackerCSRT trackerCSRT;
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private boolean isTrackerInitialised;
    private MultiTracker multiTracker;



    Detect(CascadeClassifier faceCascade,CascadeClassifier eyesCascade){
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();

        /* By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false */
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        isNeutralQueue = new LinkedList<>();
        currentGazeStatus= GazeStatus.UNKNOWN;
        trackerCSRT = TrackerCSRT.create();
        multiTracker = MultiTracker.create();
        this.eyesCascade = eyesCascade;
        this.faceCascade = faceCascade;
    }



    Mat detect(Mat frame) {
        Mat frameGray = new Mat();
        Mat frameRGB = new Mat();

        /*Creating RGB variant of frame*/
        Imgproc.cvtColor(frame,frameRGB,Imgproc.COLOR_RGBA2RGB);



        if (!isTrackerInitialised) {
            /*Creating a Grayscale version of the Image*/
            Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

            /*Increasing contrast & brightness of the image appropriately*/
            Imgproc.equalizeHist(frameGray, frameGray);

            /*Detecting faces*/
            MatOfRect faces = new MatOfRect();
            faceCascade.detectMultiScale(frameGray, faces);


            Rect face = null;
            /*Using the First Detected Face*/
            List<Rect> listOfFaces = faces.toList();

            if(listOfFaces.size()!=0) {
                face = listOfFaces.get(0);

                /*Detections made smoother*/
                face = faceDetectionSmoother.updateCoord(face);

                /*Displaying the boundary of the detected face*/
                Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
                Mat faceROI = frameGray.submat(face);

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
                List<Rect> listOfEyes = eyes.toList();
                ArrayList eyeBoundary=new ArrayList<>();
                Mat[] eyesROI=new Mat[listOfEyes.size()];
                List<Rect2d> listOfEyesRect2d  = new ArrayList<>(2);

                for(Rect eye:listOfEyes) {
                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x + eye.x;
                    eye.y = face.y + eye.y;

                    Rect2d eyeRect2d = changeRectType(eye.clone());
                    Log.d(TAG, "TrackerCSRT  init ");
                    Log.d(TAG, "eyeRect2d " + eyeRect2d.toString());

                    listOfEyesRect2d.add(eyeRect2d);
                }

                /*TODO find unique Rect2d*/
                if(listOfEyesRect2d.size() == 2 && listOfEyes.get(0).x!= listOfEyes.get(1).x) {
                    for (int i = 0; i < listOfEyes.size(); i++) { //Just get the first 2 detected eyes
                        Rect eye = listOfEyes.get(i);
                        eyesROI[i]=frame.submat(eye);
                        eyeBoundary.add(eye.clone());
                        Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));
                        Mat eyeROICanny = new Mat();
                        Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 2);
                        Mat binary= new Mat();
                        Imgproc.threshold(eyesROI[i],binary,100,255,Imgproc.THRESH_BINARY);
                        Log.d(TAG, "width: "+eyeROICanny.width());
                        Log.d(TAG, "height: "+eyeROICanny.height());
                        Mat reduceeyeROI= new Mat(binary, new Rect(0,0,eyeROICanny.cols(),eyeROICanny.rows()));
                        Log.d(TAG, "width: "+reduceeyeROI.height());
                        Log.d(TAG, "height: "+reduceeyeROI.width());
                        MatOfKeyPoint blobs = new MatOfKeyPoint();
                        Log.d(TAG, "eyeroi"+eyeROICanny.empty());
                        Log.d(TAG, "eyeroi"+eye);
                        simpleBlobDetector.detect(reduceeyeROI, blobs);
                        Rect ROI= new Rect(eye.x,eye.y,eyeROICanny.width(),eyeROICanny.height());
                        Mat dst=new Mat();
                        Mat colorCanny=new Mat();
                        Imgproc.cvtColor(eyeROICanny,colorCanny,Imgproc.COLOR_GRAY2BGR);
                        /*Core.addWeighted(frame.submat(ROI),0.0,colorCanny,1.0,0.0,frame.submat(ROI));*/
                        Log.d(TAG, "blobs: "+blobs.size().toString());
                        Log.d(TAG, "blobs: "+blobs.empty());
                        List<KeyPoint> blobArray=blobs.toList();
                        Log.d(TAG, "blobslist: "+blobArray.isEmpty());
                        Log.d(TAG, "blobslist: "+blobArray.size());
                        Log.d(TAG, "blobs: "+blobArray.get(0));
                        Log.d(TAG, "eye"+eye.x+"y is"+eye.y);
                        Log.d(TAG, "eye size"+eye.height+";"+eye.width);
                        Point blobcentre=blobArray.get(0).pt;
                        blobcentre.x=eye.x+blobcentre.x;
                        blobcentre.y=eye.y+blobcentre.y;
                        Rect2d eyerect=new Rect2d(blobcentre.x,blobcentre.y,eye.clone().width/3,eye.clone().height/3);
                        multiTracker.add(TrackerCSRT.create(),frameRGB, eyerect);}
                        isTrackerInitialised = true;
                        Log.d(TAG,"list of Rect2d :" +listOfEyesRect2d.toString());
                }
            }

        }

        if(isTrackerInitialised) {
            Log.d(TAG, "Tracker is going to be updated");
            MatOfRect2d updatedTrackerBoxes = new MatOfRect2d();
            multiTracker.update(frameRGB, updatedTrackerBoxes);
            Rect2d[] updatedRect2ds = updatedTrackerBoxes.toArray();
            for (Rect2d updatedTrackingBox : updatedRect2ds) {
                Log.d(TAG, "Updated Tracker Bounding box " + updatedTrackingBox);
                Rect updatedTrackingBoxRect = changeRectType(updatedTrackingBox);
                Imgproc.rectangle(frame, updatedTrackingBoxRect, new Scalar(0, 255, 0), 2);
            }
        }


        return frame;
    }


    /**
     * Checks if face has moved based on a threshold value and previous position of the Face
     * @param currentFace: A Section of the current frame which focuses on the Face
     * @return Value which mentions on whether face has moved or not*/
    private boolean hasFaceMoved(Rect currentFace){
        if(prevFace==null){
            prevFace=currentFace;
            return false;
        }
        else{
            float xDiff= Math.abs(prevFace.x-currentFace.x);
            float yDiff= Math.abs(prevFace.y-currentFace.y);
            /*   Log.d(TAG,"Face Movement xDiff:"+xDiff+" yDiff"+yDiff+"Threshold value "+prevFace.x*FACE_MOVEMENT_THRESHOLD);*/
            if(xDiff<(prevFace.x*FACE_MOVEMENT_THRESHOLD)&&yDiff<(prevFace.y*FACE_MOVEMENT_THRESHOLD)){
                prevFace=currentFace;
                return false;
            }
            prevFace=currentFace;
            return true;
        }
    }


    /**
     * Helper function to re-calibrate Optical Flow by using iris detection after N frames & when iris are detected again
     * @param calibrationIrisIdentified: If iris has been found
     * @param hasFaceMoved: If face has moved in the current frame in comparison to the previous frame*/
    private void calculateNeedCalibration(boolean calibrationIrisIdentified,boolean hasFaceMoved) {
        if (!calibrationIrisIdentified) {
            frameCount++;
        }
        if(hasFaceMoved){
            needCalibration=true;
            return;
        }
        if (frameCount > FRAME_CALIBRATION_RATE) {
            if (calibrationIrisIdentified) {
                frameCount = 0;
                needCalibration = false;
            } else {
                needCalibration = true;
            }
        }
    }


    /**
     *Creates Points in relation to the iris centre co-ordinates and the iris radius
     * @param irisCentre : Co-ordinates of the iris-centre
     * @param irisRadius : Radius of the Iris
     * @return A Point Array which has the relevant "Sparse" Points*/
    private Point[] getIrisSparsePoint(float irisRadius, Point irisCentre) {
        Point[] sparsePoints = new Point[5];
        sparsePoints[0] = irisCentre;
        sparsePoints[1] = new Point(irisCentre.x + irisRadius, irisCentre.y);
        sparsePoints[2] = new Point(irisCentre.x - irisRadius, irisCentre.y);
        sparsePoints[3] = new Point(irisCentre.x, irisCentre.y + irisRadius);
        sparsePoints[4] = new Point(irisCentre.x + irisRadius, irisCentre.y - irisRadius);
        return sparsePoints;
    }


    Direction getDirection() {
        if (direction == null) {
            return UNKNOWN;
        }
        return direction;
    }


    /**
     * Check if the Iris detected are not redundant and are  2 unique Iris
     * @param blob : Map which consist of the identified iris
     * @return  true if Unique Iris is identified
     */
    private boolean isUniqueIrisIdentified(HashMap<Integer, Point[]> blob) {
        if (blob.size() == 2) {
            if (blob.get(0) != null && blob.get(1) != null)
                /*First point wont be the same*/
                if(blob.get(0)[0]!=null && blob.get(1)[0]!=null )
                    return blob.get(0)[0] != blob.get(1)[0];

        }
        return false;
    }


    /**
     * Estimates the gaze direction based on current Gaze Direction and the current "Sparse" points
     * @param currentDirection : Gaze Direction based on current frame
     * @param currentPoints : Map which consists of the newly predicted/created "Sparse" Points*/
    private Direction directionEstimator(Direction currentDirection,HashMap<Integer,Point[]>currentPoints){
        if(prevDirection==null){
            prevDirection=currentDirection;
            return currentDirection;
        }
        Direction estimatedDirection=null;
        /*Log.d(TAG,"Before "+currentGazeStatus.toString()+ " CurrentDirection"+currentDirection.toString());*/
        if(currentGazeStatus!= GazeStatus.ON_THE_WAY_TO_NEUTRAL) {
            switch (currentDirection) {
                case LEFT:
                    if (prevDirection == NEUTRAL || prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.LEFT;
                    }  if (prevDirection == RIGHT) {
                    currentGazeStatus = GazeStatus.ON_THE_WAY_TO_NEUTRAL;
                }
                    estimatedDirection=LEFT;
                    break;
                case RIGHT:
                    if (prevDirection == NEUTRAL || prevDirection == RIGHT) {
                        currentGazeStatus = GazeStatus.RIGHT;

                    } else if (prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.ON_THE_WAY_TO_NEUTRAL;
                    }
                    estimatedDirection=RIGHT;
                    break;
                case NEUTRAL:
                    if(!(currentGazeStatus== GazeStatus.LEFT||currentGazeStatus== GazeStatus.RIGHT)){
                        currentGazeStatus = GazeStatus.NEUTRAL;
                        estimatedDirection=NEUTRAL;
                    }
                    else{
                        estimatedDirection=prevDirection;
                    }
            }
        }
        else{
            isNeutralQueue.add(gazeEstimator.isNeutral(currentPoints,true));
            if(isStableNeutral()){
                currentGazeStatus= GazeStatus.NEUTRAL;
                prevDirection=NEUTRAL;
                Log.d(TAG,"if- isStableNeutral returning NEUTRAL ");
                return NEUTRAL;
            }
        }
        if(estimatedDirection!=null){
            prevDirection=estimatedDirection;
            /* Log.d(TAG," if estimateddirection !=null : Estimated Direction "+estimatedDirection);*/
            return estimatedDirection;
        }
        /*Log.d(TAG," if estimateddirection ==null : Current Direction "+currentDirection);*/
        prevDirection=currentDirection;
        return currentDirection;
    }


    /**
     * Checks if the estimated Gaze Direction(Neutral) is a "True Positive"
     * @return true if the estimated Neutral is actually neutral */
    private boolean isStableNeutral(){
        if(isNeutralQueue.size()< STABLE_NEUTRAL_QUEUE_THRESHOLD){
            return false;
        }
        else{
            boolean isStableNeutral=true;
            for(int i=0;i<STABLE_NEUTRAL_QUEUE_THRESHOLD+1;i++){
                Boolean isNeutralVal = isNeutralQueue.poll();
                if (isNeutralVal != null) {
                    isStableNeutral = isStableNeutral && isNeutralVal;
                }
            }
            isNeutralQueue.clear();
            return  isStableNeutral;
        }
    }

    /* TODO: Need to do Javadoc comments*/
    private <T,U> T changeRectType(U inputRect){
        if(inputRect instanceof  Rect2d){
            Rect2d rect2d=(Rect2d) inputRect;
            Log.d(TAG,"Converted to Rect2d");
            return (T) new Rect((int)rect2d.x,(int)rect2d.y,(int)rect2d.width,(int)rect2d.height);
        }
        if (inputRect instanceof  Rect){
            Rect rect = (Rect) inputRect;
            Log.d(TAG,"Converted to Rect");
            return (T) new Rect2d(rect.x,rect.y,rect.width,rect.height);
        }

        return null;
    }

}
