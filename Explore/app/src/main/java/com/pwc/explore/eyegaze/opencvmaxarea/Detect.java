package com.pwc.explore.eyegaze.opencvmaxarea;


import android.util.Log;
import com.pwc.explore.DetectionListener;
import com.pwc.explore.Direction;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import static com.pwc.explore.Direction.BOTTOM;
import static com.pwc.explore.Direction.BOTTOM_LEFT;
import static com.pwc.explore.Direction.BOTTOM_RIGHT;
import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.NEUTRAL;
import static com.pwc.explore.Direction.RIGHT;
import static com.pwc.explore.Direction.TOP;
import static com.pwc.explore.Direction.TOP_LEFT;
import static com.pwc.explore.Direction.TOP_RIGHT;

public class Detect {

    enum DirectionEstimationMethod {METHOD_1,METHOD_2,METHOD_3,METHOD_4};
    private DetectionListener dl;
    private final Float THRESHOLD_METHOD3=0.1f;//ANY value between 0-1
    private static Point[] prevIrisPoints;
    private static final String TAG="Detect";


    Detect(DetectionListener dl){
        this.dl=dl;

    }

    private void sendDetection(Direction direction){
        dl.move(direction);
    }


    private HashMap<Direction,double[]> getBoundaryCoord(Rect boundary){
        double height=boundary.height;
        double width=boundary.width;
        double halfHeight=height/2;
        double halfWidth=width/2;
        double xTL=boundary.x; //Top left x co-ord
        double yTL=boundary.y;//Top left y co-ord
        HashMap<Direction,double[]> map=new HashMap<>(9);
        map.put(TOP_LEFT,new double[]{xTL,yTL});
        map.put(TOP,new double[]{xTL-halfWidth,yTL});
        map.put(TOP_RIGHT,new double[]{xTL-width,yTL});
        map.put(LEFT,new double[]{xTL,yTL-halfHeight});
        map.put(NEUTRAL,new double[]{xTL-halfWidth,yTL-halfHeight});
        map.put(RIGHT,new double[]{xTL-width,yTL-halfHeight});
        map.put(BOTTOM_LEFT,new double[]{xTL,yTL-height});
        map.put(BOTTOM,new double[]{xTL-halfWidth,yTL-height});
        map.put(BOTTOM_RIGHT,new double[]{xTL-width,yTL-height});
        /*Log.d(TAG,"height : "+height+ "width :"+width);*/
        return map;
    }


    private Direction findDirection(Rect eye, Point centroid, DirectionEstimationMethod method){
        /*TODO Need to fix METHOD_1 & METHOD_2*/
        if(method== DirectionEstimationMethod.METHOD_1){
            /*When one  find the nearest boundary edge/side from the center of the iris*/
            HashMap<Direction,double[]> boundaryCoordMap=getBoundaryCoord(eye);
            return findClosestBoundary(boundaryCoordMap,centroid);
        }

        else if(method== DirectionEstimationMethod.METHOD_2){
            /*When one uses the center of the rectangular boundary around the eye & the center of the iris*/
            Point centerBoundary=new Point((eye.x-(eye.width/2f)),(eye.y-(eye.height/2f)));
            double xDiff=centerBoundary.x-centroid.x;
            double yDiff=centerBoundary.y-centroid.y;
            Log.d(TAG,"Eye width "+eye.width+"Eye Height "+ eye.height);
            Log.d(TAG,"X coordinates of eye "+ eye.x+ " Y coordinates of eye"+eye.y);
            Log.d(TAG,"xDiff is"+ xDiff + "yDiff is "+yDiff + " centerboundary x & y "+ centerBoundary.x + " & "+ centerBoundary.y);
            Log.d(TAG,"CEntroid x is "+ centroid.x+"Centroid y is"+centroid.y);

            if(xDiff<0){
                return  RIGHT;
            }
            else if (xDiff>0){
                return LEFT;
            }
            else if(yDiff<0){
                return  TOP;
            }
            else  return BOTTOM;
        }
        else if(method==DirectionEstimationMethod.METHOD_3) {
            /*When one uses the actual dimension of the rectangular boundary of the eye*/
            float thresholdWidth=THRESHOLD_METHOD3*eye.width;
            float thresholdHeight=THRESHOLD_METHOD3*eye.height;
            double xDiff=(eye.width/2f)-centroid.x;
            double yDiff=(eye.height/2f)-centroid.y;
            double xAbsDiff=Math.abs(xDiff);
            double yAbsDiff= Math.abs(yDiff);

            if(xAbsDiff<thresholdWidth&&yAbsDiff<thresholdHeight){
                return NEUTRAL;
            }
            else if (xDiff<0&&xAbsDiff>thresholdWidth)
                return  RIGHT;
            else if (xDiff>0&&xAbsDiff>thresholdWidth)
                return LEFT;
            else if(yDiff<0&&yAbsDiff>thresholdHeight)
                return BOTTOM;
            else
                return TOP;}
        else {
            //METHOD_4

            return null;
        }


    }


