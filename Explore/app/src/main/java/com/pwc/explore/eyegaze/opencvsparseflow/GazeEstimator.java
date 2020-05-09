package com.pwc.explore.eyegaze.opencvsparseflow;

import com.pwc.explore.Direction;
import org.opencv.core.Point;
import java.util.HashMap;
import java.util.Queue;

public class GazeEstimator {
    private Queue<Direction> prevStates;
    private HashMap<Integer, Point[]>prevPoints;
    private Integer threshold;

    GazeEstimator(Direction currentState,HashMap<Integer,Point[]> currentPoints,Integer threshold){
//        prevState=currentState;
        prevPoints=currentPoints;
        this.threshold=threshold;
    }

    Direction estimateGaze(HashMap<Integer,Point[]> currentPoints){
        Direction estimatedMovement=estimateMovement(currentPoints,prevPoints);


        return null;
    }

   private Direction estimateMovement(HashMap<Integer, Point[]> currentPoints, HashMap<Integer, Point[]> prevPoints){
        if(currentPoints.keySet().size()==prevPoints.keySet().size()){


        }
        return Direction.UNKNOWN;
   }


}
