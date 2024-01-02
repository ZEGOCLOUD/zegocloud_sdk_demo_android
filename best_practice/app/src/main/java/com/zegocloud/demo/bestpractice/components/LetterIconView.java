package com.zegocloud.demo.bestpractice.components;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

public class LetterIconView extends FrameLayout {

    private ImageFilterView circleBackground;
    private TextView textView;
    private int circleBackgroundColor = Color.parseColor("#DBDDE3");
    private ImageFilterView customAvatarView;

    public LetterIconView(@NonNull Context context) {
        super(context);
        initView();
    }

    public LetterIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LetterIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public LetterIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        circleBackground = new ImageFilterView(getContext());
        circleBackground.setRoundPercent(1.0f);

        LayoutParams avatarParams = new LayoutParams(-1, -1);
        avatarParams.gravity = Gravity.CENTER;
        addView(circleBackground, avatarParams);

        textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        textView.setTextColor(Color.parseColor("#222222"));
        LayoutParams textViewParams = new LayoutParams(-2, -2);
        textViewParams.gravity = Gravity.CENTER;
        addView(textView, textViewParams);

        customAvatarView = new ImageFilterView(getContext());
        customAvatarView.setRoundPercent(1.0f);
        addView(customAvatarView, avatarParams);
    }

    public void setLetter(String letter) {
        if (!TextUtils.isEmpty(letter)) {
            textView.setText(letter.substring(0, 1));
            circleBackground.setBackgroundColor(circleBackgroundColor);
        } else {
            textView.setText("");
            circleBackground.setBackgroundColor(0);
        }
    }

    public void setLetterColor(@ColorInt int color) {
        textView.setTextColor(color);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int measuredWidth = circleBackground.getMeasuredWidth();
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, measuredWidth / 2f);
    }

    public void setCircleBackgroundRadius(int radius) {
        ViewGroup.LayoutParams layoutParams = circleBackground.getLayoutParams();
        layoutParams.width = radius * 2;
        layoutParams.height = radius * 2;
        circleBackground.setLayoutParams(layoutParams);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, radius);
        customAvatarView.setLayoutParams(layoutParams);
    }

    public void setCircleBackgroundColor(int color) {
        circleBackgroundColor = color;
        circleBackground.setBackgroundColor(color);
    }

    public void setIconUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            Picasso.get().load(url).fit().centerCrop().into(customAvatarView);
        } else {
            customAvatarView.setImageDrawable(null);
        }
    }
}
