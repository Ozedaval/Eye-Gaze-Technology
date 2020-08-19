package com.pwc.explore;

/**
 * Defined Directions for Eye Tracking;Implements runnable as to access main UI thread in opencvsparseflow.Detect
 * @see com.pwc.explore.eyegaze.opencvsparseflow.Detect*/

public enum Direction implements Runnable {
    TOP_LEFT,TOP,TOP_RIGHT,LEFT,NEUTRAL,RIGHT,BOTTOM_LEFT,BOTTOM,BOTTOM_RIGHT,UNKNOWN;


    @Override
    public void run() {
    }
}
