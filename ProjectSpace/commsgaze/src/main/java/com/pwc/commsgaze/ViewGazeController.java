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
    private final int DIRECTION_THRESHOLD = 50;
    private int fixedDimension;
    private int lastElementIndex;
    private int lastSeqFirstElementIndex;
    private final int firstElementIndex = 0;
    private int firstSeqLastElementIndex;



    int getSelectedViewHolderIndex(){
        return selectedViewHolderIndex;
    }


    void updateSelectedViewHolder(Direction direction){
        Log.d(TAG,"Gaze Direction estimated "+ direction.toString());
        Log.d(TAG,"Current select ViewHolder Index "+ selectedViewHolderIndex);
        /*RGB - 195,246,247 defaultLightBlue */
        if(hasNeighbourIn(direction)) {
            switch (direction) {
                case LEFT:
                    selectedViewHolderIndex--;
                    break;
                case RIGHT:
                    selectedViewHolderIndex++;
                    break;
                case TOP:
                    if(isOnFirstSeq(selectedViewHolderIndex)){
                        selectedViewHolderIndex = selectedViewHolderIndex + lastSeqFirstElementIndex;
                    }
                    else{
                        selectedViewHolderIndex-=fixedDimension;
                    }
                    break;
                case BOTTOM:
                    if(isOnLastSeq(selectedViewHolderIndex)){
                        selectedViewHolderIndex = (selectedViewHolderIndex- lastSeqFirstElementIndex) + firstElementIndex;
                    }
                    else{
                        selectedViewHolderIndex+=fixedDimension;
                    }
                    break;
            }

            Log.d(TAG,"Updated ViewHolder Index "+ selectedViewHolderIndex);
        }
    }

    boolean isOnFirstSeq(int selectedViewHolderIndex){
        return (selectedViewHolderIndex>= firstElementIndex && selectedViewHolderIndex <= firstSeqLastElementIndex);

    }
    boolean isOnLastSeq(int selectedViewHolderIndex){
        return (selectedViewHolderIndex>=lastSeqFirstElementIndex && selectedViewHolderIndex <= lastElementIndex);

    }


    boolean hasNeighbourIn(Direction direction){
        /*TODO*/
        return true;
    }

    /*Usable when one is saving  and using the previous directions*/
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
        lastElementIndex = numOfViewHolders - 1;
        lastSeqFirstElementIndex = numOfViewHolders - fixedDimension;
        firstSeqLastElementIndex = fixedDimension - 1;
    }


    ViewGazeController(int fixedDimension){
        this.fixedDimension = fixedDimension;
    }

}
