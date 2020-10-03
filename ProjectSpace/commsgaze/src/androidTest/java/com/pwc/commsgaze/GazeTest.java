package com.pwc.commsgaze;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.DetectionEngineMaker;
import com.pwc.commsgaze.detection.Detector;
import com.pwc.commsgaze.utils.FileUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;


@MediumTest
@RunWith(AndroidJUnit4.class)
public class GazeTest  {

    static{ System.loadLibrary( "opencv_java4" );}
    private DetectionEngineMaker detectionEngineMakerInstance;
    private Approach[] approaches;
    private static final String TAG= "GazeTest";
    private String[] testCaseFileNames;
    private Context appContext;
    private final String TEST_RES_HEADER = "test";


    /*TODO to make final assertion > 60%*/

    @Test
    public void approachTest() {
        for(Approach approach:approaches){
            int numOfPasses = 0;
            /*TODO need to replace this accordingly*/
            Detector detector = null;
            for(String testCase: testCaseFileNames){
                if(testCaseApproachTester(detector,approach))
                    numOfPasses++;

            }

        }
    }

    boolean testCaseApproachTester(Detector detector,Approach approach){
        VideoCapture videoCapture = new VideoCapture();
        videoCapture.open(new File(appContext.getExternalFilesDir(null), "sample.mp4").getAbsolutePath());
        Mat frame = new Mat();

        while (videoCapture.read(frame)) {

        }
        return false;
    }



    Direction getEndingDirection(String testName){
        return directionParser(testName.substring(testName.lastIndexOf("_")+1));
    }

    /* Video file names to conform to this format Test_Number_Scenario_StartingDirection_EndingDirection */
    boolean isVideoFileNameFormatted(String fileName){
        fileName = fileName.contains(".") ? fileName.substring(0,fileName.lastIndexOf(".")):fileName;
        String[] splitName =  fileName.split("_");
        return splitName.length == 5 && splitName[0].equalsIgnoreCase(TEST_RES_HEADER);
    }

    Direction directionParser(String directionName) {

        for(Direction direction:Direction.values()){
            if(direction.toString().equalsIgnoreCase(directionName)){
                return direction;
            }
        }
        Log.e(TAG,"Direction Parsed as Unknown");
       return Direction.UNKNOWN;
    }


    @Before
    public void setUp() throws Exception {
        detectionEngineMakerInstance = DetectionEngineMaker.getInstance();
        approaches = Approach.values();
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File externalFileDir = appContext.getExternalFilesDir(null);

        /*TODO need to scan all files*/
        /*TODO need to copy it to app specific directory*/

        InputStream videoInputStream = getClass().getClassLoader().getResourceAsStream("sample.mp4");
        FileOutputStream videoOutputStream = new FileOutputStream(new File(externalFileDir, "sample.mp4"));
        FileUtil.write(videoInputStream,videoOutputStream);
        Log.d(TAG,"File transferred successfully? "+ FileUtil.hasExternalStoragePrivateData("sample.mp4",appContext));
        Log.d(TAG, Arrays.toString(appContext.getExternalFilesDirs(null)));

        /*TODO for demo*/
        testCaseFileNames = new String[]{"sample.mp4"};

        for(String testCaseFileName: testCaseFileNames){
            if(!isVideoFileNameFormatted(testCaseFileName)){
                throw new AssertionError("Video File: " + testCaseFileName +" not Formatted well!");
            }
        }

    }

}