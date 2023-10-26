package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.LetterIconView;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZEGOVideoView;
import java.util.Objects;

public class PKBattleView extends FrameLayout {

    private FrameLayout connectTipsView;
    private LetterIconView iconView;
    private PKUser pkUser;
    private TextView defaultTips;

    public PKBattleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }


    private void initView() {
        iconView = new LetterIconView(getContext());
        addView(iconView);
        iconView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_444));

        connectTipsView = new FrameLayout(getContext());
        addView(connectTipsView);
        defaultTips = new TextView(getContext());
        defaultTips.setGravity(Gravity.CENTER);
        defaultTips.setText("host reconnecting");
        defaultTips.setTextColor(Color.WHITE);
        defaultTips.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        defaultTips.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_444));
        connectTipsView.addView(defaultTips, new LayoutParams(-1, -1));
        connectTipsView.setVisibility(GONE);
    }

    /**
     * @param pkUser
     * @param attachVideoView host play single stream,need attach video view and hide mix video
     */
    public void setPKUser(PKUser pkUser, boolean attachVideoView) {
        this.pkUser = pkUser;
        if (pkUser != null) {
            FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(pkUser.rect.width(),
                pkUser.rect.height());
            layoutParam.setMargins(pkUser.rect.left, pkUser.rect.top, 0, 0);
            setLayoutParams(layoutParam);
            iconView.setCircleBackgroundRadius(pkUser.rect.width() / 2);

            if (attachVideoView) {
                ZEGOVideoView videoView = new ZEGOVideoView(getContext());
                videoView.setUserID(pkUser.userID);
                videoView.setStreamID(pkUser.getPKUserStream());
                addView(videoView, 0);

                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (Objects.equals(pkUser.userID, currentUser.userID)) {
                    videoView.startPreviewOnly();
                    videoView.startPublishAudioVideo();
                } else {
                    videoView.startPlayRemoteAudioVideo();
                }
            } else {
                if (getVideoView() != null) {
                    removeView(getVideoView());
                }
            }
            defaultTips.setText(pkUser.userName + " reconnecting");
            iconView.setLetter(pkUser.userName);
            onCameraUpdate(pkUser.isCameraOpen());
        } else {
            if (getVideoView() != null) {
                getVideoView().stopPlayRemoteAudioVideo();
            }
        }
    }

    public PKUser getPkUser() {
        return pkUser;
    }

    public void updatePKUser(PKUser pkUser) {
        FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(pkUser.rect.width(), pkUser.rect.height());
        layoutParam.setMargins(pkUser.rect.left, pkUser.rect.top, 0, 0);
        setLayoutParams(layoutParam);
    }

    public ZEGOVideoView getVideoView() {
        View childAt = getChildAt(0);
        if (childAt instanceof ZEGOVideoView) {
            return (ZEGOVideoView) childAt;
        }
        return null;
    }

    private static final String TAG = "PKBattleView";

    /**
     * host user or other pk user
     *
     * @param open
     */
    public void onCameraUpdate(boolean open) {
        if (getVisibility() == VISIBLE) {
            if (open) {
                iconView.setVisibility(INVISIBLE);
                if (getVideoView() != null) {
                    getVideoView().setVisibility(VISIBLE);
                }
            } else {
                iconView.setVisibility(VISIBLE);
                if (getVideoView() != null) {
                    getVideoView().setVisibility(GONE);
                }
            }
        }
    }


    public void onTimeOut(boolean timeout) {
        if (timeout) {
            connectTipsView.setVisibility(VISIBLE);
        } else {
            connectTipsView.setVisibility(GONE);
        }
    }

    public void mutePlayAudio(boolean mute) {
        if (getVideoView() != null) {
            getVideoView().mutePlayAudio(mute);
        }
    }
}
