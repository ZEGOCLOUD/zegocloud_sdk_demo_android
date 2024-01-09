package com.zegocloud.demo.bestpractice.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.flexbox.FlexboxLayout;
import com.zegocloud.demo.bestpractice.components.LetterIconView;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import im.zego.zim.entity.ZIMUserFullInfo;

public class CheckLetterIconView extends FrameLayout {

    private LetterIconView letterIconView;
    private CheckBox checkBox;
    private String userID;
    private OnCheckedChangeListener checkedChangeListener;

    public CheckLetterIconView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CheckLetterIconView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CheckLetterIconView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        letterIconView = new LetterIconView(getContext());
        int size = Utils.dp2px(48, getResources().getDisplayMetrics());
        int margin = Utils.dp2px(8, getResources().getDisplayMetrics());
        FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(size, size);
        layoutParams.setMargins(margin, margin, margin, margin);
        letterIconView.setLayoutParams(layoutParams);

        addView(letterIconView);
        checkBox = new CheckBox(getContext());
        FrameLayout.LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.gravity = Gravity.END | Gravity.BOTTOM;
        int margin2 = Utils.dp2px(-4, getResources().getDisplayMetrics());
        layoutParams2.setMargins(0, 0, margin2, margin2);
        checkBox.setLayoutParams(layoutParams2);
        addView(checkBox);

        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkedChangeListener != null) {
                    checkedChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });

        setOnClickListener(v -> {
            checkBox.performClick();
        });
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }

    public void setCheckListener(OnCheckedChangeListener checkListener) {
        this.checkedChangeListener = checkListener;
    }

    private void setLetter(String userName) {
        letterIconView.setLetter(userName);
    }

    private void setIconUrl(String userAvatarUrl) {
        letterIconView.setIconUrl(userAvatarUrl);
    }

    public void setUserID(String userID) {
        this.userID = userID;
        ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(userID);
        setLetter(zimUserFullInfo.baseInfo.userName);
        setIconUrl(zimUserFullInfo.userAvatarUrl);
    }

    public String getUserID() {
        return userID;
    }
}
