package com.pwc.explore.eyegaze.opencvsparseflowtest;

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

/*Estimates the gaze*/
public class GazeEstimator {

    private List<Rect> eyes;
    private Float threshold;
    private static final String TAG="GazeEstimator";
    /*The region within which if "Sparse" Points are expected to be if iris is in the "Neutral"*/
    private List<Rect> eyeNeutralBoundary;


    GazeEstimator(Float threshold){
        this.threshold=threshold;
    }


    /**
     * Calculates the "Neutral" Boundary  and updates eyeNeutralBoundary
     * @param eyes: The List of eye Boundaries */
    void updateEyesBoundary(List<Rect> eyes) {
        this.eyes = eyes;
        eyeNeutralBoundary = new ArrayList<>();
        for (Rect eye : eyes) {
            Rect eyeClone= eye.clone();
            /*Calculating new top left so to align properly*/
            int newTopLeftX = (int)(eyeClone.x + eyeClone.width * threshold);
            Rect newEye= eye.clone();
            newEye.x=newTopLeftX;
            newEye.width=(int)(newEye.width*threshold);
            eyeNeutralBoundary.add(newEye);
        }
    }


    /**
     * Checks if "Sparse" points are within the eyeNeutralBoundary
     * @param currentPoint : Map of current "Sparse" points
     * @param toIncludeAll : Denotes whether to considerall "Sparse" points need to be within the eyeNeutralBoundary
     * @return When toIncludeAll is false, then True if majority of "Sparse" points within the eyeNeurtal Boundary  */
    boolean isNeutral(HashMap<Integer,Point[]> currentPoint,boolean toIncludeAll) {
        if (toIncludeAll) {
            if (eyeNeutralBoundary != null) {
                HashMap<Boolean, Integer> neutralHashMap = new HashMap<>();
                for (Map.Entry<Integer, Point[]> entry : currentPoint.entrySet()) {
                    Point[] points = entry.getValue();
                    for (Point point : points) {
                        Boolean isInNeutralBoundary = eyeNeutralBoundary.get(entry.getKey()).contains(point);
                        if (!neutralHashMap.containsKey(isInNeutralBoundary)) {
                            neutralHashMap.put(isInNeutralBoundary, 1);
                        } else {
                            neutralHashMap.put(isInNeutralBoundary, neutralHashMap.get(isInNeutralBoundary) + 1);
                        }

                    }
                }
                int maxValue = Collections.max(neutralHashMap.values());
                for (Map.Entry<Boolean, Integer> neutralStatusEntry : neutralHashMap.entrySet()) {
                    if (neutralStatusEntry.getValue() == maxValue) {
                        Log.d(TAG, neutralStatusEntry.getKey().toString() + " has the max at " + neutralStatusEntry.getValue() + "");
                        return neutralStatusEntry.getKey();
                    }
                }
            }
        } else {
            boolean isInNeutralBoundary = false;
            if (eyeNeutralBoundary != null) {
                for (Map.Entry<Integer, Point[]> entry : currentPoint.entrySet()) {
                    Point[] points = entry.getValue();
                    Point centrePoint = points[0];
                    isInNeutralBoundary = isInNeutralBoundary || eyeNeutralBoundary.get(entry.getKey()).contains(centrePoint);
                }
            }
            return isInNeutralBoundary;
        }
        return false;
    }


    /**
     * Estimates the Eye Gaze based upon Previous "Sparse" points and Current "Sparse" points
     * @param currentPoints : Map consisting of current "Sparse" points
     * @param prevPoints : Map consisting of Previous "Sparse" points*/
    Direction estimateGaze(HashMap<Integer,Point[]> prevPoints, HashMap<Integer,Point[]> currentPoints){
        return estimateMovement(prevPoints,currentPoints);
    }


    /**
     * Estimates the Movement Direction based upon Previous "Sparse" points and Current "Sparse" points
     * The estimated movement direction is based upon the most common direction of all "Sparse" points
     * @param currentPoints : Map consisting of current "Sparse" points
     * @param prevPoints : Map consisting of Previous "Sparse" points*/
    private Direction estimateMovement(HashMap<Integer, Point[]> prevPoints,HashMap<Integer, Point[]> currentPoints){
        /*TODO Consider all directions if needed*/
        if(currentPoints.keySet().size()==prevPoints.keySet().size()){
            HashMap<Direction,Integer> directions= new HashMap<>();
            List<Point> currentPointsList=unpackHashMapPoints(currentPoints);
            List<Point> prevPointsList=unpackHashMapPoints(prevPoints);

            if(currentPointsList.size() == prevPointsList.size()){
                for (int i = 0; i < currentPointsList.size(); i++) {
                    Point currentPoint = currentPointsList.get(i);
                    Point prevPoint = prevPointsList.get(i);
                    /*Log.d(TAG,"Current Point is"+currentPoint+" prevPoint is "+prevPoint);*/
                    double xDiff=currentPoint.x-prevPoint.x;
                    if(xDiff<-threshold){
                        /*Log.d(TAG,"xDiff is" +xDiff);*/
                        insertInDirections(directions,Direction.RIGHT);
                    }
                    else if(xDiff>threshold){
                        /*Log.d(TAG,"xDiff is" +xDiff);*/
                        insertInDirections(directions,Direction.LEFT);
                    }
                    else{
                        /*Log.d(TAG,"xDiff is" +xDiff);*/
                        insertInDirections(directions,Direction.NEUTRAL);
                    }
                }
            }
            int maxValue = Collections.max(directions.values());
            Direction maxDirection=Direction.UNKNOWN;
            /* int ctr=0;*/
            for (Map.Entry<Direction, Integer> directionIntegerEntry:directions.entrySet()){
                /*Log.d(TAG,ctr+".  "+directionIntegerEntry.getKey().toString()+" has num of points "+ directionIntegerEntry.getValue()+"");*/
                if(directionIntegerEntry.getValue()==maxValue){
                    maxDirection= directionIntegerEntry.getKey();
                }
            }
            return maxDirection;
        }
        return Direction.UNKNOWN;
    }


    /**
     * A custom function to insert The Direction into the directionCount map
     * @param directions : Map which consist of the Direction alongside with the
     *                     number of "Sparse"points with that estimated Direction
     * @param direction : The Direction to be added to the map
     */
    private void insertInDirections(HashMap<Direction, Integer> directions, Direction direction){
        if(!directions.containsKey(direction)){
            directions.put(direction,1);
        }
        else {
            directions.put(direction,directions.get(direction)+1);
        }
    }


    /**
     * Custom Function to convert the map containing "Sparse" Points to a list
     * @param hashMap : Map which contains "Sparse" points
     * @return A list of "Sparse" points in a specific order*/
    private List<Point> unpackHashMapPoints(HashMap<Integer, Point[]> hashMap){
        List<Point> outputList=new ArrayList<>();
        for(Integer roiID:hashMap.keySet()){
            outputList.addAll(Arrays.asList(hashMap.get(roiID)));
        }
        return outputList;
    }
}