package com.zegocloud.demo.bestpractice.internal.utils;

import android.content.Context;
import android.widget.Toast;
import androidx.annotation.StringRes;

public class ToastUtil {

    public static void show(Context context, CharSequence text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, @StringRes int text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
