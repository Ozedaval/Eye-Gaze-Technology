package com.pwc.explore.eyegaze.opencvsparseflow;

import android.util.Log;

import com.pwc.explore.Direction;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.RIGHT;
import static java.lang.Math.abs;


public class GazeEstimator {

    private List<Rect> eyes;
    private Float threshold;
    private List<Rect> eyeNeutralBoundary;
    private static final String TAG="GazeEstimator";

    GazeEstimator(Float threshold){
        this.threshold=threshold;
    }

    Direction estimateGaze(HashMap<Integer,Point[]> currentPoints){
        /*TODO Consider gaze*/
        if(eyes==null||eyes.size()!=2){
            return Direction.UNKNOWN;
        }
        return estimateMovement(currentPoints);
    }

    void updateEyesBoundary(List<Rect> eyes) {
        this.eyes = eyes;
//        eyeNeutralBoundary = new ArrayList<>();
//        for (Rect eye : eyes) {
//            int newTopLeftX = (int)(eye.x + eye.width * 0.33f);
//            Rect newEye= eye.clone();
//            newEye.x=newTopLeftX;
//            newEye.width=(int)(newEye.width*0.33f);
//            eyeNeutralBoundary.add(newEye);
//        }
    }

    private Direction estimateMovement(HashMap<Integer, Point[]> currentPoints){
        /*TODO Consider all directions*/
        HashMap<Direction,Integer> directionCount=new HashMap<>();
        for(int i=0;i<eyes.size();i++){
            Log.d(TAG,eyes.size()+"- number of eyes detected");
            HashMap<Direction,double[]> boundaryMap=getBoundaryCoord(eyes.get(i));
            Point[] points = currentPoints.get(i);
            for(Point point:points){
                for(Map.Entry<Direction, double[]> entry:boundaryMap.entrySet()){
                    Direction closestDirection=findClosestBoundary(point,boundaryMap);
                    if(!directionCount.containsKey(closestDirection)){
                        directionCount.put(closestDirection,0);
                    }
                    else{
                        directionCount.put(closestDirection,directionCount.get(closestDirection)+1);
                    }
                }
            }
        }

        Log.d(TAG, "points near to LEFT "+directionCount.get(LEFT)+"Points near to Right "+directionCount.get(RIGHT));
        int maxDirectionCount=Collections.max(directionCount.values());
        for(Direction direction:directionCount.keySet()){
            if(directionCount.get(direction)==maxDirectionCount){
                return direction;

            }        }
        return Direction.UNKNOWN;
    }

    Direction findClosestBoundary(Point point,HashMap<Direction,double[]> boundaryMap){
        double netMinimalDifference=Integer.MAX_VALUE;
        Direction closestDirection=Direction.UNKNOWN;
        Log.d(TAG,"Boundary edges LEFT X"+boundaryMap.get(LEFT)[0]+ " LEFT Y"+boundaryMap.get(LEFT)[1]+"Boundary edges RIGHT X"+boundaryMap.get(RIGHT)[0]+ " RIGHT Y"+boundaryMap.get(RIGHT)[1]);
        for(Map.Entry<Direction, double[]> entry: boundaryMap.entrySet()){
            double tempDiff;
            double pointX=point.clone().x;
            double pointY=point.clone().y;
            Log.d(TAG,pointX+" X & y is "+pointY);
            tempDiff=Math.abs(pointX-entry.getValue()[0]);
            tempDiff+=Math.abs(pointY-entry.getValue()[1]);
            Log.d(TAG,"TempDiff"+tempDiff);
            if(netMinimalDifference>tempDiff){
                closestDirection=entry.getKey();
                netMinimalDifference=tempDiff;
            }

        }
        Log.d(TAG,"Closest Direction is"+closestDirection);
        return closestDirection;
    }

    private HashMap<Direction,double[]> getBoundaryCoord(Rect boundary){
        double height=boundary.height;
        double width=boundary.width;
        double halfHeight=height/2;
        double halfWidth=width/2;
        double xTL=boundary.x; //Top left x co-ord
        double yTL=boundary.y;//Top left y co-ord
        HashMap<Direction,double[]> map=new HashMap<>(2);
        map.put(LEFT,new double[]{xTL,yTL-halfHeight});
        map.put(RIGHT,new double[]{xTL-width,yTL-halfHeight});
        return map;
    }


    List<Point> unpackHashMapPoints(HashMap<Integer,Point[]> hashMap){
        List<Point> outputList=new ArrayList<>();
        for(Integer roiID:hashMap.keySet()){
            outputList.addAll(Arrays.asList(hashMap.get(roiID)));
        }
        return outputList;
    }



}
