/*
 * Copyright (C) 2015 Francisco Gonzalez-Armijo Riádigos
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
package com.kuassivi.view;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

/**
 * An Android custom view to load an avatar or profile image with a progress indicator.
 * <p><b>Usage:</b>
 * <br>Set xml attributes of ImageView as usual.
 * <br>You don't need to transform resources placeholders or loaded images from Picasso
 * to rounded images!
 * <br>To animate the ring, call {@link ProgressProfileView#startAnimation()}
 * <p><b>Features:</b>
 * <ul style="margin-top:0;">
 *     <li>app:max="100" - Max value for progress indicator</li>
 *     <li>app:progress="50" - Current progress value</li>
 *     <li>app:backgroundRingSize="20dp" - Set the size of the background ring
 *     (not set, means use the same as the <i>progressRingSize</i>)</li>
 *     <li>app:progressRingSize="20dp" - Set the size of the progress ring</li>
 *     <li>app:backgroundRingColor="@color/my_color" - Set the color of the background ring
 *     (it can be an hex color as well)</li>
 *     <li>app:progressRingColor="@color/my_color" - Set the color of the progress ring
 *     (it can be an hex color as well)</li>
 *     <li>app:progressRingCap="BUTT" - Set the cap style of the progress ring
 *     (Possible values: BUTT, ROUND, SQUARE)
 *     <br><span style="color:red">Note:</span> SQUARE is not the same as BUTT</li>
 * </ul>
 *
 * @author Kuassivi, based on the Circle-Progress-View of Jakob Grabner
 * @see #startAnimation()
 * @see #getAnimator()
 */
public class ProgressProfileView extends ImageView {

    private static int ANIMATION_DURATION = 1200;
    private static int ANIMATION_DELAY = 500;

    /**
     * Progress values
     */
    private float mMax = 100;
    private float mProgress = 0;

    private float mCurrentProgress = 0;

    /**
     * Progress ring sizes
     */
    private float mBackgroundRingSize = 40;
    private float mProgressRingSize = mBackgroundRingSize;
    private boolean mProgressRingOutline = false;

    /**
     * Default progress colors
     */
    private int mBackgroundRingColor = 0xAA83d0c9;
    private int mProgressRingColor =  0xff009688;

    /**
     * Default progress ring cap
     */
    private Paint.Cap mProgressRingCap = Paint.Cap.BUTT;

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
    private int mViewWidth = 0;

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
    private Paint mMaskPaint;
    private Bitmap mOriginalBitmap;
    private Canvas mCacheCanvas;

    private int startAngel;
	
    public ProgressProfileView(Context context) {
        super(context);
    }

    public ProgressProfileView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public ProgressProfileView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressProfileView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Parse attributes
     *
     * @param attrs AttributeSet
     * @param defStyleAttr int
     * @param defStyleRes int
     */
    private void init(@Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ProgressProfileView, defStyleAttr, defStyleRes);

        setMax(a.getFloat(
            R.styleable.ProgressProfileView_max, mMax));
        setProgress(a.getFloat(
            R.styleable.ProgressProfileView_progress, mProgress));
        if (!a.hasValue(R.styleable.ProgressProfileView_backgroundRingSize)) {
            if (a.hasValue(R.styleable.ProgressProfileView_progressRingSize)) {
                setProgressRingSize(a.getDimension(
                    R.styleable.ProgressProfileView_progressRingSize, mProgressRingSize));
                setBackgroundRingSize(mProgressRingSize);
            }
        } else {
            setBackgroundRingSize(a.getDimension(
                R.styleable.ProgressProfileView_backgroundRingSize, mBackgroundRingSize));
            setProgressRingSize(a.getDimension(
                    R.styleable.ProgressProfileView_progressRingSize, mProgressRingSize));
        }
		
        setStartAngel(a.getInteger(R.styleable.ProgressProfileView_progressStartAngel, -90));
		
        setProgressRingOutline(
                a.getBoolean(R.styleable.ProgressProfileView_progressRingOutline, false));
        setBackgroundRingColor(a.getColor(
            R.styleable.ProgressProfileView_backgroundRingColor, mBackgroundRingColor));
        setProgressRingColor(a.getColor(
            R.styleable.ProgressProfileView_progressRingColor, mProgressRingColor));
        setProgressRingCap(a.getInt(
            R.styleable.ProgressProfileView_progressRingCap, Paint.Cap.BUTT.ordinal()));

        a.recycle();

