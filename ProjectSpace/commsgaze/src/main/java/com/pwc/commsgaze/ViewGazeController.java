package com.pwc.commsgaze;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

public class ViewGazeController {

    private RecyclerView recyclerView;
    private static final String TAG = "ViewGazeController";

    ViewGazeController(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
        Log.d(TAG,"Recycler View count "+ recyclerView.getChildCount()+"");
    }


}
