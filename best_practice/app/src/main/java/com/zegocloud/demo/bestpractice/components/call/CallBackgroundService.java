package com.zegocloud.demo.bestpractice.components.call;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import java.util.List;

public class CallBackgroundService extends Service {

    private final CallChangedListener listener;

    public CallBackgroundService() {
        listener = new CallChangedListener() {
            @Override
            public void onReceiveNewCall(String requestID, List<CallInviteUser> userList) {
                Intent intent = new Intent(getApplicationContext(), IncomingCallDialog.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        };
        ZEGOCallInvitationManager.getInstance().addCallListener(listener, false);
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