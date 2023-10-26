package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZTextButton;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;

/**
 * show pk dialog
 */
public class PKButton extends ZTextButton {

    public PKButton(@NonNull Context context) {
        super(context);
    }

    public PKButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PKButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setText("PK");
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setGravity(Gravity.CENTER);
        setTextColor(Color.parseColor("#cccccc"));
        setMinWidth(Utils.dp2px(36, getResources().getDisplayMetrics()));
        int padding = Utils.dp2px(8, getResources().getDisplayMetrics());
        setPadding(padding, 0, padding, 0);
    }

    @Override
    protected void afterClick() {
        super.afterClick();
        PKInviteDialog dialog = new PKInviteDialog(getContext());
        dialog.show();
    }
}
