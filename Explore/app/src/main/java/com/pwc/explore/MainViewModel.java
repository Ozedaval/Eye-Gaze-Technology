package com.pwc.explore;


import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Boolean> isFirstRun;

    /*Check on UI thread for shared preference before calling this
     * TODO: Use an interface & implement a listener so to check with sharedpreference on UI thread*/
    LiveData<Boolean> getIsFirstRun() {
        if (isFirstRun == null) {
            isFirstRun= new MutableLiveData<Boolean>();
            isFirstRun.setValue(true);
            Log.d(getClass().getSimpleName()+" getIsFirstRun","Called");
        }
        return isFirstRun;
    }

    void initialisationDone() {
        Log.d(getClass().getSimpleName()+" InitialisationDone","Called");
        isFirstRun.setValue(false);
    }
}
