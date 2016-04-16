/*
 * Copyright (C) 2015-2020 Francisco Gonzalez-Armijo Riádigos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.franriadigos.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

/**
 * Android custom view to load a circular progress indicator based on {@link FrameLayout}.
 * <p/>
 * <b>Usage:</b>
 * <br>You can set {@link FrameLayout} xml attributes as usual.
 * <br>To animate the progress indicator call {@link FrameLayoutCircularProgress#startAnimation()}
 * <p/>
 * <b>Features:</b>
 * <ul style="margin-top:0;">
 * <li>app:max="100" - Max value for progress indicator</li>
 * <li>app:progress="50" - Current progress value</li>
 * <li>app:backgroundRingSize="20dp" - The size of the background progress indicator
 * (if not set it use the same as <i>progressRingSize</i>)</li>
 * <li>app:progressRingSize="20dp" - The size of the progress indicator</li>
 * <li>app:backgroundRingColor="@color/my_color" - The color of the background progress indicator
 * (it can be an hex color as well)</li>
 * <li>app:progressRingColor="@color/my_color" - The color of the progress indicator
 * (it can be an hex color as well)</li>
 * <li>app:progressGradient="@array/colors" - An array of colors for a gradient indicator
 * (you must provide an array resource reference)</li>
 * <li>app:joinGradient="true" - By enabling this you get a smooth gradient on the ring corners</li>
 * <li>app:gradientFactor="1.0" - Adjust the gradient factor of the ring</li>
 * <li>app:progressRingOutline="true" - Sets the ring as an Outline based on the padding of the
 * {@link FrameLayout}, by default is false</li>
 * <li>app:progressRingCorner="ROUND" - Sets the corner style of the progress indicator
 * (by default is RECT -> Square)</li>
 * </ul>
 *
 * @author Fran Riadigos, based on the Circle-Progress-View of Jakob Grabner
 * @see #startAnimation()
 * @see #getAnimator()
 */
public class FrameLayoutCircularProgress extends FrameLayout implements CircularProgressContract {

    /**
     * Progress values
     */
    private float mMax      = 100;
    private float mProgress = 0;
    private float mCurrentProgress = 0;

    /**
     * Progress ring sizes
     */
    private float   mBackgroundRingSize  = 40;
    private float   mProgressRingSize    = mBackgroundRingSize;
    private boolean mProgressRingOutline = false;

    /**
     * Default progress colors
     */
    private int mBackgroundRingColor = DEFAULT_BG_COLOR;
    private int mProgressRingColor   = DEFAULT_RING_COLOR;
    private int[]   mProgressGradient;
    private boolean mIsJoinGradient;
    private float   mGradientFactor;

    /**
     * Default progress ring cap
     */
    private Paint.Cap mProgressRingCorner = Paint.Cap.BUTT;

    /*
     * Animator
     */
    private ObjectAnimator mAnimator;

    /*
     * Default interpolator
     */
    private Interpolator mDefaultInterpolator = new OvershootInterpolator();

    /*
     * Default sizes
     */
    private int mViewHeight = 0;
    private int mViewWidth  = 0;

    /*
     * Default padding
     */
    private int mPaddingTop;
    private int mPaddingBottom;
    private int mPaddingLeft;
    private int mPaddingRight;

    /*
     * Paints
     */
    private Paint mProgressRingPaint;
    private Paint mBackgroundRingPaint;

    /*
     * Bounds of the ring
     */
    private RectF mRingBounds;
    private float mOffsetRingSize;

    /*
     * Masks for clipping the current drawable in a circle
     */
    private Paint  mMaskPaint;
    private Bitmap mOriginalBitmap;
    private Canvas mCacheCanvas;

    public FrameLayoutCircularProgress(Context context) {
        this(context, null);
    }

