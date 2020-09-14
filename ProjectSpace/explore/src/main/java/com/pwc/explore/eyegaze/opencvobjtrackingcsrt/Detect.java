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
    private boolean eyedetect1;
    private boolean eyedetect2;
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
    private TrackerCSRT trackerCSRT;
    private TrackerCSRT trackerCSRT2;

    Detect(CascadeClassifier faceCascade,CascadeClassifier eyesCascade){
        direction = UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();
        /*By Default isFirstPairOfIrisFound,needCalibration & prevFrameHadFace is false*/
        gazeEstimator = new GazeEstimator(0.33f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);
        isNeutralQueue = new LinkedList<>();
        currentGazeStatus= GazeStatus.UNKNOWN;
        trackerCSRT = TrackerCSRT.create();
        trackerCSRT2= TrackerCSRT.create();
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
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        /*Thread.dumpStack();*/
        Log.d(TAG,"Detect method called");
        calculateNeedCalibration(false,false);
        Mat frameGray = new Mat();
        Mat frameThreeChannelTest = new Mat();
        /*Creating a Grayscale version of the Image*/
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(frameGray,frameThreeChannelTest,Imgproc.COLOR_GRAY2BGR);
        /*Increasing contrast & brightness of the image appropriately*/
        Imgproc.equalizeHist(frameGray, frameGray);
        /*Detecting faces*/
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);
        List<Rect> eyeBoundary= null;
        HashMap<Integer, Point[]> blob = new HashMap<>();
        Rect face = null;
        /*TODO: This is for testing the whether the extra module work or not*/
        Rect2d eye1Rect2d =null;
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
                KeyPoint[] blobsArray=null;
                KeyPoint[] blobsArray1=null;
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
                    if (i==0){blobsArray = blobs.toArray();}
                else {blobsArray1=blobs.toArray();}
                    if (blobsArray != null&&blobsArray1 !=null) {
                        double eyeX = 0;
                        double eyeY = 0;
                        double eyeX1 = 0;
                        double eyeY1 = 0;
                        for (int k = 0; k < blobsArray.length; k++) {
                            eyeX += blobsArray[k].pt.x;
                            eyeY += blobsArray[k].pt.y;
                        }
                        for (int k = 0; k < blobsArray1.length; k++) {
                            eyeX1 += blobsArray1[k].pt.x;
                            eyeY1 += blobsArray1[k].pt.y;
                        }
                        eyeX /= (blobsArray.length + 1);
                        eyeY /= (blobsArray.length + 1);
                        eyeX1 /= (blobsArray1.length + 1);
                        eyeY1 /= (blobsArray1.length + 1);
                        Point blobCentre = new Point(eyeX, eyeY);
                        Point blobCentre1 = new Point(eyeX1, eyeY1);
                        blobCentre.x = blobCentre.x + eye.x + 10;
                        blobCentre.y = blobCentre.y + eye.y + 8;
                        blobCentre1.x = blobCentre1.x + eye.x + 10;
                        blobCentre1.y = blobCentre1.y + eye.y + 8;
                        /*Imgproc.circle(frame, blobCentre, 4, new Scalar(255, 0, 0), 4);
                        Log.d(TAG,"Height "+eye.height+"Width "+eye.width);
                        Log.d(TAG,"Iris Centre X"+blobs.empty()+"Iris Centre Y"+blobCentre.y);*/
                        /*float irisRadius = 2;//TODO(Need to find a value dependent on the size of the eye )
                        blob.put(i, getIrisSparsePoint(irisRadius, blobCentre));*/
                            Log.d(TAG,"Number of frame channels " + frame.channels()+"");
                            Log.d(TAG,"Number of frameGray channels " + frameGray.channels()+"");
                            Log.d(TAG,"Number of frameThreeChannelTest" + frameThreeChannelTest.channels() + "");
                            Log.d(TAG,"Init going to be  called");
                            Log.d(TAG, "detect: bC"+blobCentre.x+",y "+blobCentre.y);
                            Log.d(TAG, "detect: 1bC"+blobCentre1.x+",y "+blobCentre1.y);
                            if(blobCentre1.x>blobCentre.x){
                                Log.d(TAG, "detect: Larger detected"+Double.toString(blobCentre1.x-blobCentre.x));
                            }else {Log.d(TAG, "detect: smaller detected"+Double.toString(blobCentre1.x-blobCentre.x));}
                            /*if(blobCentre.x<=blobCentre1.x){*/Rect2d boundary1=new Rect2d(blobCentre.x,blobCentre.y,2,2);
                           /* Imgproc.circle(frame, new Point(boundary1.x,boundary1.y), 2, new Scalar(255, 255, 0), 4);*/
                            trackerCSRT.init(frameThreeChannelTest,boundary1);
                            Log.d(TAG,"Init is called");
                            eyedetect1=true;
                            trackerCSRT2.init(frameThreeChannelTest,new Rect2d(blobCentre1.x,blobCentre1.y,2,2));
                            eyedetect2=true;
                        Log.d(TAG,"eyedetect1"+eyedetect1);
                        Log.d(TAG,"eyedetect2"+eyedetect2);/*}*/
                            /*else{
                                Rect2d boundary1=new Rect2d(blobCentre1.x,blobCentre1.y,2,2);
                                    Imgproc.circle(frame, new Point(boundary1.x,boundary1.y), 2, new Scalar(255, 255, 0), 2);
                                    trackerCSRT.init(frameThreeChannelTest,boundary1);
                                    Log.d(TAG,"Init is called");
                                    eyedetect1=true;
                                    trackerCSRT2.init(frameThreeChannelTest,new Rect2d(blobCentre.x,blobCentre.y,2,2));
                                    eyedetect2=true;
                                    Log.d(TAG,"eyedetect1"+eyedetect1);
                                    Log.d(TAG,"eyedetect2"+eyedetect2);
                            }*/
                    }
                }
            }
        }
        else {
            direction= UNKNOWN;
        }
        /*sparseOpticalFlow Initiator/Calibration Alternator*/
        if (face!=null&&(!isFirstPairOfIrisFound || needCalibration) && isUniqueIrisIdentified(blob)&&eyeBoundary.size()==2) {
            /*TODO : Insert object Tracking functionality here based on blob - iris co -ordinates (Cause we wanna do object tracking on the iris )*/
            calculateNeedCalibration(true, hasFaceMoved(face));
            isFirstPairOfIrisFound = true;
        }

        if (isFirstPairOfIrisFound&&eyeBoundary!=null) {
            if (eyedetect1&&eyedetect2&&eyeBoundary.size()>0){
                eye1Rect2d= changeRectType(eyeBoundary.get(0));
               Rect2d eye1Rect2d1=changeRectType(eyeBoundary.get(1));
                Boolean eyeTracked1 = trackerCSRT.update(frameThreeChannelTest, eye1Rect2d);
                if (!eyeTracked1)
                    eyedetect1 = false;
                Boolean eyeTracked2 = trackerCSRT2.update(frameThreeChannelTest, eye1Rect2d1);
                if (!eyeTracked2)
                    eyedetect2 = false;
                Imgproc.circle(frame,new Point(eye1Rect2d.x,eye1Rect2d.y),2,new Scalar(0, 0, 0),4);
                Imgproc.circle(frame,new Point(eye1Rect2d1.x,eye1Rect2d1.y),2,new Scalar(0, 0, 0),4);
                Log.d("Eyetrack",""+eyeTracked1);
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
            return (T) new Rect((int)rect2d.x,(int)rect2d.y,(int)rect2d.width,(int)rect2d.height);
        }
        if (inputRect instanceof  Rect){
            Rect rect = (Rect) inputRect;
            return (T) new Rect2d(rect.x,rect.y,rect.width,rect.height);
        }
        System.out.println("NULL ");
        return null;
    }
}