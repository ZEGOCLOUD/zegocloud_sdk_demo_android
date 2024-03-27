package com.zegocloud.demo.bestpractice.activity.call;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.ActivityCallEntryBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallExtendedData;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.entity.ZIMCallInvitationSentInfo;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CallEntryActivity extends AppCompatActivity {

    private ActivityCallEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.callUserId.getEditText().setText("samsung");
        binding.callUserVideo.setOnClickListener(v -> {
            String targetUserID = binding.callUserId.getEditText().getText().toString();
            if (TextUtils.isEmpty(targetUserID)) {
                binding.callUserId.setError("please input targetUserID");
                return;
            }
            List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
            requestPermissionIfNeeded(permissions, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        String[] split = targetUserID.split(",");
                        ZEGOCallInvitationManager.getInstance()
                            .inviteVideoCall(Arrays.asList(split), new ZIMCallInvitationSentCallback() {
                                @Override
                                public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info,
                                    ZIMError errorInfo) {
                                    if (errorInfo.code.value() == 0) {
                                        if (split.length > 1) {
                                            Intent intent = new Intent(CallEntryActivity.this,
                                                CallInvitationActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(CallEntryActivity.this, CallWaitActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });
                    }
                }
            });
        });

        binding.callUserAudio.setOnClickListener(v -> {
            String targetUserID = binding.callUserId.getEditText().getText().toString();
            if (TextUtils.isEmpty(targetUserID)) {
                binding.callUserId.setError("please input targetUserID");
                return;
            }
            List<String> permissions = Collections.singletonList(permission.RECORD_AUDIO);
            requestPermissionIfNeeded(permissions, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        String[] split = targetUserID.split(",");
                        ZEGOCallInvitationManager.getInstance().inviteVoiceCall(Arrays.asList(split), new ZIMCallInvitationSentCallback() {
                                @Override
                                public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info,
                                    ZIMError errorInfo) {
                                    // by default , requestID == roomID,used it to join room by roomID
                                    if (errorInfo.code.value() == 0) {
                                        if (split.length > 1) {
                                            Intent intent = new Intent(CallEntryActivity.this, CallInvitationActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Intent intent = new Intent(CallEntryActivity.this, CallWaitActivity.class);
                                            startActivity(intent);
                                        }
                                    }
                                }
                            });
                    }
                }
            });
        });

        binding.callRoomId.getEditText().setText("12437470458557595774");
        binding.callRoomButton.setOnClickListener(v -> {
            String callID = binding.callRoomId.getEditText().getText().toString();
            ZEGOCallInvitationManager.getInstance().setCallInviteInfo(callID, CallExtendedData.VIDEO_CALL);
            Intent intent = new Intent(CallEntryActivity.this, CallInvitationActivity.class);
            startActivity(intent);
        });
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