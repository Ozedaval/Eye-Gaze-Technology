package com.pwc.eyegaze;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import com.pwc.eyegaze.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

binding.yesTextView.setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        scaleNormalOrUp((View) view.getParent(),motionEvent);
           return false;
    }

});


binding.noTextView.setOnTouchListener(new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        scaleNormalOrUp((View) view.getParent(),motionEvent);

        return false;
    }
});
    }
    public void scaleNormalOrUp(View v, MotionEvent motionEvent){   int motion=motionEvent.getAction();
        if(motion==MotionEvent.ACTION_DOWN||motion==MotionEvent.ACTION_BUTTON_PRESS||motion==MotionEvent.ACTION_DOWN||motion==MotionEvent.ACTION_BUTTON_PRESS||motion==MotionEvent.AXIS_PRESSURE){
            v.setScaleX(1.10f);
            v.setScaleY(1.10f);}
        else{
            v.setScaleX(1);
            v.setScaleY(1);

        }

    }
}
