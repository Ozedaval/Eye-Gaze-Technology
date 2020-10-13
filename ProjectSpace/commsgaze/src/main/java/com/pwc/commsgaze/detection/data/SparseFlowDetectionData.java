package com.pwc.commsgaze.detection.data;

import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

public class SparseFlowDetectionData extends DetectionData {
    private CascadeClassifier faceCascade;
    private CascadeClassifier  eyeCascade;
    private Mat frame;



    public SparseFlowDetectionData(CascadeClassifier faceCascade, CascadeClassifier eyeCascade){
        this.faceCascade = faceCascade;
        this.eyeCascade = eyeCascade;
    }


    public void updateFrame(Mat frame){
        this.frame = frame;
    }

    public CascadeClassifier getEyeCascade() {
        return eyeCascade;
    }


    public CascadeClassifier getFaceCascade() {
        return faceCascade;
    }

    public void setFrame(Mat frame) {
        this.frame = frame;
    }

    public Mat getFrame() {
        return frame;
    }

}

