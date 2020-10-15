package com.pwc.commsgaze;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoadingViewModel extends ViewModel {

    private MutableLiveData<Boolean> isFirstRun;
    private MutableLiveData<Boolean> cameraPermissionGranted;

    private static final String TAG = "LoadingViewModel";
    /*Check on UI thread for shared preference before calling this*/
    LiveData<Boolean> getIsFirstRun() {
        if (isFirstRun == null) {
            isFirstRun = new MutableLiveData<>(true);
            Log.d(TAG,"getIsFirstRunCalled");
        }
        return isFirstRun;
    }
    LiveData<Boolean> getCameraPermissionGranted(){
        if(cameraPermissionGranted==null){
            cameraPermissionGranted = new MutableLiveData<>(false);
        }
        return  cameraPermissionGranted;
    }

    void cameraPermissionGranted(){
        cameraPermissionGranted.setValue(true);
    }

    public void initialisationDone() {
        Log.d(TAG," InitialisationDone Called");
        isFirstRun.setValue(false);
    }
}