    private Direction findClosestBoundary(HashMap<Direction, double[]> boundaryCoord, Point centroid){
        double xC=centroid.x;
        double yC=centroid.y;
        Direction nearestDirection=TOP_LEFT;
        double minDistance=Integer.MAX_VALUE;
        double tDiff=0;
        for (Direction direction :boundaryCoord.keySet()){
            double[] coord=boundaryCoord.get(direction);
            double xDiff=Math.abs((coord != null ? coord[0] : 0) -xC);
            double yDiff=Math.abs((coord != null ? coord[1] : 0) -yC);
            tDiff=xDiff+yDiff;
            if(tDiff<minDistance){
                minDistance=tDiff;
                nearestDirection=direction;
            }
            Log.d(TAG,direction.name()+tDiff+(coord==null));
        }
        return nearestDirection;

    }


    /*Iris Detection
    Ideology of finding max area used by contour:
    https://stackoverflow.com/questions/31504366/opencv-for-java-houghcircles-finding-all-the-wrong-circles*/
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {

        Mat frameGray = new Mat();

        /*Creating a Grayscale version of the Image*/
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        /*Increasing contrast & brightness of the image appropriately*/
        Imgproc.equalizeHist(frameGray, frameGray);

        /*Detecting  faces*/
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);

        /*Using the First Detected Face*/
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            Rect face = listOfFaces.get(0);

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
            Log.d(TAG,"face.x= "+face.x+"face.y = "+face.y);
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


                    /*Finding the contour area which has the largest area - Usually the Iris*/
                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat hierarchy = new Mat();
                    Mat cannyOutput = new Mat();
                    Imgproc.Canny(eyesROI[i], cannyOutput, 100, 100 * 2);
                    Imgproc.findContours(cannyOutput,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);

                    double maxArea = 0;
                    int contourNum = 0;
                    double contourArea = 0;
                    for (int c = 0; c < contours.size(); c++)
                    {
                        contourArea =Imgproc.contourArea(contours.get(c));
                        if (maxArea < contourArea)
                        {
                            maxArea = contourArea;
                            contourNum = c;
                        }
                    }
                    Moments momentsContour=Imgproc.moments(contours.get(contourNum));

                    /*
                    With respect to the location contour
                    Point centerIris= new Point((momentsContour.m10/momentsContour.m00),(momentsContour.m01/momentsContour.m00));*/
                    Point irisCenter= new Point(eye.x+(momentsContour.m10/momentsContour.m00),eye.y+(momentsContour.m01/momentsContour.m00));
                    Imgproc.circle(frame,irisCenter,2,new Scalar(255,0,0),4);

                    /*Log.d(TAG+"Iris Center is ","X: "+centerIris.x + "  Y: "+ centerIris.y);
                    Log.d(TAG+" direction is",findDirection(eye,centerIris)+"");

                    */

                    irisCenters[i]=irisCenter;
                }

                /*Temporarily just using one eye*/
                /*sendDetection(findDirection(eyesBoundary[0],irisCenters[0], DirectionEstimationMethod.METHOD_1));*/
            } catch (Exception e) {

                Log.e(TAG,"Error "+e.getMessage());
            }
        }
        return frame;
    }

}