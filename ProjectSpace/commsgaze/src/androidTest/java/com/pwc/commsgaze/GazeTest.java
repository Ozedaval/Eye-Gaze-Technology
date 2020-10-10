package com.pwc.commsgaze;

import android.content.Context;
import android.os.Environment;
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
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


import static com.pwc.commsgaze.detection.utils.DirectionUtil.directionParser;
import static com.pwc.commsgaze.detection.utils.DirectionUtil.getMaxDirection;
import static com.pwc.commsgaze.detection.utils.DirectionUtil.getOppositeDirection;


@MediumTest
@RunWith(AndroidJUnit4.class)
public class GazeTest  {

    static{ System.loadLibrary( "opencv_java4" );}
    private DetectionEngineMaker detectionEngineMakerInstance;
    private Approach[] approaches;
    private static final String TAG= "GazeTest";
    private ArrayList<String> testCaseFileNames;
    private Context appContext;
    private Context instrumentationContext;
    private final String TEST_RES_HEADER = "test";
    private final String OPENCV_HEADER = "OPENCV";
    private final float OVERALL_PASS_THRESHOLD = 0.6f;
    private StringBuilder testResultBuilder;




    @Before
    public void setUp() throws Exception {
        instrumentationContext = InstrumentationRegistry.getInstrumentation().getContext();
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testResultBuilder = new StringBuilder();
        detectionEngineMakerInstance = DetectionEngineMaker.getInstance();
        approaches = Approach.values();
        testCaseFileNames = new ArrayList<>();

        File externalFileDir = appContext.getExternalFilesDir(null);
        String[] assetTestFileNames = instrumentationContext.getAssets().list("");
        if (assetTestFileNames != null) {
            for(String testFileName:assetTestFileNames){
                if(testFileName.contains(TEST_RES_HEADER)){
                    testCaseFileNames.add(testFileName);
                    InputStream testFileInputStream = instrumentationContext.getAssets().open(testFileName);
                    FileOutputStream testFileOutputStream = new FileOutputStream(new File(externalFileDir, testFileName));
                    FileUtil.write(testFileInputStream,testFileOutputStream);
                    Log.d(TAG,"File "+ testFileName+ " transferred successfully? "+ FileUtil.hasExternalStoragePrivateData(testFileName,appContext));
                }
            }
        }
        else{

            throw new AssertionError("Test Files not in place");
        }
        for(String testCaseFileName: testCaseFileNames){
            if(!isVideoFileNameFormatted(testCaseFileName)){
                throw new AssertionError("Video File: " + testCaseFileName +" not Formatted well!");
            }
        }
    }


    @Test
    public void approachTest() throws IOException {
        for(Approach approach:approaches){
            int numOfPasses = 0;

            Log.d(TAG,"Currently testing Approach "+ approach.toString());
            testResultBuilder.append("Currently testing Approach ").append(approach.toString()).append("\n");
            for(String testCaseFileName: testCaseFileNames){
                if(testCaseApproachTester(approach,testCaseFileName))
                {   testResultBuilder.append(testCaseFileName).append(" Passes").append("\n");
                    numOfPasses++;}
            }
            Log.d(TAG,"Number of passes "+ numOfPasses);
            testResultBuilder.append("Number of passes ").append(numOfPasses).append("\n");
            float passPercentage = (float)numOfPasses/testCaseFileNames.size();
            if(passPercentage< OVERALL_PASS_THRESHOLD){
                throw new AssertionError(approach.toString()+" Failed GazeTest"+ "Pass Percentage "+passPercentage);
            }
            testResultBuilder.append(approach.toString()).append(" Passes ").append(passPercentage).append(" of the Tests").append("\n");;
            Log.d(TAG,approach.toString()+" Passes "+ passPercentage + " of the Tests");

        }

        /*From Yunsung's RealTimeTest */
        FileWriter out = new FileWriter(new File(appContext.getExternalFilesDir(null), "GazeTestResult.txt"));
        out.write(testResultBuilder.toString());
        out.close();
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
        Mat frameRGB = new Mat();
        DetectionData  detectionData = createDetectionData(approach);
        detectionEngineMakerInstance.createDetector(approach,detectionData);
        Detector detector = detectionEngineMakerInstance.getDetector();
        while (videoCapture.read(frame)) {
            /*Log.d(TAG,"Frame details:  frame empty?"+frame.empty()+ " frame size"+frame.size().toString()+" frame channels"+ frame.channels() );*/
            if(approach.equals(Approach.OPENCV_SPARSE_FLOW)){
                Imgproc.cvtColor(frame, frameRGB, Imgproc.COLOR_RGB2BGR);
                ((SparseFlowDetectionData)detectionData).setFrame(frameRGB);
            }
            detector.updateDetector(detectionData);
            Direction detectedDirection = detector.getDirection();
            directions.add(detectedDirection);
        }
        return directionTester(directions,testCaseFileName);
    }


    boolean directionTester(ArrayList<Direction> directions,String testCaseFileName){
        /*Cases L -> N ,R -> N,N -> L,N -> R */
        Direction endingDirection = getEndingDirection(testCaseFileName);
        Direction startingDirection = getStartingDirection(testCaseFileName);
        Log.d(TAG,"Ending Direction Deciphered "+ endingDirection);
        Log.d(TAG,"Starting Direction Deciphered "+ startingDirection);
        Log.d(TAG,"Direction Array content for " + testCaseFileName+" is "+directions.toString());
        Direction movementDirection;
        if(endingDirection.equals(Direction.NEUTRAL) && !startingDirection.equals(Direction.NEUTRAL)){
            movementDirection = getOppositeDirection(startingDirection);
        }
        else {
            movementDirection = endingDirection;
        }
        return movementDirection.equals(getMaxDirection(directions));
    }


    DetectionData createDetectionData(Approach approach){
        if(approach.equals(Approach.OPENCV_SPARSE_FLOW)){
            return new SparseFlowDetectionData(getOpenCvFaceCascadeClassifier(),getOpenCvEyeCascadeClassifier());
        }
        return null;
    }


    Direction getStartingDirection(String testCaseFileName){
        return directionParser(testCaseFileName.split("_")[3]);
    }

    Direction getEndingDirection(String testCaseFileName){
        return directionParser(testCaseFileName.substring(testCaseFileName.lastIndexOf("_")+1,testCaseFileName.lastIndexOf(".")));
    }



    /* Video file names to conform to this format Test_Number_Scenario_StartingDirection_EndingDirection */
    boolean isVideoFileNameFormatted(String fileName){
        fileName = fileName.contains(".") ? fileName.substring(0,fileName.lastIndexOf(".")):fileName;
        String[] splitName =  fileName.split("_");
        return splitName.length == 5 && splitName[0].equalsIgnoreCase(TEST_RES_HEADER);
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
}