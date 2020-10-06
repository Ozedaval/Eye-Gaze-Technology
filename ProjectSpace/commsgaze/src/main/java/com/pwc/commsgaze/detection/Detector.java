package com.pwc.commsgaze.detection;

import com.pwc.commsgaze.Direction;
import com.pwc.commsgaze.detection.data.DetectionData;

public abstract class Detector {
     public abstract Direction getDirection();
     public abstract void reset();
     public abstract Approach getApproach();
     public abstract DetectionData updateDetector(DetectionData detectionData);
     /*Ideally to manually release resources used by the detector*/
     public abstract void clear();

}
