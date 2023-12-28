package com.zegocloud.demo.bestpractice.internal.sdk.basic;

import android.text.TextUtils;
import java.util.Objects;

/**
 * basic users ,for express SDK room users.
 */
public class ZEGOSDKUser {

    public String userID;
    public String userName;
    private String mainStreamID;
    private String shareStreamID;
    private boolean isCameraOpen;
    private boolean isMicrophoneOpen;

    public ZEGOSDKUser(String userID, String userName) {
        this.userID = userID;
        this.userName = userName;
    }

    public String getMainStreamID() {
        return mainStreamID;
    }

    public boolean hasStream() {
        return !TextUtils.isEmpty(mainStreamID) || !TextUtils.isEmpty(shareStreamID);
    }

    public void setStreamID(String streamID) {
        if (streamID.contains("main")) {
            this.mainStreamID = streamID;
        } else if (streamID.contains("share")) {
            this.shareStreamID = streamID;
        }
    }

    public void deleteStream(String streamID) {
        if (streamID.contains("main")) {
            mainStreamID = null;
        } else {
            shareStreamID = null;
        }
    }

    public String getShareStreamID() {
        return shareStreamID;
    }

    public boolean isCameraOpen() {
        return isCameraOpen;
    }

    public void setCameraOpen(boolean cameraOpen) {
        isCameraOpen = cameraOpen;
    }

    public boolean isMicrophoneOpen() {
        return isMicrophoneOpen;
    }

    public void setMicrophoneOpen(boolean microphoneOpen) {
        isMicrophoneOpen = microphoneOpen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZEGOSDKUser userInfo = (ZEGOSDKUser) o;
        return Objects.equals(userID, userInfo.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }

    @Override
    public String toString() {
        return "ZEGOLiveUser{" +
            "userID='" + userID + '\'' +
            ", userName='" + userName + '\'' +
            ", mainStreamID='" + mainStreamID + '\'' +
            ", shareStreamID='" + shareStreamID + '\'' +
            ", isCameraOpen=" + isCameraOpen +
            ", isMicrophoneOpen=" + isMicrophoneOpen +
            '}';
    }
}
