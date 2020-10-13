package com.pwc.commsgaze.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;


import com.pwc.commsgaze.R;

import static com.pwc.commsgaze.customview.ViewUtil.parseInt;


public class CircleView extends View {


    private float radius;
    private int strokeWidth;
    private int color;
    private  RectF boundingOval;
    private Paint paint;
    private int angle;
    private int height;
    private int width;
    private float center;
    public static final int MIN_ANGLE = 0;
    public static final int MAX_ANGLE = 360;

    private final String TAG = "CircleView";
    private final String ATTR_NAMESPACE= "http://schemas.android.com/apk/res/android";


    /*Regex from https://stackoverflow.com/a/10372905/11200630
     * Used a similar  Declaration and  Retrieval of customAttributes from https://stackoverflow.com/questions/18681956/setting-color-of-a-paint-object-in-custom-view*/
    public CircleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        height = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_height"));
        width = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_width"));
        TypedArray customAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CircleView,
                0, 0);

        strokeWidth = customAttributes.getDimensionPixelSize(R.styleable.CircleView_line_width, 1);
        color = (customAttributes.getColor(R.styleable.CircleView_color,1));
        angle = (customAttributes.getColor(R.styleable.CircleView_angle,0));
        customAttributes.recycle();

        initialise();
        paint = new Paint();
    }

    void initialise(){
        center = Math.min(height,width)/2f;
        float minDim = Math.min(height,width);
        float suitableDim = minDim*0.9f;
        Log.d(TAG,"onSizeChanged "+" Height "+ height +" Width "+width + " suitableDim "+ suitableDim);
        radius = (suitableDim/2f);
        float suitableStartPoint = minDim/2f- radius + (minDim*0.1f);
        boundingOval =  new RectF(suitableStartPoint,suitableStartPoint,suitableDim,suitableDim);
        Log.d(TAG,"SuitableStartPoint "+ suitableStartPoint);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
        initialise();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.rotate(-90,center,center);
        canvas.drawArc(boundingOval,0,angle,true,paint);

    }

    public void setAngle(int angle) {
        this.angle = angle;
        invalidate();

    }

}
