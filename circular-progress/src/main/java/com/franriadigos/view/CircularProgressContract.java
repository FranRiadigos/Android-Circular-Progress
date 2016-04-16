package com.franriadigos.view;

import android.graphics.Paint;

public interface CircularProgressContract {
    int ANIMATION_DURATION = 1200;
    int ANIMATION_DELAY    = 500;
    int ANGLE_360          = 360;
    int ANGLE_90           = 90;

    int DEFAULT_BG_COLOR   = 0xAA83d0c9;
    int DEFAULT_RING_COLOR = 0xff009688;

    float getMax();

    void setMax(float max);

    float getCurrentProgress();

    void setCurrentProgress(float currentProgress);

    float getProgress();

    void setProgress(float progress);

    float getProgressRingSize();

    void setProgressRingSize(float progressRingSize);

    float getBackgroundRingSize();

    void setBackgroundRingSize(float backgroundRingSize);

    boolean isProgressRingOutline();

    void setProgressRingOutline(boolean progressRingOutline);

    int getBackgroundRingColor();

    void setBackgroundRingColor(int backgroundRingColor);

    int getProgressRingColor();

    void setProgressRingColor(int progressRingColor);

    int[] getProgressGradient();

    void setProgressGradient(int[] progressGradient);

    boolean isJoinGradient();

    void setJoinGradient(boolean isJoinGradient);

    float getGradientFactor();

    void setGradientFactor(float gradientFactor);

    Paint.Cap getProgressRingCorner();

    void setProgressRingCorner(int progressRingCorner);

    Paint.Cap getCap(int id);
}
