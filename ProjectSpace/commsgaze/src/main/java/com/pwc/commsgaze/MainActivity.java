package com.pwc.commsgaze;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pwc.commsgaze.database.Content;

import com.pwc.commsgaze.databinding.ActivityMainBinding;
import com.pwc.commsgaze.detection.Approach;
import com.pwc.commsgaze.detection.Detector;
import com.pwc.commsgaze.detection.data.DetectionData;
import com.pwc.commsgaze.detection.data.SparseFlowDetectionData;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.pwc.commsgaze.customview.CircleView.MAX_ANGLE;
import static com.pwc.commsgaze.customview.CircleView.MIN_ANGLE;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {


    static{ System.loadLibrary( "opencv_java4" );}

    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;
    private static final String TAG = "MainActivity";
    private MainRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager gridLayoutManager;
    private final int RV_FIXED_DIMENSION = 3;
    private StorageViewModel storageViewModel;
    private  final int SELECTION_THRESHOLD = 15;
    private final int CLICK_INIT_THRESHOLD = 5;
    private final int CLICK_STAGE_DURATION=2375;
    private final int SELECTION_EFFECT_DURATION =200;




    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        final View view = binding.getRoot();
        hideSystemUI();
        setContentView(view);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);
        storageViewModel = new ViewModelProvider(this).get(StorageViewModel.class);


        recyclerViewAdapter = new MainRecyclerViewAdapter((int) getResources().getDimension(R.dimen.size_main_image));
        Log.d(TAG,"RV width "+  binding.mainRecyclerView.getWidth());

        gridLayoutManager = new GridLayoutManager(this, RV_FIXED_DIMENSION,GridLayoutManager.VERTICAL,false);

        binding.mainRecyclerView.setLayoutManager(gridLayoutManager);
        binding.mainRecyclerView.setAdapter(recyclerViewAdapter);
        mainViewModel.initialiseViewGazeHolders(RV_FIXED_DIMENSION,0);
        mainViewModel.initialiseDirectionMediator(SELECTION_THRESHOLD, CLICK_INIT_THRESHOLD);



        /*TODO check the user set default approach and use it -- most prolly use the stored data on the approach and send it to initialiseApproach() */
        initialiseApproach(Approach.OPENCV_SPARSE_FLOW);

        storageViewModel.getAllContents().observe(this, new Observer<List<Content>>() {
            @Override
            public void onChanged(List<Content> contents) {
                Log.d(TAG,"Changed "+ contents.toString());
                recyclerViewAdapter.setContents(contents);
                mainViewModel.initialiseViewGazeHolders(RV_FIXED_DIMENSION,contents.size());
            }
        });



        mainViewModel.getSelectedDataIndex().observe(this, new Observer<Integer>() {

            @Override
            public void onChanged(final Integer integer) {

                MainRecyclerViewAdapter.ViewHolder prevViewHolder = (MainRecyclerViewAdapter.ViewHolder) binding.mainRecyclerView.findViewHolderForAdapterPosition(mainViewModel.getPreviousSelectedViewHolderID());

                if(prevViewHolder!=null) {
                    prevViewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(prevViewHolder.itemView.getContext(),R.color.colorLightBlue));
                    prevViewHolder.itemView.animate().scaleX(1f).scaleY(1f).setDuration(SELECTION_EFFECT_DURATION).start();

                }
                binding.mainRecyclerView.smoothScrollToPosition(integer);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        MainRecyclerViewAdapter.ViewHolder selectedViewHolder = (MainRecyclerViewAdapter.ViewHolder) binding.mainRecyclerView.findViewHolderForAdapterPosition(integer);

                        if (selectedViewHolder!=null) {
                            selectedViewHolder.cardView.setCardBackgroundColor(ContextCompat.getColor(selectedViewHolder.itemView.getContext(),R.color.colorAccent));
                            selectedViewHolder.itemView.animate().scaleX(1.10f).scaleY(1.10f).setDuration(SELECTION_EFFECT_DURATION).start();
                        }
                    }
                },100);

                /*   Log.d(TAG," New integer "+ integer + " Previous Integer "+ mainViewModel.getPreviousSelectedViewHolderID());*/

            }
        });


        /*Using this observer pattern instead of using onCameraFrame(), just in case if other library or API is decided to be used for an approach*/
        mainViewModel.getIsDetected().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isDetected) {
                DetectionData detectionData = mainViewModel.getDetectionData();
                /*  Log.d(TAG,detectionData.toString());*/
                int faceRectVisibility = detectionData.getIsFaceDetected() ? VISIBLE : INVISIBLE;
                int leftEyeRectVisibility = detectionData.getIsLeftEyeDetected() ? VISIBLE : INVISIBLE;
                int rightEyeRectVisibility = detectionData.getIsRightEyeDetected() ? VISIBLE : INVISIBLE;


                binding.mainFaceRectangleView.setVisibility(faceRectVisibility);
                binding.mainEyeLeftRectangleView.setVisibility(leftEyeRectVisibility);
                binding.mainEyeRightRectangleView.setVisibility(rightEyeRectVisibility);

            }
        });

        mainViewModel.getNeedClick().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean needClick) {
                if(needClick){
                    if(mainViewModel.getSelectedDataIndex().getValue()!=null) {
                        final int selectedDataIndex = mainViewModel.getSelectedDataIndex().getValue();
                        Log.d(TAG, "Mock Click Effect");
                        final MainRecyclerViewAdapter.ViewHolder selectedViewHolder = (MainRecyclerViewAdapter.ViewHolder) binding.mainRecyclerView.findViewHolderForAdapterPosition(selectedDataIndex);
                            /*TODO animation effect and meanwhile check if it is neutral , if not cancel click */
                            mainViewModel.setPreviousClickedDataIndex(selectedDataIndex);
                            selectedViewHolder.circleView.setVisibility(VISIBLE);

                        ValueAnimator valueAnimator = ValueAnimator.ofInt(MIN_ANGLE,MAX_ANGLE);

                        final boolean[] interrupted = {false};
                        final int initSelectedDataIndex = mainViewModel.getSelectedDataIndex().getValue();
                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                /*Log.d(TAG,"Anim Frac "+ animation.getAnimatedValue());*/
                              selectedViewHolder.circleView.setAngle((int)animation.getAnimatedValue());
                              if(initSelectedDataIndex!= mainViewModel.getSelectedDataIndex().getValue()){
                                  interrupted[0] = true;
                                  animation.cancel();
                              }
                            }
                        });

                        valueAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if (!interrupted[0]) {
                                    selectedViewHolder.itemView.callOnClick();

                                    mainViewModel.updateSentence(recyclerViewAdapter.getContent(selectedDataIndex));

                                }
                                selectedViewHolder.circleView.setVisibility(INVISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                                selectedViewHolder.circleView.setVisibility(INVISIBLE);

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        valueAnimator.setTarget(selectedViewHolder.circleView);

                        valueAnimator.setDuration(CLICK_STAGE_DURATION);
                        valueAnimator.start();
                        }
                }
            }
        });

        mainViewModel.getClickedContents().observe(this, new Observer<ArrayList<Content>>() {
            @Override
            public void onChanged(ArrayList<Content> contents) {
                StringBuilder stringBuilder = new StringBuilder();
                for (Content content:contents) {
                    stringBuilder.append(content.getWord());
                    stringBuilder.append(" ");
                }
                Log.d(TAG, "EditText being edited to " + stringBuilder.toString());
                binding.mainEditText.setText(stringBuilder.toString());
            }

        });

        mainViewModel.getGaugedDirection().observe(this, new Observer<Direction>() {
            @Override
            public void onChanged(Direction direction) {
                /*      Log.d(TAG,"Gauged Direction " + direction);*/
                mainViewModel.updateViewGazeController(direction);
            }
        });



        /*    TODO This is primarily for testing the interaction between UI and Gaze. Remove or Comment this when not in use*/
        Button[] testButtons = new Button[]{binding.mainTopButton, binding.mainLeftButton, binding.mainNeutralButton, binding.mainRightButton, binding.mainBottomButton};
        for (Button button : testButtons) {
            button.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String buttonText = (String) ((Button) v).getText();
                    Direction decipheredDirection = customTestButtonParser(buttonText);
                    Log.d(TAG, buttonText + "Button is pressed. Deciphered as " + decipheredDirection);
                    mainViewModel.updateViewGazeController(decipheredDirection);
                }
            });



        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG," onResume called ");
        /*configs go away when app activity is re-opened*/
        hideSystemUI();
        binding.openCVCameraView.enableView();
    }


    /*Hides System UI
     * https://developer.android.com/training/system-ui/immersive.html*/
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
    }


    @Override
    public void onCameraViewStopped() {

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final Detector detector = mainViewModel.getDetector();
        if(detector.getApproach().equals(Approach.OPENCV_SPARSE_FLOW)){
            /* Log.d(TAG,"On camera Update approach "+ detector.getApproach().toString());*/
           
            ((SparseFlowDetectionData) mainViewModel.getDetectionData()).setFrame(inputFrame.rgba());
            Mat updatedFrame = ((SparseFlowDetectionData) detector.updateDetector(mainViewModel.getDetectionData())).getFrame();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mainViewModel.updateDirectionMediator(detector.getDirection());
                    mainViewModel.updateDetectionStatus();

                }
            });
            return  updatedFrame;
        }
        return inputFrame.rgba();
    }


    private void initialiseApproach(Approach approach){
        if(approach.equals(Approach.OPENCV_SPARSE_FLOW) ){
            /*TODO We need to only activate this if the user has set for an approach which uses opencv -- most prolly use the stored data on the approach to check  first */
            Log.d(TAG,"opencv camera initialisation");
            binding.openCVCameraView.setVisibility(VISIBLE);
            binding.openCVCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
            binding.openCVCameraView.setCameraPermissionGranted();
            binding.openCVCameraView.disableFpsMeter();
            binding.openCVCameraView.setCvCameraViewListener(this);
            binding.openCVCameraView.bringToFront();


            CascadeClassifier faceCascade = new CascadeClassifier();
            CascadeClassifier eyesCascade = new CascadeClassifier();
            faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
            eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

            DetectionData detectionData = new SparseFlowDetectionData(faceCascade,eyesCascade);
            mainViewModel.setDetectionData(detectionData);

            mainViewModel.createDetector(Approach.OPENCV_SPARSE_FLOW,detectionData);
        }

    }


    /*    TODO This is primarily for testing the interaction between UI and Gaze. Remove or Comment this when not in use*/
    Direction customTestButtonParser(String buttonText){
        Direction[] directions = Direction.values();
        for(Direction direction:directions){
            if(buttonText.equalsIgnoreCase(direction.toString()))
                return direction;
        }
        return Direction.UNKNOWN;
    }


    public void enterClicked(View view) {
        /*TODO*/
    }





}