        setupAnimator();
    }

    /**
     * Measure to square the view
     *
     * @param widthMeasureSpec int
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
     * This method is called after measuring the dimensions of MATCH_PARENT and WRAP_CONTENT
     * Save these dimensions to setup the bounds and paints
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Save current view dimensions
        mViewWidth = w;
        mViewHeight = h;

        // Apply ring as outline
        if(isProgressRingOutline()) {
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
        if(isProgressRingOutline()) {
            outline = Float.valueOf(-mBackgroundRingSize).intValue();
        }

        // Save padding plus offset
        mPaddingTop = outline + this.getPaddingTop() + (yOffset / 2);
        mPaddingBottom = outline + this.getPaddingBottom() + (yOffset / 2);
        mPaddingLeft = outline + this.getPaddingLeft() + (xOffset / 2);
        mPaddingRight = outline + this.getPaddingRight() + (xOffset / 2);

        // Bigger ring size
        float biggerRingSize = mBackgroundRingSize > mProgressRingSize
                ? mBackgroundRingSize : mProgressRingSize;

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
        mProgressRingPaint.setColor(mProgressRingColor);
        mProgressRingPaint.setAntiAlias(true);
        mProgressRingPaint.setStrokeCap(mProgressRingCap);
        mProgressRingPaint.setStyle(Paint.Style.STROKE);
        mProgressRingPaint.setStrokeWidth(mProgressRingSize);
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
     * <br>It starts with a default delay of 500 milliseconds
     * <p>You can get an instance of the animator with the method
     * {@link ProgressProfileView#getAnimator()} and Override these values
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
        if(mMaskPaint == null) {
            setupMask();
        }

        // Cache the canvas
        if(mCacheCanvas == null) {
            mCacheCanvas = new Canvas(mOriginalBitmap);
        }

        // ImageView
        super.onDraw(mCacheCanvas);

        // Crop ImageView resource to a circle
        canvas.drawCircle(
                mRingBounds.centerX(),
                mRingBounds.centerY(),
                (mRingBounds.width() / 2) - (mBackgroundRingSize / 2),
                mMaskPaint);

        // Draw the background ring
        if (mBackgroundRingSize > 0){
            canvas.drawArc(mRingBounds, 360, 360, false, mBackgroundRingPaint);
        }
        // Draw the progress ring
        if(mProgressRingSize > 0) {
            canvas.drawArc(mRingBounds, startAngel, getSweepAngle(), false, mProgressRingPaint);
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
     * <br>You can e.g. add Listeners to it
     *
     * @return {@link ObjectAnimator}
     */
    public ObjectAnimator getAnimator() {
        return mAnimator;
    }

    public float getMax() {
        return mMax;
    }

    public void setMax(float max) {
        mMax = max;
    }

    public float getCurrentProgress() {
        return mCurrentProgress;
    }

    public void setCurrentProgress(float currentProgress) {
        mCurrentProgress = currentProgress;
    }

    public float getProgress() {
        return mProgress;
    }

    public void setProgress(float progress) {
        if (progress < 0)
            this.mProgress = 0;
        else if (progress > 100)
            this.mProgress = 100;
        else
            this.mProgress = progress;
        invalidate();
    }

    public float getProgressRingSize() {
        return mProgressRingSize;
    }

    public void setProgressRingSize(float progressRingSize) {
        mProgressRingSize = progressRingSize;
    }

    public float getBackgroundRingSize() {
        return mBackgroundRingSize;
    }

    public void setBackgroundRingSize(float backgroundRingSize) {
        mBackgroundRingSize = backgroundRingSize;
    }

    public boolean isProgressRingOutline() {
        return mProgressRingOutline;
    }

    public void setProgressRingOutline(boolean progressRingOutline) {
        mProgressRingOutline = progressRingOutline;
    }

    public int getBackgroundRingColor() {
        return mBackgroundRingColor;
    }

    public void setBackgroundRingColor(int backgroundRingColor) {
        mBackgroundRingColor = backgroundRingColor;
    }

    public int getProgressRingColor() {
        return mProgressRingColor;
    }

    public void setProgressRingColor(int progressRingColor) {
        mProgressRingColor = progressRingColor;
    }

    public Paint.Cap getProgressRingCap() {
        return mProgressRingCap;
    }

    public void setProgressRingCap(int progressRingCap) {
        mProgressRingCap = getCap(progressRingCap);
    }

    public void setStartAngel(int angel){
        startAngel = angel ;
    }

    public int getStartAngel() {
        return startAngel;
    }

    private Paint.Cap getCap(int id) {
        for (Paint.Cap value : Paint.Cap.values()) {
            if (id == value.ordinal()) {
                return value;
            }
        }
        return Paint.Cap.BUTT;
    }
	

	
}
