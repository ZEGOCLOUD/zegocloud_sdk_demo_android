package com.zegocloud.demo.bestpractice.internal.business.pk;

public interface PKListener {

    default void onPKStarted() {
    }

    default void onPKEnded() {
    }

    default void onReceivePKBattleRequest(String requestID, String inviter, String inviterName, String roomId) {
    }


    default void onPKBattleAccepted(String userID, String extendedData) {

    }

    default void onInComingPKBattleTimeout(String requestID) {
    }

    default void onPKBattleTimeout(String userID, String extendedData) {
    }

    default void onPKBattleCancelled(String userID, String extendedData) {
    }

    default void onPKBattleRejected(String userID, String extendedData) {
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
}
