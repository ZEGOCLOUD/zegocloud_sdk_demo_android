package com.zegocloud.demo.bestpractice.internal.business.pk;

import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;

public interface PKListener {

    default void onPKBattleReceived(String requestID, ZIMCallInvitationReceivedInfo info) {
    }

    default void onInComingPKBattleTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {
    }

    default void onPKBattleCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
    }

    default void onOutgoingPKBattleTimeout(String userID, String extendedData) {
    }


    default void onPKBattleAccepted(String userID, String extendedData) {

    }

    default void onPKBattleRejected(String userID, String extendedData) {
    }

    default void onPKStarted() {
    }

    default void onPKEnded() {
    }

    default void onPKUserCameraOpen(String userID, boolean open) {
    }

    default void onPKUserMicrophoneOpen(String userID, boolean open) {
    }

    default void onPKUserConnecting(String userID, long duration) {
    }

    default void onPKUserQuit(String userID, String extendedData) {

    }

    default void onPKUserJoin(String userID, String extendedData) {

    }

    default void onPKMixStreamError(int errorCode, String data) {

    }

    default void onPKUserUpdate() {

    }
}
