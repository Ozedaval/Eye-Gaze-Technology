package com.pwc.commsgaze;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.pwc.commsgaze.database.Content;
import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.DetectionEngineMaker;
import com.pwc.commsgaze.detection.Detector;
import com.pwc.commsgaze.detection.data.DetectionData;

import java.util.ArrayList;
import java.util.Arrays;


/*Holds details for MainActivity in a Activity life-conscious way */
public class MainViewModel extends ViewModel {


    private static final String TAG = "MainViewModel";
    private DetectionEngineMaker detectionEngineMakerInstance;
    private Detector detector;
    private ViewGazeController viewGazeController;
    private MutableLiveData<Integer> selectedDataIndex;
    private DirectionMediator directionMediator;
    private MutableLiveData<Direction> gaugedDirection;
    private MutableLiveData<Boolean> isDetected;
    private DetectionData detectionData;
    private MutableLiveData<Boolean> needClick;
    private Integer previousClickedDataIndex;
    private MutableLiveData<ArrayList<Content>> clickedContents;


    LiveData<Boolean> getIsDetected(){
        if(isDetected == null)
            isDetected = new MutableLiveData<>();
        return isDetected;
    }

    LiveData<Boolean> getNeedClick(){
        if(needClick == null) {
            needClick = new MutableLiveData<>();
        }
        return  needClick;
    }


    LiveData<ArrayList<Content>> getClickedContents(){
        if(clickedContents ==null) {
            clickedContents = new MutableLiveData<>();
            clickedContents.setValue(new ArrayList<Content>());
        }
        return clickedContents;
    }


    public DetectionData getDetectionData() {
        return detectionData;
    }

    public void setDetectionData(DetectionData detectionData) {
        this.detectionData = detectionData;
    }

    void updateDetectionStatus(){
        /*Log.d(TAG,"updateDetection Status" + detectionData.getIsDetected());*/
        isDetected.setValue(detectionData.getIsDetected());
    }

    LiveData<Direction> getGaugedDirection() {
        if(gaugedDirection == null)
            gaugedDirection = new MutableLiveData<>();
        return gaugedDirection;
    }


    void updateSentence(Content content){
        ArrayList<Content> contents= clickedContents.getValue();
        if(contents!=null) {

            Log.d(TAG,"Queue  "+ Arrays.toString(contents.toArray())+" "+content.getWord()+ " being added");
            contents.add(content);
            Log.d(TAG,"After adding to Queue " +  Arrays.toString(contents.toArray()));
        }
        clickedContents.setValue(contents);
    }


    void setPreviousClickedDataIndex(int previousClickedDataIndex){
        this.previousClickedDataIndex = previousClickedDataIndex;
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
        detector = detectionEngineMakerInstance.getDetector();
    }



    void updateDirectionMediator(Direction direction) {
        directionMediator.update(direction);
        if(directionMediator.getNeedUpdate()){
            gaugedDirection.setValue(directionMediator.getGaugedCurrentDirection());
        }
        if(directionMediator.getIsStableNeutral()){
            /*To make sure the same element doesnt get clicked repeatedly*/
            /*Log.d(TAG,"Selected Data Index "+ selectedDataIndex.getValue() + "Previous Clicked Index "+ previousClickedDataIndex);*/
            if(previousClickedDataIndex == null || !previousClickedDataIndex.equals(selectedDataIndex.getValue()))
                needClick.setValue(true);
        }
        else{needClick.setValue(false);}
    }



    void initialiseDirectionMediator(int selectionThreshold,int clickInitThreshold){
        directionMediator = new DirectionMediator(selectionThreshold,clickInitThreshold);
    }


    void updateViewGazeController(Direction direction){
        viewGazeController.updateSelectedDataIndex(direction);
        if(selectedDataIndex != null && selectedDataIndex.getValue() != viewGazeController.getSelectedDataIndex())
            selectedDataIndex.setValue(viewGazeController.getSelectedDataIndex());
    }

    LiveData<Integer> getSelectedDataIndex(){
        if (selectedDataIndex == null){
            selectedDataIndex = new MutableLiveData<>(0);
        }
        return selectedDataIndex;
    }

    int getPreviousSelectedViewHolderID(){
        return  viewGazeController.getPrevSelectedDataIndex();
    }


    void initialiseViewGazeHolders(int fixedDimension,int numOfPositions){

        viewGazeController = new ViewGazeController(fixedDimension,numOfPositions);

        if (selectedDataIndex == null){
            selectedDataIndex = new MutableLiveData<Integer>();
            selectedDataIndex.setValue(0);
        }
    }



}