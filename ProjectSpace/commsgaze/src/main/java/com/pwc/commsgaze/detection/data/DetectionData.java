package com.pwc.commsgaze.detection.data;



public abstract class DetectionData {
    private boolean isFaceDetected;
    private boolean isLeftEyeDetected;
    private boolean isRightEyeDetected;

    public boolean getIsDetected(){
        return isFaceDetected||isLeftEyeDetected||isRightEyeDetected;
    }

    public boolean getIsFaceDetected(){
        return isFaceDetected;
    }
    public boolean getIsLeftEyeDetected(){
        return isLeftEyeDetected;
    }

    public boolean getIsRightEyeDetected(){
        return isRightEyeDetected;
    }

   public void updateDetectionFlags(boolean isFaceDetected, boolean isLeftEyeDetected, boolean isRightEyeDetected){
        this.isFaceDetected =isFaceDetected;
        this.isLeftEyeDetected =isLeftEyeDetected;
        this.isRightEyeDetected =isRightEyeDetected;
    }


    @Override
    public String toString() {
        return "DetectionData{" +
                "isFaceDetected=" + isFaceDetected +
                ", isLeftEyeDetected=" + isLeftEyeDetected +
                ", isRightEyeDetected=" + isRightEyeDetected +
                '}';
    }
}
