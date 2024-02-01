package com.zegocloud.demo.bestpractice.internal.sdk.zim;

public interface RoomCommandCallback {

    void onSendRoomCommand(int errorCode, String errorMessage, String command);
}
