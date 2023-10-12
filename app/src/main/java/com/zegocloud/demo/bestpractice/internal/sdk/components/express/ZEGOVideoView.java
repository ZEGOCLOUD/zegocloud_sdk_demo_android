package com.zegocloud.demo.bestpractice.internal.sdk.components.express;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.TextureView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import im.zego.zegoexpress.constants.ZegoViewMode;

public class ZEGOVideoView extends TextureView {

    private String userID;
    private String streamID;

    public ZEGOVideoView(@NonNull Context context) {
        super(context);
    }

    public ZEGOVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ZEGOVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ZEGOVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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
        ZEGOSDKManager.getInstance().expressService.startPlayingStream(this, streamID, ZegoViewMode.ASPECT_FILL);
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
        ZEGOSDKManager.getInstance().expressService.startPreview(this, ZegoViewMode.ASPECT_FILL);
    }
}
