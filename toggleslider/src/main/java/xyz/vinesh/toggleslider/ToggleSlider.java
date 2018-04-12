package xyz.vinesh.toggleslider;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by vineshraju on 28/2/18.
 */

public class ToggleSlider extends View {
    private static int GAP = 0;
    private int newHeight = 0;
    private Paint
            knobPaint = new Paint(),
            textPaint = new Paint(),
            bgOnPaint = new Paint(),
            bgOffPaint = new Paint(),
            currentBgColor = bgOffPaint;

    private int X = 0;
    private int animationDuration;
    private ValueAnimator animator = null;
    private boolean state;
    private int textSize;
    private String onText, offText, currentText;
    private OnStateChangeListener listener;
    //attribute res holders
    private int sliderBackgroundOnColor;
    private int sliderBackgroundOffColor;
    private int sliderKnobColor;
    private int sliderTextColor;

    public ToggleSlider(@NonNull Context context) {
        super(context);
        init();
    }

    public ToggleSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        handleAttrs(attrs);
    }

    public ToggleSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        handleAttrs(attrs);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ToggleSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        handleAttrs(attrs);

    }

    public void setState(boolean state) {
        this.state = state;
        animateView();
    }

    private void handleAttrs(AttributeSet attrs) {
        TypedArray styles = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleSlider, 0, 0);
        try {
            sliderBackgroundOnColor = styles.getResourceId(R.styleable.ToggleSlider_sliderBackgroundOnColor, R.color.toggleSliderbgOnColor);
            sliderBackgroundOffColor = styles.getResourceId(R.styleable.ToggleSlider_sliderBackgroundOffColor, R.color.toggleSliderbgOffColor);
            sliderKnobColor = styles.getResourceId(R.styleable.ToggleSlider_sliderKnobColor, R.color.toggleSliderknobColor);
            sliderTextColor = styles.getResourceId(R.styleable.ToggleSlider_sliderTextColor, R.color.toggleSlidertextColor);
            onText = styles.getString(R.styleable.ToggleSlider_sliderTextOn);
            offText = styles.getString(R.styleable.ToggleSlider_sliderTextOff);
            animationDuration = styles.getInt(R.styleable.ToggleSlider_sliderAnimationDuration,
                    getResources().getInteger(R.integer.toggleSliderDefAnimationDuration));
            state = styles.getBoolean(R.styleable.ToggleSlider_sliderState, false);
        } finally {
            styles.recycle();
            init();
        }
    }

    private void init() {
        //set dimen
        dimenUpdated();

        GAP = (int) pxFromDp(1f);

        //set color
        bgOnPaint.setColor(ContextCompat.getColor(getContext(), sliderBackgroundOnColor));
        bgOffPaint.setColor(ContextCompat.getColor(getContext(), sliderBackgroundOffColor));
        knobPaint.setColor(ContextCompat.getColor(getContext(), sliderKnobColor));
        textPaint.setColor(ContextCompat.getColor(getContext(), sliderTextColor));
        knobPaint.setAntiAlias(true);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true);

        //set text
        currentText = state ? onText : offText;
        //set callback
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                animateAndToggle();
            }
        });

    }

    private void dimenUpdated() {
        newHeight = getMeasuredHeight();
        X = newHeight / 2;
        textSize = newHeight / 3;
        textPaint.setTextSize(textSize);
        animateView();
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        this.listener = listener;
    }

    private void animateAndToggle() {
        //toggle state
        state = !state;
        if (listener != null) {
            listener.onStateChange(state);
        }

        //cancel old animation
        animateView();
    }

    private void animateView() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        //configure new animator
        animator = state ? ValueAnimator.ofInt(X, getWidth() - (newHeight / 2)) : ValueAnimator.ofInt(X, newHeight / 2);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(animationDuration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                X = (int) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                currentBgColor = state ? bgOnPaint : bgOffPaint;
                currentText = state ? onText : offText;
                if (state) {
                    textPaint.setTextAlign(Paint.Align.RIGHT);
                } else {
                    textPaint.setTextAlign(Paint.Align.LEFT);
                }
                invalidate();
            }
        });

        animator.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        dimenUpdated();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int r = newHeight / 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(0, 0, getWidth(), getHeight(), r, r, currentBgColor);
        } else {
            canvas.drawPath(getRoundedRectPath(0, 0, getWidth(), getHeight(), r, r, false), currentBgColor);

        }
        canvas.drawCircle(X, r, r - GAP, knobPaint);
        canvas.drawText(currentText, getWidth() / 2, r + (textSize / 3), textPaint);
    }


    private float pxFromDp(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private Path getRoundedRectPath(float left, float top, float right, float bottom, float rx, float ry, boolean roundTopAlone) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        path.rLineTo(-widthMinusCorners, 0);
        path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        path.rLineTo(0, heightMinusCorners);

        if (roundTopAlone) {
            path.rLineTo(0, ry);
            path.rLineTo(width, 0);
            path.rLineTo(0, -ry);
        } else {
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
            path.rLineTo(widthMinusCorners, 0);
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }

    public interface OnStateChangeListener {
        void onStateChange(boolean newState);
    }
}
