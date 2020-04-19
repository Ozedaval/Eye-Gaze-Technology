package com.pwc.explore.face;

import android.util.Log;
import android.view.MotionEvent;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.pwc.explore.databinding.ActivityEventUiBinding;
import static com.pwc.explore.face.FaceEventActivity.scaleNormalOrUp;
import static com.pwc.explore.face.FaceEventActivity.setText;

public class FaceTracker extends Tracker {
    /*TODO (1.Use Cursor Class appropriately
            2.Check if reference to binding is causing memory leaks) */
    private ActivityEventUiBinding binding;

    public FaceTracker(ActivityEventUiBinding binding) {
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
        float eulerY= face.getEulerY();
        Boolean isRightEyeClosed= face.getIsLeftEyeOpenProbability()<0.3;
        Boolean isLeftEyeClosed=face.getIsRightEyeOpenProbability()<0.3;
        String rightEyeIs= isRightEyeClosed?"not open":"open";
        String leftEyeIs= isLeftEyeClosed?"not open":"open";
        Log.d("FaceTrackerCallback","Tracking At Euler Y "+face.getEulerY() + "/n Tracking At Euler X"+face.getEulerZ()+"\n Left Eye is "+leftEyeIs+"\n Right Eye is "+rightEyeIs);

        if(eulerY>15){
            // face turns left

            scaleNormalOrUp(binding.linearLayoutYes,MotionEvent.AXIS_PRESSURE);
            if(isRightEyeClosed||isLeftEyeClosed) { setText(binding.yesTextView,"SELECTED YES");}
            else{setText(binding.yesTextView,"YES");} }
        else if (eulerY<-30) {
            // face turns right
            scaleNormalOrUp(binding.linearLayoutNo, MotionEvent.AXIS_PRESSURE);
            if(isRightEyeClosed||isLeftEyeClosed) { setText(binding.noTextView,"Selected NO");}
            else{setText(binding.noTextView,"NO");}
        }

        else{
            setText(binding.yesTextView,"YES");
            setText(binding.noTextView,"NO");
            scaleNormalOrUp(binding.linearLayoutYes,MotionEvent.ACTION_CANCEL);
            scaleNormalOrUp(binding.linearLayoutNo,MotionEvent.ACTION_CANCEL);
        }

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
