package com.pwc.commsgaze;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.DetectionEngineMaker;
import com.pwc.commsgaze.detection.Detector;
import com.pwc.commsgaze.detection.data.DetectionData;


/*Holds details for MainActivity in a Activity life-conscious way */
public class MainViewModel extends ViewModel {

    private MutableLiveData<Boolean> isFirstRun;
    private static final String TAG = "MainViewModel";
    private DetectionEngineMaker detectionEngineMakerInstance;
    private Detector detector;
    private ViewGazeController viewGazeController;
    private MutableLiveData<Integer> selectedViewHolderID;
    private DirectionMediator directionMediator;
    private MutableLiveData<Direction> gaugedDirection;

    LiveData<Direction> getGaugedDirection() {
        if(gaugedDirection == null)
            gaugedDirection = new MutableLiveData<>();
        return gaugedDirection;
    }


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



    void updateDirectionMediator(Direction direction) {
        directionMediator.update(direction);
        if(directionMediator.getNeedUpdate()){
            gaugedDirection.setValue(directionMediator.getGaugedCurrentDirection());
        }
    }



    void initialiseDirectionMediator(int frameThreshold){
        directionMediator = new DirectionMediator(frameThreshold);

    }


    void updateViewGazeController(Direction direction){
        viewGazeController.updateSelectedViewHolder(direction);
        if(selectedViewHolderID!= null && selectedViewHolderID.getValue() != viewGazeController.getSelectedViewHolderIndex())
        selectedViewHolderID.setValue(viewGazeController.getSelectedViewHolderIndex());
    }

    LiveData<Integer> getSelectedViewHolderID(){
        if (selectedViewHolderID == null){
            selectedViewHolderID = new MutableLiveData<>(0);
        }
    return  selectedViewHolderID;
    }

    int getPreviousSelectedViewHolderID(){
        return  viewGazeController.getPrevSelectedViewHolderIndex();
    }


    void initialiseViewGazeHolders(int fixedDimension,int numOfPositions){

            viewGazeController = new ViewGazeController(fixedDimension,numOfPositions);

        if (selectedViewHolderID == null){
            selectedViewHolderID = new MutableLiveData<Integer>();
            selectedViewHolderID.setValue(0);
        }
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