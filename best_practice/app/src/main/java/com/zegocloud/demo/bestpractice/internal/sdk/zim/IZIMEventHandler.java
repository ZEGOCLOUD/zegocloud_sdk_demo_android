package com.zegocloud.demo.bestpractice.internal.sdk.zim;

import im.zego.zim.callback.ZIMEventHandler;
import java.util.List;
import java.util.Map;

public abstract class IZIMEventHandler extends ZIMEventHandler {

    // ---- room
    public void onInComingRoomRequestReceived(String requestID, String extendedData) {
    }

    public void onInComingRoomRequestCancelled(String requestID, String extendedData) {
    }

    public void onAcceptIncomingRoomRequest(int errorCode, String requestID, String extendedData) {
    }

    public void onRejectIncomingRoomRequest(int errorCode, String requestID, String extendedData) {
    }

    public void onSendRoomRequest(int errorCode, String requestID, String extendedData) {
    }

    public void onCancelOutgoingRoomRequest(int errorCode, String requestID, String extendedData) {
    }

    public void onOutgoingRoomRequestAccepted(String requestID,String extendedData) {
    }

    public void onOutgoingRoomRequestRejected(String requestID,String extendedData) {
    }

    public void onRoomAttributesUpdated2(List<Map<String, String>> setProperties,
        List<Map<String, String>> deleteProperties) {
    }
    // ---------room end---

    public void onInComingUserRequestReceived(String requestID, String inviter, String extendedData) {
    }

    public void onInComingUserRequestTimeout(String requestID) {
    }

    public void onInComingUserRequestCancelled(String requestID, String inviter, String extendedData) {
    }

    public void onOutgoingUserRequestTimeout(String requestID) {
    }

    public void onOutgoingUserRequestAccepted(String requestID, String invitee, String extendedData) {
    }

    public void onOutgoingUserRequestRejected(String requestID, String invitee, String extendedData) {
    }

    public void onUserAvatarUpdated(String userID, String url) {
    }


    public void onSendRoomCommand(int errorCode, String command) {

    }

    public void onRoomCommandReceived(String senderID, String command) {

    }
}
