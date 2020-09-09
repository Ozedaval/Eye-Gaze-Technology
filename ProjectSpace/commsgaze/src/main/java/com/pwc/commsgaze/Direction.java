package com.pwc.commsgaze;

/**
 * Defined Directions for Eye Tracking;Implements runnable as to access main UI thread
 */

public enum Direction implements Runnable {
    TOP,LEFT,NEUTRAL,RIGHT,BOTTOM,UNKNOWN;


    @Override
    public void run() {
    }
}
