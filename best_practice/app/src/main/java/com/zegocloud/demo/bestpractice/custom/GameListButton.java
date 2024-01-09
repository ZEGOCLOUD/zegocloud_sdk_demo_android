package com.zegocloud.demo.bestpractice.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZTextButton;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;

public class GameListButton extends ZTextButton {

    public GameListButton(@NonNull Context context) {
        super(context);
    }

    public GameListButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameListButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setText("Game List");
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setGravity(Gravity.CENTER);
        setTextColor(Color.WHITE);
        setMinWidth(Utils.dp2px(36, getResources().getDisplayMetrics()));
        int padding = Utils.dp2px(8, getResources().getDisplayMetrics());
        setPadding(padding, 0, padding, 0);
    }
}
