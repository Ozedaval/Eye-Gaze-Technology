package com.pwc.explore.eyegaze.opencvsparseflow;

import android.util.Log;

import com.pwc.explore.Direction;

import org.opencv.core.Mat;
import org.opencv.core.Size;

import org.opencv.video.DISOpticalFlow;
import org.opencv.video.SparseOpticalFlow;
import org.opencv.video.SparsePyrLKOpticalFlow;

import java.util.Arrays;

public class SparseOpticalFlowDetector {
    private SparseOpticalFlow sparseOpticalFlow;
    private Mat prevFrame;

    SparseOpticalFlowDetector(Size winSize){
        sparseOpticalFlow = SparsePyrLKOpticalFlow.create(winSize);
    }

    Mat predictPoints(Mat currentFrame, Mat prevPoints){
        if(prevFrame==null){
            prevFrame=currentFrame;
            return prevPoints;
        }
        else{
            Mat nextPoints = new Mat();
            Mat status=new Mat();
            sparseOpticalFlow.calc(prevFrame,currentFrame,prevPoints,nextPoints,status);
            Log.d(getClass().getSimpleName(),"Prev Point is"+ Arrays.toString(prevPoints.get(0, 0)) + "Next Point is "+ Arrays.toString(nextPoints.get(0, 0)));
            Log.d(getClass().getSimpleName(),"Prev Point is"+ Arrays.toString(prevPoints.get(0, 1)) + "Next Point is "+ Arrays.toString(nextPoints.get(0, 1)));
           prevFrame=currentFrame;
            return nextPoints;
        }
//sparseOpticalFlow.calc();

    }

}
