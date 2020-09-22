package com.pwc.commsgaze;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ViewGazeController  {

    private static final String TAG = "ViewGazeController";
    private int selectedViewHolderIndex = 0;
    private int numOfViewHolders;
    private ArrayList<Direction> directionList = new ArrayList<>(20);
    private final int DIRECTION_THRESHOLD = 50;
    private int fixedDimension;


    int getSelectedViewHolderIndex(){
        return selectedViewHolderIndex;
    }


    void updateSelectedViewHolder(Direction direction){
       /* Log.d(TAG,"Direction "+ direction.toString());*/
        /*RGB - 195,246,247 defaultLightBlue */

        directionList.add(direction);

            if (directionList.size() == DIRECTION_THRESHOLD) {
                directionList.remove(0);
            }

            Direction suitableDirection = getSuitableDirection(directionList);
           /* Log.d(TAG, "Suitable Direction is " + suitableDirection);*/
            if (suitableDirection.equals(Direction.RIGHT)) {

            }
            else if (suitableDirection.equals(Direction.LEFT)) {

            }

    }

    Direction getSuitableDirection(ArrayList<Direction> directions){
        HashMap<Direction,Integer> directionFreq = new HashMap<>();

        for(Direction direction:Direction.values()){
            directionFreq.put(direction,Collections.frequency(directions,direction));
        }
        Direction suitableDirection = Direction.UNKNOWN;
        int maxFreq = 0 ;
        for(Map.Entry<Direction,Integer> freqEntry:directionFreq.entrySet()){
            if(freqEntry.getValue()>maxFreq){
                maxFreq = freqEntry.getValue();
                suitableDirection = freqEntry.getKey();
            }
        }
        return suitableDirection;
    }


    void initialiseViewHolders(int numOfViewHolders){
        Log.d(TAG,"Initialising  num of ViewHolders "+ numOfViewHolders);
        this.numOfViewHolders = numOfViewHolders;
    }


    ViewGazeController(int fixedDimension){
        this.fixedDimension = fixedDimension;
    }

}
