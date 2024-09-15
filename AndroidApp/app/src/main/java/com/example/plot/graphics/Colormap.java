package com.example.plot.graphics;

import android.graphics.Color;

import androidx.annotation.NonNull;

public class Colormap {
//    private static float r, g, b;

    static public final char HSV = 0,
        GRAY = 1,
        JET = 2,
        COOL = 3,
        SPRING = 4,
        SUMMER = 5,
        AUTUMN = 6,
        WINTER = 7,
        HOT = 8,
        COPPER = 9,
        BONE = 10,
        PINK = 11;

    static int hsv2rgb(float h, float s, float v){
        float c = s*v;
        h = (6*h)%6;
        float x = c*(1-Math.abs(h%2-1));
        float m = v-c;
        float r = 0, g = 0, b = 0;

        h = (float) Math.floor(h);
        switch ((int) h){
            case 0:
                r = c;
                g = x;
                b = 0;
                break;
            case 1:
                r = x;
                g = c;
                b = 0;
                break;
            case 2:
                r = 0;
                g = c;
                b = x;
                break;
            case 3:
                r = 0;
                g = x;
                b = c;
                break;
            case 4:
                r = x;
                g = 0;
                b = c;
                break;
            case 5:
                r = c;
                g = 0;
                b = x;
                break;
        }
        r += m;
        g += m;
        b += m;
        return colorPack(r, g, b);
    }

    private static int colorPack(float r, float g, float b){
        int rgb = 0xFF;
        rgb <<= 8;
        rgb |= floatToInt(r);
        rgb <<= 8;
        rgb |= floatToInt(g);
        rgb <<= 8;
        rgb |= floatToInt(b);
        return rgb;
    }

    private static int floatToInt(float c){
        c = Math.round(0xFF*c);
        return (int) c;
    }

    @SuppressWarnings("unused")
    public static int setAlpha(int rgb, float alpha){
        int c = floatToInt(alpha);
        c <<= 24;
        rgb &= 0x00FFFFFF;
        c |= rgb;
        return c;
    }

    // Colormaps
    public static int toInt(float x,char colormap){
        if (x>1f)
            x = 1f;
        if (x<0)
            x = 0f;
        switch (colormap){
            case HSV:
                return hue(x);
            default:
            case GRAY:
                return gray(x);
            case JET:
                return jet(x);
            case COOL:
                return cool(x);
            case SPRING:
                return spring(x);
            case SUMMER:
                return summer(x);
            case AUTUMN:
                return autumn(x);
            case WINTER:
                return winter(x);
            case HOT:
                return hot(x);
            case COPPER:
                return copper(x);
            case BONE:
                return bone(x);
            case PINK:
                return pink(x);
        }
    }

    @SuppressWarnings("unused")
    static public void arrayToInt(float[] vector, int[] color, char colormap){
        for (int i=0; i< vector.length; i++)
            color[i] = toInt(vector[i],colormap);
    }

    @SuppressWarnings("unused")
    static public void arrayToInt(float[] vector, int[] color, char colormap
            , float minValue, float maxValue){
        float c = 0;
        if (maxValue!=minValue)
            c = 1/(maxValue-minValue);
        for (int i=0; i< vector.length; i++) {
            if (Float.isNaN(vector[i]))
                color[i] = Color.TRANSPARENT;
            else
                color[i] = toInt((vector[i] - minValue) * c, colormap);
        }
    }

    @SuppressWarnings("unused")
    static public void arrayToInt(float[] arrayY, float[] arrayX, int[] color, char colormap
            , float minValue, float maxValue, float minX, float maxX){
        float c = 0;
        if (maxValue!=minValue)
            c = 1/(maxValue-minValue);
        for (int i=0; i< arrayY.length; i++) {

            if (Float.isNaN(arrayY[i]))
                color[i] = Color.BLACK;
            else
                color[i] = toInt((arrayY[i] - minValue) * c, colormap);
        }
    }

    @SuppressWarnings("unused")
    static public void arrayToInt(float[][] matrix, int[] color, char colormap
            , float minValue, float maxValue){
        float c = 0;
        int i,j,k = 0;
        if (maxValue!=minValue)
            c = 1/(maxValue-minValue);
        for (i=0; i< matrix.length; i++)
            for (j=0; j<matrix[0].length; j++)
                if (Float.isNaN(matrix[i][j]))
                    color[k++] = Color.TRANSPARENT;
                else
                    color[k++] = toInt((matrix[i][j] - minValue) * c, colormap);
    }

