package com.example.plot.dsp;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class Numeric {

    static float[] p = new float[4];
    static float[] b = new float[4];
    static float[][] q = new float[4][4];
    static float[][] a = new float[4][4];

    static float[] temp1, temp2;

    public static final int TYPE_NONE = 0,
        TYPE_LINEAR = 1,
        TYPE_CUBIC = 2;

    // Interpolation

    @SuppressWarnings("unused")
    public static float cubic(float[] arrayY, float[] arrayX, float x) {
        if (x<arrayX[0] | x>arrayX[arrayX.length-1])
            return Float.NaN;
        if (arrayY.length<2)
            return 0;
        int index = nearestLess(arrayX,x);
        if (index < 0)
            return 0;
        if (index<1)
            index = 1;
        p[0] = (index!=1) ? arrayY[index-2] : 2*arrayY[index-1]-arrayY[index];
        p[1] = arrayY[index-1];
        p[2] = arrayY[index];
        p[3] = (index>=arrayY.length-1) ? 2*arrayY[index]-arrayY[index-1] : arrayY[index+1];
        b[0] = -0.5f*p[0]+1.5f*p[1]-1.5f*p[2]+0.5f*p[3];
        b[1] = p[0]-2.5f*p[1]+2*p[2]-0.5f*p[3];
        b[2] = -0.5f*p[0]+0.5f*p[2];
        b[3] = p[1];
        x = (x-arrayX[index-1])/(arrayX[index]-arrayX[index-1]);
        return b[0]*x*x*x+b[1]*x*x+b[2]*x+b[3];
    }

    @SuppressWarnings("unused")
    public static void interpolate(float[] arrayY, float[] arrayX, float[] newY, float[] newX, int TYPE) {
        switch (TYPE) {
            case TYPE_LINEAR:
                for (int i = 0; i < newX.length; i++)
                    newY[i] = linear(arrayY, arrayX, newX[i]);
                break;
            case TYPE_CUBIC:
                for (int i = 0; i < newX.length; i++)
                    newY[i] = cubic(arrayY, arrayX, newX[i]);
                break;
        }
    }

    @SuppressWarnings("unused")
    public static void interpolate(float[] arrayY, float[] arrayX, float[][]matrix, float[] newY, float[] newX, float[][] newZ, int TYPE) {
        switch (TYPE) {
            case TYPE_LINEAR:
                for (int iy = 0; iy < newX.length; iy++)
                    for (int ix=0; ix<newY.length; ix++)
                        newZ[iy][ix] = bilinear(arrayY, arrayX, matrix, newY[iy],newX[ix]);
                break;
            case TYPE_CUBIC:
                for (int ix = 0; ix < newX.length; ix++)
                    for (int iy=0; iy<newY.length; iy++)
                        newZ[iy][ix] = bicubic(arrayY, arrayX, matrix, newY[iy],newX[ix]);
                break;
        }
    }

    @SuppressWarnings("unused")
    public static float linear(float[] arrayY, float[] arrayX, float x){
        if (x<arrayX[0] | x>arrayX[arrayX.length-1])
            return Float.NaN;
        int index = nearestLess(arrayX,x);
        if (index<1)
            index = 1;
        float a, b;
        a = arrayY[index]-arrayY[index-1];
        b = arrayY[index-1];
        x = (x-arrayX[index-1])/(arrayX[index]-arrayX[index-1]);
        return a*x+b;
    }

    public static float lagrange(float[] arrayY, float[] arrayX, float x){
        float s = 0, l;
        for (int i=0; i<arrayX.length; i++){
            l = 1;
            for (int j=0; j<arrayX.length; j++)
                if (i!=j)
                    l = l*(x-arrayX[j])/(arrayX[i]-arrayX[j]);
            s += arrayY[i]*l;
        }
        return s;
    }

    @SuppressWarnings("unused")
    public static float bilinear(float[] arrayY, float[] arrayX, float[][] matrix, float y, float x) {
        if ( y < arrayY[0] | y > arrayY[arrayY.length - 1]
                |x < arrayX[0] | x > arrayX[arrayX.length - 1])
            return Float.NaN;
        if (arrayY.length < 2 | arrayX.length < 2)
            return 0;
        int iY = nearestLess(arrayY, y);
        if (iY < 0)
            return 0;
        if (iY < 1)
            iY = 1;
        int iX = nearestLess(arrayX, x);
        if (iX < 0)
            return 0;
        if (iX < 1)
            iX = 1;

        p[0] = matrix[iY-1][iX-1];
        p[1] = matrix[iY-1][iX];
        p[2] = matrix[iY][iX-1];
        p[3] = matrix[iY][iX];

        b[0] = p[0];
        b[1] = p[2]-p[0];
        b[2] = p[1]-p[0];
        b[3] = p[0]-p[1]-p[2]+p[3];

        x = (x-arrayX[iX-1])/(arrayX[iX]-arrayX[iX-1]);
        y = (y-arrayY[iY-1])/(arrayY[iY]-arrayY[iY-1]);
        return b[0]+b[1]*y+b[2]*x+b[3]*x*y;
    }

    @SuppressWarnings("unused")
    public static float bicubic(float[] arrayY, float[] arrayX, float[][] matrix, float y, float x) {
        if (x<arrayX[0] | x>arrayX[arrayX.length-1]
            | y<arrayY[0] | y>arrayY[arrayY.length-1])
            return Float.NaN;
        if (arrayY.length<2 | arrayX.length<2)
            return 0;
        int iX = nearestLess(arrayX,x);
        if (iX < 0)
            return 0;
        if (iX<1)
            iX = 1;
        int iY = nearestLess(arrayY,y);
        if (iY < 0)
            return 0;
        if (iY<1)
            iY = 1;

        q[1][1] = matrix[iY-1][iX-1];
        q[1][2] = matrix[iY-1][iX];
        q[2][1] = matrix[iY][iX-1];
        q[2][2] = matrix[iY][iX];

        if (iX==1){
            q[1][0] = q[1][1];
            q[2][0] = q[2][1];
        } else {
            q[1][0] = matrix[iY-1][iX-2];
            q[2][0] = matrix[iY][iX-2];
        }
        if (iX>=arrayX.length-1){
            q[1][3] = q[1][2];
            q[2][3] = q[2][2];
        } else {
            q[1][3] = matrix[iY-1][iX+1];
            q[2][3] = matrix[iY][iX+1];
        }

        if (iY==1){
            q[0][0] = q[1][0];
            q[0][1] = q[1][1];
            q[0][2] = q[1][2];
            q[0][3] = q[1][3];
        } else {
            if (iX==1)
                q[0][0] = q[1][0];
            else
                q[0][0] = matrix[iY-2][iX-2];
            q[0][1] = matrix[iY-2][iX-1];
            q[0][2] = matrix[iY-2][iX];
            if (iX>=arrayY.length-1)
                q[0][3] = q[1][3];
            else
                q[0][3] = matrix[iY-2][iX+1];
        }
        if (iY>=arrayY.length-1){
            q[3][0] = q[2][0];
            q[3][1] = q[2][1];
            q[3][2] = q[2][2];
            q[3][3] = q[2][3];
        } else {
            if (iX==1)
                q[3][0] = q[2][0];
            else
                q[3][0] = matrix[iY+1][iX-2];
            q[3][1] = matrix[iY+1][iX-1];
            q[3][2] = matrix[iY+1][iX];
            if (iX>=arrayY.length-1)
                q[3][3] = q[2][3];
            else
                q[3][3] = matrix[iY+1][iX+1];
        }

        a[0][0] = q[1][1];
        a[0][1] = -0.5f*q[1][0]+0.5f*q[1][2];
        a[0][2] = q[1][0]-2.5f*q[1][1]+2*q[1][2]-0.5f*q[1][3];
        a[0][3] = -0.5f*q[1][0]+1.5f*q[1][1]-1.5f*q[1][2]+0.5f*q[1][3];
        a[1][0] = -0.5f*q[0][1]+0.5f*q[2][1];
        a[1][1] = 0.25f*q[0][0]-0.25f*q[0][2]-0.25f*q[2][0]+0.25f*q[2][2];
        a[1][2] = -0.5f*q[0][0]+1.25f*q[0][1]-q[0][2]+0.25f*q[0][3]
                +0.5f*q[2][0]-1.25f*q[2][1]+q[2][2]-0.25f*q[0][3];
        a[1][3] = 0.25f*q[0][0]-0.75f*q[0][1]+0.75f*q[0][2]-0.25f*q[0][3]
                -0.25f*q[2][0]+0.75f*q[2][1]-0.75f*q[2][2]+0.25f*q[2][3];
        a[2][0] = q[0][1]-2.5f*q[1][1]+2*q[2][1]-0.5f*q[3][1];
        a[2][1] = -0.5f*q[0][0]+0.5f*q[0][2]+1.25f*q[1][0]-1.25f*q[1][2]
                -q[2][0]+q[2][2]+0.25f*q[3][0]-0.25f*q[3][2];
        a[2][2] = q[0][0] - 2.5f * q[0][1] + 2f * q[0][2] - .5f * q[0][3] - 2.5f * q[1][0]
                + 6.25f * q[1][1] - 5f * q[1][2] + 1.25f * q[1][3] + 2f * q[2][0]
                - 5f * q[2][1] + 4f * q[2][2] - q[2][3] - .5f * q[3][0]
                + 1.25f * q[3][1] - q[3][2] + .25f * q[3][3];
        a[2][3] = -.5f * q[0][0] + 1.5f * q[0][1] - 1.5f * q[0][2] + .5f * q[0][3]
                + 1.25f * q[1][0] - 3.75f * q[1][1] + 3.75f * q[1][2]
                - 1.25f * q[1][3] - q[2][0] + 3f * q[2][1] - 3f * q[2][2] + q[2][3]
                + .25f * q[3][0] - .75f * q[3][1] + .75f * q[3][2] - .25f * q[3][3];
        a[3][0] = -.5f * q[0][1] + 1.5f * q[1][1] - 1.5f * q[2][1] + .5f * q[3][1];
        a[3][1] = .25f * q[0][0] - .25f * q[0][2] - .75f * q[1][0] + .75f * q[1][2]
                + .75f * q[2][0] - .75f * q[2][2] - .25f * q[3][0] + .25f * q[3][2];
        a[3][2] = .5f * q[0][0] + 1.25f * q[0][1] - q[0][2] + .25f * q[0][3]
                + 1.5f * q[1][0] - 3.75f * q[1][1] + 3f * q[1][2] - .75f * q[1][3]
                - 1.5f * q[2][0] + 3.75f * q[2][1] - 3f * q[2][2] + .75f * q[2][3]
                + .5f * q[3][0] - 1.25f * q[3][1] + q[3][2] - .25f * q[3][3];
        a[3][3] = .25f * q[0][0] - .75f * q[0][1] + .75f * q[0][2] - .25f * q[0][3]
                - .75f * q[1][0] + 2.25f * q[1][1] - 2.25f * q[1][2] + .75f * q[1][3]
                + .75f * q[2][0] - 2.25f * q[2][1] + 2.25f * q[2][2] - .75f * q[2][3]
                - .25f * q[3][0] + .75f * q[3][1] - .75f * q[3][2] + .25f * q[3][3];
        x = (x-arrayX[iX-1])/(arrayX[iX]-arrayX[iX-1]);
        y = (y-arrayY[iY-1])/(arrayY[iY]-arrayY[iY-1]);
        return a[0][0]+a[0][1]*x+a[0][2]*x*x+a[0][3]*x*x*x+
                (a[1][0]+a[1][1]*x+a[1][2]*x*x+a[1][3]*x*x*x)*y+
                (a[2][0]+a[2][1]*x+a[2][2]*x*x+a[2][3]*x*x*x)*y*y+
                (a[3][0]+a[3][1]*x+a[3][2]*x*x+a[3][3]*x*x*x)*y*y*y;
    }

    @SuppressWarnings("unused")
    public static int nearestMore(@NonNull float[] array, float x) {
        int index = -1;
        for (int i=0; i<array.length; i++){
            if (x>array[i]) {
                index = i;
                break;
            }
        }
        return index;
    }

    @SuppressWarnings("unused")
    public static int nearestLess(@NonNull float[] array, float x) {
        int index = -1;
        for (int i=0; i<array.length; i++){
            if (x<=array[i]) {
                index = i;
                break;
            }
        }
        return index;
    }

    @SuppressWarnings("unused")
    public static int nearest(@NonNull float[] array, float x) {
        float d = 0; int index = -1;
        for (int i=0; i<array.length-1; i++){
            if (d>(array[i]-x)) {
                d = array[i]-x;
                index = i;
            }
        }
        return index;
    }

    public static void linspace(float[] array, float[] newArray){
        float delta = (array[array.length-1]-array[0])/(newArray.length-1);
        for (int i=0; i<newArray.length; i++)
            newArray[i] = i*delta+array[0];
    }

    public static void linspace(float min, float max, float[] newArray){
        float delta = (max-min)/(newArray.length-1);
        for (int i=0; i<newArray.length; i++)
            newArray[i] = i*delta+min;
    }

    // Numerical methods

    @SuppressWarnings("unused")
    public static float abs(float a){
        return a>0?a:-1;
    }

    @SuppressWarnings("unused")
    public static void abs(float[] a){
        for (int i=0; i<a.length; i++)
            if (a[i]<0)
                a[i] = -a[i];
    }

    @SuppressWarnings("unused")
    public static void abs(float[][] a){
        for (int i=0; i<a.length; i++)
            for (int j=0; j<a[0].length; j++)
                if (a[i][j]<0)
                    a[i][j] = -a[i][j];
    }

    @SuppressWarnings("unused")
    public static float max(float[][] a, boolean finite){
        float t = a[0][0];
        for (int i=0; i<a.length; i++)
            for (int j=0; j<a[0].length; j++)
//            if (finite)
                if (Float.isFinite(a[i][j]))
                    if (t < a[i][j])
                        t = a[i][j];
        return t;
    }

    @SuppressWarnings("unused")
    public static float max(float[] array, boolean finite){
        float t = array[0];
        for (float v : array)
            if (finite)
//                if (Float.isFinite(v))
                if (t < v)
                    t = v;
        return t;
    }

    @SuppressWarnings("unused")
    public static float min(float[] array, boolean finite){
        float t = array[0];
        int i;
        for (i=0; i<array.length; i++)
            if (finite)
//                if (Float.isFinite(array[i]))
                if (t > array[i])
                    t = array[i];
        return t;
    }

    @SuppressWarnings("unused")
    public static float min(float[][] a, boolean finite){
        float t = a[0][0];
        int i,j;
        for (i=0; i<a.length; i++)
            for (j=0; j<a[0].length; j++)
                if (finite)
//                if (Float.isFinite(array[i]))
                if (t > a[i][j])
                    t = a[i][j];
        return t;
    }

    @SuppressWarnings("unused")
    public static float mean(float[] array){
        return sum(array)/array.length;
    }

    @SuppressWarnings("unused")
    public static float sum(float[] array){
        float t = 0;
        int i;
        for (i=0; i< array.length; i++)
            if (Float.isFinite(array[i]))
                t += array[i];
        return t;
    }

    // Arithmetics

    @SuppressWarnings("unused")
    public static void equ(float[] array1, float[] array2){
        if (array1.length != array2.length)
            throw new IllegalArgumentException("Lengths are not same");
        for (int i=0; i<array1.length; i++)
            array2[i] = array1[i];
    }

    @SuppressWarnings("unused")
    public static void equ(float[] array, float val){
        for (int i=0; i<array.length; i++)
            array[i] = val;
    }

    @SuppressWarnings("unused")
    public static void add(float[] array1, float[] array2){
        if (array1.length != array2.length)
            throw new IllegalArgumentException("Lengths are not same");
        for (int i=0; i<array1.length; i++)
            array1[i] += array2[i];
    }

    @SuppressWarnings("unused")
    public static void add(float[] array, float val){
        for (int i=0; i<array.length; i++)
            array[i] += val;
    }

    @SuppressWarnings("unused")
    public static void sub(float[] array1, float[] array2){
        if (array1.length != array2.length)
            throw new IllegalArgumentException("Lengths are not same");
        for (int i=0; i<array1.length; i++)
            array1[i] -= array2[i];
    }

    @SuppressWarnings("unused")
    public static void sub(float[] array, float val){
        for (int i=0; i<array.length; i++)
            array[i] -= val;
    }

    @SuppressWarnings("unused")
    public static void sub(float val, float[] array){
        for (int i=0; i<array.length; i++)
            array[i] = val-array[i];
    }

    @SuppressWarnings("unused")
    public static void mul(float[] array1, float[] array2){
        if (array1.length != array2.length)
            throw new IllegalArgumentException("Lengths are not same");
        for (int i=0; i<array1.length; i++)
            array1[i] *= array2[i];
    }

    @SuppressWarnings("unused")
    public static void mul(float[] array, float val){
        for (int i=0; i<array.length; i++)
            array[i] *= val;
    }

    @SuppressWarnings("unused")
    public static void div(float[] array1, float[] array2){
        if (array1.length != array2.length)
            throw new IllegalArgumentException("Lengths are not same");
        for (int i=0; i<array1.length; i++)
            array1[i] /= array2[i];
    }

    @SuppressWarnings("unused")
    public static void div(float[] array, float val){
        for (int i=0; i<array.length; i++)
            array[i] /= val;
    }

    @SuppressWarnings("unused")
    public static void div(float val, float[] array){
        for (int i=0; i<array.length; i++)
            array[i] = val/array[i];
    }

    @SuppressWarnings("unused")
    public static void normalize(float[] array){
        sub(array,min(array,true));
        mul(array,1/max(array,true));
    }

    @SuppressWarnings("unused")
    public static void log10(float[] array){
        for (int i=0; i<array.length; i++)
            array[i] = (float) Math.log10(array[i]);
    }

    @SuppressWarnings("unused")
    public static void pow(float base, float[] array){
        for (int i=0; i<array.length; i++)
            array[i] = (float) Math.pow(base,array[i]);
    }


    @SuppressWarnings("unused")
    public static void pow(float[] array, float power){
        for (int i=0; i<array.length; i++)
            array[i] = (float) Math.pow(array[i],power);
    }

    @SuppressWarnings("unused")
    public static void sin(float[] array){
        for (int i=0; i<array.length; i++)
            array[i] = (float) Math.sin(array[i]);
    }

    @SuppressWarnings("unused")
    public static void cos(float[] array){
        for (int i=0; i<array.length; i++)
            array[i] = (float) Math.cos(array[i]);
    }

    @SuppressWarnings("unused")
    public static void sinc(float[] array){
        for (int i=0; i<array.length; i++)
            if (array[i]==0)
                array[i] = 1;
            else
                array[i] = (float) Math.sin(Math.PI*array[i]);
    }

    @SuppressWarnings("unused")
    public static float sinc(float val){
        if (val==0)
            val = 1;
        else
            val = (float) Math.sin(Math.PI*val);
        return val;
    }

    @SuppressWarnings("unused")
    public static void diric(float[] array, float N){
        for (int i=0; i<array.length; i++)
            array[i] = sinc(N*i)/sinc(i);
    }

    @SuppressWarnings("unused")
    public static void atan2(float[] re, float[] im){
        for (int i=0; i<re.length; i++)
            re[i] = (float) Math.atan2(im[i],re[i]);
    }

    @SuppressWarnings("unused")
    public static boolean compare(float[] arrayA, float[] arrayB) {
        if (arrayA==null | arrayB==null)
            return false;
        if (arrayA.length != arrayB.length)
            return false;
        for (int i = 0; i < arrayA.length; i++)
            if (arrayA[i] != arrayB[i])
                return false;
        return true;
    }

    @SuppressWarnings("unused")
    public static boolean compare(int[] arrayA, int[] arrayB) {
        if (arrayA==null | arrayB==null)
            return false;
        if (arrayA.length != arrayB.length)
            return false;
        for (int i = 0; i < arrayA.length; i++)
            if (arrayA[i] != arrayB[i])
                return false;
        return true;
    }

    @SuppressWarnings("unused")
    public static void conv(float[] arrayIn, float[] arrayW, float[] arrayOut) {
        float[] arrayOutPtr = arrayOut;
        if (arrayIn == arrayW) {
            if (temp1 == null)
                temp1 = arrayW.clone();
            else if (temp1.length != arrayW.length)
                temp1 = new float[arrayW.length];
            System.arraycopy(arrayW,0,temp1,0,arrayW.length);
            arrayW = temp1;
        }
        if (arrayIn == arrayOut) {
            if (temp2 == null)
                temp2 = new float[arrayOut.length];
            else if (temp2.length != arrayOut.length)
                temp2 = new float[arrayOut.length];
            Arrays.fill(temp2,0);
            arrayOut = temp2;
        }
        int k,j,j0,j1,i,delta = arrayW.length/2;
        if (arrayIn.length == arrayOut.length){ // Same
            for (k=0; k<arrayOut.length; k++){
                j0 = Math.max(0,k+delta-arrayW.length+1);
                j1 = Math.min(k+delta,arrayIn.length-1);
                arrayOut[k] = 0;
                for (j=j0; j<=j1; j++)
                    arrayOut[k] += arrayIn[j]*arrayW[k+delta-j];
            }
        }
        else if (arrayIn.length+arrayW.length-1 == arrayOut.length){   // Full
            for (k=0; k<arrayOut.length; k++){
                j0 = Math.max(0,k-arrayW.length+1);
                j1 = Math.min(k,arrayIn.length-1);
                arrayOut[k] = 0;
                for (j=j0; j<=j1; j++)
                    arrayOut[k] += arrayIn[j]*arrayW[k-j];
            }
        }
        else Log.e("conv","Length mismatch");
        System.arraycopy(arrayOut,0,arrayOutPtr,0,arrayOut.length);
    }

    @SuppressWarnings("unused")
    public static float deg2rad(float deg){
        return (float)(deg*Math.PI/360);
    }

    @SuppressWarnings("unused")
    public static float rad2deg(float rad){
        return (float)(rad*360/Math.PI);
    }


}
