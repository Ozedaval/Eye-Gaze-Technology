package com.pwc.commsgaze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DirectionMediator {
    private int frameThreshold;
    ArrayList<Direction> directions;
    private Direction gaugedCurrentDirection;
    private int frameCounter;
    private boolean needUpdate;


    DirectionMediator(int frameThreshold){
        directions = new ArrayList<Direction>(frameThreshold);
        gaugedCurrentDirection = Direction.UNKNOWN;
        frameCounter = 0;
        this.frameThreshold = frameThreshold;
    }

    void update(Direction direction) {
        needUpdate = false;
        if (directions.size()==frameThreshold){
            directions.remove(0);
        }
        directions.add(direction);
        frameCounter++;

       if(frameCounter == frameThreshold){
           gaugedCurrentDirection = getSuitableDirection();
           frameCounter = 0;
           needUpdate = true;
       }

    }

  boolean getNeedUpdate(){
        return needUpdate;
  }

    public Direction getGaugedCurrentDirection() {
        return gaugedCurrentDirection;
    }

    /*Usable when one is saving and using the previous directions*/
   private Direction getSuitableDirection(){
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
        return suitableDirection;
    }

}
