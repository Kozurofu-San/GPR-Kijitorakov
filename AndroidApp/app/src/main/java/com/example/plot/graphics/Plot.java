package com.example.plot.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.plot.R;
import com.example.plot.dsp.Numeric;

import java.util.ArrayList;
import java.util.Random;

public class Plot extends Graphic {

    static class Data{
        ArrayList<float[]> chartListX = new ArrayList<>();
        ArrayList<float[]> chartListY = new ArrayList<>();
        ArrayList<Paint> paintList = new ArrayList<>();
        ArrayList<String> legendList = new ArrayList<>();
    }
    Data d = new Data();

    static class Settings{

    }
    Settings s = new Settings();

    public Plot(Context context){
        super(context);
    }

    public Plot(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public Plot(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Plot(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public int count = 0;
    public class Chart{
        public int index;
        public float[] X, Y;
        public Paint paint;
        public String legend;
        public Chart(float[] X, float[] Y, Paint paint, String legend){
//            while (!surfaceCreated);
            if (X.length==Y.length) {
                    this.Y = Y.clone();
                    this.X = X.clone();
                    this.paint = new Paint(paint);
                    this.paint.setAntiAlias(a.switchAntiAliasing);
                    this.legend = legend;
                    index = count;
                    count++;
                    addChart(this.X, this.Y, this.paint, this.legend);
                    isInit = true;
            }
            else{
                Log.e("Plot","Vectors have different lengths");
            }
        }

        @SuppressWarnings("unused")
        public void setX(float[] X){
            if (this.X.length==X.length) {
                System.arraycopy(X, 0, this.X, 0, X.length);
                d.chartListX.set(index, this.X);
            }
        }

        @SuppressWarnings("unused")
        public void setY(float Y){
            if (this.Y.length - 1 >= 0) System.arraycopy(this.Y, 0, this.Y, 1, this.Y.length - 1);
            this.Y[0] = Y;
            d.chartListY.set(index,this.Y);
            dataUpdated = UPDATE_NEED;
        }

        @SuppressWarnings("unused")
        public void setY(float[] Y){
            if (this.Y.length==Y.length) {
                System.arraycopy(Y, 0, this.Y, 0, Y.length);
                d.chartListY.set(index, this.Y);
            }
            else {
                this.Y = Y.clone();
                float minX = this.X[0];
                float maxX = this.X[X.length-1];
                this.X = new float[Y.length];
                Numeric.linspace(minX,maxX,this.X);
                d.chartListX.set(index,this.X);
                d.chartListY.set(index,this.Y);
            }
            dataUpdated = UPDATE_NEED;
        }

        @SuppressWarnings("unused")
        public void setPaint(Paint paint){
            this.paint = paint;
            d.paintList.set(index, this.paint);
        }

        @SuppressWarnings("unused")
        public void setLegend(String legend){
            this.legend = legend;
            d.legendList.set(index, this.legend);
        }
    }

    @SuppressWarnings("unused")
    public void addChart(float[] X, float[] Y, Paint paint, String legend){
        if (X.length == Y.length) {
            d.chartListX.add(X);
            d.chartListY.add(Y);
            d.paintList.add(paint);
            d.legendList.add(legend);
        }
        else Log.e("addChart","Vectors X and Y have not same length");
    }

    @SuppressWarnings("unused")
    public void addChart(float[] Y, Paint paint, String legend){
        float[] X = new float[Y.length];
        for (int i=0; i<X.length; i++)
            X[i] = i;
        d.chartListX.add(X);
        d.chartListY.add(Y);
        d.paintList.add(paint);
        d.legendList.add(legend);
    }

    @SuppressWarnings("unused")
    public void addChart(int n, Paint paint, String legend){
        float[] X = new float[n];
        for (int i=0; i<n; i++)
            X[i] = i;
        float[] Y = new float[n];
        d.chartListX.add(X);
        d.chartListY.add(Y);
        d.paintList.add(paint);
        d.legendList.add(legend);
    }

    @SuppressWarnings("unused")
    public void clearChart(){
        count = 0;
        d.chartListX.clear();
        d.chartListY.clear();
        d.paintList.clear();
        d.legendList.clear();
    }

    @SuppressWarnings("unused")
    public void label(String xLabel, String yLabel){
        a.xLabel = xLabel;
        a.yLabel = yLabel;
    }

    float checkValue(float a){
        if (a==Float.NEGATIVE_INFINITY)
            a = Float.MIN_VALUE;
        else if (a==Float.POSITIVE_INFINITY)
            a = Float.MAX_VALUE;
        else if (Float.isNaN(a))
            a = 0;
        return a;
    }

    public void xLim(float left, float right){
        view.leftX = left;
        view.rightX = right;
        view.leftInitX = left;
        view.rightInitX = right;
        view.initRequired = DragZoom.NO_INIT;
    }

    private int cnt = 0;
    private long timeSaved = 0;
    @SuppressWarnings("FieldCanBeLocal")
    private float[] arrayX, arrayY;

    @Override
    public void draw(Canvas canvas) {
        if (canvas==null | d.chartListY==null)
            return;
        super.draw(canvas);
        if (isInEditMode()){
            view = new DragZoom();
            d.chartListX = new ArrayList<>();
            d.chartListY = new ArrayList<>();
            d.paintList = new ArrayList<>();
            d.legendList = new ArrayList<>();
            view.initRequired = DragZoom.INIT_REQUIRE;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            view.dpi = displayMetrics.densityDpi;
            arrayX = new float[100];
            for (int i=0; i<arrayX.length; i++)
                arrayX[i] = 0.1f*i;
            d.chartListX.add(arrayX);
            arrayY = new float[arrayX.length];
            for (int i=0; i< arrayY.length; i++)
                arrayY[i] = (float)Math.sin(2*Math.PI*(double)i/(arrayY.length-1));
            d.chartListY.add(arrayY);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Random random = new Random();
            paint.setColor(random.nextInt());
            paint.setAlpha(255);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(7);
            d.paintList.add(paint);
            d.legendList.add("Legend");
            a.checkGridEnable = true;
            a.switchGridDash = true;
        }

        float stepValueX;
        float stepValueY;
        int n;
        if (!a.switchAutoscalingPlot) {
            if (view.initRequired > DragZoom.NO_INIT) {
                view.height = getHeight();  // Vertical
                view.width = getWidth();    // Horizontal
//                if (interpInitNeed) {
//                    interpY = new float[(int) view.width];
//                    interpX = new float[(int) view.width];
//                    Numeric.linspace(chartListX.get(0), interpX);
//                    interpInitNeed = false;
//                }

                // Analyze
                view.leftInitX = view.rightInitX = view.topInitY = view.bottomInitY = 0;
                float minX, maxX = minX = 0;
                for (int i = 0; i < d.chartListX.size(); i++) {
                    float minY,maxY;
                    arrayX = d.chartListX.get(i);
                    arrayY = d.chartListY.get(i);
                    minX = maxX = arrayX[0];
                    minY = maxY = arrayY[0];
                            n = arrayX.length;
                    for (int j = 0; j < n; j++) {
                        if (Float.isFinite(arrayX[j]) & Float.isFinite(arrayY[j])) {
                            maxY = Math.max(maxY, arrayY[j]);
                            minY = Math.min(minY, arrayY[j]);
                            maxX = Math.max(maxX, arrayX[j]);
                            minX = Math.min(minX, arrayX[j]);
                        }
                    }
                    if (minX == 0 & maxX == 0) {
                        view.leftInitX = -1;
                        view.rightInitX = 1;
                    } else {
                        view.leftInitX = Math.min(view.leftInitX, minX);
                        view.rightInitX = Math.max(view.rightInitX, maxX);
                    }
                    if (minY == 0 & maxY == 0
                            & view.bottomInitY == 0 & view.topInitY == 0) {
                        view.bottomInitY = -1;
                        view.topInitY = 1;
                    } else {
                        view.bottomInitY = Math.min(view.bottomInitY, minY);
                        view.topInitY = Math.max(view.topInitY, maxY);
                    }
                }
                stepValueX = view.stepCalculate(view.rightInitX - view.leftInitX, view.width);
                view.leftInitX = (float) (Math.floor(view.leftInitX / stepValueX) * stepValueX)
                        - 0.05f * (view.rightInitX - view.leftInitX);
                view.leftInitX = checkValue(view.leftInitX);
                view.rightInitX = (float) (Math.ceil(view.rightInitX / stepValueX) * stepValueX)
                        + 0.00f * (view.rightInitX - view.leftInitX);
                view.rightInitX = checkValue(view.rightInitX);
                stepValueY = view.stepCalculate(view.topInitY - view.bottomInitY, view.height);
                view.bottomInitY = (float) (Math.floor(view.bottomInitY / stepValueY) * stepValueY)
                        - 0.00f * (view.topInitY - view.bottomInitY);
                view.bottomInitY = checkValue(view.bottomInitY);
                view.topInitY = (float) (Math.ceil(view.topInitY / stepValueY) * stepValueY)
                        + 0.05f * (view.topInitY - view.bottomInitY);
                view.topInitY = checkValue(view.topInitY);
                view.leftInitX = minX;
                view.rightInitX = maxX;

                if (view.initRequired == DragZoom.INIT_REQUIRE) {
                    view.leftX = view.leftInitX;
                    view.rightX = view.rightInitX;
                    view.topY = view.topInitY;
                    view.bottomY = view.bottomInitY;
                }
                view.initRequired = DragZoom.NO_INIT;
            }
        }
        else{
            float minX, minY, maxX, maxY;
            minX = maxX = d.chartListX.get(0)[0];
            minY = maxY = d.chartListY.get(0)[0];
            for (int i = 0; i < d.chartListY.size(); i++) {
                arrayX = d.chartListX.get(i);
                arrayY = d.chartListY.get(i);
                minX = Math.min(minX,Numeric.min(arrayX, true));
                maxX = Math.max(maxX,Numeric.max(arrayX, true));
                maxY = Math.max(maxY,Numeric.max(arrayY, true));
                minY = Math.min(minY,Numeric.min(arrayY, true));
            }
            view.leftX = minX;
            view.rightX = maxX;
            view.topY = maxY;
            view.bottomY = minY;
        }


            stepValueX = view.stepCalculate(view.rightX - view.leftX, view.width);
            stepValueY = view.stepCalculate(view.topY - view.bottomY, view.height);
            // Grid
        float textSize;
        if (a.checkGridEnable) {
                textSize = paintText.getTextSize() / 2;
            float value = (float) (Math.floor(view.leftX / stepValueX) * stepValueX);
                path.reset();
            float pixel = view.valueToPixelX(value);
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
                    value = Math.round(value / stepValueX) * stepValueX;
                    pixel = view.valueToPixelX(value);
                }
                canvas.drawText(a.xLabel, view.width - textSize * (a.xLabel.length() + 1), view.height - textSize, paintLabel);

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
                    value -= stepValueY;
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
            // Charts
        int position = 0;
            for (int i = 0; i < d.chartListX.size(); i++) {
                arrayX = d.chartListX.get(i);
                arrayY = d.chartListY.get(i);
                Paint paint = d.paintList.get(i);
                String legend = d.legendList.get(i);
                n = arrayX.length;
                path.reset();
                boolean isCut = true;
                for (int j = 0; j < n; j++) {
                    float pixelX = view.valueToPixelX(arrayX[j]);
                    float pixelY = view.valueToPixelY(arrayY[j]);
                    if (pixelX == Float.NEGATIVE_INFINITY |
                            pixelX == Float.POSITIVE_INFINITY |
                            pixelY == Float.NEGATIVE_INFINITY |
                            pixelY == Float.POSITIVE_INFINITY |
                            Float.isNaN(pixelX) | Float.isNaN(pixelY)) {
                        isCut = true;
                        continue;
                    }
                    if (isCut) {
                        path.moveTo(pixelX, pixelY);
                        isCut = false;
                    } else
                        path.lineTo(pixelX, pixelY);
                }
                canvas.drawPath(path, paint);
                // Legend
                if (legend != null) {
                    textSize = paint.getTextSize();
                    position += 3 * textSize;
                    canvas.drawLine(view.width - 40 - 2 * textSize * legend.length()
                            , position - textSize
                            , view.width - 5 - 2 * textSize * legend.length()
                            , position - textSize, paint);
                    canvas.drawText(legend, view.width - 2 * textSize * legend.length()
                            , position, paintText);
                    if (isInEditMode())
                        position -= 3 * textSize;
                }
            }
            dataUpdated = UPDATED;

//            if (timeSaved != 0) {
//                canvas.drawText(timeSaved + " us", view.width / 2, 50, paintLabel);
//                canvas.drawText(1000000 / timeSaved + " fps", view.width / 2, 100, paintLabel);
//            }
//            cnt++;
//            if (cnt > 30) {
//                cnt = 0;
//                timeSaved = time / 1000;
//            }
        }

        void setData(Plot plot){
        if (plot!=null)
            d = plot.d;
        }


}
