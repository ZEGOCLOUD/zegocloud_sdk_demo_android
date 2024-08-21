package com.zegocloud.demo.bestpractice.internal.sdk.express;

import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import java.util.ArrayList;
import java.util.List;

public abstract class IExpressEngineEventHandler extends IZegoEventHandler {

    public void onRoomExtraInfoUpdate2(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
    }

    public void onCameraOpen(String userID, boolean open) {
    }

    public void onMicrophoneOpen(String userID, boolean open) {
    }

    public void onReceiveStreamAdd(List<ZEGOSDKUser> userList, String roomID) {
    }

    public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
    }

    public void onUserEnter(List<ZEGOSDKUser> userList) {
    }

    public void onUserLeft(List<ZEGOSDKUser> userList) {
    }

    public void onIMSendBarrageMessageResult(int errorCode, String message, String messageID) {
    }
}
