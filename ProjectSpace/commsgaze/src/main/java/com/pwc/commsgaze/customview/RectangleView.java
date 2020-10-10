package com.pwc.commsgaze.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.pwc.commsgaze.R;

import static com.pwc.commsgaze.customview.ViewUtil.parseInt;

public class RectangleView extends View {
    private final String TAG = "RectangleView";
    private final String ATTR_NAMESPACE= "http://schemas.android.com/apk/res/android";

    private int width;
    private int height;
    private int color;
    private int strokeWidth;


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG,"W"+w+"h"+h);
        width = w;
        height = h;
    }

    /*Regex from https://stackoverflow.com/a/10372905/11200630
     * Used a similar  Declaration and  Retrieval of customAttributes from https://stackoverflow.com/questions/18681956/setting-color-of-a-paint-object-in-custom-view*/
    public RectangleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        height = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_height"));
        width = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_width"));
        TypedArray customAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RectangleView,
                0, 0);

        strokeWidth = customAttributes.getDimensionPixelSize(R.styleable.RectangleView_line_width, 1);
        color = (customAttributes.getColor(R.styleable.RectangleView_color,1));


        Log.d(TAG,"Color"+ color);
        customAttributes.recycle();

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();

        float leftX = 0;
        float topY = 0;

        float rightX = width;
        float bottomY = height;


        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(leftX, topY, rightX, bottomY, paint);
    }
}
