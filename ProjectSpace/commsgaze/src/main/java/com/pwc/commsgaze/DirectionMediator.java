package com.pwc.commsgaze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.pwc.commsgaze.detection.utils.DirectionUtil.getMaxDirection;

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
        return getMaxDirection(directions);
    }

}
