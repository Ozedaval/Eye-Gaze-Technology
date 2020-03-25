package com.pwc.explore.eyegaze;
import org.opencv.objdetect.Objdetect;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.pwc.explore.databinding.ActivityEventUiBinding;



public class EyeGazeEventActivity extends AppCompatActivity {
/*    TODO MAO(1.Temporarily do actually manipulation of eyegaze here.
               2.Add UI changes accordingly
               3. Use Cursor Class appropriately) */

    private ActivityEventUiBinding binding;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityEventUiBinding.inflate(getLayoutInflater());
        View view =binding.getRoot();
        setContentView(view);

    }
}
