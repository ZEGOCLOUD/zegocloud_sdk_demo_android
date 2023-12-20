package com.zegocloud.demo.bestpractice.components.call;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.business.call.FullCallInfo;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import java.util.List;

public class CallBackgroundService extends Service {

    private final CallChangedListener listener;

    public CallBackgroundService() {
        listener = new CallChangedListener() {
            @Override
            public void onReceiveNewCall(String requestID, String inviterUserID, CallExtendedData originalExtendedData,
                List<CallInviteUser> userList) {
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();

                FullCallInfo fullCallInfo = new FullCallInfo();
                fullCallInfo.callID = requestID;
                fullCallInfo.callType = originalExtendedData.type;
                fullCallInfo.callerUserID = originalExtendedData.userID;
                fullCallInfo.callerUserName = originalExtendedData.userName;
                fullCallInfo.calleeUserID = localUser.userID;
                fullCallInfo.isOutgoingCall = false;

                Intent intent = new Intent(getApplicationContext(), IncomingCallDialog.class);
                intent.putExtra("callInfo", fullCallInfo.toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
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