package com.pwc.commsgaze;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pwc.commsgaze.databinding.ActivityMainBinding;
import com.pwc.commsgaze.detection.SparseFlowDetector;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import java.util.Arrays;
import java.util.Set;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final int PERMISSION_REQUEST_CODE = 1;
    private MainViewModel mainViewModel;
    private Boolean isFirstRun;
    private ActivityMainBinding binding;
    private FragmentManager fragmentManager;
    private static final String TAG = "MainActivity";
    private MainRecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.LayoutManager gridLayoutManager;

    private final String[] TEMP_DATA = new String[]{"Hello","Hi","Bye","Eat","Sleep","Sad","Run"};
    static{ System.loadLibrary( "opencv_java4" );}
    private CascadeClassifier faceCascade;
    private CascadeClassifier eyesCascade;
    public SparseFlowDetector detect;
    int runFrame;
    RecyclerView.SmoothScroller smoothScroller;
    /*TODO remove this once Room Database is connected with RecyclerView. The below data is for just testing the recyclerView*/



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        View view = binding.getRoot();
        setContentView(view);

        hideSystemUI();
        runFrame=0;
        this.smoothScroller = new LinearSmoothScroller(this) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            }
        }
        isFirstRun = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.main_first_run_preference_key), true);

        if (isFirstRun) {
            fragmentManager = getSupportFragmentManager();
            mainViewModel = new ViewModelProvider(this)
                    .get(MainViewModel.class);

            mainViewModel.getIsFirstRun().observe(this, new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    if (!aBoolean) {
                        Log.d(TAG," OnChangedLiveData"+"Changed to "+aBoolean);
                        Snackbar.make(binding.mainCoordinatorLayout,
                                getString(R.string.main_initialisation_done_msg),
                                Snackbar.LENGTH_LONG).show();
                        Log.d(TAG, "Initialisation done");
                        SharedPreferences.Editor sharedPreferencesEditor = getSharedPreferences(getString(R.string.main_preference_key), Context.MODE_PRIVATE).edit();
                        sharedPreferencesEditor.putBoolean(getString(R.string.main_first_run_preference_key), false);
                        sharedPreferencesEditor.apply();
                        Log.d(TAG, "Files present " + Arrays.toString(fileList()));
                        isFirstRun= false;
                        mainViewModel.getIsFirstRun().removeObserver(this);
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.init_fragment_tag));
                        if(fragment != null){
                            fragmentManager.beginTransaction().remove(fragment).commit();
                        }
                    }
                }
            });

            if (mainViewModel.getIsFirstRun().getValue() != null && mainViewModel.getIsFirstRun().getValue()) {
                Log.d(TAG ," onResume "+"ViewModel LiveData is a" + mainViewModel.getIsFirstRun().getValue());
                Log.d(TAG , "on Resume Called");

                DialogFragment initialisationFragment = new InitialisationFragment();
                initialisationFragment.setCancelable(false);
                initialisationFragment.show(fragmentManager, getString(R.string.init_fragment_tag));
            }
        }
        Log.d(TAG ,  "isFirstRun is "+isFirstRun+"");

        recyclerViewAdapter = new MainRecyclerViewAdapter(TEMP_DATA,this);
        gridLayoutManager = new GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false);
        binding.recyclerViewMain.setLayoutManager(gridLayoutManager);
        binding.recyclerViewMain.setAdapter(recyclerViewAdapter);
        binding.recyclerViewMain.scrollToPosition(Integer.MAX_VALUE / 2);

        recyclerViewAdapter.getAllBoundedViewHolders().observe(this, new Observer<Set<MainRecyclerViewAdapter.ViewHolder>>() {
            @Override
            public void onChanged(Set<MainRecyclerViewAdapter.ViewHolder> viewHolders) {
                Log.d(TAG,"Total Visible View Holder " + viewHolders.toString());
            }
        });

        binding.openCVCameraView.setVisibility(VISIBLE);
        binding.openCVCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        binding.openCVCameraView.setCameraPermissionGranted();
        binding.openCVCameraView.disableFpsMeter();
        binding.openCVCameraView.setCvCameraViewListener(this);

        faceCascade = new CascadeClassifier();
        eyesCascade = new CascadeClassifier();
        binding.openCVCameraView.bringToFront();
        /*Log.d(TAG, Arrays.toString(fileList()));
        Log.d(TAG, getFileStreamPath("eyeModel.xml").getAbsolutePath());
        Log.d(TAG, getFileStreamPath("faceModel.xml").getAbsolutePath());*/
        faceCascade.load(getFileStreamPath("faceModel.xml").getAbsolutePath());
        eyesCascade.load(getFileStreamPath("eyeModel.xml").getAbsolutePath());

        detect=new SparseFlowDetector();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                finish();
            }
        }
        Log.d(getClass().getName() + "isFirstRun is ",  isFirstRun+"");
    }

    @Override
    protected void onResume() {
        super.onResume();

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
        binding.recyclerViewMain.post(new Runnable() {
            @Override
            public void run() {

                // to prevent from cases where selecting elements too fast
                // number can be changed or removed
                if(runFrame %8==0){

                    if(detect.getDirection()==Direction.LEFT){

                        recyclerViewAdapter.selectedItem-=1;
                        if(recyclerViewAdapter.selectedItem<0) {
                            smoothScroller.setTargetPosition(TEMP_DATA.length-1);
                            gridLayoutManager.startSmoothScroll(smoothScroller);
                            recyclerViewAdapter.selectedItem = TEMP_DATA.length - 1;
                        }
                        recyclerViewAdapter.selectionEffect(recyclerViewAdapter.selectedItem);
                    }
                    else if(detect.getDirection()==Direction.RIGHT){
                        recyclerViewAdapter.selectedItem+=1;
                        if(recyclerViewAdapter.selectedItem==TEMP_DATA.length){
                            recyclerViewAdapter.selectedItem=0;
                            smoothScroller.setTargetPosition(0);
                            gridLayoutManager.startSmoothScroll(smoothScroller);
                        }
                        recyclerViewAdapter.selectionEffect(recyclerViewAdapter.selectedItem);


                    }
                }
                runFrame++;
            }
        });
        return detect.detect(inputFrame.rgba(),faceCascade,eyesCascade);
    }


}