package com.zegocloud.demo.bestpractice.internal.business.call;

public interface CallChangedListener {

    void onReceiveNewCall(String requestID);

    void onInviteNewUser(String requestID, CallInviteUser inviteUser);

    void onBusyRejectCall(String requestID);

    void onInvitedUserRejected(String requestID, CallInviteUser rejectUser);

    void onInvitedUserTimeout(String requestID, CallInviteUser timeoutUser);

    void onInvitedUserQuit(String requestID, CallInviteUser quitUser);

    void onInvitedUserAccepted(String requestID, CallInviteUser acceptUser);

    void onCallEnded(String requestID);

    void onCallCancelled(String requestID);

    /**
     * i received a call,and don't response util the call timeout
     *
     * @param requestID
     */
    void onCallTimeout(String requestID);
}
