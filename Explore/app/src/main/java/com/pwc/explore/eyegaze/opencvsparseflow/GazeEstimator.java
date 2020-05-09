package com.pwc.explore.eyegaze.opencvsparseflow;

import android.util.Log;
import android.webkit.HttpAuthHandler;

import com.pwc.explore.Direction;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class GazeEstimator {
    private Direction prevMovingState;

    private Integer threshold;
    private static final String TAG="GazeEstimator";

    GazeEstimator(Direction currentState,Integer threshold){
        this.threshold=threshold;
    }


    Direction estimateGaze(HashMap<Integer,Point[]> prevPoints,HashMap<Integer,Point[]> currentPoints){
        /*TODO Consider gaze*/
        return estimateMovement(prevPoints,currentPoints);
    }

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
            for (Map.Entry<Direction, Integer> directionIntegerEntry:directions.entrySet()){
                if(directionIntegerEntry.getValue()==maxValue){
                   /* Log.d(TAG,directionIntegerEntry.getKey().toString()+" has the max at "+ directionIntegerEntry.getValue()+"");*/
                    return directionIntegerEntry.getKey();
                }
            }
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
