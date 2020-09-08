package com.pwc.commsgaze.detection;

/*If let's say the user wants to change the gaze detection approach*/
public class DetectionEngineMaker {
    /*TODO make sure */
    private Approach activeApproach;
    private static DetectionEngineMaker instance;
    private Detector detector;

    private DetectionEngineMaker(){
    }

    public static DetectionEngineMaker getInstance() {
        if(instance == null)
            instance = new DetectionEngineMaker();
        return instance;
    }

    public void setActiveApproach(Approach approach){
      /*TODO add another data calsse containing various info for detector and use it's instance as a parameter for this function*/
        detector = getDetector(approach);
    }


    private Detector getDetector(Approach approach){
        /*TODO*/
        switch (approach){
            case OPEN_CV_SPARSE_FLOW:

        }

    return null;}


    public Detector getDetector() {
        if (activeApproach == null) {
                    activeApproach = Approach.OPEN_CV_SPARSE_FLOW;
                    detector = new SparseFlowDetector();
                    return detector;

            }
            return detector;
        }
}


