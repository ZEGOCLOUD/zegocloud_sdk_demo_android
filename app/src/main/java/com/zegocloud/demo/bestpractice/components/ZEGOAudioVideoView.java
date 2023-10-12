package com.zegocloud.demo.bestpractice.components;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.zegoexpress.constants.ZegoViewMode;

public class ZEGOAudioVideoView extends ConstraintLayout {

    private String userID;
    private String streamID;
    private TextureView textureView;
    private LetterIconView letterIconView;

    public ZEGOAudioVideoView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZEGOAudioVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZEGOAudioVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public ZEGOAudioVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        letterIconView = new LetterIconView(getContext());
        ConstraintLayout.LayoutParams params = new LayoutParams(LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.MATCH_CONSTRAINT);
        params.matchConstraintPercentWidth = 0.45f;
        params.dimensionRatio = "H,1:1";
        params.startToStart = LayoutParams.PARENT_ID;
        params.endToEnd = LayoutParams.PARENT_ID;
        params.topToTop = LayoutParams.PARENT_ID;
        params.bottomToBottom = LayoutParams.PARENT_ID;
        letterIconView.setLayoutParams(params);
        addView(letterIconView, params);
        letterIconView.setVisibility(GONE);

        textureView = new TextureView(getContext());
        addView(textureView);
    }

    public String getUserID() {
        return userID;
    }


    public void setUserID(String userID) {
        this.userID = userID;
        ZEGOSDKUser user = ZEGOSDKManager.getInstance().expressService.getUser(userID);
        if (user != null) {
            letterIconView.setLetter(user.userName);
        } else {
            letterIconView.setLetter("");
        }
    }

    public String getStreamID() {
        return streamID;
    }

    public void setStreamID(String streamID) {
        this.streamID = streamID;
    }

    public void startPlayRemoteAudioVideo() {
        if (TextUtils.isEmpty(streamID)) {
            return;
        }
        ZEGOSDKManager.getInstance().expressService.startPlayingStream(textureView, streamID, ZegoViewMode.ASPECT_FILL);
    }

    public void stopPlayRemoteAudioVideo() {
        if (TextUtils.isEmpty(streamID)) {
            return;
        }
        ZEGOSDKManager.getInstance().expressService.stopPlayingStream(streamID);
    }

    public void mutePlayAudio(boolean mute) {
        if (TextUtils.isEmpty(streamID)) {
            return;
        }
        ZEGOSDKManager.getInstance().expressService.mutePlayStreamAudio(streamID, mute);
    }

    public void startPublishAudioVideo() {
        ZEGOSDKManager.getInstance().expressService.startPublishingStream(streamID);
    }

    public void stopPublishAudioVideo() {
        ZEGOSDKManager.getInstance().expressService.stopPublishingStream();
    }

    public void startPreviewOnly() {
        ZEGOSDKManager.getInstance().expressService.startPreview(textureView, ZegoViewMode.ASPECT_FILL);
    }

    public void showVideoView() {
        textureView.setVisibility(VISIBLE);
        letterIconView.setVisibility(GONE);
    }

    public void showAudioView() {
        textureView.setVisibility(GONE);
        letterIconView.setVisibility(VISIBLE);
    }
}
