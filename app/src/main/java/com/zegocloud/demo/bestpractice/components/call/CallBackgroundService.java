package com.zegocloud.demo.bestpractice.components.call;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.FullCallInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.ReceiveCallListener;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;

public class CallBackgroundService extends Service {

    public CallBackgroundService() {
        ZEGOCallInvitationManager.getInstance().setReceiveCallListener(new ReceiveCallListener() {
            @Override
            public void onReceiveNewCall(String requestID, String inviter, String userName, int type) {
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                FullCallInfo fullCallInfo = new FullCallInfo();
                fullCallInfo.callID = requestID;
                fullCallInfo.callType = type;
                fullCallInfo.callerUserID = inviter;
                fullCallInfo.callerUserName = userName;
                fullCallInfo.calleeUserID = localUser.userID;
                fullCallInfo.isOutgoingCall = false;

                Intent intent = new Intent(getApplicationContext(), IncomingCallDialog.class);
                intent.putExtra("callInfo", fullCallInfo.toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZEGOCallInvitationManager.getInstance().setReceiveCallListener(null);
    }
}