package com.pwc.commsgaze;

import java.util.ArrayList;

import static com.pwc.commsgaze.detection.utils.DirectionUtil.getMaxDirection;
import static com.pwc.commsgaze.detection.utils.DirectionUtil.isStable;

import static com.pwc.commsgaze.detection.utils.DirectionUtil.getMaxDirection;

public class DirectionMediator {
    private int selectionThreshold;
    ArrayList<Direction> directions;
    private Direction gaugedCurrentDirection;
    private int frameCounter;
    private boolean needUpdate;
    private boolean isStableNeutral;
    private int clickInitThreshold;


    DirectionMediator(int selectionThreshold, int clickInitThreshold){
        directions = new ArrayList<Direction>(selectionThreshold);
        gaugedCurrentDirection = Direction.UNKNOWN;
        frameCounter = 0;
        this.selectionThreshold = selectionThreshold;
        this.clickInitThreshold = clickInitThreshold;
    }


    void update(Direction direction) {
        needUpdate = false;
        isStableNeutral = isStable(Direction.NEUTRAL,directions,clickInitThreshold);
        if (directions.size()== selectionThreshold){
            directions.remove(0);
        }
        directions.add(direction);
        frameCounter++;

        if(frameCounter == selectionThreshold){
            gaugedCurrentDirection = getSuitableDirection();
            frameCounter = 0;
            needUpdate = true;
        }
    }

    boolean getIsStableNeutral(){return  isStableNeutral;}

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
