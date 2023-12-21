package com.zegocloud.demo.bestpractice.components.call;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;

public class CallBackgroundService extends Service {

    private final CallChangedListener listener;

    public CallBackgroundService() {
        listener = new CallChangedListener() {
            @Override
            public void onReceiveNewCall(String requestID) {
                Intent intent = new Intent(getApplicationContext(), IncomingCallDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onInviteNewUser(String requestID, CallInviteUser inviteUser) {

            }

            @Override
            public void onBusyRejectCall(String requestID) {

            }

            @Override
            public void onInvitedUserRejected(String requestID, CallInviteUser rejectUser) {

            }

            @Override
            public void onInvitedUserTimeout(String requestID, CallInviteUser timeoutUser) {

            }

            @Override
            public void onInvitedUserQuit(String requestID, CallInviteUser quitUser) {

            }

            @Override
            public void onInvitedUserAccepted(String requestID, CallInviteUser acceptUser) {

            }

            @Override
            public void onCallEnded(String requestID) {

            }

            @Override
            public void onCallCancelled(String requestID) {

            }

            @Override
            public void onCallTimeout(String requestID) {

            }
        };
        ZEGOCallInvitationManager.getInstance().addCallListener(listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZEGOCallInvitationManager.getInstance().removeCallListener(listener);
    }
}