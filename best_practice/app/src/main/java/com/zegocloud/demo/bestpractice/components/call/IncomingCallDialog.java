package com.zegocloud.demo.bestpractice.components.call;

import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.activity.CallInvitationActivity;
import com.zegocloud.demo.bestpractice.activity.CallWaitActivity;
import com.zegocloud.demo.bestpractice.databinding.DialogIncomingCallBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.FullCallInfo;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import org.json.JSONObject;

public class IncomingCallDialog extends AppCompatActivity {

    private DialogIncomingCallBinding binding;
    private FullCallInfo callInfo;
    private IZIMEventHandler zimEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setGravity(Gravity.TOP);

        binding = DialogIncomingCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setFinishOnTouchOutside(false);

        zimEventHandler = new IZIMEventHandler() {

            @Override
            public void onInComingUserRequestTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {
                if (requestID.equals(callInfo.callID)) {
                    finish();
                }
            }

            @Override
            public void onInComingUserRequestCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
                if (requestID.equals(callInfo.callID)) {
                    finish();
                }
            }
        };
        ZEGOSDKManager.getInstance().zimService.addEventHandler(zimEventHandler);

        callInfo = FullCallInfo.parse(getIntent().getStringExtra("callInfo"));

        binding.dialogCallIcon.setLetter(callInfo.callerUserName);

        binding.dialogCallName.setText(callInfo.callerUserName);
        if (callInfo.isVideoCall()) {
            binding.dialogCallType.setText("ZEGO VIDEO CALL");
            binding.dialogCallAccept.setImageResource(R.drawable.call_icon_dialog_video_accept);
        } else {
            binding.dialogCallType.setText("ZEGO VOICE CALL");
            binding.dialogCallAccept.setImageResource(R.drawable.call_icon_dialog_voice_accept);
        }

        binding.dialogCallAccept.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance().acceptCallRequest(callInfo.callID, new UserRequestCallback() {
                @Override
                public void onUserRequestSend(int errorCode, String requestID) {
                    if (errorCode == 0) {
                        if (callInfo.isVideoCall()) {
                            ZEGOSDKManager.getInstance().expressService.setRoomScenario(
                                ZegoScenario.STANDARD_VIDEO_CALL);
                        } else {
                            ZEGOSDKManager.getInstance().expressService.setRoomScenario(
                                ZegoScenario.STANDARD_VOICE_CALL);
                        }
                        ZEGOSDKManager.getInstance().expressService.loginRoom(callInfo.callID,
                            new IZegoRoomLoginCallback() {
                                @Override
                                public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                                    if (errorCode == 0) {
                                        CallInvitationActivity.startActivity(IncomingCallDialog.this, callInfo);
                                    } else {
                                        ToastUtil.show(IncomingCallDialog.this, "joinExpressRoom failed :" + errorCode);
                                    }
                                    finish();
                                }
                            });
                    } else {
                        ToastUtil.show(IncomingCallDialog.this, "callAccept failed :" + errorCode);
                        finish();
                    }
                }
            });
        });
        binding.dialogCallDecline.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance().rejectCallRequest(callInfo.callID, new UserRequestCallback() {
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
            CallWaitActivity.startActivity(IncomingCallDialog.this, callInfo);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZEGOSDKManager.getInstance().zimService.removeEventHandler(zimEventHandler);
    }
}