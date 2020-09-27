package com.pwc.commsgaze;


import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ViewGazeController  {

    private static final String TAG = "ViewGazeController";
    private int selectedViewHolderIndex = 0;
    private int fixedDimension;
    private int lastElementIndex;
    private int lastSeqFirstElementIndex;
    private int prevSelectedViewHolderIndex;
    private final int firstElementIndex = 0;
    private int firstSeqLastElementIndex;


    int getSelectedViewHolderIndex(){
        return selectedViewHolderIndex;
    }
    int getPrevSelectedViewHolderIndex(){return prevSelectedViewHolderIndex;}

    void updateSelectedViewHolder(Direction direction){
        Log.d(TAG,"Gaze Direction estimated "+ direction.toString());
        Log.d(TAG,"Current select ViewHolder Index "+ selectedViewHolderIndex);
        prevSelectedViewHolderIndex = selectedViewHolderIndex;
        if(hasNeighbourIn(direction,selectedViewHolderIndex)) {

            switch (direction) {
                case LEFT:
                    selectedViewHolderIndex--;
                    break;
                case RIGHT:
                    selectedViewHolderIndex++;
                    break;
                case TOP:
                    selectedViewHolderIndex-=fixedDimension;
                    break;
                case BOTTOM:
                    selectedViewHolderIndex+=fixedDimension;
                    break;
            }
        }
        else{
            if(direction.equals(Direction.TOP)){
                selectedViewHolderIndex = (selectedViewHolderIndex % fixedDimension) + lastSeqFirstElementIndex;
            }
            else if(direction.equals(Direction.BOTTOM)) {
                    selectedViewHolderIndex = (selectedViewHolderIndex % fixedDimension) +firstElementIndex;
            }
        }
        Log.d(TAG,"Updated ViewHolder Index "+ selectedViewHolderIndex);}


    boolean isOnFirstSeq(int selectedViewHolderIndex){
        return (selectedViewHolderIndex>= firstElementIndex && selectedViewHolderIndex <= firstSeqLastElementIndex);

    }
    boolean isOnLastSeq(int selectedViewHolderIndex){
        return (selectedViewHolderIndex>=lastSeqFirstElementIndex && selectedViewHolderIndex <= lastElementIndex);

    }

    boolean hasNeighbourIn(Direction direction,int selectedViewHolderIndex){
        int remainder = selectedViewHolderIndex % fixedDimension;
        if(direction.equals(Direction.RIGHT)) {
            return remainder + 1 <= fixedDimension - 1;
        }
        else if(direction.equals(Direction.LEFT)){
            return remainder - 1 >= 0;
        }
        else if (direction.equals(Direction.TOP)){
            return !isOnFirstSeq(selectedViewHolderIndex);
        }
        else  if(direction.equals(Direction.BOTTOM)){
            return  !isOnLastSeq(selectedViewHolderIndex);
        }
        return true;
    }


    /*Usable when one is saving and using the previous directions*/
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



    ViewGazeController(int fixedDimension,int numOfPositions){
        this.fixedDimension = fixedDimension;
        firstSeqLastElementIndex = firstElementIndex + fixedDimension-1;
        lastSeqFirstElementIndex = numOfPositions - fixedDimension;
        lastElementIndex = numOfPositions - 1;
    }

}