    public FrameLayoutCircularProgress(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FrameLayoutCircularProgress(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FrameLayoutCircularProgress(@NonNull Context context, @Nullable AttributeSet attrs,
                                       @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        new AttributesHelper(this).loadFromAttributes(attrs, defStyleAttr, 0);
        setupAnimator();
    }

    /**
     * Measure to square the view
     *
     * @param widthMeasureSpec  int
     * @param heightMeasureSpec int
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Process complexity measurements
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Squared size
        int size;

        // Get getMeasuredWidth() and getMeasuredHeight().
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // Remove padding to avoid bad size ratio calculation
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();

        // Depending on the size ratio, calculate the final size without padding
        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        // Report back the measured size.
        // Add pending padding
        setMeasuredDimension(
                size + getPaddingLeft() + getPaddingRight(),
                size + getPaddingTop() + getPaddingBottom());
    }

    /**
     * This method is called after measuring the dimensions of MATCH_PARENT and WRAP_CONTENT Save
     * these dimensions to setup the bounds and paints
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Save current view dimensions
        mViewWidth = w;
        mViewHeight = h;

        // Apply ring as outline
        if (isProgressRingOutline()) {
            setPadding(
                    Float.valueOf(mBackgroundRingSize + getPaddingLeft()).intValue(),
                    Float.valueOf(mBackgroundRingSize + getPaddingTop()).intValue(),
                    Float.valueOf(mBackgroundRingSize + getPaddingRight()).intValue(),
                    Float.valueOf(mBackgroundRingSize + getPaddingBottom()).intValue());
        }

        setupBounds();
        setupBackgroundRingPaint();
        setupProgressRingPaint();

        requestLayout();
        invalidate();
    }

    /**
     * Set the common bounds of the rings
     */
    private void setupBounds() {
        // Min value for squared size
        int minValue = Math.min(mViewWidth, mViewHeight);

        // Calculate the Offset if needed
        int xOffset = mViewWidth - minValue;
        int yOffset = mViewHeight - minValue;

        // Apply ring as outline
        int outline = 0;
        if (isProgressRingOutline()) {
            outline = Float.valueOf(-mBackgroundRingSize).intValue();
        }

        // Save padding plus offset
        mPaddingTop = outline + this.getPaddingTop() + (yOffset / 2);
        mPaddingBottom = outline + this.getPaddingBottom() + (yOffset / 2);
        mPaddingLeft = outline + this.getPaddingLeft() + (xOffset / 2);
        mPaddingRight = outline + this.getPaddingRight() + (xOffset / 2);

        // Bigger ring size
        float biggerRingSize = mBackgroundRingSize > mProgressRingSize
                               ? mBackgroundRingSize
                               : mProgressRingSize;

        // Save the half of the progress ring
        mOffsetRingSize = biggerRingSize / 2;

        int width = getWidth();
        int height = getHeight();

        // Create the ring bounds Rect
        mRingBounds = new RectF(
                mPaddingLeft + mOffsetRingSize,
                mPaddingTop + mOffsetRingSize,
                width - mPaddingRight - mOffsetRingSize,
                height - mPaddingBottom - mOffsetRingSize);
    }

    private void setupMask() {
        mOriginalBitmap = Bitmap.createBitmap(
                getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Shader shader = new BitmapShader(mOriginalBitmap,
                                         Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mMaskPaint = new Paint();
        mMaskPaint.setAntiAlias(true);
        mMaskPaint.setShader(shader);
    }

    private void setupProgressRingPaint() {
        mProgressRingPaint = new Paint();
        mProgressRingPaint.setAntiAlias(true);
        mProgressRingPaint.setStrokeCap(mProgressRingCorner);
        mProgressRingPaint.setStyle(Paint.Style.STROKE);
        mProgressRingPaint.setStrokeWidth(mProgressRingSize);
        mProgressRingPaint.setColor(mProgressRingColor);

        if (mProgressGradient != null) {
            int[] colors = mProgressGradient;
            float[] positions;
            if (isJoinGradient()) {
                colors = new int[mProgressGradient.length + 1];
                positions = new float[colors.length];
                int i = 0;
                positions[i] = i;
                for (int color : mProgressGradient) {
                    colors[i] = color;
                    if (i == mProgressGradient.length - 1) {
                        positions[i] = (ANGLE_360 - mProgressRingSize * getGradientFactor())
                                       / ANGLE_360;
                    } else if (i > 0) {
                        positions[i] = ((float) i / (float) colors.length);
                    }
                    i++;
                }
                colors[i] = colors[0];
                positions[i] = 1;
            }

            SweepGradient gradient = new SweepGradient(mRingBounds.centerX(),
                                                       mRingBounds.centerY(),
                                                       colors, null);

            mProgressRingPaint.setShader(gradient);
            Matrix matrix = new Matrix();
            mProgressRingPaint.getShader().setLocalMatrix(matrix);
            matrix.postTranslate(-mRingBounds.centerX(), -mRingBounds.centerY());
            matrix.postRotate(-ANGLE_90);
            matrix.postTranslate(mRingBounds.centerX(), mRingBounds.centerY());
            mProgressRingPaint.getShader().setLocalMatrix(matrix);
            mProgressRingPaint.setColor(mProgressGradient[0]);
        }
    }

    private void setupBackgroundRingPaint() {
        mBackgroundRingPaint = new Paint();
        mBackgroundRingPaint.setColor(mBackgroundRingColor);
        mBackgroundRingPaint.setAntiAlias(true);
        mBackgroundRingPaint.setStyle(Paint.Style.STROKE);
        mBackgroundRingPaint.setStrokeWidth(mBackgroundRingSize);
    }

    private void setupAnimator() {
        mAnimator = ObjectAnimator.ofFloat(
                this, "progress", this.getProgress(), this.getProgress());
        mAnimator.setDuration(ANIMATION_DURATION);
        mAnimator.setInterpolator(mDefaultInterpolator);
        mAnimator.setStartDelay(ANIMATION_DELAY);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setCurrentProgress((float) animation.getAnimatedValue());
                setProgress(getCurrentProgress());
            }
        });
    }

    /**
     * It will start animating the progress ring to the progress value set
     * <br>Default animation duration is 1200 milliseconds
     * <br/>It starts with a default delay of 500 milliseconds
     * <br/>You can get an instance of the animator with the method {@link
     * FrameLayoutCircularProgress#getAnimator()} and Override these values
     *
     * @see ObjectAnimator
     */
    @SuppressWarnings("unused")
    public void startAnimation() {
        float finalProgress = this.getProgress();
        this.setProgress(this.getCurrentProgress());
        mAnimator.setFloatValues(this.getCurrentProgress(), finalProgress);
        mAnimator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Setup the mask at first
        if (mMaskPaint == null) {
            setupMask();
        }

        // Cache the canvas
        if (mCacheCanvas == null) {
            mCacheCanvas = new Canvas(mOriginalBitmap);
        }

        super.onDraw(mCacheCanvas);
//
//        // Crop ImageView resource to a circle
//        canvas.drawCircle(
//                mRingBounds.centerX(),
//                mRingBounds.centerY(),
//                (mRingBounds.width() / 2) - (mBackgroundRingSize / 2),
//                mMaskPaint);

        // Draw the background ring
        if (mBackgroundRingSize > 0) {
            canvas.drawArc(mRingBounds, ANGLE_360, ANGLE_360, false, mBackgroundRingPaint);
        }
        // Draw the progress ring
        if (mProgressRingSize > 0) {
            canvas.drawArc(mRingBounds, -ANGLE_90, getSweepAngle(), false, mProgressRingPaint);
        }
    }

    private float getSweepAngle() {
        return (360f / mMax * mProgress);
    }

    /* *************************
     * GETTERS & SETTERS
     * *************************/

    /**
     * Get an instance of the current {@link ObjectAnimator}
     * <br/>You can e.g. add Listeners to it
     *
     * @return {@link ObjectAnimator}
     */
    public ObjectAnimator getAnimator() {
        return mAnimator;
    }

    @Override
    public float getMax() {
        return mMax;
    }

    @Override
    public void setMax(float max) {
        mMax = max;
    }

    @Override
    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    @Override
    public void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
    }

