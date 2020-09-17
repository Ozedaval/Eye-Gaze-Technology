package com.pwc.commsgaze;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.DetectionEngineMaker;
import com.pwc.commsgaze.detection.Detector;
import com.pwc.commsgaze.detection.data.DetectionData;

import java.util.ArrayList;

/*Holds details for MainActivity in a Activity life-conscious way */
public class MainViewModel extends ViewModel {

    private MutableLiveData<Boolean> isFirstRun;
    private static final String TAG = "MainViewModel";
    private DetectionEngineMaker detectionEngineMakerInstance;
    private Detector detector;
    private ViewGazeController viewGazeController;


    Direction getDirection(){
        return  (detector==null)? Direction.UNKNOWN:detector.getDirection();
    }

    Detector getDetector(){
        return  detectionEngineMakerInstance.getDetector();
    }

    void createDetector(Approach approach, DetectionData detectionData){
        if(detectionEngineMakerInstance == null){
            detectionEngineMakerInstance = DetectionEngineMaker.getInstance();
        }

        detectionEngineMakerInstance.createDetector(approach,detectionData);
        this.detector = detectionEngineMakerInstance.getDetector();
    }

    void setViewGazeController(ViewGazeController viewGazeController){
        this.viewGazeController = viewGazeController;
    }

    void updateViewGazeController(){
        viewGazeController.updateSelectedViewHolder(getDirection());
    }

    void initialiseViewGazeHolders(ArrayList<MainRecyclerViewAdapter.ViewHolder> viewHolders){
        viewGazeController.initialiseViewHolders(viewHolders);
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