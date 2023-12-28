package com.zegocloud.demo.bestpractice.activity;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.ActivityCallInvitationBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONObject;
import timber.log.Timber;

public class CallInvitationActivity extends AppCompatActivity {

    private ActivityCallInvitationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("CallInvitationActivity");

        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOCallInvitationManager.getInstance().setCallInviteUserComparator(new Comparator<CallInviteUser>() {
            @Override
            public int compare(CallInviteUser o1, CallInviteUser o2) {
                if (o1.getUserID().equals(currentUser.userID) && !o2.getUserID().equals(currentUser.userID)) {
                    return -1;
                } else if (!o1.getUserID().equals(currentUser.userID) && o2.getUserID().equals(currentUser.userID)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        ZEGOCallInvitationManager.getInstance().joinRoom(new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    onRoomJoinSuccess();
                } else {
                    ToastUtil.show(CallInvitationActivity.this, "Join Room failed,errorCode:" + errorCode);
                    finish();
                }
            }
        });
        listenSDKEvent();
    }

    private void onRoomJoinSuccess() {
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        if (callInviteInfo.isVideoCall()) {
            binding.callCameraBtn.open();
            binding.callCameraBtn.setVisibility(View.VISIBLE);
            binding.callCameraSwitchBtn.setVisibility(View.VISIBLE);
        } else {
            binding.callCameraBtn.close();
            binding.callCameraBtn.setVisibility(View.GONE);
            binding.callCameraSwitchBtn.setVisibility(View.GONE);
        }

        binding.callHangupBtn.setOnClickListener(v -> {
            finish();
        });

        binding.callMicBtn.open();
        binding.callSpeakerBtn.open();

        List<String> permissions;
        if (callInviteInfo.isVideoCall()) {
            permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        } else {
            permissions = Collections.singletonList(permission.RECORD_AUDIO);
        }
        requestPermissionIfNeeded(this, permissions, new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                binding.layoutMain.onPermissionAnswered(allGranted, grantedList, deniedList);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOCallInvitationManager.getInstance().quitCallAndLeaveRoom();
        }
    }

    public void listenSDKEvent() {
        ZEGOCallInvitationManager.getInstance().addCallListener(new CallChangedListener() {

            @Override
            public void onCallEnded(String requestID) {
                Timber.d("onCallEnded() called with: requestID = [" + requestID + "]");
                finish();
            }
        });
    }

    private void requestPermissionIfNeeded(Context context, List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init((FragmentActivity) context).permissions(permissions)
            .onExplainRequestReason((scope, deniedList) -> {
                String message = "";
                if (permissions.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = context.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = context.getString(R.string.permission_explain_mic);
                    }
                } else {
                    if (deniedList.size() == 1) {
                        if (deniedList.contains(permission.CAMERA)) {
                            message = context.getString(R.string.permission_explain_camera);
                        } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                            message = context.getString(R.string.permission_explain_mic);
                        }
                    } else {
                        message = context.getString(R.string.permission_explain_camera_mic);
                    }
                }
                scope.showRequestReasonDialog(deniedList, message, context.getString(R.string.ok));
            }).onForwardToSettings((scope, deniedList) -> {
                String message = "";
                if (permissions.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = context.getString(R.string.settings_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = context.getString(R.string.settings_mic);
                    }
                } else {
                    if (deniedList.size() == 1) {
                        if (deniedList.contains(permission.CAMERA)) {
                            message = context.getString(R.string.settings_camera);
                        } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                            message = context.getString(R.string.settings_mic);
                        }
                    } else {
                        message = context.getString(R.string.settings_camera_mic);
                    }
                }
                scope.showForwardToSettingsDialog(deniedList, message, context.getString(R.string.settings),
                    context.getString(R.string.cancel));
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