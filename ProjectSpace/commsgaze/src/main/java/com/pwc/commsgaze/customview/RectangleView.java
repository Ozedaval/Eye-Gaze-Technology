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

public class RectangleView extends View {
    private final String TAG = "RectangleView";
    private final String ATTR_NAMESPACE= "http://schemas.android.com/apk/res/android";


    private int margin;
    private int width;
    private int height;
    private int marginLeft;
    private int marginRight;
    private int marginTop;
    private int marginBottom;
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
        margin = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_margin"));
        marginLeft = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_marginLeft"));
        marginRight = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_marginRight"));
        marginBottom =  parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_marginBottom"));
        marginTop = parseInt(attrs.getAttributeValue(ATTR_NAMESPACE,"layout_marginTop"));
        Log.d(TAG,"Height" + height+ "Margin"+ margin);

        TypedArray customAttributes = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.RectangleView,
                0, 0);

        this.strokeWidth = (customAttributes.getColor(R.styleable.RectangleView_lineWidth,0xff000000));
        this.color = (customAttributes.getColor(R.styleable.RectangleView_color,1));


        Log.d(TAG,"Color"+this.color);
        customAttributes.recycle();

    }


    int parseInt(String floatString){
        if(floatString!= null && !floatString.isEmpty()){
        floatString = floatString.replaceAll("[^\\d.]", "");
        return (int)(Float.parseFloat(floatString));}
        return 0;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();

        float leftX = marginLeft==0? margin:marginLeft;
        float topY =  marginTop==0? margin:marginTop;

        float rightX = marginRight==0? width-margin:width-marginRight;
        float bottomY = marginBottom==0?height-margin:height-marginBottom;


        paint.setStrokeWidth(strokeWidth);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(leftX, topY, rightX, bottomY, paint);
    }
}
