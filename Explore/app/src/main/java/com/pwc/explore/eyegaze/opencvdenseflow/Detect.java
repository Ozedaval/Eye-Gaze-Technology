
package com.pwc.explore.eyegaze.opencvdenseflow;


import android.util.Log;

import com.pwc.explore.DetectionListener;
import com.pwc.explore.Direction;

import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.HashMap;
import java.util.List;


public class Detect {

    private DetectionListener detectionListener;
    private boolean isFirstPairOfIrisFound;
    private SimpleBlobDetector simpleBlobDetector;
    private DenseOpticalFlowDetector sparseOpticalFlowDetector;
    private static final String TAG="Detect";


    Detect(DetectionListener dl) {
        this.detectionListener = dl;
        simpleBlobDetector = SimpleBlobDetector.create();
        isFirstPairOfIrisFound = false;
        sparseOpticalFlowDetector = new DenseOpticalFlowDetector(new Size(30,30 ),2);
    }


    private void sendDetection(Direction direction) {
        detectionListener.move(direction);
    }


    /*Iris Detection*/
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {
        /*Thread.dumpStack();*/

        Mat frameGray = new Mat();

        /*Creating a Grayscale version of the Image*/
        Imgproc.cvtColor(frame, frameGray, Imgproc.COLOR_BGR2GRAY);

        /*Increasing contrast & brightness of the image appropriately*/
        Imgproc.equalizeHist(frameGray, frameGray);

        /*Detecting faces*/
        MatOfRect faces = new MatOfRect();
        faceCascade.detectMultiScale(frameGray, faces);

        /*Using the First Detected Face*/
        List<Rect> listOfFaces = faces.toList();
        if (!listOfFaces.isEmpty()) {
            Rect face = listOfFaces.get(0);
            /*Log.d(TAG + "Face ", " X co-ordinate  is " + center.x + "Y co ordinate" + center.y);
            Log.d(TAG, "Height " + face.height + "Width " + face.width);*/

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);

            List<Rect> listOfEyes=null;



            if (!isFirstPairOfIrisFound) {

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
                listOfEyes = eyes.toList();
                Mat[] eyesROI = new Mat[2];

                /*Log.d(TAG,"face.x= "+face.x+"face.y = "+face.y);*/

                for (int i = 0; i < listOfEyes.size(); i++) { //Just get the first 2 detected eyes
                    Rect eye = listOfEyes.get(i);

                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x = face.x + eye.x;
                    eye.y = face.y + eye.y;

                    /*Updates Eyes for DetectionSmoother*/
                    /*eye=dsEyes[i].updateCoord(eye);*/

                    /*Cropping an eye Image*/
                    eyesROI[i] = frame.submat(eye);


                    /*Point eyeCenter = new Point(face.x + eye.x + eye.width / 2f, face.y + eye.y + eye.height / 2f);
                    int radiusEye = (int) Math.round((eye.width + eye.height) * 0.25);
                    Log.d(TAG, " EYE X co-ordinate  is " + eyeCenter.x + "Y co ordinate" + eyeCenter.y);
                  */

                    /*Displaying boundary of the detected eye*/
                    Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));

                    /*Iris Detection via Blob Detection*/
                    Mat eyeROICanny = new Mat();
                    Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 3);

                    MatOfKeyPoint blobs = new MatOfKeyPoint();
                    simpleBlobDetector.detect(eyeROICanny, blobs);
                    /*Log.d(TAG+ " Number of blobs ", blobs.toArray().length + "");*/

                    /*Finding Iris*/
                    KeyPoint[] blobsArray = blobs.toArray();
                    if (blobsArray.length != 0) {
                        Point blobCentre = blobsArray[0].pt;
                        blobCentre.x = blobCentre.x + eye.x;
                        blobCentre.y = blobCentre.y + eye.y;
                        Imgproc.circle(frame, blobCentre, 2, new Scalar(255, 0, 0), 4);
                        Log.d(TAG,"Height "+eye.height+"Width "+eye.width);
                        float irisRadius=5;
                        sparseOpticalFlowDetector.setROIPoints(i,getIrisSparsePoint(irisRadius,blobCentre));
                    }
                }

            }

            /*sparseOpticalFlow Initiator*/
            if (!isFirstPairOfIrisFound && listOfEyes.size()==2) {
                isFirstPairOfIrisFound = true;
            }

            if (isFirstPairOfIrisFound) {
                Mat irisPointsMat = new Mat(sparseOpticalFlowDetector.getNumberOfRows(), 2, CvType.CV_32F);
               Mat filledUpIrisPointsMat=sparseOpticalFlowDetector.fillUpMatPoints(irisPointsMat);
                Mat irisPointsPredictions=sparseOpticalFlowDetector.predictPoints(frame,filledUpIrisPointsMat);
                HashMap<Integer,Point[]> predictions=sparseOpticalFlowDetector.unpackPrediction(irisPointsPredictions);
                Point[][][] irisPredictedSparsePointss= new Point[][][]
                        {{predictions.get(0)},{predictions.get(1)}};
                for (Point[][] irisPredictedSparsePoints : irisPredictedSparsePointss) {
                    for (Point[] points : irisPredictedSparsePoints) {
                        Log.d(TAG,(points==null)+"");
                        if (points != null) {
                            for (Point point : points) {
                                Imgproc.circle(frame, point, 3, new Scalar(255, 0, 0));
                            }
                        }
                    }

                }
            }

        }

        return frame;
    }


    private Point[] getIrisSparsePoint(float irisRadius, Point irisCentre){
        Point[] sparsePoints= new Point[3];
        sparsePoints[0]=irisCentre;
        sparsePoints[1]=new Point(irisCentre.x+irisRadius,irisCentre.y);
        sparsePoints[2]=new Point(irisCentre.x-irisRadius,irisCentre.y);
       /* sparsePoints[3]=new Point(irisCentre.x,irisCentre.y+irisRadius);
        sparsePoints[4]=new Point(irisCentre.x+irisRadius,irisCentre.y-irisRadius);*/
        return sparsePoints;
    }
}