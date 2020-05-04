package com.pwc.explore.eyegaze.opencvshape;
import org.opencv.core.Mat;
import java.util.LinkedList;
import java.util.Queue;

public class OpticalFlowDetector {
    private static OpticalFlowDetector instance = null;
    private Queue<Mat[]>frameQueue =new LinkedList<>();
    private  int capacity=30;


    public static OpticalFlowDetector getInstance(){
        if(instance==null){
            instance=new OpticalFlowDetector();
        }
        return instance;
    }

    void update(Mat face,Mat eye){
        if(frameQueue.size()<31){
            process();
        frameQueue.add(new Mat[]{face,eye});
    }
    }

    void process(){

    }

    void setCapacity(int capacity){
        this.capacity=capacity;
    }

}
