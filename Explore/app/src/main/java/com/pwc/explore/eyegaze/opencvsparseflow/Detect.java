package com.pwc.explore.eyegaze.opencvsparseflow;


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

import java.util.LinkedList;
import java.util.List;


public class Detect {

    private DetectionListener dl;
    private boolean isFirstPairOfIrisFound;
    private SimpleBlobDetector simpleBlobDetector;
    private LinkedList<Point> irisPoints;
    private SparseOpticalFlowDetector sparseOpticalFlowDetector;


    Detect(DetectionListener dl) {
        this.dl = dl;
        simpleBlobDetector = SimpleBlobDetector.create();
        isFirstPairOfIrisFound = false;
        irisPoints = new LinkedList<>();
        sparseOpticalFlowDetector = new SparseOpticalFlowDetector(new Size(30, 30));
    }


    private void sendDetection(Direction direction) {
        dl.move(direction);
    }


    /*Iris Detection
     */
    Mat detect(Mat frame, CascadeClassifier faceCascade, CascadeClassifier eyesCascade) {

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
            /*Log.d(getClass().getName() + "Face ", " X co-ordinate  is " + center.x + "Y co ordinate" + center.y);*/



            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame, face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);
            /*Log.d(getClass().getSimpleName(), "Height " + face.height + "Width " + face.width);*/


            List<Rect> listOfEyes=null;


            if (!isFirstPairOfIrisFound) {

                /*Detecting Eyes of the face*/
                MatOfRect eyes = new MatOfRect();
                eyesCascade.detectMultiScale(faceROI, eyes);
              listOfEyes = eyes.toList();
                Mat[] eyesROI = new Mat[2];

                /*Log.d(getClass().getSimpleName(),"face.x= "+face.x+"face.y = "+face.y);*/
                try {
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
                    Log.d("Detect" + " Eyes ", " X co-ordinate  is " + eyeCenter.x + "Y co ordinate" + eyeCenter.y);
                  */


                        /*Displaying boundary of the detected eye*/
                        Imgproc.rectangle(frame, eye, new Scalar(10, 0, 255));

                        /*Iris Detection via Blob Detection*/
                        Mat eyeROICanny = new Mat();
                        Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 3);

                        MatOfKeyPoint blobs = new MatOfKeyPoint();
                        simpleBlobDetector.detect(eyeROICanny, blobs);
                        Log.d(getClass().getSimpleName() + " Number of blobs ", blobs.toArray().length + "");

                        /*Finding Iris*/
                        KeyPoint[] blobsArray = blobs.toArray();
                        if (blobsArray.length != 0) {
                            Point blobCentre = blobsArray[0].pt;
                            blobCentre.x = blobCentre.x + eye.x;
                            blobCentre.y = blobCentre.y + eye.y;
                            Imgproc.circle(frame, blobCentre, 2, new Scalar(255, 0, 0), 4);
                            irisPoints.add(blobCentre);
                        }

                    }

                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "Error " + e.getMessage());
                }
            }



                /*sparseOpticalFlow Initiator*/
                if (!isFirstPairOfIrisFound && irisPoints.size()==2) {
                    isFirstPairOfIrisFound = true;

                }

                if (isFirstPairOfIrisFound) {
                    Mat irisPointsMat = new Mat(2, 2, CvType.CV_32F);
                    Log.d(getClass().getSimpleName(),"After finding iris , sending data IrisPoint");
                    Log.d(getClass().getSimpleName(),"Iris Point Size :"+irisPoints.size());
                    for (int m = 0; m < irisPointsMat.rows(); m++) {

                        Log.d(getClass().getSimpleName(),"Iris Point X :"+irisPoints.get(m).x + " iris point Y: "+irisPoints.get(m).y);
                        irisPointsMat.put(m, 0, irisPoints.get(m).x);
                        irisPointsMat.put(m, 1, irisPoints.get(m).y);

                    }
                    Log.d(getClass().getSimpleName(), "IrisPoints Size : " + irisPointsMat.size());
                    Mat predictedPoints=sparseOpticalFlowDetector.predictPoints(frameGray, irisPointsMat);
                    irisPoints.clear();
                    for(int m = 0;m<predictedPoints.rows();m++){
                       double xPoint =predictedPoints.get(m,0)[0];
                       double yPoint =predictedPoints.get(m,1)[0];
                       irisPoints.add(new Point(xPoint,yPoint));
                    }
                    Log.d(getClass().getSimpleName(),"Iris Point size after conversion "+irisPoints.size());
                    Imgproc.circle(frame,irisPoints.get(1),3,new Scalar(255,0,0));
                    Imgproc.circle(frame,irisPoints.get(0),3,new Scalar(255,0,0));

                } else {
                    /*If initially only one iris is detected*/
                    irisPoints.clear();
                }


            }
            return frame;
        }

}