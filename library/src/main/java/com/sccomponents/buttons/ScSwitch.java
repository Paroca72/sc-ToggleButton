package com.sccomponents.buttons;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Create a simple switch control extending the ScToggleButton class.
 */

public class ScSwitch extends ScToggleButton {

    // ***************************************************************************************
    // Constants and statics

    private static final int MIN_WIDTH = 48;
    private static final int MIN_HEIGHT = 24;
    private static final long ANIMATION_DURATION = 100;
    private static final int FONT_SIZE = 10;


    // ***************************************************************************************
    // Private and protected attributes

    protected int mBackgroundColor = Color.parseColor("#803F51B5");
    protected boolean mAnimate = true;


    // ***************************************************************************************
    // Privates variable

    private int mCurrentLeftPosition = Integer.MIN_VALUE;

    private Paint mBackgroundPaint = null;
    private Bitmap mHalfBitmap = null;
    private Canvas mHalfCanvas = null;


    // ***************************************************************************************
    // Constructors

    public ScSwitch(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScSwitch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Privates methods

    /**
     * Init the component.
     * Retrieve all attributes with the default values if needed.
     * Check the values for internal use and create the painters.
     *
     * @param context  the owner context
     * @param attrs    the attribute set
     * @param defStyle the style
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        //--------------------------------------------------
        // ATTRIBUTES

        // Get the attributes list
        final TypedArray attrArray = context
                .obtainStyledAttributes(attrs, R.styleable.ScButtons, defStyle, 0);

        this.mFontSize = attrArray.getDimension(
                R.styleable.ScButtons_fontSize, this.dipToPixel(ScSwitch.FONT_SIZE));

        this.mBackgroundColor = attrArray.getColor(
                R.styleable.ScButtons_backgroundColor, Color.parseColor("#803F51B5"));

        this.mShowLed = attrArray.getBoolean(
                R.styleable.ScButtons_showLed, false);

        int fillMode = attrArray.getInt(
                R.styleable.ScButtons_filling, FillMode.ALWAYS.ordinal());
        this.mFilling = FillMode.values()[fillMode];

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INIT

        // Painter
        this.mBackgroundPaint = new Paint();
        this.mBackgroundPaint.setAntiAlias(true);
        this.mBackgroundPaint.setDither(true);
        this.mBackgroundPaint.setStyle(Paint.Style.FILL);
    }


    /**
     * Init a bitmap and canvas by half with of the original size
     */
    private void initHalfCanvas(int width, int height) {
        if (this.mHalfBitmap != null)
            this.mHalfBitmap.recycle();

        this.mHalfBitmap = Bitmap.createBitmap(width / 2, height, Bitmap.Config.ARGB_8888);
        this.mHalfCanvas = new Canvas(this.mHalfBitmap);
    }


    // **************************************************************************************
    // Draw

    /**
     * Draw the solid background
     *
     * @param canvas where to draw
     */
    private void drawSolidBackground(Canvas canvas) {
        // Set the painter
        this.mBackgroundPaint.setColor(this.mBackgroundColor);

        // Draw the background
        RectF area = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
        canvas.drawRoundRect(
                area,
                this.mCornerRadius, this.mCornerRadius,
                this.mBackgroundPaint
        );
    }


    // **************************************************************************************
    // Override

    /**
     * Draw the component by the settings
     *
     * @param canvas to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Check for the position
        if (this.mCurrentLeftPosition == Integer.MIN_VALUE)
            this.mCurrentLeftPosition = this.isSelected() ? this.getWidth() / 2 : 0;

        // Draw the background and the half canvas in the right position
        this.drawSolidBackground(canvas);

        // Check for empty values
        if (this.mHalfBitmap == null ||
                this.mHalfBitmap.getWidth() == 0 || this.mHalfBitmap.getHeight() == 0)
            return;

        // Draw the super on this canvas
        this.mHalfCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        super.onDraw(this.mHalfCanvas);

        // Draw the button on the correct alignment
        canvas.drawBitmap(this.mHalfBitmap, this.mCurrentLeftPosition, 0, null);
    }

    /**
     * Take the measure of the component
     *
     * @param widthMeasureSpec  measured width
     * @param heightMeasureSpec measured height
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Get suggested dimensions
        int width = View.getDefaultSize(this.getSuggestedMinimumWidth(), widthMeasureSpec);
        int height = View.getDefaultSize(this.getSuggestedMinimumHeight(), heightMeasureSpec);

        // If have some dimension to wrap will use the path boundaries for have the right
        // dimension summed to the global padding.
        if (this.getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT)
            width = Math.round(this.dipToPixel(ScSwitch.MIN_WIDTH));
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
            height = Math.round(this.dipToPixel(ScSwitch.MIN_HEIGHT));

        // Set the calculated dimensions
        this.setMeasuredDimension(width, height);
        this.initHalfCanvas(width, height);
    }

    /**
     * The selection method with added the group managing calling
     *
     * @param selected the status
     */
    @Override
    public void setSelected(boolean selected) {
        // Super
        super.setSelected(selected);

        // Check for animate
        int width = this.getWidth();
        if (this.mAnimate && width != 0) {
            // Set the range
            int start = this.mCurrentLeftPosition;
            int end = this.isSelected() ? width / 2 : 0;

            // Animator
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.setDuration(ScSwitch.ANIMATION_DURATION);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mCurrentLeftPosition = (int) valueAnimator.getAnimatedValue();
                    invalidate();
                }
            });

            // Start
            animator.start();

        } else {
            // Else force the draw method to calculate the final the position
            this.mCurrentLeftPosition = Integer.MIN_VALUE;
            this.invalidate();
        }
    }


    // ***************************************************************************************
    // Instance state

    /**
     * Save the current instance state
     *
     * @return the state
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        // Call the super and get the parent state
        Parcelable superState = super.onSaveInstanceState();

        // Create a new bundle for store all the variables
        Bundle state = new Bundle();
        // Save all starting from the parent state
        state.putParcelable("PARENT", superState);
        state.putInt("mBackgroundColor", this.mBackgroundColor);
        state.putBoolean("mAnimate", this.mAnimate);

        // Return the new state
        return state;
    }

    /**
     * Restore the current instance state
     *
     * @param state the state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Implicit conversion in a bundle
        Bundle savedState = (Bundle) state;

        // Recover the parent class state and restore it
        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        // Now can restore all the saved variables values
        this.mBackgroundColor = savedState.getInt("mBackgroundColor");
        this.mAnimate = savedState.getBoolean("mAnimate");
    }


    // *******************************************************************************************
    // Public methods

    /**
     * Get the background color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getBackgroundColor() {
        return this.mBackgroundColor;
    }

    /**
     * Set the background color
     *
     * @param value the color
     */
    @SuppressWarnings("unused")
    public void setBackgroundColor(int value) {
        if (this.mBackgroundColor != value) {
            this.mBackgroundColor = value;
            this.invalidate();
        }
    }


    /**
     * Animate the selector movement
     *
     * @return true animated
     */
    @SuppressWarnings("unused")
    public boolean getAnimate() {
        return this.mAnimate;
    }

    /**
     * Animate the selector movement
     *
     * @param value true animated
     */
    @SuppressWarnings("unused")
    public void setAnimate(boolean value) {
        if (this.mAnimate != value) {
            this.mAnimate = value;
            this.invalidate();
        }
    }

}
