package com.zegocloud.demo.bestpractice.internal.business.call;

public interface ReceiveCallListener {

    void onReceiveNewCall(String requestID, String inviter, String userName, int type);
}
