package com.zegocloud.demo.bestpractice.internal.business.pk;

public interface PKListener {

    default void onPKStarted() {
    }

    default void onPKEnded() {
    }

    default void onReceiveStartPKRequest(String requestID, String inviter, String inviterName, String roomId) {
    }

    default void onReceiveStopPKRequest(String requestID) {
    }

    default void onInComingStartPKRequestTimeout(String requestID) {
    }

    default void onOutgoingStartPKRequestTimeout() {
    }

    default void onInComingStartPKRequestCancelled(String requestID) {
    }

    default void onOutgoingStartPKRequestRejected() {
    }

    default void onPKCameraOpen(String userID, boolean open) {
    }

    default void onPKMicrophoneOpen(String userID, boolean open) {
    }

    default void onPKSEITimeOut(String userID, boolean timeout) {
    }
}