    static int hue(float x){
        return hsv2rgb(x,1,1);
    }

    static int jet(float x){
        float r = 0, g = 0, b = 0;
        float s = 4;
        float d = 0.125f;
            if (x>=0 & x<d){
                r = 0;
                g = 0;
                b = s*(x+d);
            }
            x -= d;
            if (x>=0 & x<2*d){
                r = 0;
                g = s*x;
                b = 1;
            }
            x -= 2*d;
            if (x>=0 & x<2*d){
                r = s*x;
                g = 1;
                b = 1-s*x;
            }
            x -= 2*d;
            if (x>=0 & x<2*d){
                r = 1;
                g = 1-s*x;
                b = 0;
            }
            x -= 2*d;
            if (x>=0){
                r = 1-s*x;
                g = 0;
                b = 0;
            }
        return colorPack(r, g, b);
    }

    static int gray(float x){
        float r, g , b;
        r = x;
        g = x;
        b = x;
        return colorPack(r, g, b);
    }

    static int cool(float x){
        float r, g, b;
        r = x;
        g = 1-x;
        b = 1;
        return colorPack(r, g, b);
    }

    static int spring(float x){
        float r, g, b;
        r = 1;
        g = x;
        b = 1-x;
        return colorPack(r, g, b);
    }

    static int summer(float x){
        float r, g, b;
        r = x;
        g = 0.5f*(1-x);
        b = 0.4f;
        return colorPack(r, g, b);
    }

    static int autumn(float x){
        float r, g, b;
        r = 1;
        g = x;
        b = 0;
        return colorPack(r, g, b);
    }

    static int winter(float x){
        float r, g, b;
        r = 0;
        g = x;
        b = 1-0.5f*x;
        return colorPack(r, g, b);
    }

    static int hot(float x){
        float r = 0, g = 0, b = 0;
        float s = 3;
        float d = 1/s;
            if (x>=0 & x<d){
                r = s*x;
                g = 0;
                b = 0;
            }
            x -= d;
            if (x>=0 & x<d){
                r = 1;
                g = s*x;
                b = 0;
            }
            x -= d;
            if (x>=0){
                r = 1;
                g = 1;
                b = s*x;
            }
        return colorPack(r, g, b);
    }

    static int copper(float x){
        float r, g, b;
        r = (0<=x & x<0.8f) ? 1.25f*x : 1;
        g = 0.8f*x;
        b = 0.5f*x;
        return colorPack(r, g, b);
    }

    static int bone(float x){
        float r = 0, g = 0, b = 0;
        float d = 1/3f;
        float a = 0.4f;
        float s = 1-a*d;
            if (x>=0 & x<d){
                r = s*x;
                g = s*x;
                b = (s+a)*x;
            }
            x -= d;
            if (x>=0 & x<d){
                r = s*(d+x);
                g = s*d+(s+a)*x;
                b = (s+a)*d+s*x;
            }
            x -= d;
            if (x>=0){
                r = 2*s*d+(s+a)*x;
                g = (2*s+a)*d+s*x;
                b = (2*s+a)*d+s*x;
            }
        return colorPack(r, g, b);
    }

    static int pink(float x){
        int rgb;
        float r1, g1, b1, r2, g2, b2;
        float r, g, b ;

        rgb = gray(x);
        b1 = (float) (rgb&0xFF)/0xFF;
        rgb >>= 8;
        g1 = (float) (rgb&0xFF)/0xFF;
        rgb >>= 8;
        r1 = (float) (rgb&0xFF)/0xFF;

        rgb = hot(x);
        b2 = (float) (rgb&0xFF)/0xFF;
        rgb >>= 8;
        g2 = (float) (rgb&0xFF)/0xFF;
        rgb >>= 8;
        r2 = (float) (rgb&0xFF)/0xFF;

        r = (float) Math.sqrt((2*r1+r2)/3f);
        g = (float) Math.sqrt((2*g1+g2)/3f);
        b = (float) Math.sqrt((2*b1+b2)/3f);
        return colorPack(r, g, b);
    }

}
