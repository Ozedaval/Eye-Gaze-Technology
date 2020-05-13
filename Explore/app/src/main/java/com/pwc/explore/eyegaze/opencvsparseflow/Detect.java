package com.pwc.explore.eyegaze.opencvsparseflow;


import android.util.Log;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.NEUTRAL;
import static com.pwc.explore.Direction.RIGHT;
import static com.pwc.explore.Direction.UNKNOWN;


public class Detect {


    private boolean isFirstPairOfIrisFound;
    private SimpleBlobDetector simpleBlobDetector;
    private SparseOpticalFlowDetector sparseOpticalFlowDetector;
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
    enum GazeStatus{ON_THE_WAY_TO_NEUTRAL,LEFT,RIGHT,UNKNOWN,NEUTRAL};
    private Queue<Direction> neutralQueue;
    private static final int STABLE_NEUTRAL_QUEUE_THRESHOLD = 2;


    Detect() {
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();
        /*By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false*/
        sparseOpticalFlowDetector = new SparseOpticalFlowDetector(new Size(30, 30), 2);
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        neutralQueue = new LinkedList();

    }


    /*Iris Detection*/
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        /*Thread.dumpStack();*/
        /*Log.d(TAG,"Detect method called");*/
        calculateNeedCalibration(false,false);
        Mat frameGray = new Mat();

        /*Creating a Grayscale version of the Image*/
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        /*Increasing contrast & brightness of the image appropriately*/
        Imgproc.equalizeHist(frameGray, frameGray);

