package com.example.plot.graphics;

import android.annotation.SuppressLint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

import java.util.concurrent.Semaphore;

public class Touch implements View.OnTouchListener {
    GestureDetectorCompat gestureDetectorCompat;
    Graphic graphic;
    DragZoom view;
    @SuppressLint("ClickableViewAccessibility")
    public Touch(Graphic graphic) {;
        this.graphic = graphic;
        view = graphic.view;
        gestureDetectorCompat = new GestureDetectorCompat(graphic.getContext(),new TapListener());
        graphic.setOnTouchListener(this);
    }

    float prevX, prevY, prevX1, prevY1 ,valueX, valueY, prevSpanX, prevSpanY, dx, dx1, dy, dy1;
    int modeZoom = 0, p;
    private static final int ZOOM_X = 1, ZOOM_Y = 2, ZOOM_DIAG = 3, ON_UP = 4;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event){
        gestureDetectorCompat.onTouchEvent(event);
        int actionMasked = event.getActionMasked();
        int actionIndex = event.getActionIndex();
        int pointerCount = event.getPointerCount();
        if (pointerCount==1) {
            switch (actionMasked) {
                case MotionEvent.ACTION_DOWN:
                    prevX = event.getX();
                    prevY = event.getY();
                case MotionEvent.ACTION_POINTER_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    float value = view.leftX - view.pixelToValueX(event.getX() - prevX);
                    view.leftX += value;
                    view.rightX += value;
                    value = view.topY - view.pixelToValueY(event.getY() - prevY);
                    view.topY += value;
                    view.bottomY += value;
                    prevX = event.getX();
                    prevY = event.getY();
                    graphic.update();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (modeZoom==ON_UP)
                        modeZoom = 0;
                    break;
            }
        }
        else if (pointerCount==2){
            switch (actionMasked){
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    p = event.getX(0)<event.getX(1)?0:1;
                    prevX = event.getX(p);
                    prevX1 = event.getX(1-p);
                    p = event.getY(0)<event.getY(1)?0:1;
                    prevY = event.getY(p);
                    prevY1 = event.getY(1-p);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    prevX = event.getX(1-actionIndex);
                    prevY = event.getY(1-actionIndex);
                    modeZoom = ON_UP;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (modeZoom==0) {
                        // onScaleBegin
                        p = event.getX(0)<event.getX(1)?0:1;
                        dx = event.getX(p) - prevX;
                        dx1 = event.getX(1-p) - prevX1;
                        p = event.getY(0)<event.getY(1)?0:1;
                        dy = event.getY(p) - prevY;
                        dy1 = event.getY(1-p) - prevY1;
                        prevSpanX = Math.abs(dx-dx1);
                        prevSpanY = Math.abs(dy-dy1);
                        if (prevSpanX >= 0.5f * prevSpanY & prevSpanX <= 1.5f * prevSpanY & prevSpanX != 0)
                            modeZoom = ZOOM_DIAG;
                        else if (prevSpanX < 0.5f * prevSpanY)
                            modeZoom = ZOOM_Y;
                        else if (prevSpanX > 1.5f * prevSpanY)
                            modeZoom = ZOOM_X;
                    } else if (modeZoom>0){
                        //onScale
                        p = event.getX(0)<event.getX(1)?0:1;
                        dx = event.getX(p) - prevX;
                        dx1 = event.getX(1-p) - prevX1;
                        p = event.getY(0)<event.getY(1)?0:1;
                        dy = event.getY(p) - prevY;
                        dy1 = event.getY(1-p) - prevY1;
                        valueX = view.leftX-view.pixelToValueX(dx1-dx);
                        valueY = view.topY-view.pixelToValueY(dy1-dy);
                        switch (modeZoom){
                            case ZOOM_X:
                                view.leftX -= valueX;
                                view.rightX += valueX;
                                break;
                            case ZOOM_Y:
                                view.topY -= valueY;
                                view.bottomY += valueY;
                                break;
                            case ZOOM_DIAG:
                                view.leftX -= valueX;
                                view.rightX += valueX;
                                view.topY -= valueY;
                                view.bottomY += valueY;
                                break;
                        }
                        graphic.update();
                    }
                    p = event.getX(0)<event.getX(1)?0:1;
                    prevX = event.getX(p);
                    prevX1 = event.getX(1-p);
                    p = event.getY(0)<event.getY(1)?0:1;
                    prevY = event.getY(p);
                    prevY1 = event.getY(1-p);
                    graphic.update();
                    break;
            }
        }
        return true;
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener
            implements GestureDetector.OnDoubleTapListener{
        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            view.initRequired = DragZoom.INIT_REQUIRE;
            graphic.update();
            return true;
        }
//
//        @Override
//        public void onLongPress(MotionEvent e) {
//            super.onLongPress(e);
//            float x = e.getX();
////            x = view.pixelToValueX(x);
//            float y = e.getY();
////            y = view.pixelToValueY(y);
//            // Compare new value with old ones
//            int i;
//            for (i=0; i<graphic.markerX.length; i++)
//                if (x<1.1*graphic.markerX[i] &
//                    x>0.9*graphic.markerX[i] &
//                    y<1.1*graphic.markerY[i] &
//                    y>0.9*graphic.markerY[i])
//                    break;
//            if (i==graphic.markerX.length) {
//                // Add new value
//                for (i = 0; i < graphic.markerX.length; i++)
//                    if (Float.isNaN(graphic.markerX[i])) {
//                        graphic.markerX[i] = x;
//                        graphic.markerY[i] = y;
//                        break;
//                    }
//            }
//            else {
//                // Update old Values
//                graphic.markerX[i] = x;
//                graphic.markerY[i] = y;
//            }
//        }
    }
}
