package com.zegocloud.demo.bestpractice.internal.business;

public interface UserRequestCallback {

    void onUserRequestSend(int errorCode, String requestID);
}
