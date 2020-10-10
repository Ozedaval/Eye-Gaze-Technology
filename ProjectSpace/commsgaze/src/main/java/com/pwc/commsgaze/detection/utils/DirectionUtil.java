package com.pwc.commsgaze.detection.utils;

import android.util.Log;

import com.pwc.commsgaze.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DirectionUtil {

    private final static String TAG= "DirectionUtil";



    public static Direction directionParser(String directionName) {
        for(Direction direction:Direction.values()){
            if(direction.toString().equalsIgnoreCase(directionName)){
                return direction;
            }
        }
        Log.e(TAG,"Direction Parsed as Unknown");
        return Direction.UNKNOWN;
    }

    public static Direction getOppositeDirection(Direction direction) {
        if (direction.equals(Direction.LEFT))
            return Direction.RIGHT;
        if (direction.equals(Direction.RIGHT))
            return Direction.LEFT;
        return direction;
    }


    public static Direction getMaxDirection(ArrayList<Direction> directions){
        HashMap<Direction,Integer> directionFreq = new HashMap<>();

        for(Direction direction:Direction.values()){
            directionFreq.put(direction, Collections.frequency(directions,direction));
        }
        Direction suitableDirection = Direction.UNKNOWN;
        int maxFreq = 0 ;
        for(Map.Entry<Direction,Integer> freqEntry:directionFreq.entrySet()){
            if(freqEntry.getValue()>maxFreq){
                maxFreq = freqEntry.getValue();
                suitableDirection = freqEntry.getKey();
            }
        }

        return  suitableDirection;}



    public static boolean isStable(Direction direction,ArrayList<Direction> directions,int threshold){
        int directionListSize = directions.size();
        if(directions.size()<threshold){
            return false;
        }
        for(int i = directionListSize-1;i>directionListSize-threshold;i--){
            if(!directions.get(i).equals(direction))
            {
                return false;
            }
        }
        return true;
    }


}
