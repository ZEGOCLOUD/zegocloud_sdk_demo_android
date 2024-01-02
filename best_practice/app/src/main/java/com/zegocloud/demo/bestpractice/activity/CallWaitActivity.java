package com.zegocloud.demo.bestpractice.activity;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.ActivityCallWaitBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;

public class CallWaitActivity extends AppCompatActivity {

    private ActivityCallWaitBinding binding;
    private CallChangedListener listener;
    private CallInviteInfo callInviteInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallWaitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("CallWaitActivity");
        callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();

        if (callInviteInfo.isOutgoingCall) {
            binding.incomingCallAcceptButton.setVisibility(View.GONE);
            binding.incomingCallRejectButton.setVisibility(View.GONE);
            binding.outgoingCallCancelButton.setVisibility(View.VISIBLE);

            CallInviteUser inviteUser = callInviteInfo.userList.get(0);
            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            if (Objects.equals(inviteUser.getUserID(), currentUser.userID)) {
                inviteUser = callInviteInfo.userList.get(1);
            }
            ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(
                inviteUser.getUserID());
            if (zimUserFullInfo != null) {
                binding.waitingIcon.setLetter(zimUserFullInfo.baseInfo.userName);
                binding.waitingIcon.setIconUrl(zimUserFullInfo.userAvatarUrl);
            }
        } else {
            binding.incomingCallAcceptButton.setVisibility(View.VISIBLE);
            binding.incomingCallRejectButton.setVisibility(View.VISIBLE);
            binding.outgoingCallCancelButton.setVisibility(View.GONE);

            String inviter = callInviteInfo.inviter;
            ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(inviter);
            if (zimUserFullInfo != null) {
                binding.waitingIcon.setLetter(zimUserFullInfo.baseInfo.userName);
                binding.waitingIcon.setIconUrl(zimUserFullInfo.userAvatarUrl);
            }
        }

        List<String> permissions;
        if (callInviteInfo.isVideoCall()) {
            permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        } else {
            permissions = Collections.singletonList(permission.RECORD_AUDIO);
        }
        requestPermissionIfNeeded(permissions, new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (grantedList.contains(permission.CAMERA)) {
                    ZEGOSDKManager.getInstance().expressService.openCamera(true);
                    binding.videoView.startPreviewOnly();
                }
            }
        });

        if (callInviteInfo.isVideoCall()) {
            binding.incomingCallAcceptButton.setImageResource(R.drawable.call_icon_video_accept);
        } else {
            binding.incomingCallAcceptButton.setImageResource(R.drawable.call_icon_voice_accept);
        }

        binding.incomingCallAcceptButton.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance()
                .acceptCallRequest(callInviteInfo.requestID, new UserRequestCallback() {
                    @Override
                    public void onUserRequestSend(int errorCode, String requestID) {
                        if (errorCode == 0) {
                            Intent intent = new Intent(CallWaitActivity.this, CallInvitationActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            ToastUtil.show(CallWaitActivity.this, "send invite failed :" + errorCode);
                        }
                    }
                });
        });
        binding.incomingCallRejectButton.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance()
                .rejectCallRequest(callInviteInfo.requestID, new UserRequestCallback() {
                    @Override
                    public void onUserRequestSend(int errorCode, String requestID) {
                        if (errorCode != 0) {
                            ToastUtil.show(CallWaitActivity.this, "send reject failed :" + errorCode);
                        }
                        finish();
                    }
                });
        });

        binding.outgoingCallCancelButton.setOnClickListener(v -> {
            ZEGOCallInvitationManager.getInstance().endCallRequest(callInviteInfo.requestID, new UserRequestCallback() {
                @Override
                public void onUserRequestSend(int errorCode, String requestID) {
                    if (errorCode != 0) {
                        ToastUtil.show(CallWaitActivity.this, "end call failed :" + errorCode);
                    }
                    finish();

                }
            });
        });

        listener = new CallChangedListener() {
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

            @Override
            public void onInvitedUserAccepted(String requestID, CallInviteUser acceptUser) {
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (Objects.equals(acceptUser.getUserID(), currentUser.userID)) {
                    return;
                }
                if (requestID.equals(callInviteInfo.requestID)) {
                    Intent intent = new Intent(CallWaitActivity.this, CallInvitationActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        ZEGOCallInvitationManager.getInstance().addCallListener(listener);
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOCallInvitationManager.getInstance().removeCallListener(listener);
            ZEGOSDKManager.getInstance().expressService.openCamera(false);
            ZEGOSDKManager.getInstance().expressService.stopPreview();
        }
    }


    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.permission_explain_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.permission_explain_mic);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera_mic);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.settings_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.settings_mic);
                    }
                } else {
                    message = this.getString(R.string.settings_camera_mic);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                getString(R.string.cancel));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }
}