package com.pwc.explore.eyegaze.opencvtrackingtest;

import android.util.Log;

import com.pwc.explore.Direction;

import org.opencv.core.Core;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.tracking.TrackerCSRT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.NEUTRAL;
import static com.pwc.explore.Direction.RIGHT;
import static com.pwc.explore.Direction.UNKNOWN;

/* Does the Detection and tracking of the Iris*/
public class Detect {


    private SimpleBlobDetector simpleBlobDetector;
    private static final String TAG = "Detect";
    private boolean needCalibration;
    public boolean lookdown;
    private int frameCount;
    private static final int FRAME_CALIBRATION_RATE = 90;
    private GazeEstimator gazeEstimator;
    private Direction direction;
    private Direction prevDirection;
    private Direction gaugeDirection;
    private DetectionSmoother faceDetectionSmoother;
    private Rect prevFace;
    private static final float FACE_MOVEMENT_THRESHOLD=0.1f;
    private GazeStatus currentGazeStatus;
    private Stack<Boolean> isNeutralStack;
    private static final int STABLE_NEUTRAL_QUEUE_THRESHOLD = 2;
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    private boolean isTrackerInitialised;
    private TrackerCSRT trackerCSRTFirst;
    private TrackerCSRT trackerCSRTSecond;
    private  TrackerCSRT[] trackerCSRTs;
    private int noofblack;
    ArrayList<Rect> eyeBoundary;
    HashMap<Integer, Point[]> prevPoints;
    HashMap<Integer, Point[]> currentPoints;




