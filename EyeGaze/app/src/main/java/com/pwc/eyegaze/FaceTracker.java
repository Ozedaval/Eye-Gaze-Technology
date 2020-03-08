package com.pwc.eyegaze;

import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class FaceTracker extends Tracker {
    public FaceTracker() {
        super();
    }

    @Override
    public void onNewItem(int i, Object o) {
        super.onNewItem(i, o);
    }

    @Override
    public void onUpdate(Detector.Detections detections, Object o) {

       Face face= (Face) detections.getDetectedItems().valueAt(0);
       if(face.getEulerY()>0){
//select Left
       }
       else{
           //select left
       }
       // Since from the camera perspective left is right & vice versa
       String rightEyeIs= face.getIsLeftEyeOpenProbability()>0.5?"open":"not open";
        String leftEyeIs= face.getIsRightEyeOpenProbability()>0.5?"open":"not open";
       Log.d("FaceTrackerCallback","Tracking At Euler Y "+face.getEulerY() + "/n Tracking At Euler X"+face.getEulerZ()+"\n Left Eye is "+leftEyeIs+"\n Right Eye is "+rightEyeIs);

}

    @Override
    public void onMissing(Detector.Detections detections) {
        super.onMissing(detections);
    }

    @Override
    public void onDone() {
        super.onDone();
    }
}
