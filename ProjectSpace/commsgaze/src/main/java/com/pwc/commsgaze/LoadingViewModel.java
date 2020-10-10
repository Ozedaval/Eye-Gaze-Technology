package com.pwc.commsgaze;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LoadingViewModel extends ViewModel {

    private MutableLiveData<Boolean> isFirstRun;
    private static final String TAG = "LoadingViewModel";
    /*Check on UI thread for shared preference before calling this*/
    LiveData<Boolean> getIsFirstRun() {
        if (isFirstRun == null) {
            isFirstRun = new MutableLiveData<Boolean>();
            isFirstRun.setValue(true);
            Log.d(TAG,"getIsFirstRunCalled");
        }
        return isFirstRun;
    }

    public void initialisationDone() {
        Log.d(TAG," InitialisationDone Called");
        isFirstRun.setValue(false);
    }
}
