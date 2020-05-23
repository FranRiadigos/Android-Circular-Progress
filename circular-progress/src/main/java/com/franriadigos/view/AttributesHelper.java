package com.franriadigos.view;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

@RestrictTo(LIBRARY_GROUP_PREFIX)
public class AttributesHelper {
    @NonNull
    private final CircularProgressContract mView;

    public AttributesHelper(@NonNull CircularProgressContract view) {
        mView = view;
    }

    public void loadFromAttributes(@Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        final TypedArray a = ((View) mView).getContext().obtainStyledAttributes(
                attrs, R.styleable.CircularProgress, defStyleAttr, defStyleRes);

        mView.setMax(a.getFloat(
                R.styleable.CircularProgress_max, mView.getMax()));
        mView.setProgress(a.getFloat(
                R.styleable.CircularProgress_progress, mView.getProgress()));
        if (!a.hasValue(R.styleable.CircularProgress_backgroundRingSize)) {
            if (a.hasValue(R.styleable.CircularProgress_progressRingSize)) {
                mView.setProgressRingSize(a.getDimension(
                        R.styleable.CircularProgress_progressRingSize, mView.getProgressRingSize()));
                mView.setBackgroundRingSize(mView.getProgressRingSize());
            }
        } else {
            mView.setBackgroundRingSize(a.getDimension(
                    R.styleable.CircularProgress_backgroundRingSize, mView.getBackgroundRingSize()));
            mView.setProgressRingSize(a.getDimension(
                    R.styleable.CircularProgress_progressRingSize, mView.getProgressRingSize()));
        }
        mView.setProgressRingOutline(
                a.getBoolean(R.styleable.CircularProgress_progressRingOutline, false));
        mView.setBackgroundRingColor(a.getColor(
                R.styleable.CircularProgress_backgroundRingColor, mView.getBackgroundRingColor()));
        mView.setProgressRingColor(a.getColor(
                R.styleable.CircularProgress_progressRingColor, CircularProgressContract.DEFAULT_RING_COLOR));

        try {
            if (a.hasValue(R.styleable.CircularProgress_progressGradient)) {
                int[] gradient;
                int i = -1;
                try {
                    int resourceId = a
                            .getResourceId(R.styleable.CircularProgress_progressGradient, 0);
                    if(((View) mView).isInEditMode()) {
                        String[] gradientRes = ((View) mView).getResources().getStringArray(resourceId);
                        gradient = new int[gradientRes.length];
                        i = 0;
                        for (String color : gradientRes) {
                            gradient[i] = Color.parseColor(color);
                            i++;
                        }
                    } else {
                        if(!a.getResources().getResourceTypeName(resourceId).equals("array")) {
                            throw new IllegalArgumentException("Resource is not an array");
                        }
                        TypedArray ta = a.getResources().obtainTypedArray(resourceId);
                        int len = ta.length();
                        gradient = new int[len];
                        i = 0;
                        for (int c = 0; c < len; c++) {
                            String colorString = ta.getString(c);
                            if(colorString != null) {
                                gradient[i] = Color.parseColor(colorString);
                                i++;
                            } else {
                                throw new IllegalArgumentException();
                            }
                        }
                        ta.recycle();
                    }
                } catch (IllegalArgumentException e) {
                    if(i == -1) {
                        throw e;
                    }
                    throw new IllegalArgumentException("Unknown Color at position " + i);
                }

                mView.setProgressGradient(gradient);

                mView.setJoinGradient(a.getBoolean(R.styleable.CircularProgress_joinGradient, false));

                mView.setGradientFactor(
                        a.getFloat(R.styleable.CircularProgress_gradientFactor, 1f));
            }
        } catch (Exception e) {
            if(! ((View) mView).isInEditMode()) {
                throw e;
            }
        }

        mView.setProgressRingCorner(a.getInt(
                R.styleable.CircularProgress_progressRingCorner, Paint.Cap.BUTT.ordinal()));

        a.recycle();
    }
}
