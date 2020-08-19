package com.pwc.explore.eyegaze.opencvsparseflow;

import android.util.Log;
import org.opencv.core.Rect;

/*Removes Jittery effect caused by Object Detection by using previous Rect of the
* detected object;Uses Area or Co-ordinates based constraints to decide on
* whether to readjust the Rect or not */
class DetectionSmoother {

    private Rect prevRect =null;
    private float THRESHOLD;
    private static final String TAG="DetectionSmoother";

    DetectionSmoother(final float THRESHOLD){
        this.THRESHOLD=THRESHOLD;
    }

    /**
     * Updates the Rect if the object detected has moved away based on the  Co-ordinates and THRESHOLD value
     * @param currentRect: The current Rect(section of the Frame/ Boundary of the object detected)  of the Object detected
     * @return The Updated Rect*/
    Rect updateCoord(Rect currentRect){
        if(prevRect == null){
            prevRect = currentRect;
            return currentRect;
        }
        boolean isXinRanges=currentRect.x<prevRect.x+(prevRect.x*THRESHOLD)&& currentRect.x>prevRect.x-(prevRect.x*THRESHOLD);
        boolean isYinRanges=currentRect.y<prevRect.y+(prevRect.y*THRESHOLD)&& currentRect.y>prevRect.y-(prevRect.y*THRESHOLD);
        if(isXinRanges&& isYinRanges){
            return prevRect;
        }
        else{
            prevRect = currentRect;
            return currentRect;
        }
    }


    /**
     * Updates the Rect if the object detected has moved away based on the  Area and THRESHOLD value
     * @param currentRect: The current Rect(section of the Frame/ Boundary of the object detected)  of the Object detected
     * @return The Updated Rect*/
    Rect updateArea(Rect currentRect){
        if(prevRect ==null){
            prevRect =currentRect;
            return currentRect;
        }
          /* Finding intersection Area
          Using formula from https://math.stackexchange.com/questions/99565/simplest-way-to-calculate-the-intersect-area-of-two-rectangles
          IA = (Min(xBR1, xBR2) - Max(xTL1, xTL2) )*  (Min(yBR1, yBR2) - Max(yTL1, yTL2))*/
        int xTL1=currentRect.x;
        int xTL2= prevRect.x;
        int yTL1=currentRect.y;
        int yTL2= prevRect.y;
        int xBR1=currentRect.x+currentRect.width;
        int xBR2= prevRect.x+ prevRect.width;
        int yBR1=currentRect.y+currentRect.height;
        int yBR2 = prevRect.y+ prevRect.height;

        int intersectionArea=(Math.min(xBR1,xBR2)-Math.max(xTL1,xTL2))*(Math.min(yBR1,yBR2)- Math.max(yTL1,yTL2));
        Log.d(TAG," intersection Area"+intersectionArea);
        double unionArea= currentRect.area()+ prevRect.area();
        double spanArea = unionArea-intersectionArea;
        float intersectionCoveragePercent= (float) (intersectionArea/spanArea) ;
        Log.d(TAG , " Intersection Area "+intersectionCoveragePercent+ "");

        return intersectionCoveragePercent>=THRESHOLD? prevRect :currentRect;
    }
}