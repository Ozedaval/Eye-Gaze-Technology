package com.pwc.commsgaze;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pwc.commsgaze.detection.DetectionEngineMaker;
import com.pwc.commsgaze.detection.Detector;

/*Holds details for MainActivity in a Activity life-conscious way */
public class MainViewModel extends ViewModel {

    private MutableLiveData<Boolean> isFirstRun;
    private static final String TAG = "MainViewModel";
    private DetectionEngineMaker detectionEngineMakerInstance;
    private Detector detector;

    Direction getDirection(){
        if(detectionEngineMakerInstance == null){
            detectionEngineMakerInstance = DetectionEngineMaker.getInstance();
            this.detector = detectionEngineMakerInstance.getDetector();
        }
      return  (detector==null)? Direction.UNKNOWN:detector.getDirection();
    }

    /*Check on UI thread for shared preference before calling this*/
    LiveData<Boolean> getIsFirstRun() {
        if (isFirstRun == null) {
            isFirstRun = new MutableLiveData<Boolean>();
            isFirstRun.setValue(true);
            Log.d(TAG,"getIsFirstRunCalled");
        }
        return isFirstRun;
    }

    public void initialisationDone() {
        Log.d(TAG," InitialisationDone Called");
        isFirstRun.setValue(false);
    }
}