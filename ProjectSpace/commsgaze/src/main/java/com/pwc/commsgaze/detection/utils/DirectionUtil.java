package com.pwc.commsgaze.detection.utils;

import android.util.Log;

import com.pwc.commsgaze.Direction;

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

    public static Direction getOppositeDirection(Direction direction){

        return  null;

    }
}
