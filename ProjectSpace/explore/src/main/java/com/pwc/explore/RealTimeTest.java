package com.pwc.explore;

public class RealTimeTest {
    private int numOfTry;
    private int numOfCorrect;
    private float accuracy;

    public RealTimeTest() {
        this.numOfTry = 0;
        this.numOfCorrect = 0;
        this.accuracy = 0.0f;
    }

    public int getNumOfTry() { return numOfTry; }
    public void setNumOfTry(int numOfTry) { this.numOfTry = numOfTry; }
    public int getNumOfCorrect() { return numOfCorrect; }
    public void setNumOfCorrect(int numOfCorrect) { this.numOfCorrect = numOfCorrect; }
    public float getAccuracy() { return accuracy; }
    public void setAccuracy() { this.accuracy = (float) (this.numOfCorrect * 100) / this.numOfTry; }
}
