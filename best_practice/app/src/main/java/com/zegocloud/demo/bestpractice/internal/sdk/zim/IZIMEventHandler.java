package com.zegocloud.demo.bestpractice.internal.sdk.zim;

import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationEndedInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import im.zego.zim.entity.ZIMCallUserStateChangeInfo;
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

    public void onOutgoingRoomRequestAccepted(String requestID, String extendedData) {
    }

    public void onOutgoingRoomRequestRejected(String requestID, String extendedData) {
    }

    public void onRoomAttributesUpdated2(List<Map<String, String>> setProperties,
        List<Map<String, String>> deleteProperties) {
    }
    // ---------room end---

    public void onInComingUserRequestReceived(String requestID, ZIMCallInvitationReceivedInfo info) {
    }

    public void onInComingUserRequestCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
    }

    public void onOutgoingUserRequestTimeout(String requestID) {
    }

    public void onOutgoingUserRequestAccepted(String requestID, String invitee, String extendedData) {
    }

    public void onOutgoingUserRequestRejected(String requestID, String invitee, String extendedData) {
    }

    public void onUserRequestStateChanged(ZIMCallUserStateChangeInfo info, String requestID) {

    }

    public void onUserRequestEnded(String requestID, ZIMCallInvitationEndedInfo info) {

    }

    public void onInComingUserRequestTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {

    }

    public void onSendRoomCommand(int errorCode, String errorMessage, String command) {

    }

    public void onRoomCommandReceived(String senderID, String command) {

    }
}
