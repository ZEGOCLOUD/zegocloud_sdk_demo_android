package com.zegocloud.demo.bestpractice.internal.sdk.components.express;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;

public class ToggleMicrophoneButton extends ZImageButton {

    public ToggleMicrophoneButton(@NonNull Context context) {
        super(context);
    }

    public ToggleMicrophoneButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ToggleMicrophoneButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setImageResource(R.drawable.call_icon_mic_on, R.drawable.call_icon_mic_off);
    }

    @Override
    public void open() {
        super.open();
        ZEGOSDKManager.getInstance().expressService.openMicrophone(true);
    }

    @Override
    public void close() {
        super.close();
        ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
    }
}