        /*Detecting faces*/
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);


        List<Rect> eyeBoundary=null;
        HashMap<Integer, Point[]> blob = new HashMap<>();
        Rect face = null;

        /*Using the First Detected Face*/
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            face = listOfFaces.get(0);
            /*Detections made smoother*/
            face=faceDetectionSmoother.updateCoord(face);

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);


            if (!isFirstPairOfIrisFound || needCalibration) {

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
                List<Rect> listOfEyes = eyes.toList();
                eyeBoundary=new ArrayList<>();
                Mat[] eyesROI = new Mat[listOfEyes.size()];

                for (int i = 0; i < listOfEyes.size(); i++) { //Just get the first 2 detected eyes
                    Rect eye = listOfEyes.get(i);


                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x + eye.x;
                    eye.y = face.y + eye.y;

                    /*Cropping an eye Image*/
                    eyesROI[i] = frame.submat(eye);
                    eyeBoundary.add(eye.clone());// avoiding references to the actual object

                    /*Displaying boundary of the detected eye*/
                    Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));

                    /*Iris Detection via Blob Detection*/
                    Mat eyeROICanny = new Mat();
                    Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 3);

                    MatOfKeyPoint blobs = new MatOfKeyPoint();
                    simpleBlobDetector.detect(eyeROICanny, blobs);
                    /*Log.d(TAG+ " Number of blobs ", blobs.toArray().length + "");*/
                    /*Log.d(TAG," Eye width:"+eye.width+" Eye height"+eye.height);*/

                    /*Finding Iris*/
                    KeyPoint[] blobsArray = blobs.toArray();
                    if (blobsArray.length != 0) {
                        Point blobCentre = blobsArray[0].pt;
                        blobCentre.x = blobCentre.x + eye.x;
                        blobCentre.y = blobCentre.y + eye.y;
                        Imgproc.circle(frame, blobCentre, 2, new Scalar(255, 0, 0), 4);
                       /* Log.d(TAG,"Height "+eye.height+"Width "+eye.width);
                        Log.d(TAG,"Iris Centre X"+blobCentre.x+"Iris Centre Y"+blobCentre.y);*/
                        float irisRadius = 2;//TODO(Need to find a value dependent on the size of the eye )
                        blob.put(i, getIrisSparsePoint(irisRadius, blobCentre));
                    }
                }
            }
        }
        else {
            direction= UNKNOWN;
        }

        /*sparseOpticalFlow Initiator/Calibration Alternator*/
        if (face!=null&&(!isFirstPairOfIrisFound || needCalibration) && isUniqueIrisIdentified(blob)&& eyeBoundary.size()==2) {
            sparseOpticalFlowDetector.resetSparseOpticalFlow();
            for (Integer roiID : blob.keySet()) {
                sparseOpticalFlowDetector.setROIPoints(roiID, blob.get(roiID));
            }
            gazeEstimator.updateEyesBoundary(eyeBoundary);
            calculateNeedCalibration(true,hasFaceMoved(face));
            isFirstPairOfIrisFound = true;
        }


        if (isFirstPairOfIrisFound) {
            HashMap<Integer, Point[]> prevPoints = (HashMap<Integer, Point[]>) sparseOpticalFlowDetector.getROIPoints().clone();// For ease of debugging
            /*Log.d(TAG,"Eye A Previous Points: "+ Arrays.toString(prevPoints.get(0))+"  Eye B Previous Points: "+ Arrays.toString(prevPoints.get(1)));*/
            HashMap<Integer, Point[]> predictionsMap = sparseOpticalFlowDetector.predictPoints(frameGray);
            /*Log.d(TAG,"Eye A Predicted Points: "+ Arrays.toString(predictionsMap.get(0))+"  Eye B Predicted Points: "+ Arrays.toString(predictionsMap.get(1)));*/
            direction=directionEstimator(gazeEstimator.estimateGaze(prevPoints,predictionsMap),predictionsMap);
            Log.d(TAG,"Frame Num"+frameCount+ "   is at direction "+direction + " ");
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
            }
        }
        return frame;
    }


    private boolean hasFaceMoved(Rect currentFace){
        if(prevFace==null){
            prevFace=currentFace;
            return false;
        }
        else{
            float xDiff= Math.abs(prevFace.x-currentFace.x);
            float yDiff=Math.abs(prevFace.y-currentFace.y);
            /*   Log.d(TAG,"Face Movement xDiff:"+xDiff+" yDiff"+yDiff+"Threshold value "+prevFace.x*FACE_MOVEMENT_THRESHOLD);*/
            if(xDiff<(prevFace.x*FACE_MOVEMENT_THRESHOLD)&&yDiff<(prevFace.y*FACE_MOVEMENT_THRESHOLD)){
                prevFace=currentFace;
                return false;
            }
            prevFace=currentFace;
            return true;
        }
    }


    /*Helper function to re-calibrate Optical Flow by using iris detection after N frames & when eyes are detected again*/
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


    private boolean isUniqueIrisIdentified(HashMap<Integer, Point[]> blob) {
        if (blob.size() == 2) {
            if (blob.get(0) != null && blob.get(1) != null)
                /*First point wont be the same*/
                if(blob.get(0)[0]!=null && blob.get(1)[0]!=null )
                    return blob.get(0)[0] != blob.get(1)[0];
        }
        return false;
    }

    private Direction directionEstimator(Direction currentDirection, HashMap<Integer, Point[]> currentPoints){
        if(prevDirection==null){
            prevDirection=currentDirection;
            return currentDirection;
        }

        if(currentGazeStatus!=GazeStatus.ON_THE_WAY_TO_NEUTRAL) {
            switch (currentDirection) {
                case LEFT:
                    if (prevDirection == NEUTRAL || prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.LEFT;

                    } else if (prevDirection == RIGHT) {
                        currentGazeStatus = GazeStatus.ON_THE_WAY_TO_NEUTRAL;
                    }
                    break;
                case RIGHT:
                    if (prevDirection == NEUTRAL || prevDirection == RIGHT) {
                        currentGazeStatus = GazeStatus.RIGHT;

                    } else if (prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.ON_THE_WAY_TO_NEUTRAL;
                    }
                    break;
                case NEUTRAL:
                    currentGazeStatus = GazeStatus.NEUTRAL;

            }
        }
        else{
            neutralQueue.add(currentDirection);
            if(isStableNeutral()){
                currentGazeStatus=GazeStatus.NEUTRAL;
                prevDirection=NEUTRAL;
                return NEUTRAL;
            }

        }
        if(currentGazeStatus==GazeStatus.ON_THE_WAY_TO_NEUTRAL){
            return NEUTRAL;
        }
        prevDirection=currentDirection;
        return currentDirection;    }


    private boolean isStableNeutral(){
        if(neutralQueue.size()< STABLE_NEUTRAL_QUEUE_THRESHOLD){
            return false;
        }
        else{
            boolean isStableNeutral=true;
            while(neutralQueue.size()!=0){
                isStableNeutral=isStableNeutral && neutralQueue.poll()==NEUTRAL;
            }
            return  isStableNeutral;
        }

    }
  /*   private Direction directionEstimator(Direction currentDirection, HashMap<Integer, Point[]> currentPoints){
        if(prevDirection==null){
            prevDirection=currentDirection;
            return currentDirection;
        }

        switch(prevDirection){
            case NEUTRAL:
                if(currentDirection== LEFT || currentDirection== RIGHT){
                    return  currentDirection;
                }
            case LEFT:
                if(currentDirection== NEUTRAL){
                    if(gazeEstimator.isNeutral(currentPoints)){
                        prevDirection = NEUTRAL;
                        return NEUTRAL;
                    }
                    prevDirection= LEFT;
                    return LEFT;
                }else if(currentDirection== RIGHT){
                    prevDirection= RIGHT;
                    return NEUTRAL;
                }
            case RIGHT:
                if(currentDirection== NEUTRAL){
                    if(gazeEstimator.isNeutral(currentPoints)){
                        prevDirection= NEUTRAL;
                        return NEUTRAL;
                    }
                    Log.d(TAG,"NEUTRAL");
                    prevDirection= RIGHT;
                    return RIGHT;
                }else if(currentDirection== LEFT){
                    prevDirection= LEFT;
                    return NEUTRAL;
                }
            default:
                return currentDirection;

        }
    }
*/


}