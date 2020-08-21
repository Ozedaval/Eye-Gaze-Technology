package com.pwc.explore.face;

import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.pwc.explore.databinding.ActivityEventUiBinding;


import java.lang.ref.WeakReference;

import static com.pwc.explore.face.FaceEventActivity.scaleNormalOrUp;
import static com.pwc.explore.face.FaceEventActivity.setText;

public class FaceTracker extends Tracker {
    /*TODO (1.Use Cursor Class appropriately
            2.Check if reference to binding is causing memory leaks) */
    private WeakReference<ActivityEventUiBinding> weakReferenceBinding;

    public FaceTracker(ActivityEventUiBinding binding) {
        weakReferenceBinding=new WeakReference<>(binding);

    }

    @Override
    public void onNewItem(int i, Object o) {
        super.onNewItem(i, o);
    }


    @Override
    public void onUpdate(Detector.Detections detections, Object o) {
        // Since from the camera perspective left is right & vice versa
        ActivityEventUiBinding binding= weakReferenceBinding.get();
        Face face= (Face) detections.getDetectedItems().valueAt(0);
        float eulerY = face.getEulerY();
        Boolean isRightEyeClosed = face.getIsLeftEyeOpenProbability() < 0.3;
        Boolean isLeftEyeClosed = face.getIsRightEyeOpenProbability() < 0.3;
        String rightEyeIs = isRightEyeClosed ? "not open" : "open";
        String leftEyeIs = isLeftEyeClosed ? "not open" : "open";
        Log.d(getClass().getName(), "Tracking At Euler Y " + face.getEulerY() + "/n Tracking At Euler X" + face.getEulerZ() + "\n Left Eye is " + leftEyeIs + "\n Right Eye is " + rightEyeIs);

        if (eulerY > 15) {
            /*face turns left*/
            scaleNormalOrUp(binding.linearLayoutLeft, MotionEvent.AXIS_PRESSURE);
            if (isRightEyeClosed || isLeftEyeClosed) {
                setText(binding.leftTextView, "Selected Left");
            } else {
                setText(binding.leftTextView, "Left");
            }
        } else if (eulerY < -30) {
            // face turns right
            scaleNormalOrUp(binding.linearLayoutRight, MotionEvent.AXIS_PRESSURE);
            if (isRightEyeClosed || isLeftEyeClosed) {
                setText(binding.rightTextView, "Selected Right ");
            } else {
                setText(binding.rightTextView, "Right");
            }
        } else {
            setNormal(binding);
        }
    }


    @Override
    public void onMissing(Detector.Detections detections) {
        super.onMissing(detections);
        setNormal(weakReferenceBinding.get());
    }


    @Override
    public void onDone() {
        super.onDone();
    }


    /**
     * Sets the state of associated TextView and Buttons back to normal
     * @param binding : Binding of the the associated activity
     */
    private void setNormal(ActivityEventUiBinding binding){
        setText(binding.leftTextView, "Left");
        setText(binding.rightTextView, "Right");
        scaleNormalOrUp(binding.linearLayoutLeft, MotionEvent.ACTION_CANCEL);
        scaleNormalOrUp(binding.linearLayoutRight, MotionEvent.ACTION_CANCEL);
    }
}
