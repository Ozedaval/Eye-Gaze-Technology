package com.pwc.commsgaze.detection;

import android.util.Log;

import com.pwc.commsgaze.detection.data.DetectionData;

/*If let's say the user wants to change the gaze detection approach*/
public class DetectionEngineMaker {
    private static final String TAG = "DetectionEngineMaker";
    private Approach activeApproach;
    private static DetectionEngineMaker instance;
    private Detector detector;

    private DetectionEngineMaker(){
    }

    public static DetectionEngineMaker getInstance() {
        if(instance == null)
            instance = new DetectionEngineMaker();
        return instance;
    }


    public void setActiveApproach(Approach approach,DetectionData detectionData){
        if(approach == activeApproach && detector!= null){
            detector.reset();
        }

    }

    /* Primarily to be used once */
    public void createDetector(Approach approach,DetectionData detectionData){
        if(activeApproach != null) {
            Log.d(TAG, " Active approach to be deleted -- " + activeApproach.toString());
        }
        if(detector!=null){
            detector.clear();
        }

        switch (approach){
            case OPENCV_SPARSE_FLOW:
                Log.d(TAG, " Active approach set to  -- " + approach.toString());
                detector = new SparseFlowDetector(detectionData);
                break;

        }
        activeApproach = approach;
    }


    private Approach getActiveApproach(){
        return activeApproach;
    }
    public Detector getDetector() {
        return detector;
    }
}


