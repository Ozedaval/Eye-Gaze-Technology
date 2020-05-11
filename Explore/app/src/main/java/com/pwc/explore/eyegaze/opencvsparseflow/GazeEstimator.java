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



public class GazeEstimator {

    private List<Rect> eyes;
    private Float threshold;
    private List<Rect> eyeNeutralBoundary;
    private static final String TAG="GazeEstimator";


    GazeEstimator(Float threshold){
        this.threshold=threshold;
    }
    void updateEyesBoundary(List<Rect> eyes) {
        this.eyes = eyes;
        eyeNeutralBoundary = new ArrayList<>();
        for (Rect eye : eyes) {
            Rect eyeClone= eye.clone();
            int newTopLeftX = (int)(eyeClone.x + eyeClone.width * 0.33f);
            Rect newEye= eye.clone();
            newEye.x=newTopLeftX;
            newEye.width=(int)(newEye.width*0.33f);
            eyeNeutralBoundary.add(newEye);
        }
    }

    boolean isNeutral(HashMap<Integer,Point[]> currentPoint){
        if(eyeNeutralBoundary!=null){
            HashMap<Boolean,Integer> neutralHashMap = new HashMap<>();
            for(Map.Entry<Integer, Point[]> entry:currentPoint.entrySet()){
                Point[] points=entry.getValue();
                for(Point point:points){
                    Boolean isInNeutralBoundary=eyeNeutralBoundary.get(entry.getKey()).contains(point);
                    if(!neutralHashMap.containsKey(isInNeutralBoundary)){
                        neutralHashMap.put(isInNeutralBoundary,1);
                    }
                    else{
                        neutralHashMap.put(isInNeutralBoundary,neutralHashMap.get(isInNeutralBoundary)+1);
                    }

                }
            }
            int maxValue = Collections.max(neutralHashMap.values());
            for (Map.Entry<Boolean, Integer> neutralStatusEntry:neutralHashMap.entrySet()) {
                if (neutralStatusEntry.getValue() == maxValue) {
                  /*   Log.d(TAG,neutralStatusEntry.getKey().toString()+" has the max at "+ neutralStatusEntry.getValue()+"");*/
                    return neutralStatusEntry.getKey();
                }
            }

        }

    return false;}
    Direction estimateGaze(HashMap<Integer,Point[]> prevPoints,HashMap<Integer,Point[]> currentPoints){
        /*TODO Consider gaze*/

        return estimateMovement(prevPoints,currentPoints);
    }

  /*  private Direction estimateMovement(HashMap<Integer, Point[]> prevPoints,HashMap<Integer, Point[]> currentPoints){
        *//*TODO Consider all directions*//*
        if(currentPoints.keySet().size()==prevPoints.keySet().size()) {
            for(int i=0;i<prevPoints.size();i++){
                if(prevPoints.get(i)!=null&&currentPoints.get(i)!=null&&prevPoints.get(i).length==currentPoints.get(i).length){


                }

            }
        }
        return Direction.UNKNOWN;
    }*/


    private Direction estimateMovement(HashMap<Integer, Point[]> prevPoints,HashMap<Integer, Point[]> currentPoints){
        /*TODO Consider all directions*/
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
                        directions=insertInDirections(directions,Direction.LEFT);
                    }
                    else if(xDiff>threshold){
                        /*Log.d(TAG,"xDiff is" +xDiff);*/
                        directions=insertInDirections(directions,Direction.RIGHT);
                    }
                    else{
                        /*Log.d(TAG,"xDiff is" +xDiff);*/
                        directions=insertInDirections(directions,Direction.NEUTRAL);
                    }
                }
            }
            int maxValue = Collections.max(directions.values());
            Direction maxDirection=Direction.UNKNOWN;
            int ctr=0;
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


    HashMap<Direction,Integer> insertInDirections(HashMap<Direction,Integer> directions,Direction direction){
        if(!directions.containsKey(direction)){
            directions.put(direction,1);
        }
        else {
            directions.put(direction,directions.get(direction)+1);
        }
        return  directions;
    }




    List<Point> unpackHashMapPoints(HashMap<Integer,Point[]> hashMap){
        List<Point> outputList=new ArrayList<>();
        for(Integer roiID:hashMap.keySet()){
            outputList.addAll(Arrays.asList(hashMap.get(roiID)));
        }
        return outputList;
    }



}