package com.pwc.commsgaze.detection;

import android.util.Log;

import com.pwc.commsgaze.Direction;
import com.pwc.commsgaze.detection.data.DetectionData;
import com.pwc.commsgaze.detection.data.SparseFlowDetectionData;
import com.pwc.commsgaze.detection.utils.DetectionSmoother;
import com.pwc.commsgaze.detection.utils.GazeEstimator;
import com.pwc.commsgaze.detection.utils.GazeStatus;
import com.pwc.commsgaze.detection.utils.SparseOpticalFlowMediator;

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
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import static com.pwc.commsgaze.Direction.LEFT;
import static com.pwc.commsgaze.Direction.NEUTRAL;
import static com.pwc.commsgaze.Direction.RIGHT;
import static com.pwc.commsgaze.Direction.UNKNOWN;

/* Does the Detection and tracking of the Iris*/
public class SparseFlowDetector extends Detector {

    private boolean isFirstPairOfIrisFound;
    private SimpleBlobDetector simpleBlobDetector;
    private SparseOpticalFlowMediator sparseOpticalFlowMediator;
    private static final String TAG = "SparseFlowDetector";
    private boolean needCalibration;
    private int frameCount;
    private static final int FRAME_CALIBRATION_RATE = 30;
    private GazeEstimator gazeEstimator;
    private Direction direction;
    private Direction prevDirection;
    private DetectionSmoother faceDetectionSmoother;
    private Rect prevFace;
    private static final float FACE_MOVEMENT_THRESHOLD = 0.1f;
    private GazeStatus currentGazeStatus;
    private Stack<Boolean> isNeutralStack;
    private static final int STABLE_NEUTRAL_STACK_THRESHOLD = 2;
    private final Approach approach = Approach.OPENCV_SPARSE_FLOW;
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyeCascade;
    private boolean isFaceDetected;
    private boolean isLeftEyeDetected;
    private boolean isRightEyeDetected;





