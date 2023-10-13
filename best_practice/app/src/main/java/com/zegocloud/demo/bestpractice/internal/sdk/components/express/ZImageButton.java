package com.zegocloud.demo.bestpractice.internal.sdk.components.express;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;

public class ZImageButton extends androidx.appcompat.widget.AppCompatImageView {

    protected GestureDetectorCompat gestureDetectorCompat;
    private long lastClickTime = 0;
    private static final int CLICK_INTERVAL = 200;
    private boolean open = false;
    private int openDrawable;
    private int closeDrawable;

    public ZImageButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZImageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (System.currentTimeMillis() - lastClickTime < CLICK_INTERVAL) {
                    return true;
                }
                boolean result = beforeClick();
                performClick();
                if (result) {
                    afterClick();
                }
                lastClickTime = System.currentTimeMillis();
                return true;
            }
        });
    }

    protected void afterClick() {
        toggle();
    }

    protected boolean beforeClick() {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetectorCompat.onTouchEvent(event);
    }


    public void setImageResource(@DrawableRes int openDrawable, @DrawableRes int closeDrawable) {
        this.openDrawable = openDrawable;
        this.closeDrawable = closeDrawable;
        updateState(open);
    }

    /**
     * should override to imply function
     */
    public void open() {
        this.open = true;
        updateState(true);
    }

    /**
     * should override to imply function
     */
    public void close() {
        this.open = false;
        updateState(false);
    }

    /**
     * only update ui,don't override
     */
    public void updateState(boolean state) {
        this.open = state;
        if (state) {
            setImageResource(openDrawable);
        } else {
            setImageResource(closeDrawable);
        }
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        boolean open = isOpen();
        if (open) {
            close();
        } else {
            open();
        }
    }
}
