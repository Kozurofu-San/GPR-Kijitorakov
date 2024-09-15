package com.example.plot.graphics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GestureDetectorCompat;

import com.example.plot.R;
import com.example.plot.dsp.Complex;
import com.example.plot.dsp.Numeric;
import com.example.plot.graphics.Colormap;
import com.example.plot.graphics.Plot;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Waterfall extends Graphic {
    DragZoom v = null;
    static private final char FLOW_MODE = 11, MATRIX_MODE = 12;

    static class Data {
        Bitmap bitmap;
        float[] valuesX;
        float[] valuesY;
        float[] chartX, chartY, interpX, interpY;
        float[][] chartXY, interpZ;
        int[] chartColor, tempVector;
        private final Matrix matrix = new Matrix();
        Paint paint = new Paint();
        char mode = 0;
    }
    Data d = new Data();

    static class Settings{
        char nColormap;
        boolean switchAutoscalingWaterfall;
        boolean switchHardwareInterpolation;
    }
    Settings s = new Settings();

    public Waterfall(Context context) {
        super(context);
    }

    public Waterfall(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Waterfall(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Waterfall(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressWarnings("unused")
    public void setX(@NonNull float[] X) {
        if (d.valuesX.length == X.length) {
            System.arraycopy(X, 0, d.valuesX, 0, X.length);
        }
    }

    @SuppressWarnings("unused")
    void setY(@NonNull float[] Y) {
        if (d.valuesY.length == Y.length) {
            System.arraycopy(Y, 0, d.valuesY, 0, Y.length);
        }
    }


    @SuppressWarnings("unused")
    public void setChart(@NonNull float[] vector) {
        if (d.chartY.length==vector.length)
            System.arraycopy(vector, 0, d.chartY, 0, vector.length);
        else{
            d.chartY = vector.clone();
            float minX = d.chartX[0];
            float maxX = d.chartX[d.chartX.length-1];
            d.chartX = new float[vector.length];
            Numeric.linspace(minX, maxX, d.chartX);
        }
        dataUpdated = UPDATE_NEED;
    }

    @SuppressWarnings("unused")
    public void setChart(@NonNull float[][] matrix) {
        for (int x = 0; x < matrix.length; x++)
            for (int y = 0; y < matrix[0].length; y++)
                d.chartXY[x][ y] = matrix[x][y];
        dataUpdated = UPDATE_NEED;
    }

    @SuppressWarnings("unused")
    public void initChart(float[] arrayY, float[] arrayX,int nVertical) {
        while (!surfaceCreated);
        if (arrayY.length != arrayX.length)
            Log.e("WF init", "Length mismatch");
        d.chartY = arrayY.clone();
        d.chartX = arrayX.clone();
        d.interpX = new float[1000];
        d.interpY = new float[d.interpX.length];
        d.chartColor = new int[d.interpX.length];
        d.mode = FLOW_MODE;
        if (d.bitmap == null)
            d.bitmap = Bitmap.createBitmap(d.interpY.length, nVertical, Bitmap.Config.ARGB_8888);
        d.matrix.reset();
        d.matrix.setScale(view.width / d.bitmap.getWidth(), view.height / d.bitmap.getHeight());
        d.tempVector = new int[d.bitmap.getWidth() * (d.bitmap.getHeight() - 1)];
        d.paint.setDither(false);
        d.paint.setAntiAlias(false);
        d.paint.setFilterBitmap(false);
        isInit = true;
    }

    public void initChart(float[][] matrix){
        while (!surfaceCreated);
        d.chartXY = matrix.clone();
        d.chartY = new float[matrix.length];
        for (int i=0; i<d.chartY.length; i++)
            d.chartY[i] = i;
        d.chartX = new float[matrix[0].length];
        for (int i=0; i<d.chartX.length; i++)
            d.chartX[i] = i;
        for (int x = 0; x < matrix.length; x++)
            for (int y = 0; y < matrix[0].length; y++)
                d.chartXY[x][ y] = matrix[x][y];
        d.interpX = new float[300];
        for (int i=0; i<d.interpX.length; i++)
            d.interpX[i] = i-2;
        d.interpY = d.interpX.clone();
        d.interpZ = new float[d.interpX.length][d.interpX.length];
        d.chartColor = new int[d.interpX.length*d.interpY.length];
        d.mode = MATRIX_MODE;
        if (d.bitmap==null)
            d.bitmap = Bitmap.createBitmap(d.interpZ.length,d.interpZ[0].length,Bitmap.Config.ARGB_8888);
        this.d.matrix.reset();
        this.d.matrix.setScale(view.width/d.bitmap.getWidth(),view.height/d.bitmap.getHeight());
        d.paint.setDither(false);
        d.paint.setAntiAlias(false);
        d.paint.setFilterBitmap(false);
        isInit = true;
    }

    @SuppressWarnings("unused")
    public void label(String xLabel, String yLabel){
        a.xLabel = xLabel;
        a.yLabel = yLabel;
    }

    private float[] arrayX, arrayY;
    @SuppressWarnings("FieldCanBeLocal")
    private float stepValueX, stepValueY;
    private int n;
    private float maxY, minY, maxX, minX;
    private float textSize,value,pixel;

    @Override
    public void draw(Canvas canvas) {
        if (canvas==null | d.interpY==null)
            return;
        super.draw(canvas);
        if (isInEditMode()) {
            view.height = getHeight();  // Vertical
            view.width = getWidth();    // Horizontal
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_waterfall);
            assert drawable != null;
            DrawableCompat.setTint(drawable,colorText);
            d.bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            d.bitmap.eraseColor(Color.BLACK);
            Canvas c = new Canvas(d.bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(c);
            d.matrix.reset();
//            d.matrix.setScale((float) this.getWidth() / d.bitmap.getWidth(), (float) this.getHeight() / d.bitmap.getHeight());
            a.checkGridEnable = true;
            canvas.drawBitmap(d.bitmap, d.matrix, d.paint);
        }

        if (d.mode == 0)
            Log.e("Waterfall", "No data");
        if (d.mode == FLOW_MODE) {
            float temp;
            if (view.topY > view.bottomY) {
                temp = view.bottomY;
                view.bottomY = view.topY;
                view.topY = temp;
            }
            if (view.initRequired == DragZoom.INIT_REQUIRE) {
                if (v != null) {
                    view.leftX = view.leftInitX = v.leftX;
                    view.rightX = view.rightInitX = v.rightX;
                } else {
                    view.leftX = view.leftInitX = Numeric.min(d.chartX, true);
                    view.rightX = view.rightInitX = Numeric.max(d.chartX, true);
                }
                view.topY = view.topInitY = 0;
                    view.bottomY = view.bottomInitY = d.bitmap.getHeight();
                view.initRequired = DragZoom.NO_INIT;
            }
            d.matrix.reset();
            float dx = view.width / d.bitmap.getWidth() * (view.leftInitX - view.rightInitX) / (view.leftX - view.rightX);
            float dy = view.height / d.bitmap.getHeight() * (view.bottomInitY - view.topInitY) / (view.bottomY - view.topY);
            if (Float.isNaN(dx)) {
                dy = 0;
                dx = 0;
            }
            d.matrix.setScale(dx, dy);
            dx = view.valueToPixelX(view.leftInitX);
            dy = view.valueToPixelY(view.topInitY);
            if (Float.isNaN(dx)) {
                dy = 0;
                dx = 0;
            }
            d.matrix.postTranslate(dx, dy);

            if (dataUpdated == UPDATE_NEED) {

            if (s.switchAutoscalingWaterfall) {
                Numeric.linspace(d.chartX[0], d.chartX[d.chartX.length - 1], d.interpX);
                Numeric.interpolate(d.chartY, d.chartX, d.interpY, d.interpX, Numeric.TYPE_LINEAR);
                Colormap.arrayToInt(d.interpY, d.chartColor, s.nColormap,
                        Numeric.min(d.chartY, true), Numeric.max(d.chartY, true));
            } else if (v != null) {
                Numeric.linspace(v.leftX, v.rightX, d.interpX);
                Numeric.interpolate(d.chartY, d.chartX, d.interpY, d.interpX, Numeric.TYPE_LINEAR);
                Colormap.arrayToInt(d.interpY, d.chartColor, s.nColormap,
                        v.bottomY, v.topY);
            } else {
                Numeric.linspace(view.leftX, view.rightX, d.interpX);
                Numeric.interpolate(d.chartY, d.chartX, d.interpY, d.interpX, Numeric.TYPE_LINEAR);
                Colormap.arrayToInt(d.interpY, d.chartColor, s.nColormap,
                        view.bottomY, view.topY);
            }

                d.bitmap.getPixels(d.tempVector, 0, d.bitmap.getWidth()
                        , 0, 0, d.bitmap.getWidth(), d.bitmap.getHeight() - 1);
                d.bitmap.setPixels(d.tempVector, 0, d.bitmap.getWidth()
                        , 0, 1, d.bitmap.getWidth(), d.bitmap.getHeight() - 1);
                d.bitmap.setPixels(d.chartColor, 0, d.bitmap.getWidth(), 0, 0, d.bitmap.getWidth(), 1);

                dataUpdated = UPDATED;
            }
            canvas.drawBitmap(d.bitmap, d.matrix, d.paint);
        }


        if (d.mode == MATRIX_MODE) {
            float temp;
            if (view.topY > view.bottomY) {
                temp = view.bottomY;
                view.bottomY = view.topY;
                view.topY = temp;
            }
            if (view.initRequired == DragZoom.INIT_REQUIRE) {
                if (v != null) {
                    view.leftX = view.leftInitX = v.leftX;
                    view.rightX = view.rightInitX = v.rightX;
                } else {
                    view.leftX = view.leftInitX = Numeric.min(d.chartX, true);
                    view.rightX = view.rightInitX = Numeric.max(d.chartX, true);
                }
                view.topY = view.topInitY = 0;
                view.bottomY = view.bottomInitY = d.bitmap.getHeight();
                view.initRequired = DragZoom.NO_INIT;
            }
            d.matrix.reset();
            float dx = view.width / d.bitmap.getWidth() * (view.leftInitX - view.rightInitX) / (view.leftX - view.rightX);
            float dy = view.height / d.bitmap.getHeight() * (view.bottomInitY - view.topInitY) / (view.bottomY - view.topY);
            if (Float.isNaN(dx)) {
                dy = 0;
                dx = 0;
            }
            d.matrix.setScale(dx, dy);
            dx = view.valueToPixelX(view.leftInitX);
            dy = view.valueToPixelY(view.topInitY);
            if (Float.isNaN(dx)) {
                dy = 0;
                dx = 0;
            }
            d.matrix.postTranslate(dx, dy);
            if (dataUpdated == UPDATE_NEED){
                Numeric.linspace(d.chartX[0], d.chartX[d.chartX.length-1], d.interpX);
                Numeric.linspace(d.chartY[0], d.chartY[d.chartY.length-1], d.interpY);
                Numeric.interpolate(d.chartY, d.chartX, d.chartXY, d.interpY, d.interpX, d.interpZ, Numeric.TYPE_LINEAR);
                Colormap.arrayToInt(d.interpZ,d.chartColor,s.nColormap,Numeric.min(d.interpZ,true),Numeric.max(d.interpZ,true));
                d.bitmap.setPixels(d.chartColor, 0, d.bitmap.getWidth(), 0, 0, d.bitmap.getWidth(), d.bitmap.getHeight());
                dataUpdated = UPDATED;
            }
            canvas.drawBitmap(d.bitmap, d.matrix, d.paint);
        }
//        canvas.drawBitmap(d.bitmap, d.matrix, d.paint);

        // Grid
        if (a.checkGridEnable) {
            stepValueX = view.stepCalculate(view.rightX - view.leftX, view.width);
            path.reset();
            textSize = paintText.getTextSize() / 2;
            value = (float) (Math.floor(view.leftX / stepValueX) * stepValueX);
            pixel = view.valueToPixelX(value);
            while (pixel < view.width) {
                if (pixel > textSize * 4) {
                    if (value == 0)
                        canvas.drawLine(pixel, 0,
                                pixel, view.height, paintAxis);
                    else {
                        path.moveTo(pixel, 0);
                        path.lineTo(pixel, view.height - textSize);
                    }
                    if (pixel < view.width - textSize * (a.xLabel.length() + 4))
                        canvas.drawText(view.parseValue(value), pixel, view.height, paintText);
                }
                value += stepValueX;
                value = (float) (Math.round(value / stepValueX) * stepValueX);
                pixel = view.valueToPixelX(value);
            }
            canvas.drawText(a.xLabel, view.width - textSize * (a.xLabel.length() + 1), view.height - textSize, paintLabel);

            stepValueY = view.stepCalculate(view.topY - view.bottomY, view.height);
            value = (float) (Math.floor(view.topY / stepValueY) * stepValueY);
            if (value == Float.NEGATIVE_INFINITY)
                value = -Float.MAX_VALUE;
            pixel = view.valueToPixelY(value);
            while (pixel < view.height - textSize * 2) {
                if (pixel > 0) {
                    if (value == 0)
                        canvas.drawLine(0, pixel,
                                view.width, pixel, paintAxis);
                    else {
                        path.moveTo(4 * textSize, pixel);
                        path.lineTo(view.width, pixel);
                    }
                    if (pixel > 0.1 * view.height)
                        canvas.drawText(view.parseValue(value), 0, pixel, paintText);
                }
                value += stepValueY;
                value = Math.round(value / stepValueY) * stepValueY;
                pixel = view.valueToPixelY(value);
            }
            canvas.drawText(a.yLabel, 0, textSize * 2, paintLabel);
            if (a.switchGridDash)
                paintGrid.setPathEffect(dash);
            else
                paintGrid.setPathEffect(null);
            canvas.drawPath(path, paintGrid);
        }
    }

    public void setLimits(DragZoom view){
        this.v = view;
    }

    void setData(Waterfall waterfall){
        if (waterfall!=null) {
            d = waterfall.d;
            v = waterfall.v;
        }
    }
}