    SparseFlowDetector(DetectionData detectionData) {
        SparseFlowDetectionData sparseFlowDetectionData = (SparseFlowDetectionData)detectionData;
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();
        /*By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false*/
        sparseOpticalFlowMediator = new SparseOpticalFlowMediator(new Size(30, 30), 2);
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        isNeutralStack = new Stack<>();
        currentGazeStatus = GazeStatus.UNKNOWN;
        this.faceCascade = sparseFlowDetectionData.getFaceCascade();
        this.eyeCascade = sparseFlowDetectionData.getEyeCascade();
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
     */
    Mat detect(Mat frame) {
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
            face = faceDetectionSmoother.updateCoord(face);

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);
            isFaceDetected = true;

            if (!isFirstPairOfIrisFound || needCalibration) {

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyeCascade.detectMultiScale(faceROI, eyes);
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

                    /*Updating Detection Flag accordingly*/


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
                        float irisRadius = 2;
                        blob.put(i, getIrisSparsePoint(irisRadius, blobCentre));
                    }
                }
                updateEyeDetectionFlag( eyeBoundary,face);
            }
        }
        else {
            direction= UNKNOWN;
        }

        /*sparseOpticalFlow Initiator/Calibration Alternator*/
        if (face!=null && (!isFirstPairOfIrisFound || needCalibration) && isUniqueIrisIdentified(blob)&& eyeBoundary.size()==2) {
            sparseOpticalFlowMediator.resetSparseOpticalFlow();
            for (Integer roiID : blob.keySet()) {
                sparseOpticalFlowMediator.setROIPoints(roiID, blob.get(roiID));
            }
            gazeEstimator.updateEyesBoundary(eyeBoundary);
            calculateNeedCalibration(true,hasFaceMoved(face));
            isFirstPairOfIrisFound = true;
        }
        if (isFirstPairOfIrisFound) {
            HashMap<Integer, Point[]> prevPoints = (HashMap<Integer, Point[]>) sparseOpticalFlowMediator.getROIPoints().clone();// For ease of debugging
            /*Log.d(TAG,"Eye A Previous Points: "+ Arrays.toString(prevPoints.get(0))+"  Eye B Previous Points: "+ Arrays.toString(prevPoints.get(1)));*/
            HashMap<Integer, Point[]> predictionsMap = sparseOpticalFlowMediator.predictPoints(frameGray);
            /*Log.d(TAG,"Eye A Predicted Points: "+ Arrays.toString(predictionsMap.get(0))+"  Eye B Predicted Points: "+ Arrays.toString(predictionsMap.get(1)));*/
            direction=directionEstimator(gazeEstimator.estimateGaze(prevPoints,predictionsMap),predictionsMap);
            /*Log.d(TAG,"Frame Num"+frameCount+ "   is at direction "+direction + " GazeStatus"+ currentGazeStatus.toString() );*/
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


    void updateEyeDetectionFlag(List<Rect> eyes,Rect face){
        for(Rect eye:eyes){
            /*Center of the eye compared to the one half of the face*/
            if(eye.x+(eye.width/2)<=face.x+(face.width/2)){
                isRightEyeDetected = true;
                /*Log.d(TAG,"Right eye is detected");*/
            }
            else {
                isLeftEyeDetected = true;
               /* Log.d(TAG,"Left eye is detected");*/
            }
        }
    }


    void clearDetectionFlags(boolean considerOnlyEyes) {

        if(considerOnlyEyes){
            isLeftEyeDetected=false;
            isRightEyeDetected = false;
        }
        else{ isFaceDetected = false;}
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
            clearDetectionFlags(true);
            return;
        }
        if (frameCount > FRAME_CALIBRATION_RATE) {
            if (calibrationIrisIdentified) {
                frameCount = 0;
                needCalibration = false;
            } else {
                clearDetectionFlags(true);
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


    @Override
    public Direction getDirection() {
        if (direction == null) {
            return UNKNOWN;
        }
        return direction;
    }


    @Override
    public void reset() {
        sparseOpticalFlowMediator.resetSparseOpticalFlow();
    }

    @Override
    public Approach getApproach() {
        return approach;
    }

    @Override
    public DetectionData updateDetector(DetectionData detectionData) {
        clearDetectionFlags(false);
        SparseFlowDetectionData sparseFlowDetectionData = (SparseFlowDetectionData) detectionData;
        Mat newFrame = detect(sparseFlowDetectionData.getFrame());
        sparseFlowDetectionData.updateFrame((newFrame));
       /* Log.d(TAG,"IsFacedDetected "+ isFaceDetected + " isLeftEyeDetected "+ isLeftEyeDetected + " isRightEyeDetected "+ isRightEyeDetected);*/
        sparseFlowDetectionData.updateDetectionFlags(isFaceDetected,isLeftEyeDetected,isRightEyeDetected);
        return detectionData;
    }

    @Override
    public void clear() {
        /*TODO fill accordingly*/

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
        if(currentGazeStatus!=GazeStatus.ON_THE_WAY_TO_NEUTRAL) {
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
                    if(!(currentGazeStatus==GazeStatus.LEFT||currentGazeStatus==GazeStatus.RIGHT)){
                        currentGazeStatus = GazeStatus.NEUTRAL;
                        estimatedDirection=NEUTRAL;
                    }
                    else{
                        estimatedDirection=prevDirection;
                    }
            }
        }
        else{
            isNeutralStack.add(gazeEstimator.isNeutral(currentPoints,true));
            if(isStableNeutral()){
                currentGazeStatus=GazeStatus.NEUTRAL;
                prevDirection=NEUTRAL;
                /* Log.d(TAG,"if- isStableNeutral returning NEUTRAL ");*/
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
        if(isNeutralStack.size()< STABLE_NEUTRAL_STACK_THRESHOLD){
            return false;
        }
        else{
            boolean isStableNeutral=true;
            for(int i = 0; i< STABLE_NEUTRAL_STACK_THRESHOLD; i++){
                Boolean isNeutralVal = isNeutralStack.pop();
                if (isNeutralVal != null) {
                    isStableNeutral = isStableNeutral && isNeutralVal;
                }
            }
            isNeutralStack.clear();
            return  isStableNeutral;
        }
    }
}
