/*
package com.pwc.explore.eyegaze.opencvsparseflow;

import com.pwc.explore.Direction;

import java.util.Queue;

import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.NEUTRAL;
import static com.pwc.explore.Direction.RIGHT;

public class DirectionEstimator extends Thread {

    private Queue<Direction> neutralQueue;
    DirectionEstimator(){


    }    @Override
    public void run() {

    }

    private Direction directionEstimator(Direction currentDirection){
        if(prevDirection==null){
            prevDirection=currentDirection;
            return currentDirection;
        }

        if(currentGazeStatus!=GazeStatus.ON_THE_WAY_TO_NEUTRAL) {
            switch (currentDirection) {
                case LEFT:
                    if (prevDirection == NEUTRAL || prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.LEFT;

                    } else if (prevDirection == RIGHT) {
                        currentGazeStatus = GazeStatus.ON_THE_WAY_TO_NEUTRAL;
                    }
                    break;
                case RIGHT:
                    if (prevDirection == NEUTRAL || prevDirection == RIGHT) {
                        currentGazeStatus = GazeStatus.RIGHT;

                    } else if (prevDirection == LEFT) {
                        currentGazeStatus = GazeStatus.ON_THE_WAY_TO_NEUTRAL;
                    }
                    break;
                case NEUTRAL:
                    currentGazeStatus = GazeStatus.NEUTRAL;
                    break;
            }
        }
        else{
            neutralQueue.add(currentDirection);
            if(isStableNeutral()){
                currentGazeStatus=GazeStatus.NEUTRAL;
                prevDirection=NEUTRAL;
                return NEUTRAL;
            }

        }
        if(currentGazeStatus==GazeStatus.ON_THE_WAY_TO_NEUTRAL){
            return NEUTRAL;
        }
        prevDirection=currentDirection;
        return currentDirection;    }
}
*/