    @Override
    public float getProgress() {
        return mProgress;
    }

    @Override
    public void setProgress(float progress) {
        if (progress < 0) {
            this.mProgress = 0;
        } else if (progress > 100) {
            this.mProgress = 100;
        } else {
            this.mProgress = progress;
        }
        invalidate();
    }

    @Override
    public float getProgressRingSize() {
        return mProgressRingSize;
    }

    @Override
    public void setProgressRingSize(float progressRingSize) {
        mProgressRingSize = progressRingSize;
    }

    @Override
    public float getBackgroundRingSize() {
        return mBackgroundRingSize;
    }

    @Override
    public void setBackgroundRingSize(float backgroundRingSize) {
        mBackgroundRingSize = backgroundRingSize;
    }

    @Override
    public boolean isProgressRingOutline() {
        return mProgressRingOutline;
    }

    @Override
    public void setProgressRingOutline(boolean progressRingOutline) {
        mProgressRingOutline = progressRingOutline;
    }

    @Override
    public int getBackgroundRingColor() {
        return mBackgroundRingColor;
    }

    @Override
    public void setBackgroundRingColor(int backgroundRingColor) {
        mBackgroundRingColor = backgroundRingColor;
    }

    @Override
    public int getProgressRingColor() {
        return mProgressRingColor;
    }

    @Override
    public void setProgressRingColor(int progressRingColor) {
        mProgressRingColor = progressRingColor;
    }

    @Override
    public int[] getProgressGradient() {
        return mProgressGradient;
    }

    @Override
    public void setProgressGradient(int[] progressGradient) {
        this.mProgressGradient = progressGradient;
    }

    @Override
    public boolean isJoinGradient() {
        return mIsJoinGradient;
    }

    @Override
    public void setJoinGradient(boolean isJoinGradient) {
        this.mIsJoinGradient = isJoinGradient;
    }

    @Override
    public float getGradientFactor() {
        return mGradientFactor;
    }

    @Override
    public void setGradientFactor(float gradientFactor) {
        this.mGradientFactor = gradientFactor;
    }

    @Override
    public Paint.Cap getProgressRingCorner() {
        return mProgressRingCorner;
    }

    @Override
    public void setProgressRingCorner(int progressRingCorner) {
        mProgressRingCorner = getCap(progressRingCorner);
    }

    @Override
    public Paint.Cap getCap(int id) {
        for (Paint.Cap value : Paint.Cap.values()) {
            if (id == value.ordinal()) {
                return value;
            }
        }
        return Paint.Cap.BUTT;
    }
}
