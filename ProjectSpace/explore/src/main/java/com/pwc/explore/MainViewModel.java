package com.pwc.explore;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/*Holds details for MainActivity in a Activity life-conscious way */
public class MainViewModel extends ViewModel {

    private MutableLiveData<Boolean> isFirstRun;
    private static final String TAG="MainViewModel";


    /*Check on UI thread for shared preference before calling this*/
    LiveData<Boolean> getIsFirstRun() {
        if (isFirstRun == null) {
            isFirstRun= new MutableLiveData<Boolean>();
            isFirstRun.setValue(true);
            Log.d(TAG,"getIsFirstRunCalled");
        }
        return isFirstRun;
    }

    void initialisationDone() {
        Log.d(TAG," initialisationDone Called");
        isFirstRun.setValue(false);
    }
}