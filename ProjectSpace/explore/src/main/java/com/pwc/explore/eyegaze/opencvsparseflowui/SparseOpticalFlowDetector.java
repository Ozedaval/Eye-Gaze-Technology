package com.pwc.explore.eyegaze.opencvsparseflowui;

import android.util.Log;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.video.SparseOpticalFlow;
import org.opencv.video.SparsePyrLKOpticalFlow;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;


class SparseOpticalFlowDetector {
    private SparseOpticalFlow sparseOpticalFlow;
    private Mat prevFrame;
    private HashMap<Integer, Point[]> roiPoints;
    private Mat roiPointsMat;
    private static final String TAG="SparseOFDetector";

    SparseOpticalFlowDetector(Size winSize,Integer numROI){
        sparseOpticalFlow = SparsePyrLKOpticalFlow.create(winSize);
        roiPoints=new HashMap<>(numROI);

        /*Thread.dumpStack();*/
    }

    HashMap<Integer,Point[]> predictPoints(Mat currentFrame){

        if(prevFrame==null){
            /*Log.d(TAG,"predicting Points for the first time");*/
            fillUpMatPoints();
            prevFrame=currentFrame;
        }
        else{
            /*Log.d(TAG,"predicting Points");*/
            Mat status=new Mat();

            Mat nextPoints=new Mat(); // For ease of debugging
            /*Log.d(TAG,"Eye A: 1st Prev Point is"+ "["+Arrays.toString(roiPointsMat.get(0,0))+","+ Arrays.toString(roiPointsMat.get(0, 1))+"]");
            Log.d(TAG,"Eye B: 1st Prev Point is"+ "["+Arrays.toString(roiPointsMat.get(1, 0))+","+ Arrays.toString(roiPointsMat.get(1, 1))+"]");*/
            sparseOpticalFlow.calc(prevFrame,currentFrame, roiPointsMat,nextPoints,status);
          /*Log.d(TAG,"Eye A: 1st Next Point is"+ "["+Arrays.toString(nextPoints.get(0,0))+","+ Arrays.toString(nextPoints.get(0, 1))+"]");
            Log.d(TAG,"Eye B: 1st Next Point is"+ "["+Arrays.toString(nextPoints.get(1, 0))+","+ Arrays.toString(nextPoints.get(1, 1))+"]");*/
            prevFrame=currentFrame;
            roiPointsMat=nextPoints;
            unpackPrediction();
            fillUpMatPoints();
            status.release();//TODO check if it will cause problem
            nextPoints.release();
        }

        return roiPoints; }



    /*Will unpack the predictions in roiPointsMat to the roiPoint HashMap*/
    private void unpackPrediction(){
        if(roiPointsMat !=null){
            /*Log.d(TAG,"Unpacking Prediction Mat: roiPointMat has rows"+roiPointsMat.rows());*/
            Queue<Point> pointsQueue=new LinkedList<>();
            for(int m = 0; m< roiPointsMat.rows(); m++){
                double xPoint = roiPointsMat.get(m, 0)[0];
                double yPoint = roiPointsMat.get(m, 1)[0];
                Point addedPoint=new Point(xPoint,yPoint);
                pointsQueue.add(addedPoint);
                /*Log.d(TAG,"unpackPrediction"+": Adding "+addedPoint.toString());*/
            }
            for(int roiID:roiPoints.keySet()){
                Point[] points=new Point[roiPoints.get(roiID).length];
                for(int t=0;t<roiPoints.get(roiID).length;t++){
                    if(pointsQueue.peek()!=null) {
                        points[t] = pointsQueue.poll();
                    }
                }
                roiPoints.put(roiID,points);
            }
        }
    }


    /*Will use the roiPoint HashMap to fill up roiPointMat */
    private void fillUpMatPoints() {
        int totalSparsePoints = 0;

        for (int r = 0; r < roiPoints.size(); r++) {
            if (roiPoints.get(r) != null) {
                totalSparsePoints = totalSparsePoints + roiPoints.get(r).length;
                /*Log.d(TAG, r + "th Iris Point Size :" + roiPoints.get(r).length);*/
            }
        }

        /*Log.d(TAG,"Filling up Mat Points:  roiPoint - totalSparsePoints "+totalSparsePoints);*/

        int matCounter=0;
        roiPointsMat = new Mat(totalSparsePoints, 2, CvType.CV_32F);
        for (Integer roiID : roiPoints.keySet()) {
            if (roiPoints.get(roiID) != null) {
                for (int m = 0; m < roiPoints.get(roiID).length; m++) {
                    /*Log.d(TAG, m + "th Iris Point X :" + roiPoints.get(roiID)[m].x + "  "+ m + "th Iris Point Y :" + roiPoints.get(roiID)[m].y+" matCounter"+matCounter + " m is "+m);*/
                    roiPointsMat.put(matCounter, 0, roiPoints.get(roiID)[m].x);
                    roiPointsMat.put(matCounter, 1, roiPoints.get(roiID)[m].y);
                    matCounter++;
                }
            }

        }
    }

    void setROIPoints(int roiID, Point[] points) {
        roiPoints.put(roiID,points);
        /*Log.d(TAG,"Setting up ROIPoints.Minor Check - Point 1 X"+ roiPoints.get(roiID)[0].x);*/
    }

    HashMap<Integer, Point[]> getROIPoints() {
        return roiPoints;
    }


    void resetSparseOpticalFlow(){
        if(sparseOpticalFlow!=null ) {
            sparseOpticalFlow.clear();
        }
        prevFrame=null;
    }
}
