package com.pwc.commsgaze;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.DetectionEngineMaker;
import com.pwc.commsgaze.detection.Detector;
import com.pwc.commsgaze.detection.data.DetectionData;
import com.pwc.commsgaze.detection.data.SparseFlowDetectionData;
import com.pwc.commsgaze.utils.FileUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static com.pwc.commsgaze.detection.utils.DirectionUtil.directionParser;


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
    private final String OPENCV_HEADER = "OPENCV";
    private final float PASS_THRESHOLD = 0.6f;


    /*TODO to make final assertion > 60%*/

    @Test
    public void approachTest() {
        for(Approach approach:approaches){
            int numOfPasses = 0;
            /*TODO need to replace this accordingly*/

            for(String testCaseFileName: testCaseFileNames){
                if(testCaseApproachTester(approach,testCaseFileName))
                    numOfPasses++;
            }
            float passPercentage = (float)numOfPasses/testCaseFileNames.length;
            if(passPercentage<PASS_THRESHOLD){
                throw new AssertionError(approach.toString()+" Failed GazeTest");
            }
            Log.d(TAG,approach.toString()+" Passes "+ passPercentage+" of the Tests");
        }
    }

    boolean testCaseApproachTester(Approach approach,String testCaseFileName){
        if(approach.toString().contains(OPENCV_HEADER)){
            return openCvTestCaseApproachTester(approach,testCaseFileName);
        }
        return false;
    }


    boolean openCvTestCaseApproachTester(Approach approach,String testCaseFileName){
        VideoCapture videoCapture = new VideoCapture();
        videoCapture.open(new File(appContext.getExternalFilesDir(null), testCaseFileName).getAbsolutePath());
        ArrayList<Direction> directions = new ArrayList<>();
        Mat frame = new Mat();
        DetectionData  detectionData = createDetectionData(approach);
        detectionEngineMakerInstance.createDetector(approach,detectionData);
        Detector detector = detectionEngineMakerInstance.getDetector();
        while (videoCapture.read(frame)) {
            if(approach.equals(Approach.OPENCV_SPARSE_FLOW)){
                ((SparseFlowDetectionData)detectionData).setFrame(frame);
            }
            detector.updateDetector(detectionData);
            directions.add(detector.getDirection());
        }
        return directionTester(directions,testCaseFileName);
    }


   boolean directionTester(ArrayList<Direction> directions,String testCaseFileName){
        return false;
   }

    DetectionData createDetectionData(Approach approach){
        if(approach.equals(Approach.OPENCV_SPARSE_FLOW)){
            return  new SparseFlowDetectionData(getOpenCvFaceCascadeClassifier(),getOpenCvEyeCascadeClassifier());
        }
        return null;
    }


    CascadeClassifier getOpenCvFaceCascadeClassifier(){
        String fileNameAbsPath = appContext.getFileStreamPath("faceModel.xml").getAbsolutePath();
        CascadeClassifier faceCascadeClassifier = new CascadeClassifier();
        faceCascadeClassifier.load(fileNameAbsPath);
        return faceCascadeClassifier;
    }

    CascadeClassifier getOpenCvEyeCascadeClassifier(){
        String fileNameAbsPath = appContext.getFileStreamPath("eyeModel.xml").getAbsolutePath();
        CascadeClassifier eyeCascadeClassifier = new CascadeClassifier();
        eyeCascadeClassifier.load(fileNameAbsPath);
        return eyeCascadeClassifier;
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