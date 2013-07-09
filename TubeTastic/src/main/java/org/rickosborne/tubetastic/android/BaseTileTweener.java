package org.rickosborne.tubetastic.android;

import aurelienribon.tweenengine.TweenAccessor;

public class BaseTileTweener implements TweenAccessor<BaseTile> {

    public static final int X        =  1;
    public static final int Y        =  2;
    public static final int SCALE    =  4;
    public static final int ROTATION =  8;
    public static final int ALPHA    = 16;

    @Override
    public int getValues(BaseTile target, int tweenType, float[] returnValues) {
        int valueCount = 0;
        if ((tweenType & X)        != 0) { returnValues[++valueCount] = target.getX(); }
        if ((tweenType & Y)        != 0) { returnValues[++valueCount] = target.getY(); }
        if ((tweenType & SCALE)    != 0) { returnValues[++valueCount] = target.getScale(); }
        if ((tweenType & ROTATION) != 0) { returnValues[++valueCount] = target.getRotation(); }
        if ((tweenType & ALPHA)    != 0) { returnValues[++valueCount] = target.getAlpha(); }
        return valueCount;
    }

    @Override
    public void setValues(BaseTile target, int tweenType, float[] newValues) {
        int valueCount = -1;
        if ((tweenType & X)        != 0) { target.setX(newValues[++valueCount]); }
        if ((tweenType & Y)        != 0) { target.setY(newValues[++valueCount]); }
        if ((tweenType & SCALE)    != 0) { target.setScale(newValues[++valueCount]); }
        if ((tweenType & ROTATION) != 0) { target.setRotation(newValues[++valueCount]); }
        if ((tweenType & ALPHA)    != 0) { target.setAlpha(newValues[++valueCount]); }
    }

}
