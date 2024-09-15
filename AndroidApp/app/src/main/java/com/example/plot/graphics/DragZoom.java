package com.example.plot.graphics;

public class DragZoom {
    public final static char NO_INIT = 1, INIT_REQUIRE = 2;
    public float leftX = -1, rightX = 1, bottomY = -1, topY = 1,
            leftInitX = -1, rightInitX = 1, bottomInitY = -1, topInitY = 1,
            width, height, dpi, fps;
    public char initRequired = INIT_REQUIRE;
    private final StringBuilder stringBuilder = new StringBuilder();

    DragZoom(){

    }

    DragZoom(DragZoom v){
        this.leftX = v.leftX;
        this.rightX = v.rightX;
        this.topY = v.topY;
        this.bottomY = v.bottomY;
    }

    void copy(DragZoom v){
        this.leftX = v.leftX;
        this.rightX = v.rightX;
        this.topY = v.topY;
        this.bottomY = v.bottomY;
        this.leftInitX = v.leftInitX;
        this.rightInitX = v.rightInitX;
        this.topInitY = v.topInitY;
        this.bottomInitY = v.bottomInitY;
        this.width = v.width;
        this.height = v.height;
    }

    public float valueToPixelX(float val){
        return width/(rightX-leftX)*(val-leftX);
    }
    public float valueToPixelY(float val){
        return height/(bottomY-topY)*(val-topY);
    }
    public float pixelToValueX(float pix){
        return (rightX-leftX)/width*pix+leftX;
    }
    public float pixelToValueY(float pix){
        return (bottomY-topY)/height*pix+topY;
    }

    private float prev;
    private final float[] gridStep = {0.1f, 0.2f, 0.4f, 1f, 2f};
    public float stepCalculate(float delta, float range){
        if (delta==0)
            return 1;
        float step = 5;
        delta = Math.abs(delta);
        if (delta > 10* step)
            while (delta > 10* step)
                step *= 10f;
        else if (delta <= step)
            while (delta <= step)
                step *= 0.1f;
        delta /= Math.round(range / dpi * 2f);
        int i; float current;
        for (i =0; i <gridStep.length; i++) {
            current = Math.abs(delta - step * gridStep[i]);
            if (current >=prev & i >0) {
                step *= gridStep[i -1];
                return step;
            }
            prev = current;
        }
        return step *2;
    }

    public String parseValue(float n) {
        int order = 0;
        if (Math.abs(n)>999){
            while (Math.abs(n)>999){
                n /= 1e3;
                order++;
            }
        } else if (Math.abs(n)<0.001){
            while (Math.abs(n)<0.001 & n!=0){
                n *= 1e3;
                order--;
            }
        }
        int l = stringBuilder.length();
        if (l > 0) {
            stringBuilder.delete(0, l);
        }
        stringBuilder.append(n);
        l = stringBuilder.length();
        int dot = 0;
        for (int i = 0; i < l; i++){
            if (stringBuilder.charAt(i) == '.')
                dot = i;
        }
        int nz = dot;
        for (int i = dot + 1; i < Math.min(l, dot + 4); i++)
            if (stringBuilder.charAt(i) != '0')
                nz = i;
        if (nz != l -1) {
            if (nz != dot)
                nz++;
            stringBuilder.delete(nz, l);
        }
        switch (order) {
            case 1: stringBuilder.append('K'); break;
            case 2: stringBuilder.append('M'); break;
            case 3: stringBuilder.append('G'); break;
            case 4: stringBuilder.append('T'); break;
            case -1: stringBuilder.append('m'); break;
            case -2: stringBuilder.append('u'); break;
            case -3: stringBuilder.append('n'); break;
            case -4: stringBuilder.append('p'); break;
            case -5: stringBuilder.append('f'); break;
        }
        return stringBuilder.toString();
    }
}
