package com.pwc.commsgaze.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

public class RectangleView extends View {
    private final String TAG = "RectangleView";
    private final String ATTR_NAMESPACE= "http://schemas.android.com/apk/res/android";

    private int margin;
    private int width;
    private int height;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG,"W"+w+"h"+h);
        width = w;
        height = h;
    }

    /*Regex from https://stackoverflow.com/a/10372905/11200630*/
    public RectangleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        String marginString = attrs.getAttributeValue(ATTR_NAMESPACE,"layout_margin");
        marginString = marginString.replaceAll("[^\\d.]", "");
        this.margin = (int)(Float.parseFloat(marginString));
        Log.d(TAG,"Height" + height+ "Margin"+ margin);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        float leftX = margin;
        float topY =  margin;
        float rightX =  width-margin;
        float bottomY =  height-margin;
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);

        canvas.drawRect(leftX, topY, rightX, bottomY, paint);
    }
}
