package com.sccomponents.togglebutton;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Create a custom toggle button
 */

public class ScToggleButton extends View {

    // ***************************************************************************************
    // Constants and statics

    private static final int MIN_WIDTH = 96;
    private static final int MIN_HEIGHT = 48;

    private static final int CORNER_RADIUS = 5;
    private static final int FONT_SIZE = 14;
    private static final int STROKE_SIZE = 2;

    private static List<ScToggleButton> mGlobalButtons = null;
    private static List<OnGroupChangeListener> mGroupChangeListener = null;


    // ***************************************************************************************
    // Enumerators

    /**
     * The text alignment.
     */
    @SuppressWarnings("unuse")
    public enum TextAlign {
        LEFT,
        CENTER,
        RIGHT
    }

    /**
     * The filling mode.
     */
    @SuppressWarnings("unuse")
    public enum FillMode {
        NEVER,
        ALWAYS,
        ON,
        OFF
    }


    // ***************************************************************************************
    // Private and protected attributes

    protected float mFontSize = this.dipToPixel(ScToggleButton.FONT_SIZE);
    protected String mFontFamily = null;
    protected boolean mFontIsBold = true;
    protected boolean mFontIsItalic = false;

    protected float mStrokeSize = ScToggleButton.STROKE_SIZE;
    protected float mCornerRadius = this.dipToPixel(ScToggleButton.CORNER_RADIUS);
    protected FillMode mFilling = FillMode.NEVER;

    protected String mText = null;
    protected TextAlign mTextAlign = TextAlign.CENTER;
    protected boolean mAllCaps = true;
    protected boolean mShowLed = true;

    protected int mOffColor = Color.parseColor("#3F51B5");
    protected int mOnColor = Color.parseColor("#45AA46");
    protected int mLedColor = -1;

    protected boolean mChangeBorderColorOnChecked = true;
    protected boolean mChangeTextColorOnChecked = true;

    protected String mGroup = null;
    protected boolean mOnlyOneSelected = true;


    // ***************************************************************************************
    // Privates variable

    private GestureDetector mDetector = null;
    private OnChangeListener mChangeListener = null;

    // Painters
    private Paint mStrokePaint = null;
    private Paint mHighlightPaint = null;
    private BlurMaskFilter mHighLightEffect = null;
    private TextPaint mTextPaint = null;


    // ***************************************************************************************
    // Constructors

    public ScToggleButton(Context context) {
        super(context);
        this.init(context, null, 0);
    }

