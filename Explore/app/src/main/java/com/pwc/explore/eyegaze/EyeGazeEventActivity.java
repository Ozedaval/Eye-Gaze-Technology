package com.pwc.explore.eyegaze;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.pwc.explore.databinding.ActivityEventUiBinding;
import org.opencv.objdetect.CascadeClassifier;


public class EyeGazeEventActivity extends AppCompatActivity {
/*    TODO  (1.Temporarily do actually manipulation of eyegaze here.
               2.Add UI changes accordingly
               3. Use Cursor Class appropriately) */

    private ActivityEventUiBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEventUiBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);


//TODO 2(After TODO 1,Need to pass eye model & face XML string path to cascadeClassifier)
        CascadeClassifier faceCascade = new CascadeClassifier();
        CascadeClassifier eyeCascade= new CascadeClassifier();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String path=getApplicationInfo().dataDir;
            Log.d("Path name", path) ;
        }
    }
}

