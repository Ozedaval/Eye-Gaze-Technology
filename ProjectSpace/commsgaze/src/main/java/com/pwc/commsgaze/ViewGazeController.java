package com.pwc.commsgaze;


import android.graphics.Color;
import android.util.Log;

import androidx.recyclerview.widget.GridLayoutManager;


import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ViewGazeController {

    private MainRecyclerViewAdapter mainRecyclerViewAdapter;
    private static final String TAG = "ViewGazeController";
    private int selectedViewHolderIndex = 0;
    private ArrayList<MainRecyclerViewAdapter.ViewHolder> viewHolders;
    private ArrayList<Direction> directionList= new ArrayList<>(20);
    private final int DIRECTION_THRESHOLD =50;



    void updateSelectedViewHolder(Direction direction){
       /* Log.d(TAG,"Direction "+ direction.toString());*/
        /*RGB - 195,246,247 defaultLightBlue */


        directionList.add(direction);

            if (directionList.size() == DIRECTION_THRESHOLD) {
                directionList.remove(0);
            }
            Direction suitableDirection = getSuitableDirection(directionList);
            Log.d(TAG, "Suitable Direction is " + suitableDirection);
            viewHolders.get(selectedViewHolderIndex).itemView.setBackgroundColor(Color.rgb(195, 246, 247));
            if (suitableDirection.equals(Direction.RIGHT)) {
                if (selectedViewHolderIndex < viewHolders.size() - 1) {
                    ++selectedViewHolderIndex;
                    viewHolders.get(selectedViewHolderIndex).itemView.setBackgroundColor(Color.LTGRAY);
                }


            } else if (suitableDirection.equals(Direction.LEFT)) {
                if (selectedViewHolderIndex >= 1) {
                    --selectedViewHolderIndex ;
                    viewHolders.get(selectedViewHolderIndex).itemView.setBackgroundColor(Color.LTGRAY);
                }
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

    void initialiseViewHolders(ArrayList<MainRecyclerViewAdapter.ViewHolder> viewHolders){
        Log.d(TAG,"Intialising ViewHolders "+ viewHolders.toString());
        this.viewHolders = viewHolders;

    }


    ViewGazeController(MainRecyclerViewAdapter mainRecyclerViewAdapter){
        this.mainRecyclerViewAdapter = mainRecyclerViewAdapter;
    }

}
