package com.zegocloud.demo.bestpractice.internal.business.call;

import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import java.util.List;

public interface CallChangedListener {

    /**
     * @param requestID
     * @param userList  all users called in this call.
     */
    default void onReceiveNewCall(String requestID, List<CallInviteUser> userList) {
    }

    default void onBusyRejectCall(String requestID) {
    }


    default void onInviteNewUser(String requestID, CallInviteUser inviteUser) {
    }


    default void onInvitedUserRejected(String requestID, CallInviteUser rejectUser) {
    }


    default void onInvitedUserTimeout(String requestID, CallInviteUser timeoutUser) {
    }


    default void onInvitedUserQuit(String requestID, CallInviteUser quitUser) {
    }


    default void onInvitedUserAccepted(String requestID, CallInviteUser acceptUser) {
    }


    default void onCallEnded(String requestID) {
    }


    default void onCallCancelled(String requestID) {
    }

    default void onCallUserInfoUpdate(ArrayList<ZIMUserFullInfo> userList) {
    }

    /**
     * i received a call,and don't response util the call timeout
     *
     * @param requestID
     */
    default void onCallTimeout(String requestID) {
    }
}
