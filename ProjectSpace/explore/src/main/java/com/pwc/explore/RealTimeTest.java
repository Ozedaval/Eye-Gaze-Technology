package com.pwc.explore;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

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

    public void saveResult(Context context, Boolean isInternal) {
        String TAG = "RealTimeTest";
        try {
            if (isInternal) {
                FileWriter out = new FileWriter(new File(context.getFilesDir(), "testResult.txt"));
                out.write(toString());
                out.close();
            }
            else {
                FileWriter out = new FileWriter(new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "testResult.txt"));
                out.write(toString());
                out.close();
            }
            Log.d(TAG, "saveResult :" +context.getFilesDir());
            Log.d(TAG, "saveResult");
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "RealTimeTest {" +
                "numOfTry=" + numOfTry +
                ", numOfCorrect=" + numOfCorrect +
                ", accuracy=" + accuracy + '%' +
                '}';
    }

}
