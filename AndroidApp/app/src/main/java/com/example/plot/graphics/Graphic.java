package com.example.plot.graphics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;

import com.example.plot.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Graphic extends SurfaceView implements SurfaceHolder.Callback{
    static final char UPDATE_NEED = 21, UPDATED = 22;
    final SurfaceHolder holder = getHolder();
    public DragZoom view;
    final Path path = new Path();
    final Paint paintGrid = new Paint();
    public final DashPathEffect dash = new DashPathEffect(new float[] {10, 20},0);
    public Paint paintAxis, paintText, paintLabel;
    boolean isInit = false;
    Semaphore semaphore;
//    float[] markerX = new float[10], markerY = new float[10];
    int colorOnPrimary;
    int colorText;
    TypedValue typedValue = new TypedValue();
    char dataUpdated = UPDATE_NEED;
    boolean threadCreated;
    boolean surfaceCreated = false;

    static class Settings {
        boolean checkGridEnable;
        boolean switchGridDash;
        boolean switchAutoscalingPlot;
        boolean switchAntiAliasing;
        boolean checkLogX;
        boolean checkLogY;
        String xLabel = "X label", yLabel = "Y label";
    }
    Settings a = new Settings();

    public String id;

    public Graphic(Context context){
        super(context);
        init(context);
    }

    public Graphic(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Graphic(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public Graphic(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context){
//        isInit = false;
        context.getTheme().resolveAttribute(R.attr.colorOnPrimary,typedValue,true);
        colorOnPrimary = typedValue.data;
        context.getTheme().resolveAttribute(android.R.attr.textColor,typedValue,true);
        colorText = typedValue.data;

        holder.addCallback(this);
        setFocusable(true);
        paintGrid.setAntiAlias(false);
        paintGrid.setColor(Color.GRAY);
        paintGrid.setStyle(Paint.Style.STROKE);
        paintGrid.setStrokeWidth(10);
        paintAxis = new Paint(paintGrid);
        paintGrid.setPathEffect(dash);
        paintGrid.setStrokeWidth(3);
        paintGrid.setTextSize(40);
        paintText = new Paint(paintGrid);
        paintText.setSubpixelText(true);
        paintText.setStyle(Paint.Style.FILL);
        paintLabel = new Paint(paintText);
        paintLabel.setSubpixelText(true);
        paintLabel.setTextSize(50);
        paintLabel.setColor(colorText);
        paintLabel.setTypeface(Typeface.create(Typeface.SERIF,Typeface.ITALIC));
        paintGrid.setAlpha(0x88);
        paintGrid.setStrokeWidth(2f);

//        Arrays.fill(markerX,Float.NaN);
//        Arrays.fill(markerY,Float.NaN);
    }

    @SuppressLint("SuspiciousIndentation")
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
//        while (!isInit);
        view.width = getWidth();
        view.height = getHeight();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        view.dpi = displayMetrics.densityDpi;
        Display display = getDisplay();
        view.fps = display.getRefreshRate();
        isInit = true;
        if(threadCreated)
        update();
        threadCreated = true;
        surfaceCreated = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public void draw(Canvas canvas) {
        if (canvas!=null) {
            super.draw(canvas);
            canvas.drawColor(colorOnPrimary);
        }
    }

    void update(){
        if(semaphore.availablePermits()==0)
            semaphore.release();
    }

    void setData(Graphic graphic){
//        while(isInit);
        if (graphic!=null)
            view = graphic.view;
        if (view==null)
            view = new DragZoom();
        new Touch(this);
        if (graphic!=null) {
            view.leftX = graphic.view.leftX;
            view.rightX = graphic.view.rightX;
            view.topY = graphic.view.topY;
            view.bottomY = graphic.view.bottomY;
            view.initRequired = graphic.view.initRequired;
            a = graphic.a;
        }
    }

}