    public ScToggleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context, attrs, 0);
    }

    public ScToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context, attrs, defStyleAttr);
    }


    // ***************************************************************************************
    // Classes

    /**
     * Gesture detector for single tap on component
     */
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

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
                .obtainStyledAttributes(attrs, R.styleable.ScToggleButton, defStyle, 0);

        boolean isSelected = attrArray.getBoolean(
                R.styleable.ScToggleButton_selected, false);

        this.mFontSize = attrArray.getDimension(
                R.styleable.ScToggleButton_fontSize,
                this.dipToPixel(ScToggleButton.FONT_SIZE));
        this.mFontFamily = attrArray.getString(
                R.styleable.ScToggleButton_fontFamily);
        this.mFontIsBold = attrArray.getBoolean(

                R.styleable.ScToggleButton_bold, true);
        this.mFontIsItalic = attrArray.getBoolean(
                R.styleable.ScToggleButton_italic, false);
        this.mAllCaps = attrArray.getBoolean(
                R.styleable.ScToggleButton_allCaps, true);
        this.mShowLed = attrArray.getBoolean(
                R.styleable.ScToggleButton_showLed, true);

        this.mCornerRadius = attrArray.getDimension(
                R.styleable.ScToggleButton_cornerRadius,
                this.dipToPixel(ScToggleButton.CORNER_RADIUS));
        this.mStrokeSize = attrArray.getDimension(
                R.styleable.ScToggleButton_strokeSize,
                this.dipToPixel(ScToggleButton.STROKE_SIZE));
        int fillMode = attrArray.getInt(
                R.styleable.ScToggleButton_filling, FillMode.NEVER.ordinal());
        this.mFilling = FillMode.values()[fillMode];

        this.mText = attrArray.getString(
                R.styleable.ScToggleButton_text);
        int textAlign = attrArray.getInt(
                R.styleable.ScToggleButton_align, TextAlign.CENTER.ordinal());
        this.mTextAlign = TextAlign.values()[textAlign];

        this.mOffColor = attrArray.getColor(
                R.styleable.ScToggleButton_offColor, Color.parseColor("#3F51B5"));
        this.mOnColor = attrArray.getColor(
                R.styleable.ScToggleButton_onColor, Color.parseColor("#45AA46"));
        this.mLedColor = attrArray.getColor(
                R.styleable.ScToggleButton_ledColor, -1);

        this.mChangeBorderColorOnChecked = attrArray.getBoolean(
                R.styleable.ScToggleButton_changeBorder, true);
        this.mChangeTextColorOnChecked = attrArray.getBoolean(
                R.styleable.ScToggleButton_changeText, true);

        this.mGroup = attrArray.getString(
                R.styleable.ScToggleButton_group);
        this.mOnlyOneSelected = attrArray.getBoolean(
                R.styleable.ScToggleButton_onlyOneSelected, true);

        // Recycle
        attrArray.recycle();

        //--------------------------------------------------
        // INIT

        if (ScToggleButton.mGlobalButtons == null)
            ScToggleButton.mGlobalButtons = new ArrayList<>();

        this.mDetector = new GestureDetector(this.getContext(), new SingleTapConfirm());

        this.mStrokePaint = new Paint();
        this.mStrokePaint.setAntiAlias(true);
        this.mStrokePaint.setDither(true);

        this.mHighlightPaint = new Paint();
        this.mHighlightPaint.setAntiAlias(true);
        this.mHighlightPaint.setDither(true);
        this.mHighlightPaint.setStyle(Paint.Style.STROKE);

        this.mHighLightEffect = new BlurMaskFilter(5, BlurMaskFilter.Blur.SOLID);

        this.mTextPaint = new TextPaint();
        this.mTextPaint.setAntiAlias(true);
        this.mTextPaint.setDither(true);

        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        this.setClickable(true);
        this.setSelected(isSelected);
    }

    /**
     * Get the display metric.
     * This method is used for screen measure conversion.
     *
     * @param context the current context
     * @return the display metrics
     */
    private DisplayMetrics getDisplayMetrics(Context context) {
        // Get the window manager from the window service
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        // Create the variable holder and inject the values
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        // Return
        return displayMetrics;
    }

    /**
     * Convert Dip to Pixel using the current display metrics.
     *
     * @param dip the start value in Dip
     * @return the correspondent value in Pixels
     */
    //
    private float dipToPixel(float dip) {
        // Get the display metrics
        DisplayMetrics metrics = this.getDisplayMetrics(this.getContext());
        // Calc the conversion by the screen density
        return dip * metrics.density;
    }

    /**
     * Compare two string minding the null values.
     *
     * @param str1 first
     * @param str2 second
     * @return true if equal
     */
    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    /**
     * Check if the component background if filled
     */
    private boolean isFilled() {
        switch (this.mFilling) {
            case ALWAYS:
                return true;
            case NEVER:
                return false;
            case ON:
                return this.isSelected();
            case OFF:
                return !this.isSelected();
        }
        return false;
    }

    /**
     * Get back the border color by the current state
     *
     * @return the color
     */
    private int choiceBorderColor() {
        if (this.mChangeBorderColorOnChecked && this.isSelected())
            return this.mOnColor;
        else
            return this.mOffColor;
    }

    /**
     * If the same color of background choice the other color
     */
    private int inverseColor(int color) {
        int backgroundColor = this.choiceBorderColor();
        if (this.isFilled() && color == backgroundColor)
            return color == this.mOnColor ? this.mOffColor : this.mOnColor;
        else
            return color;
    }

    /**
     * Get back the text color by the current state
     *
     * @return the color
     */
    private int choiceTextColor() {
        int color = this.mChangeTextColorOnChecked && this.isSelected() ?
                this.mOnColor : this.mOffColor;
        return this.inverseColor(color);
    }

    /**
     * Get back the highlight color by the current state
     *
     * @return the color
     */
    private int choiceHighlightColor() {
        int color = this.mLedColor == -1 ? this.mOnColor : this.mLedColor;
        color = this.isSelected() ? color : this.mOffColor;
        return this.inverseColor(color);
    }


    // **************************************************************************************
    // Groups

    /**
     * Give back a filtered list of the global buttons by the group name.
     *
     * @param group Group name
     * @return a list of buttons
     */
    @SuppressWarnings("unused")
    public static List<ScToggleButton> getButtonsGroup(String group) {
        List<ScToggleButton> list = new ArrayList<>();
        if (group != null && group.length() > 0)
            for (ScToggleButton button : ScToggleButton.mGlobalButtons) {
                if (ScToggleButton.equals(button.getGroup(), group))
                    list.add(button);
            }
        return list;
    }

    /**
     * Check if the button belongs to a group
     *
     * @return true if within a group
     */
    @SuppressWarnings("unused")
    public boolean hasGroup() {
        return this.getGroup() != null && this.getGroup().length() > 0;
    }

    /**
     * Reset the group selection.
     */
    @SuppressWarnings("unused")
    public static void resetGroup(String group, ScToggleButton excluded) {
        // Get all buttons in groups
        List<ScToggleButton> list = ScToggleButton.getButtonsGroup(group);

        // If status is true reset all other buttons
        for (ScToggleButton button : list)
            if (excluded == null || button != excluded)
                button.setSelected(false);
    }

    /**
     * Reset the group selection.
     */
    @SuppressWarnings("unused")
    public static void resetGroup(String group) {
        ScToggleButton.resetGroup(group, null);
    }

    /**
     * Get back the selected buttons list inside the group
     */
    @SuppressWarnings("unused")
    public static ScToggleButton[] getGroupSelection(String group) {
        // Get all buttons in groups
        List<ScToggleButton> list = ScToggleButton.getButtonsGroup(group);

        // Check if have at least one button selected
        List<ScToggleButton> selected = new ArrayList<>();
        for (ScToggleButton button : list)
            if (button.isSelected())
                selected.add(button);

        // Convert to an array and return it
        return selected.toArray(new ScToggleButton[selected.size()]);
    }

    /**
     * Check if a group has selection
     */
    @SuppressWarnings("unused")
    public static boolean groupHasSelection(String group) {
        return ScToggleButton.getGroupSelection(group).length > 0;
    }

    /**
     * Update the group status selection.
     */
    private void manageGroupSelection() {
        // Manage the group status only if just one can be selected
        if (!this.mOnlyOneSelected)
            return;

        // If status is true reset all other buttons
        if (this.isSelected())
            ScToggleButton.resetGroup(this.getGroup(), this);

        // Check for constraints
        if (this.hasGroup() &&
                !ScToggleButton.groupHasSelection(this.getGroup())) {
            // Select the first button of group
            List<ScToggleButton> groups = ScToggleButton.getButtonsGroup(this.getGroup());
            if (groups.size() > 0)
                groups.get(0).setSelected(true);
        }
    }


    // **************************************************************************************
    // Draw

    /**
     * Draw the border
     *
     * @param canvas where to draw
     */
    private void drawBorder(Canvas canvas) {
        // Check for empty values
        if (this.mStrokeSize > 0) {
            // Create the drawing area
            float middle = this.mStrokeSize / 2;
            RectF area = new RectF(
                    0 + middle,
                    0 + middle,
                    canvas.getWidth() - middle,
                    canvas.getHeight() - middle);

            // Set the painter
            this.mStrokePaint.setColor(this.choiceBorderColor());
            this.mStrokePaint.setStrokeWidth(this.mStrokeSize);
            this.mStrokePaint.setStyle(
                    this.isFilled() ? Paint.Style.FILL_AND_STROKE : Paint.Style.STROKE);

            // Draw the background
            canvas.drawRoundRect(
                    area,
                    this.mCornerRadius, this.mCornerRadius,
                    this.mStrokePaint
            );
        }
    }

    /**
     * Draw the led
     *
     * @param canvas where to draw
     */
    private void drawLed(Canvas canvas) {
        // Check visibility
        if (this.mShowLed) {
            // Setting the painter
            this.mHighlightPaint.setColor(this.choiceHighlightColor());
            this.mHighlightPaint.setStrokeWidth(this.mStrokeSize * 2);
            this.mHighlightPaint.setMaskFilter(this.isSelected() ? this.mHighLightEffect : null);

            // Draw
            int left = canvas.getWidth() / 4;
            int right = left * 3;
            int bottom = canvas.getHeight() - (int) this.mStrokeSize * 4;
            canvas.drawLine(left, bottom, right, bottom, this.mHighlightPaint);
        }
    }

    /**
     * Draw the text
     *
     * @param canvas where to draw
     */
    private void drawText(Canvas canvas) {
        // Check for empty values
        if (this.mText != null && this.mText.length() > 0) {
            // Get the drawing area
            Rect area = new Rect(
                    0 + this.getPaddingLeft(),
                    0 + this.getPaddingTop(),
                    canvas.getWidth() - this.getPaddingRight(),
                    canvas.getHeight() - this.getPaddingBottom()
            );

            // Setting the painter
            this.mTextPaint.setColor(this.choiceTextColor());
            this.mTextPaint.setTextSize(this.mFontSize);

            // Typeface
            int style = Typeface.NORMAL;
            if (this.mFontIsBold && this.mFontIsItalic)
                style = Typeface.BOLD_ITALIC;
            else {
                if (this.mFontIsBold) style = Typeface.BOLD;
                if (this.mFontIsItalic) style = Typeface.ITALIC;
            }

            Typeface typeFace = this.mFontFamily == null ?
                    Typeface.create(Typeface.DEFAULT, style) :
                    Typeface.create(this.mFontFamily, style);
            this.mTextPaint.setTypeface(typeFace);

            // Find the alignment
            Layout.Alignment align = Layout.Alignment.ALIGN_CENTER;
            switch (this.mTextAlign) {
                case LEFT:
                    align = Layout.Alignment.ALIGN_NORMAL;
                    break;
                case RIGHT:
                    align = Layout.Alignment.ALIGN_OPPOSITE;
                    break;
            }

            // Create the text layout
            StaticLayout staticLayout = new StaticLayout(
                    this.mAllCaps ? this.mText.toUpperCase() : this.mText,
                    this.mTextPaint,
                    area.width(), align,
                    1, 0, false
            );

            // Center and print
            canvas.save();
            canvas.translate(
                    (canvas.getWidth() - staticLayout.getWidth()) / 2,
                    (canvas.getHeight() - staticLayout.getHeight()) / 2);
            staticLayout.draw(canvas);
            canvas.restore();
        }
    }


    // **************************************************************************************
    // Override

    /**
     * Manage the single click event
     *
     * @param e the event
     * @return always true
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Single click
        if (this.mDetector.onTouchEvent(e))
            this.setSelected(!this.isSelected());

        return true;
    }

    /**
     * Draw the component by the settings
     *
     * @param canvas to draw
     */
    @Override
    protected void onDraw(Canvas canvas) {
        // Custom
        this.drawBorder(canvas);
        this.drawLed(canvas);
        this.drawText(canvas);
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
            width = Math.round(this.dipToPixel(ScToggleButton.MIN_WIDTH));
        if (this.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
            height = Math.round(this.dipToPixel(ScToggleButton.MIN_HEIGHT));

        // Set the calculated dimensions
        this.setMeasuredDimension(width, height);
    }

    /**
     * Attach and detach the component from parents
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // Remove this toggle buttons from the global buttons list
        ScToggleButton.mGlobalButtons.remove(this);
        this.setSelected(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add this button at the global list of buttons
        if (!ScToggleButton.mGlobalButtons.contains(this)) {
            ScToggleButton.mGlobalButtons.add(this);
            this.manageGroupSelection();
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
        state.putFloat("mFontSize", this.mFontSize);
        state.putString("mFontFamily", this.mFontFamily);
        state.putBoolean("mFontIsBold", this.mFontIsBold);
        state.putBoolean("mFontIsItalic", this.mFontIsItalic);

        state.putFloat("mStrokeSize", this.mStrokeSize);
        state.putFloat("mCornerRadius", this.mCornerRadius);
        state.putInt("mFilling", this.mFilling.ordinal());

        state.putString("mText", this.mText);
        state.putInt("mTextAlign", this.mTextAlign.ordinal());
        state.putBoolean("mAllCaps", this.mAllCaps);
        state.putBoolean("mShowLed", this.mShowLed);

        state.putInt("mOffColor", this.mOffColor);
        state.putInt("mOnColor", this.mOnColor);
        state.putInt("mLedColor", this.mLedColor);

        state.putBoolean("mChangeBorderColorOnChecked", this.mChangeBorderColorOnChecked);
        state.putBoolean("mChangeTextColorOnChecked", this.mChangeTextColorOnChecked);

        state.putString("mGroup", this.mGroup);
        state.putBoolean("mOnlyOneSelected", this.mOnlyOneSelected);

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
        this.mFontSize = savedState.getFloat("mFontSize");
        this.mFontFamily = savedState.getString("mFontFamily");
        this.mFontIsBold = savedState.getBoolean("mFontIsBold");
        this.mFontIsItalic = savedState.getBoolean("mFontIsItalic");

        this.mStrokeSize = savedState.getFloat("mStrokeSize");
        this.mCornerRadius = savedState.getFloat("mCornerRadius");
        this.mFilling = FillMode.values()[savedState.getInt("mFilling")];

        this.mText = savedState.getString("mText");
        this.mTextAlign = TextAlign.values()[savedState.getInt("mTextAlign")];
        this.mAllCaps = savedState.getBoolean("mAllCaps");
        this.mShowLed = savedState.getBoolean("mShowLed");

        this.mOffColor = savedState.getInt("mOffColor");
        this.mOnColor = savedState.getInt("mOnColor");
        this.mLedColor = savedState.getInt("mLedColor");

        this.mChangeBorderColorOnChecked = savedState.getBoolean("mChangeBorderColorOnChecked");
        this.mChangeTextColorOnChecked = savedState.getBoolean("mChangeTextColorOnChecked");

        this.mGroup = savedState.getString("mGroup");
        this.mOnlyOneSelected = savedState.getBoolean("mOnlyOneSelected");
    }


    // *******************************************************************************************
    // Public listener and interface

    /**
     * Change status event listener
     */
    @SuppressWarnings("all")
    public interface OnChangeListener {

        /**
         * When the selection change
         *
         * @param the current button status
         */
        void onChanged(boolean isSelected);

    }

    /**
     * Set the change event listener
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public void setOnChangeListener(OnChangeListener listener) {
        this.mChangeListener = listener;
    }


    /**
     * Group change status event listener
     */
    @SuppressWarnings("all")
    public interface OnGroupChangeListener {

        /**
         * When the selection change
         *
         * @param the current button status
         */
        void onChanged(ScToggleButton source);

    }

    /**
     * Add it to the global listener.
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public static void addOnGroupChangeListener(OnGroupChangeListener listener) {
        // Check for empty values
        if (ScToggleButton.mGroupChangeListener == null)
            ScToggleButton.mGroupChangeListener = new ArrayList<>();

        // Add the listener
        ScToggleButton.mGroupChangeListener.add(listener);
    }

    /**
     * Remove it to the global listener.
     *
     * @param listener the listener
     */
    @SuppressWarnings("unused")
    public static void removeOnGroupChangeListener(OnGroupChangeListener listener) {
        // Check for empty values
        if (ScToggleButton.mGroupChangeListener != null)
            // Add the listener
            ScToggleButton.mGroupChangeListener.remove(listener);
    }

    /**
     * Perform an global listener action
     */
    @SuppressWarnings("unused")
    private void performGroupOnChange(ScToggleButton source) {
        // Cycle all listeners
        if (ScToggleButton.mGroupChangeListener != null)
            for (OnGroupChangeListener listener : ScToggleButton.mGroupChangeListener)
                try {
                    // Check for null value and try to execute the event
                    if (listener != null)
                        listener.onChanged(source);

                } catch (Exception e) {
                    e.printStackTrace();
                }
    }


    // *******************************************************************************************
    // Public methods

    /**
     * The selection method with added the group managing calling
     *
     * @param selected the status
     */
    @Override
    public void setSelected(boolean selected) {
        // Apply only if changed.
        if (this.isSelected() == selected)
            return;

        // If belongs to a group check for group constraints.
        if (this.hasGroup()) {
            ScToggleButton[] list = ScToggleButton.getGroupSelection(this.getGroup());
            if (this.isSelected() && this.mOnlyOneSelected && list.length < 2)
                return;
        }

        // Make the selection
        super.setSelected(selected);
        this.manageGroupSelection();

        // Group event
        if (this.hasGroup())
            this.performGroupOnChange(this);

        // Button event
        if (this.mChangeListener != null)
            this.mChangeListener.onChanged(this.isSelected());
    }


    /**
     * Get the current font size
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public float getFontSize() {
        return this.mFontSize;
    }

    /**
     * Set the current font size
     *
     * @param value the new size in pixel
     */
    @SuppressWarnings("unused")
    public void setFontSize(float value) {
        if (this.mFontSize != value && value > 0) {
            this.mFontSize = value;
            this.invalidate();
        }
    }


    /**
     * Get the current font family
     *
     * @return the current family name
     */
    @SuppressWarnings("unused")
    public String getFontFamily() {
        return this.mFontFamily;
    }

    /**
     * Set the current font family
     *
     * @param value the new family name
     */
    @SuppressWarnings("unused")
    public void setFontFamily(String value) {
        if (!ScToggleButton.equals(this.mFontFamily, value)) {
            this.mFontFamily = value;
            this.invalidate();
        }
    }


    /**
     * Get if the font is marked as bold
     *
     * @return true if bold
     */
    @SuppressWarnings("unused")
    public boolean getFontIsBold() {
        return this.mFontIsBold;
    }

    /**
     * Set the font bold status
     *
     * @param value if bold
     */
    @SuppressWarnings("unused")
    public void setFontIsBold(boolean value) {
        if (this.mFontIsBold != value) {
            this.mFontIsBold = value;
            this.invalidate();
        }
    }


    /**
     * Get if the font is marked as italic
     *
     * @return true if italic
     */
    @SuppressWarnings("unused")
    public boolean getFontIsItalic() {
        return this.mFontIsItalic;
    }

    /**
     * Set the font italic status
     *
     * @param value if italic
     */
    @SuppressWarnings("unused")
    public void setFontIsItalic(boolean value) {
        if (this.mFontIsItalic != value) {
            this.mFontIsItalic = value;
            this.invalidate();
        }
    }


    /**
     * Get the current stroke size
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public float getStrokeSize() {
        return this.mStrokeSize;
    }

    /**
     * Set the current stroke size
     *
     * @param value the new size in pixel
     */
    @SuppressWarnings("unused")
    public void setStrokeSize(float value) {
        if (this.mStrokeSize != value && value > 0) {
            this.mStrokeSize = value;
            this.invalidate();
        }
    }


    /**
     * Get the current corner radius
     *
     * @return the status
     */
    @SuppressWarnings("unused")
    public float getCornerRadius() {
        return this.mCornerRadius;
    }

    /**
     * Set the current corner radius
     *
     * @param value the new size in pixel
     */
    @SuppressWarnings("unused")
    public void setCornerRadius(float value) {
        if (this.mCornerRadius != value && value > 0) {
            this.mCornerRadius = value;
            this.invalidate();
        }
    }


    /**
     * Get the fill mode
     *
     * @return the mode
     */
    @SuppressWarnings("unused")
    public FillMode getFilling() {
        return this.mFilling;
    }

    /**
     * Set the fill mode
     *
     * @param value the mode
     */
    @SuppressWarnings("unused")
    public void setFilling(FillMode value) {
        if (this.mFilling != value) {
            this.mFilling = value;
            this.invalidate();
        }
    }


    /**
     * Get the current text
     *
     * @return the text
     */
    @SuppressWarnings("unused")
    public String getText() {
        return this.mText;
    }

    /**
     * Set the current text
     *
     * @param value the text
     */
    @SuppressWarnings("unused")
    public void setText(String value) {
        if (!ScToggleButton.equals(this.mText, value)) {
            this.mText = value;
            this.invalidate();
        }
    }


    /**
     * Get the text alignment
     *
     * @return the alignment
     */
    @SuppressWarnings("unused")
    public TextAlign getTextAlign() {
        return this.mTextAlign;
    }

    /**
     * Set the text alignment
     *
     * @param value the alignment
     */
    @SuppressWarnings("unused")
    public void setTextAlign(TextAlign value) {
        if (this.mTextAlign != value) {
            this.mTextAlign = value;
            this.invalidate();
        }
    }


    /**
     * Get the all caps text status
     *
     * @return true if all caps
     */
    @SuppressWarnings("unused")
    public boolean getAllCaps() {
        return this.mAllCaps;
    }

    /**
     * Set the text as all caps.
     *
     * @param value if true all caps
     */
    @SuppressWarnings("unused")
    public void setAllCaps(boolean value) {
        if (this.mAllCaps != value) {
            this.mAllCaps = value;
            this.invalidate();
        }
    }


    /**
     * Get the off status color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getOffColor() {
        return this.mOffColor;
    }

    /**
     * Set the off status color
     *
     * @param value the color
     */
    @SuppressWarnings("unused")
    public void setOffColor(int value) {
        if (this.mOffColor != value) {
            this.mOffColor = value;
            this.invalidate();
        }
    }


    /**
     * Get the on status color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getOnColor() {
        return this.mOnColor;
    }

    /**
     * Set the on status color
     *
     * @param value the color
     */
    @SuppressWarnings("unused")
    public void setOnColor(int value) {
        if (this.mOnColor != value) {
            this.mOnColor = value;
            this.invalidate();
        }
    }


    /**
     * Get the highlight color
     *
     * @return the color
     */
    @SuppressWarnings("unused")
    public int getHighlightColor() {
        return this.mLedColor;
    }

    /**
     * Set the highlight color
     *
     * @param value the color
     */
    @SuppressWarnings("unused")
    public void setHighlightColor(int value) {
        if (this.mLedColor != value) {
            this.mLedColor = value;
            this.invalidate();
        }
    }


    /**
     * Get if the border color will changed on the button selection
     *
     * @return true if will changed
     */
    @SuppressWarnings("unused")
    public boolean getChangeBorderColorOnChecked() {
        return this.mChangeBorderColorOnChecked;
    }

    /**
     * Set if the border color will changed on the button selection
     *
     * @param value if true will changed
     */
    @SuppressWarnings("unused")
    public void setChangeBorderColorOnChecked(boolean value) {
        if (this.mChangeBorderColorOnChecked != value) {
            this.mChangeBorderColorOnChecked = value;
            this.invalidate();
        }
    }


    /**
     * Get if the text color will changed on the button selection
     *
     * @return true if will changed
     */
    @SuppressWarnings("unused")
    public boolean getChangeTextColorOnChecked() {
        return this.mChangeTextColorOnChecked;
    }

    /**
     * Set if the text color will changed on the button selection
     *
     * @param value if true will changed
     */
    @SuppressWarnings("unused")
    public void setChangeTextColorOnChecked(boolean value) {
        if (this.mChangeTextColorOnChecked != value) {
            this.mChangeTextColorOnChecked = value;
            this.invalidate();
        }
    }


    /**
     * Get the group name
     *
     * @return the group
     */
    @SuppressWarnings("unused")
    public String getGroup() {
        return this.mGroup;
    }

    /**
     * Set the group name
     *
     * @param value the group
     */
    @SuppressWarnings("unused")
    public void setGroup(String value) {
        if (!ScToggleButton.equals(this.mGroup, value)) {
            this.mGroup = value;
            this.manageGroupSelection();
            this.invalidate();
        }
    }


    /**
     * If only one button per group can be selected at the same time
     *
     * @return true only one
     */
    @SuppressWarnings("unused")
    public boolean getOnlyOneSelected() {
        return this.mOnlyOneSelected;
    }

    /**
     * If only one button per group can be selected at the same time
     *
     * @param value if true only one
     */
    @SuppressWarnings("unused")
    public void setOnlyOneSelected(boolean value) {
        if (this.mOnlyOneSelected != value) {
            this.mOnlyOneSelected = value;
            this.manageGroupSelection();
            this.invalidate();
        }
    }

}
