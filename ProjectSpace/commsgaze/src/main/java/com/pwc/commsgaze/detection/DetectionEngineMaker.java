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

    public void setActiveApproach(Approach approach){
        /*TODO add another data class containing various info for detector and use it's instance as a parameter for this function*/
        if(approach == activeApproach && detector!= null){
            detector.reset();
        }

    }

    public void createDetector(Approach approach,DetectionData detectionData){
        if(activeApproach != null) {
            Log.d(TAG, " Active approach to be deleted -- " + activeApproach.toString());
        }
            switch (approach){
                case OPEN_CV_SPARSE_FLOW:
                    detector = new SparseFlowDetector(detectionData);
                    break;

            }
    }


    private Approach getActiveApproach(){
        return activeApproach;
    }
    public Detector getDetector() {
        return detector;
    }
}


