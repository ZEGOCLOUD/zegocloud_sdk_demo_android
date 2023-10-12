package com.zegocloud.demo.bestpractice.internal.utils;

import android.util.DisplayMetrics;
import android.util.TypedValue;

public class Utils {

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }

    public static int sp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, displayMetrics);
    }
}
