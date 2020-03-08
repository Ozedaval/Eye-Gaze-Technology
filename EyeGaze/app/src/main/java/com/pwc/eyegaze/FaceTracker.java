package com.pwc.eyegaze;

import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;

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

        Log.d("FaceTrackerCallback","updating");
        super.onUpdate(detections, o);

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