    Detect(CascadeClassifier faceCascade,CascadeClassifier eyesCascade){
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();

        /* By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false */
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        isNeutralStack = new Stack<Boolean>();
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
        Mat frameHSV= new Mat(); //new direction code

        frameCount++;
        /*Creating RGB variant of frame*/
        Imgproc.cvtColor(frame,frameRGB,Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(frame,frameHSV,Imgproc.COLOR_BGR2HSV); //new direction code
        HashMap<Integer, Point[]> blob = new HashMap<>();



        if (!isTrackerInitialised || frameCount%30 ==0) {
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
                 Mat[] eyesROI2=new Mat[listOfEyesRect.size()]; //new code for direction



                for(Rect eye:listOfEyesRect) {
                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x +eye.x;
                    eye.y = face.y +eye.y;

                }

                if(listOfEyesRect.size()==2){
                    eyeBoundary=new ArrayList<>();
                }


                if(listOfEyesRect.size() == 2 && (Math.abs(listOfEyesRect.get(0).x- listOfEyesRect.get(1).x)>listOfEyesRect.get(0).width*0.3)) {
                    for (int i = 0; i < listOfEyesRect.size(); i++) { //Just get the first 2 detected eyes
                        Rect eye = listOfEyesRect.get(i);
                       // eye.y= (int) (eye.y+ eye.height/3.5);
                       // eye.height= eye.height-eye.height/3;
                        Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));
                        eyesROI[i] = frame.submat(eye);
                        eyeBoundary.add(eye.clone());
                        Mat eyeROICanny = new Mat();
                        Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 3);
                       /* Log.d(TAG, "width: "+eyeROICanny.width());
                        Log.d(TAG, "height: "+eyeROICanny.height());
                        Log.d(TAG, "width: "+reduceeyeROI.height());
                        Log.d(TAG, "height: "+reduceeyeROI.width());*/
                        MatOfKeyPoint blobs = new MatOfKeyPoint();
                        /*Log.d(TAG, "eyeroi"+eyeROICanny.empty());
                        Log.d(TAG, "eyeroi"+eye);
                        Log.d(TAG, "blobs: "+blobs.size().toString());
                        Log.d(TAG, "blobs: "+blobs.empty());*/
                        List<KeyPoint> blobArray = blobs.toList();
                        if (blobArray.size() == 0) {
                            simpleBlobDetector.detect(eyeROICanny, blobs);
                            blobArray = blobs.toList();
                            //Log.d(TAG, "blob zero");
                        }
                        /*Log.d(TAG, "blobslist: "+blobArray.isEmpty());
                        Log.d(TAG, "blobslist: "+blobArray.size());
                        Log.d(TAG, "blobs: "+blobArray.get(0));
                        Log.d(TAG, "eye" + eye.x + "y is" + eye.y);
                        Log.d(TAG, "eye size" + eye.height + ";" + eye.width);*/
                        if (blobArray.size() != 0) {
                            Point blobcentre = blobArray.get(0).pt;
                            blobcentre.x = blobcentre.x + eye.x;
                            blobcentre.y = blobcentre.y + eye.y;
                            Rect2d rect2d = new Rect2d(blobcentre.x - eyeBoundary.get(i).width / 7.5, blobcentre.y - eyeBoundary.get(i).width / 7.5, eyeBoundary.get(i).width / 3.25, eyeBoundary.get(i).width / 3.25);
                            isTrackerInitialised = trackerCSRTs[i].init(frameRGB, rect2d);
                            //  Log.d(TAG, "is Tracker Init: " + isTrackerInitialised);
                            blob.put(i, new Point[]{blobcentre});
                        }
                        double sum=0;                                                   //new
                        for (int k=0;k<5;k++) {                                         //new
                            sum+=getLookdownposition(eye, frameHSV);                    //new
                        }                                                               //new
                        if(sum/5<95){                                                   //new
                            lookdown=false;                                             //new
                        }                                                               //new
                        else{lookdown=true;}                                            //new
                        /*Log.d(TAG,"list of Rect2d :" +listOfEyesRect2d.toString());*/
                    }
                    gazeEstimator.updateEyesBoundary(eyeBoundary);
                    isTrackerInitialised=true;
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


            for (int i = 0;i<trackerCSRTs.length;i++){
                Rect2d irisUpdatedRect = new Rect2d();
                boolean success=trackerCSRTs[i].update(frameRGB,irisUpdatedRect);
                if(success) {
                   // Log.d(TAG, "Updated" + i+"th Iris Rect (x,y): " + irisUpdatedRect.x + ", " + irisUpdatedRect.y);
                   // Imgproc.rectangle(frame, eyeBoundary.get(i), new Scalar(255,0 , 0));
                    double sum=0;                                                   //new
                    for (int k=0;k<5;k++) {                                         //new
                        sum+=getLookdownposition(eyeBoundary.get(i), frameHSV);     //new
                    }                                                               //new
                    if(sum/5<90){                                                   //new
                        lookdown=false;                                             //new
                    }                                                               //new
                    else{lookdown=true;}                                            //new
                    Imgproc.circle(frame, new Point(irisUpdatedRect.x + eyeBoundary.get(i).width / 7.5, irisUpdatedRect.y + eyeBoundary.get(i).width / 7.5), 3, new Scalar(0, 255, 0));
                    Point point = new Point(irisUpdatedRect.x + eyeBoundary.get(i).width / 7.5, irisUpdatedRect.y + eyeBoundary.get(i).width / 7.5);
                    currentPoints.put(i, new Point[]{point});
                }
                else{
                    isTrackerInitialised=false;
                    trackerCSRTFirst = TrackerCSRT.create();
                    trackerCSRTSecond = TrackerCSRT.create();
                    trackerCSRTs = new TrackerCSRT[]{trackerCSRTFirst,trackerCSRTSecond};
                    return frame;
                }
            }

            /* Log.d(TAG,"Movement predicted "+ gazeEstimator.estimateGaze(prevPoints,currentPoints));*/
            Log.d(TAG, "Gaze Status: "+currentGazeStatus+"frame "+frameCount);
            direction = directionEstimator(gazeEstimator.estimateGaze((HashMap<Integer, Point[]>) prevPoints.clone(), (HashMap<Integer, Point[]>) currentPoints.clone()), (HashMap<Integer, Point[]>) currentPoints.clone());
            Log.d(TAG, "Direction: "+direction+" frame "+frameCount);
            gaugeDirection=gaugeEstimator(direction);
            Log.d(TAG,"Gauge Direction: "+gaugeDirection+" frame "+frameCount);
            prevPoints = (HashMap<Integer, Point[]>) currentPoints.clone();}
        /*     Log.d(TAG,"Direction is "+ direction);*/

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
     * Return true if the given frame have the eye look down
     * @param eye: give it the Rect of the eye
     * @param frameHSV: give it the current frame
     * @return Boolean true or false to tell eye look down or not*/
    private double getLookdownposition(Rect eye, Mat frameHSV){
        Mat blacksub=new Mat();// new code
        Rect eye2 = new Rect(eye.x,eye.y,eye.width,eye.height/2); //new code for direction
        Mat eyesROI2=frameHSV.submat(eye2);// new code for direction
        Scalar blacklow= new Scalar(0,0,0);//new code
        Scalar blackhigh=new Scalar(360,255,38.25);// new code
        Core.inRange(eyesROI2,blacklow,blackhigh,blacksub);//new code
        noofblack=Core.countNonZero(blacksub);
        Log.d(TAG, "eyesize "+eye.width);
        Log.d(TAG, "eyesize "+eye.height);
        Log.d(TAG, "eyesize "+eye.width*eye.height);
        Log.d(TAG, "noofnonblack: "+noofblack);
        Log.d(TAG, "noofnonblack: "+((double)(eye.width*eye.height/2-noofblack)/(eye.width*eye.height/2))*100);
        double percentage=(double)((eye.width*eye.height/2-noofblack)/(eye.width*eye.height/2))*100;
        return percentage;
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
            isNeutralStack.push(gazeEstimator.isNeutral(currentPoints,true));
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
     *  Estimates the gaze direction based on current Gaze Direction and the current "Sparse" points
     */
    private Direction gaugeEstimator(Direction currentDirection){
        if(currentGazeStatus== GazeStatus.ON_THE_WAY_TO_NEUTRAL){
            return NEUTRAL;
        }
        else{
           return currentDirection;
        }
    }



    /**
     * Checks if the estimated Gaze Direction(Neutral) is a "True Positive"
     * @return true if the estimated Neutral is actually neutral */
    private boolean isStableNeutral(){
        if(isNeutralStack.size()< STABLE_NEUTRAL_QUEUE_THRESHOLD){
            return false;
        }
        else{
            boolean isStableNeutral=true;
            for(int i=0;i<STABLE_NEUTRAL_QUEUE_THRESHOLD;i++){
                Boolean isNeutralVal = isNeutralStack.pop();
                if (isNeutralVal != null) {
                    isStableNeutral = isStableNeutral && isNeutralVal;
                }
            }
            isNeutralStack.clear();
            return  isStableNeutral;
        }
    }

    /* TODO: Need to do Javadoc comments*/
    private <T,U> T changeRectType(U inputRect){
        if(inputRect instanceof  Rect2d){
            Rect2d rect2d=(Rect2d) inputRect;
            //Log.d(TAG,"Converted to Rect2d");
            return (T) new Rect((int)rect2d.x,(int)rect2d.y,(int)rect2d.width,(int)rect2d.height);
        }
        if (inputRect instanceof  Rect){
            Rect rect = (Rect) inputRect;
           // Log.d(TAG,"Converted to Rect");
            return (T) new Rect2d(rect.x,rect.y,rect.width,rect.height);
        }

        return null;
    }

}