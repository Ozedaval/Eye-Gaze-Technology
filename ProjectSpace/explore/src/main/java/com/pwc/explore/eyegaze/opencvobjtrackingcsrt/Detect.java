package com.pwc.explore.eyegaze.opencvobjtrackingcsrt;

import android.util.Log;

import com.pwc.explore.Direction;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.tracking.TrackerBoosting;
import org.opencv.tracking.TrackerCSRT;

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
    private static final int FRAME_CALIBRATION_RATE = 30;
    private GazeEstimator gazeEstimator;
    private Direction direction;
    private Direction prevDirection;
    private DetectionSmoother faceDetectionSmoother;
    private Rect prevFace;
    private static final float FACE_MOVEMENT_THRESHOLD=0.1f;
    private GazeStatus currentGazeStatus;
    private Queue<Boolean> isNeutralQueue;
    private static final int STABLE_NEUTRAL_QUEUE_THRESHOLD = 2;
    private TrackerCSRT[] trackerCSRTs;



    Detect() {
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();
        /* By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false */
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        isNeutralQueue = new LinkedList<>();
        currentGazeStatus= GazeStatus.UNKNOWN;

        /* Creating 2 TrackCSRT for 2 irises */
        trackerCSRTs = new TrackerCSRT[]{TrackerCSRT.create(),TrackerCSRT.create()};
    }


    /**
     * Detects and Tracks the Iris
     * Firstly, Face and Eyes are detected using Object Detection via the respective classifiers.
     * Then,the iris is detected via Blob Detection.
     * Sparse Optical Flow is used;5 "Sparse" Points are set on the iris.
     * Then the gaze is estimated based on :
     *  Detected motion based on previous "Sparse" points and current "Sparse" points
     *  Previous GazeStatus
     * Note: "Sparse" Points are re-calibrated every 30 frames or if the face has significantly moved,given that Iris can be detected.
     * See https://docs.google.com/presentation/d/1f_IIDERz56QFGvuWGBGN9E3q1qTx5n0Pq30BDqufBJk/edit#slide=id.g857a29acf1_0_2481
     * @param frame: OpenCV multidimensional array like form of the Image.
     * @param faceCascade: Classifier object to detect faces
     * @param eyesCascade: Classifier object to detect eyes */
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        /*Thread.dumpStack();*/
        /*Log.d(TAG,"Detect method called");*/
        calculateNeedCalibration(false,false);
        Mat frameGray = new Mat();
        Mat frameRGB = new Mat();

        /*Creating a frame which has 3 channels exactly, incoming frame is having 4 channels - most probably a,R,G,B*/
        Imgproc.cvtColor(frame,frameRGB,Imgproc.COLOR_RGBA2RGB);

        /*Creating a Grayscale version of the Image*/
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        /*Increasing contrast & brightness of the image appropriately*/
        Imgproc.equalizeHist(frameGray, frameGray);

        /*Detecting faces*/
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);

        List<Rect> eyeBoundary = null;
        HashMap<Integer, Point[]> blob = new HashMap<>();
        Rect face = null;

        /*Using the First Detected Face*/
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            face = listOfFaces.get(0);

            /*Detections made smoother*/
            face = faceDetectionSmoother.updateCoord(face);

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);

            if (!isFirstPairOfIrisFound || needCalibration) {

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
                List<Rect> listOfEyes = eyes.toList();
                eyeBoundary = new ArrayList<>();
                Mat[] eyesROI = new Mat[listOfEyes.size()];

                for (int eyeIndex = 0; eyeIndex < listOfEyes.size(); eyeIndex++) {
                    Rect eye = listOfEyes.get(eyeIndex);

                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x + eye.x;
                    eye.y = face.y + eye.y;

                    /*Cropping an eye Image*/
                    eyesROI[eyeIndex] = frame.submat(eye);

                    eyeBoundary.add(eye.clone());// avoiding references to the actual object

                    /*Displaying boundary of the detected eye*/
                   /* Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));*/

                    /*Iris Detection via Blob Detection*/
                    Mat eyeROICanny = new Mat();
                    Imgproc.Canny(eyesROI[eyeIndex], eyeROICanny, 50, 50 * 3);

                    MatOfKeyPoint blobs = new MatOfKeyPoint();
                    simpleBlobDetector.detect(eyeROICanny, blobs);
                    /*Log.d(TAG+ " Number of blobs ", blobs.toArray().length + "");*/
                    /*Log.d(TAG," Eye width:"+eye.width+" Eye height"+eye.height);*/

                    /*Finding Iris*/
                    KeyPoint[] blobsArray = blobs.toArray();
                    if (blobsArray.length != 0) {
                        Point blobCentre = blobsArray[0].pt;

                        /* blob is being detected w.r.t the eye boundary, changes below are to ensure it is w.r.t to the whole frame */
                        blobCentre.x = blobCentre.x + eye.x;
                        blobCentre.y = blobCentre.y + eye.y;

                   /*     Imgproc.circle(frame, blobCentre, 2, new Scalar(255, 0, 0), 2);*/
                       /*Log.d(TAG,"Height "+eye.height+"Width "+eye.width);
                        Log.d(TAG,"Iris Centre X"+blobCentre.x+"Iris Centre Y"+blobCentre.y);*/
                        float irisRadius = 2;//TODO(Need to find a value dependent on the size of the eye )
                        blob.put(eyeIndex, new Point[]{blobCentre});

                        /*Log.d(TAG,"Estimated iris size "+ eye.width/3);*/
                        /*Imgproc.rectangle(frame,new Rect((int) (blobCentre.x - eye.width/6), (int) (blobCentre.y - eye.width/6), eye.width/3,eye.width/3),new Scalar(0,255,0),1);*/
                    }
                }
            }
        }
        else {
            direction= UNKNOWN;
        }

        /*sparseOpticalFlow Initiator/Calibration Alternator*/
        if (face!= null && (!isFirstPairOfIrisFound || needCalibration) && isUniqueIrisIdentified(blob) && eyeBoundary.size() == 2) {

            /*Converting Rect eye to Rect2D eye*/
            Rect2d eyeFirst = changeRectType(eyeBoundary.get(0));
            Rect2d eyeSecond = changeRectType(eyeBoundary.get(1));




            /*Reset the Tracker */
            if(eyeFirst!=null) {
                trackerCSRTs[0].init(frameRGB, eyeFirst);
                Log.d(TAG,"EyeFirst details "+eyeFirst.toString());
            }
            if(eyeSecond!=null) {
                trackerCSRTs[1].init(frameRGB, eyeSecond);
                Log.d(TAG,"EyeSecond details "+eyeSecond.toString());
            }
            gazeEstimator.updateEyesBoundary(eyeBoundary);
            calculateNeedCalibration(true,hasFaceMoved(face));
            isFirstPairOfIrisFound = true;
        }

        if (isFirstPairOfIrisFound) {
            /*TODO get predicted points here*/
            Rect2d updatedEyeFirstBoundary = new Rect2d();
            Rect2d updatedEyeSecondBoundary = new Rect2d();

            Log.d(TAG,"Updating Tracker");
            trackerCSRTs[0].update(frameRGB,updatedEyeFirstBoundary);
            trackerCSRTs[1].update(frameRGB,updatedEyeSecondBoundary);
           /* Log.d(TAG,"Updated Rect Size "+ updatedEyeFirstBoundary.width);*/


            /*Converting to Rect*/
            Rect updatedEyeFirstBoundaryRect= (Rect) changeRectType(updatedEyeFirstBoundary);
            Rect updatedEyeSecondBoundaryRect= (Rect) changeRectType(updatedEyeFirstBoundary);
            if(updatedEyeFirstBoundaryRect!= null) {
                Imgproc.rectangle(frame, updatedEyeFirstBoundaryRect, new Scalar(255, 0, 0), 4);
                Log.d(TAG,"Updated EyeFirst details "+updatedEyeFirstBoundaryRect.toString());
            }
            if(updatedEyeSecondBoundaryRect!= null) {
                Imgproc.rectangle(frame, updatedEyeSecondBoundaryRect, new Scalar(255, 0, 0), 4);
                Log.d(TAG,"Updated EyeSecond details "+updatedEyeSecondBoundaryRect.toString());
            }
            /*         HashMap<Integer, Point[]> prevPoints = null;// For ease of debugging
             *//*Log.d(TAG,"Eye A Previous Points: "+ Arrays.toString(prevPoints.get(0))+"  Eye B Previous Points: "+ Arrays.toString(prevPoints.get(1)));*//*
            HashMap<Integer, Point[]> predictionsMap = null;
            *//*Log.d(TAG,"Eye A Predicted Points: "+ Arrays.toString(predictionsMap.get(0))+"  Eye B Predicted Points: "+ Arrays.toString(predictionsMap.get(1)));*//*
            direction=directionEstimator(gazeEstimator.estimateGaze(prevPoints,predictionsMap),predictionsMap);
            Log.d(TAG,"Frame Num"+frameCount+ "   is at direction "+direction + " GazeStatus"+ currentGazeStatus.toString() );
            Point[][][] irisPredictedSparsePointss = new Point[][][]
                    {{predictionsMap.get(0)}, {predictionsMap.get(1)}};
            for (Point[][] irisPredictedSparsePoints : irisPredictedSparsePointss) {
                for (Point[] points : irisPredictedSparsePoints) {
                    if (points != null) {
                        for (Point point : points) {
                            Imgproc.circle(frame, point, 3, new Scalar(255, 0, 0));
                        }
                    }
                }
            }*/
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
