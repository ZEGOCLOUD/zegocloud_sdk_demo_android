package com.zegocloud.demo.bestpractice.components.call;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.activity.call.CallInvitationActivity;
import com.zegocloud.demo.bestpractice.activity.call.CallWaitActivity;
import com.zegocloud.demo.bestpractice.databinding.DialogIncomingCallBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zim.entity.ZIMUserFullInfo;

public class IncomingCallDialog extends AppCompatActivity {

    private DialogIncomingCallBinding binding;
    private CallChangedListener callChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setGravity(Gravity.TOP);

        binding = DialogIncomingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFinishOnTouchOutside(false);

        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        callChangedListener = new CallChangedListener() {
            @Override
            public void onCallEnded(String requestID) {
                if (requestID.equals(callInviteInfo.requestID)) {
                    finish();
                }
            }

            @Override
            public void onCallCancelled(String requestID) {
                if (requestID.equals(callInviteInfo.requestID)) {
                    finish();
                }
            }

            @Override
            public void onCallTimeout(String requestID) {
                if (requestID.equals(callInviteInfo.requestID)) {
                    finish();
                }
            }
        };
        ZEGOCallInvitationManager.getInstance().addCallListener(callChangedListener);

        ZIMUserFullInfo userInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(callInviteInfo.inviter);
        if (userInfo != null) {
            binding.dialogCallIcon.setLetter(userInfo.baseInfo.userName);
            binding.dialogCallIcon.setIconUrl(userInfo.userAvatarUrl);
        }

        binding.dialogCallName.setText(userInfo.baseInfo.userName);
        if (callInviteInfo.isVideoCall()) {
            binding.dialogCallType.setText(R.string.zego_video_call);
            binding.dialogCallAccept.setImageResource(R.drawable.call_icon_dialog_video_accept);
        } else {
            binding.dialogCallType.setText(R.string.zego_voice_call);
            binding.dialogCallAccept.setImageResource(R.drawable.call_icon_dialog_voice_accept);
        }

        binding.dialogCallAccept.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance().acceptCallRequest(callInviteInfo.requestID, new UserRequestCallback() {
                    @Override
                    public void onUserRequestSend(int errorCode, String requestID) {
                        if (errorCode == 0) {
                            Intent intent = new Intent(IncomingCallDialog.this, CallInvitationActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            ToastUtil.show(IncomingCallDialog.this, "callAccept failed :" + errorCode);
                            finish();
                        }
                    }
                });
        });
        binding.dialogCallDecline.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance()
                .rejectCallRequest(callInviteInfo.requestID, new UserRequestCallback() {
                    @Override
                    public void onUserRequestSend(int errorCode, String requestID) {
                        if (errorCode != 0) {
                            ToastUtil.show(IncomingCallDialog.this, "callReject failed :" + errorCode);
                        }
                        finish();
                    }
                });
        });

        binding.dialogReceiveCall.setOnClickListener(v -> {
            Intent intent = new Intent(IncomingCallDialog.this, CallWaitActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZEGOCallInvitationManager.getInstance().removeCallListener(callChangedListener);
    }
}