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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
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

    private SimpleBlobDetector simpleBlobDetector;
    private static final String TAG = "Detect";
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
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private boolean isTrackerInitialised;
    private TrackerCSRT trackerCSRTFirst;
    private TrackerCSRT trackerCSRTSecond;
    private  TrackerCSRT[] trackerCSRTs;
    ArrayList<Rect> eyeBoundary;
    HashMap<Integer, Point[]> prevPoints;
    HashMap<Integer, Point[]> currentPoints;




    Detect(CascadeClassifier faceCascade,CascadeClassifier eyesCascade){
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();

        /* By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false */
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        isNeutralQueue = new LinkedList<>();
        currentGazeStatus= GazeStatus.UNKNOWN;
        this.eyesCascade = eyesCascade;
        this.faceCascade = faceCascade;
        trackerCSRTFirst = TrackerCSRT.create();
        trackerCSRTSecond = TrackerCSRT.create();
        trackerCSRTs = new TrackerCSRT[]{trackerCSRTFirst,trackerCSRTSecond};

    }



    Mat detect(Mat frame) {
        Mat frameGray = new Mat();
        Mat frameRGB = new Mat();
        frameCount++;
        /*Creating RGB variant of frame*/
        Imgproc.cvtColor(frame,frameRGB,Imgproc.COLOR_RGBA2RGB);
        HashMap<Integer, Point[]> blob = new HashMap<>();


        if (!isTrackerInitialised || frameCount%100==0) {
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
                List<Rect> listOfEyesRect = eyes.toList();

                Mat[] eyesROI=new Mat[listOfEyesRect.size()];
                for(int i = 0; i < listOfEyesRect.size(); i++){
                    Rect eye = listOfEyesRect.get(i);
                    eyeBoundary = new ArrayList<Rect>(2);

                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x + eye.x;
                    eye.y = face.y + eye.y;

                    Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));
                    eyesROI[i] = frame.submat(eye);
                    eyeBoundary.add(eye.clone());

                }


                if(isUniqueEyeFound(eyeBoundary)) {
                    /*Assumption*/
                    boolean isSuccessfulTracked = true;
                    for (int i = 0; i < listOfEyesRect.size(); i++) {
                        Rect eye = listOfEyesRect.get(i);
                        Mat eyeROICanny = new Mat();
                        Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 2);
                        MatOfKeyPoint blobs = new MatOfKeyPoint();

                        /*Finding Iris*/
                        simpleBlobDetector.detect(eyeROICanny, blobs);
                        List<KeyPoint> blobArray = blobs.toList();


                        if (blobArray.size() != 0) {
                            Point blobCentre = blobArray.get(0).pt;
                            blobCentre.x = eye.x + blobCentre.x;
                            blobCentre.y = eye.y + blobCentre.y;
                            Log.d(TAG,"Eye Boundary "+ eyeBoundary.toString());
                            Rect2d rect2d = new Rect2d(blobCentre.x-eyeBoundary.get(i).width/7.5,blobCentre.y-eyeBoundary.get(i).width/7.5,eyeBoundary.get(i).width/3.25,eyeBoundary.get(i).width/3.25);
                            isSuccessfulTracked &= trackerCSRTs[i].init(frameRGB,rect2d);
                            Log.d(TAG,"isSuccessfulTracked " + isSuccessfulTracked);
                            blob.put(i,  new Point[]{blobCentre});
                        }

                    }
                    isTrackerInitialised = isSuccessfulTracked;
                    gazeEstimator.updateEyesBoundary(eyeBoundary);

                }
            }

        }

        if(isTrackerInitialised) {
            if(prevPoints == null){
                prevPoints = (HashMap<Integer, Point[]>) blob.clone();
            }
            if(currentPoints == null){
                currentPoints = new HashMap<>();
            }

            /*     Log.d(TAG, "Tracker is going to be updated");*/
          /*  MatOfRect2d updatedTrackerBoxes = new MatOfRect2d();
            multiTracker.update(frameRGB, updatedTrackerBoxes);*/
            /*Rect2d[] updatedRect2ds = updatedTrackerBoxes.toArray();*/
            for (int i = 0;i<trackerCSRTs.length;i++){
                Rect2d rect2d = new Rect2d();
                trackerCSRTs[i].update(frameRGB,rect2d);
                Imgproc.circle(frame,new Point(rect2d.x+eyeBoundary.get(i).width/7.5,rect2d.y+eyeBoundary.get(i).width/7.5),3,new Scalar(0,255,0));
                Point point  = new Point(rect2d.x+eyeBoundary.get(i).width/7.5,rect2d.y+eyeBoundary.get(i).width/7.5);
                Log.d(TAG,"Updated point "+ point.toString());
                currentPoints.put(i,new Point[]{point});

            }
            /* Log.d(TAG,"Movement predicted "+ gazeEstimator.estimateGaze(prevPoints,currentPoints));*/
            direction = directionEstimator(gazeEstimator.estimateGaze((HashMap<Integer, Point[]>) prevPoints.clone(), (HashMap<Integer, Point[]>) currentPoints.clone()), (HashMap<Integer, Point[]>) currentPoints.clone());
            prevPoints = (HashMap<Integer, Point[]>) currentPoints.clone();
            /*     Log.d(TAG,"Direction is "+ direction);*/

        }
        //Mat Cannyframe=new Mat();
        //Imgproc.Canny(frame,Cannyframe,50,50*2);
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


    boolean isUniqueEyeFound(List<Rect> eyes){
        if(eyes!=null && eyes.size()==2){
            boolean isXCoordSame = eyes.get(0).x == eyes.get(1).x;
            boolean isYCoordSame = eyes.get(0).y == eyes.get(1).y;
     /*    Log.d(TAG,"is Unique irises "+ (!isXCoordSame || !isYCoordSame));*/
        return !isXCoordSame || !isYCoordSame;
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
        if(currentGazeStatus!=GazeStatus.ON_THE_WAY_TO_NEUTRAL) {
            switch (currentDirection) {
                case LEFT:
                    if (prevDirection == NEUTRAL || prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.LEFT;
                    }  if (prevDirection == RIGHT) {
                    currentGazeStatus =GazeStatus.ON_THE_WAY_TO_NEUTRAL;
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
                    if(!(currentGazeStatus==GazeStatus.LEFT||currentGazeStatus== GazeStatus.RIGHT)){
                        currentGazeStatus =GazeStatus.NEUTRAL;
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
                currentGazeStatus=GazeStatus.NEUTRAL;
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