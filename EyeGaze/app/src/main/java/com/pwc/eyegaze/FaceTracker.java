package com.pwc.eyegaze;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.pwc.eyegaze.databinding.ActivityMainBinding;

import static com.pwc.eyegaze.MainActivity.scaleNormalOrUp;

public class FaceTracker extends Tracker {
    private ActivityMainBinding binding;
    public FaceTracker(ActivityMainBinding binding) {
      this.binding=binding;
    }

    @Override
    public void onNewItem(int i, Object o) {
        super.onNewItem(i, o);
    }




    @Override
    public void onUpdate(Detector.Detections detections, Object o) {
        // Since from the camera perspective left is right & vice versa
       Face face= (Face) detections.getDetectedItems().valueAt(0);
       if(face.getEulerY()>15){

                   scaleNormalOrUp(binding.linearLayoutYes,MotionEvent.AXIS_PRESSURE);


       }
       else if (face.getEulerY()<-30){

                   scaleNormalOrUp(binding.linearLayoutNo,MotionEvent.AXIS_PRESSURE);

       }
       else{
           scaleNormalOrUp(binding.linearLayoutYes,MotionEvent.ACTION_CANCEL);
           scaleNormalOrUp(binding.linearLayoutNo,MotionEvent.ACTION_CANCEL);
       }

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
