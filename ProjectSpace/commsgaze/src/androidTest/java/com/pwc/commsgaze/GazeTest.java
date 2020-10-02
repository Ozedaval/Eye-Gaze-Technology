package com.pwc.commsgaze;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.test.filters.MediumTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.DetectionEngineMaker;
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
    private Approach currentApproach;
    private static final String TAG= "GazeTest";


    /*TODO to make final assertion > 60%*/

    @Test
    public void approachTest() {

    }



    @Before
    public void setUp() throws Exception {
        detectionEngineMakerInstance = DetectionEngineMaker.getInstance();
        approaches = Approach.values();
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File externalFileDir = appContext.getExternalFilesDir(null);

        /*TODO need to scan all files*/
        InputStream videoInputStream = getClass().getClassLoader().getResourceAsStream("sample.mp4");
        FileOutputStream videoOutputStream = new FileOutputStream(new File(externalFileDir, "sample.mp4"));
        FileUtil.write(videoInputStream,videoOutputStream);
        Log.d(TAG,"File transferred successfully? "+FileUtil.hasExternalStoragePrivateData("sample.mp4",appContext));
        Log.d(TAG, Arrays.toString(appContext.getExternalFilesDirs(null)));

        VideoCapture videoCapture = new VideoCapture();
        videoCapture.open(new File(appContext.getExternalFilesDir(null), "sample.mp4").getAbsolutePath());
        Mat frame = new Mat();

        /*Mat img = Imgcodecs.imread();
        System.out.println("Image Empty? "+img.empty());
        System.out.println(img.size().toString());*/
        int frameCount = 0;
        while (videoCapture.read(frame)) {
        Log.d(TAG,"Frame count " + frameCount++);
        }



    }

}