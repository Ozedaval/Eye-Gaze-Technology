package com.pwc.explore.eyegaze.opencvblob;



import android.util.Log;
import com.pwc.explore.DetectionListener;
import com.pwc.explore.Direction;
import org.opencv.core.Algorithm;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.video.DenseOpticalFlow;
import org.opencv.video.FarnebackOpticalFlow;
import org.opencv.video.SparseOpticalFlow;
import org.opencv.video.SparsePyrLKOpticalFlow;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static com.pwc.explore.Direction.BOTTOM;
import static com.pwc.explore.Direction.BOTTOM_LEFT;
import static com.pwc.explore.Direction.BOTTOM_RIGHT;
import static com.pwc.explore.Direction.LEFT;
import static com.pwc.explore.Direction.NEUTRAL;
import static com.pwc.explore.Direction.RIGHT;
import static com.pwc.explore.Direction.TOP;
import static com.pwc.explore.Direction.TOP_LEFT;
import static com.pwc.explore.Direction.TOP_RIGHT;


public class Detect {


    private DetectionListener dl;
    /*private DetectionSmoother[] dsEyes;*/
    private  DetectionSmoother dsFace;
    private SimpleBlobDetector simpleBlobDetector;
    private static final String TAG="Detect";

    
    Detect(DetectionListener dl){
        this.dl=dl;
        /*dsEyes=new DetectionSmoother[]{new DetectionSmoother(0.2f),new DetectionSmoother(0.2f)};*/
        dsFace=new DetectionSmoother(0.2f);
        simpleBlobDetector=SimpleBlobDetector.create();

    }


    private void sendDetection(Direction direction){
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
            /*Log.d(TAG + "Face ", " X co-ordinate  is " + center.x + "Y co ordinate" + center.y);*/

            /*Updates face for DetectionSmoother*/
            face=dsFace.updateCoord(face);

            /*Displaying the boundary of the detected face*/
            Imgproc.rectangle(frame,face, new Scalar(0, 250, 0));
            Mat faceROI = frameGray.submat(face);
            Log.d(TAG,"Height "+ face.height+"Width "+face.width );


            /*Detecting Eyes of the face*/
            MatOfRect eyes = new MatOfRect();
            eyesCascade.detectMultiScale(faceROI, eyes);
            List<Rect> listOfEyes = eyes.toList();
            Mat[] eyesROI = new Mat[2];
            Set<Integer> blobID=new HashSet<>();


            Log.d(TAG,"face.x= "+face.x+"face.y = "+face.y);
            try {
                for (int i = 0; i < listOfEyes.size(); i++) { //Just get the first 2 detected eyes
                    Rect eye = listOfEyes.get(i);

                    /*Making changes so to get x & y co-ordinates with respective to the frame*/
                    eye.x=face.x+eye.x;
                    eye.y=face.y+eye.y;

                    /*Updates Eyes for DetectionSmoother*/
                    /*eye=dsEyes[i].updateCoord(eye);*/

                    /*Cropping an eye Image*/
                    eyesROI[i] = frame.submat(eye);


                    /*Point eyeCenter = new Point(face.x + eye.x + eye.width / 2f, face.y + eye.y + eye.height / 2f);
                    int radiusEye = (int) Math.round((eye.width + eye.height) * 0.25);
                    Log.d(TAG, " EYE X co-ordinate  is " + eyeCenter.x + "Y co ordinate" + eyeCenter.y);
                  */


                    /*Displaying boundary of the detected eye*/
                    Imgproc.rectangle(frame,eye,new Scalar(10, 0, 255));

                    /*Iris Detection via Blob Detection*/
                    Mat eyeROICanny= new Mat();
                    Imgproc.Canny(eyesROI[i], eyeROICanny, 50, 50 * 3);

                    MatOfKeyPoint blobs= new MatOfKeyPoint();
                    simpleBlobDetector.detect(eyeROICanny,blobs);
                    Log.d(TAG +" Number of blobs ",blobs.toArray().length+"");

                    /*Finding Iris*/
                    KeyPoint[] blobsArray=blobs.toArray();
                    if(blobsArray.length!=0){
                        Point blobCentre= blobsArray[0].pt;
                        blobCentre.x=blobCentre.x+eye.x;
                        blobCentre.y=blobCentre.y+eye.y;
                        Imgproc.circle(frame,blobCentre,2,new Scalar(255,0,0),4);
                        blobID.add(i);
                        Log.d(TAG + "eye gaze point " , blobCentre.x + " "+  blobCentre.y);

                    }

                }
            }catch (Exception e) {
                Log.e(TAG,"Error "+e.getMessage());
            }
        }
        return frame; }
}