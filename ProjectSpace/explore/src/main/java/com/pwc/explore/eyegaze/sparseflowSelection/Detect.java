
package com.pwc.explore.eyegaze.sparseflowSelection;


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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private DetectionSmoother faceDetectionSmoother;
    private Rect prevFace;
    private Direction prevDirection;
    private static final float FACE_MOVEMENT_THRESHOLD=0.1f;
    double cb_eyeX; // calibration eye x, y
    double cb_eyeY;
    int calibration_status = 0;
    double averageX;
    double averageY;
    double SCREEN_WIDTH;
    double SCREEN_HEIGHT;
    ItemAdapter itemAdapter;
    boolean calibrateStart =false;



    Detect() {
        direction = Direction.UNKNOWN;
        simpleBlobDetector = SimpleBlobDetector.create();
        /*By Default isFirstPairOfIrisFound & needCalibration is false*/
        sparseOpticalFlowDetector = new SparseOpticalFlowDetector(new Size(30, 30), 2);
        gazeEstimator = new GazeEstimator(1f);
        faceDetectionSmoother=new DetectionSmoother(0.2f);


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

        /*Using the First Detected Face*/
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            Rect face = listOfFaces.get(0);

            /*Detections made smoother*/
            face=faceDetectionSmoother.updateCoord(face);

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);

            List<Rect> eyeBoundary=new ArrayList<>();

            HashMap<Integer, Point[]> blob = new HashMap<>();
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
                    eyeBoundary.add(eye.clone());

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
                        cb_eyeX = blobCentre.x;
                        cb_eyeY = blobCentre.y;

                        if(calibration_status == 1){ // center has been calculated
                            Log.d(TAG, "Center of the eye is: x = "+ averageX + " y = " + averageY);
                            Point cb_centre = new Point(averageX,averageY);
                            calibration_status=0;
                        }

                       /* if(blobCentre.x < averageX){
                            Log.d(TAG, "The user is looking at the element two");
                        }else{
                            Log.d(TAG, "The user is looking at the element one");
                        }*/

                        float irisRadius = 2;//TODO(Need to find a value dependent on the size of the eye )
                        blob.put(i, getIrisSparsePoint(irisRadius, blobCentre));


                    }


                }
            }


            /*sparseOpticalFlow Initiator/Calibration Alternator*/
            if ((!isFirstPairOfIrisFound || needCalibration) && isUniqueIrisIdentified(blob)) {
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
                sparseOpticalFlowDetector.predictPoints(frameGray);
                HashMap<Integer, Point[]> predictionsMap = sparseOpticalFlowDetector.getROIPoints();
                /*Log.d(TAG,"Eye A Predicted Points: "+ Arrays.toString(predictionsMap.get(0))+"  Eye B Predicted Points: "+Arrays.toString(predictionsMap.get(1)));*/
               direction=directionEstimator(gazeEstimator.estimateGaze(prevPoints,predictionsMap),predictionsMap);
                /*Log.d(TAG,"Frame Num"+frameCount+ "   is at direction "+direction);*/
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
            Log.d(TAG,"Face Movement xDiff:"+xDiff+" yDiff"+yDiff+"Threshold value "+prevFace.x*FACE_MOVEMENT_THRESHOLD);
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
            return Direction.UNKNOWN;
        }
        return direction;
    }


    private boolean isUniqueIrisIdentified(HashMap<Integer, Point[]> blob) {
        if (blob.size() == 2) {
            if (blob.get(0) != null && blob.get(1) != null)
                /*First point wont be the same*/
                return blob.get(0)[0] != blob.get(1)[0];
        }
        return false;
    }


    public  HashMap<Integer, Point[]> copy(HashMap<Integer, Point[]> original)
    {
        HashMap<Integer, Point[]> copy = new HashMap<Integer, Point[]>();
        for (Map.Entry<Integer, Point[]> entry : original.entrySet())
        {
            Point[] newPoint= new Point[entry.getValue().length];
            Point oldPoint[]= entry.getValue();
            for(int i=0;i<oldPoint.length;i++){
                newPoint[i]=oldPoint[i];
            }
            copy.put(entry.getKey(),newPoint);
        }
        return copy;
    }


    private Direction directionEstimator(Direction currentDirection, HashMap<Integer, Point[]> currentPoints){
      if(prevDirection==null){
          prevDirection=currentDirection;
          return currentDirection;
      }
        if(gazeEstimator.isNeutral(currentPoints)){
            prevDirection=Direction.NEUTRAL;
            return Direction.NEUTRAL;
        }
      switch(prevDirection){
          case NEUTRAL:
              if(currentDirection==Direction.LEFT || currentDirection==Direction.RIGHT){
                  return  currentDirection;
              }
          case LEFT:
              if(currentDirection==Direction.NEUTRAL){
                  prevDirection=Direction.LEFT;
                  return Direction.LEFT;
              }else if(currentDirection==Direction.RIGHT){
                  prevDirection=Direction.RIGHT;
                  return Direction.NEUTRAL;
              }
          case RIGHT:

              if(currentDirection==Direction.NEUTRAL){
                  /*Log.d(TAG,"NUETRAL");*/
                  prevDirection=Direction.RIGHT;
                  return Direction.RIGHT;
              }else if(currentDirection==Direction.LEFT){
                  prevDirection=Direction.LEFT;
                  return Direction.NEUTRAL;
              }
          default:
              return currentDirection;

      }
    }

}