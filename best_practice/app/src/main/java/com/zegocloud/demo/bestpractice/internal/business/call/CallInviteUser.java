package com.zegocloud.demo.bestpractice.internal.business.call;

import im.zego.zim.enums.ZIMCallUserState;

public class CallInviteUser {

    private String userID;
    public String userName;
    private String extendedData;
    private ZIMCallUserState callUserState;

    public CallInviteUser(String userID, ZIMCallUserState callUserState, String extendedData) {
        this.userID = userID;
        this.callUserState = callUserState;
        this.extendedData = extendedData;
    }

    public String getUserID() {
        return userID;
    }

    public void setExtendedData(String extendedData) {
        this.extendedData = extendedData;
    }

    public void setCallUserState(ZIMCallUserState callUserState) {
        this.callUserState = callUserState;
    }

    public String getExtendedData() {
        return extendedData;
    }

    public ZIMCallUserState getCallUserState() {
        return callUserState;
    }

    public boolean isAccepted() {
        return callUserState == ZIMCallUserState.ACCEPTED;
    }

    public boolean isWaiting() {
        return callUserState == ZIMCallUserState.RECEIVED;
    }

    @Override
    public String toString() {
        return "CallInviteUser{" +
            "userID='" + userID + '\'' +
            ", userName='" + userName + '\'' +
            ", extendedData='" + extendedData + '\'' +
            ", callUserState=" + callUserState +
            '}';
    }
}
