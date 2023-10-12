package com.zegocloud.demo.bestpractice.activity;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.ZEGOAudioVideoView;
import com.zegocloud.demo.bestpractice.databinding.ActivityCallInvitationBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.FullCallInfo;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CallInvitationActivity extends AppCompatActivity {

    private ActivityCallInvitationBinding binding;
    private FullCallInfo callInfo;

    public static void startActivity(Context context, FullCallInfo callInfo) {
        Intent intent = new Intent(context, CallInvitationActivity.class);
        intent.putExtra("callInfo", callInfo.toString());
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("CallInvitationActivity");

        callInfo = FullCallInfo.parse(getIntent().getStringExtra("callInfo"));

        listenExpressEvent();

        if (callInfo.isVideoCall()) {
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
        if (callInfo.isVideoCall()) {
            permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        } else {
            permissions = Collections.singletonList(permission.RECORD_AUDIO);
        }
        requestPermissionIfNeeded(permissions, new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                // my video show in small view
                if (callInfo.isOutgoingCall) {
                    binding.selfVideoView.setUserID(callInfo.callerUserID);
                    binding.otherVideoView.setUserID(callInfo.calleeUserID);
                } else {
                    binding.selfVideoView.setUserID(callInfo.calleeUserID);
                    binding.otherVideoView.setUserID(callInfo.callerUserID);
                }

                binding.selfVideoView.startPreviewOnly();
                if (callInfo.isVideoCall()) {
                    binding.selfVideoView.showVideoView();
                } else {
                    binding.selfVideoView.showAudioView();
                }
                ViewGroup parent = (ViewGroup) binding.selfVideoView.getParent();
                parent.setVisibility(View.VISIBLE);

                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                String streamID = ZEGOCallInvitationManager.getInstance()
                    .generateUserStreamID(currentUser.userID, currentRoomID);
                binding.selfVideoView.setStreamID(streamID);
                binding.selfVideoView.startPublishAudioVideo();
            }
        });

        binding.selfVideoView.setOnClickListener(v -> {
            if (callInfo.isVideoCall()) {

            }
            ViewGroup selfVideoViewParent = (ViewGroup) binding.selfVideoView.getParent();
            ViewGroup otherVideoViewParent = (ViewGroup) binding.otherVideoView.getParent();
            if (otherVideoViewParent.getVisibility() != View.VISIBLE || callInfo.isVoiceCall()) {
                return;
            }
            selfVideoViewParent.removeView(binding.selfVideoView);
            otherVideoViewParent.removeView(binding.otherVideoView);
            selfVideoViewParent.addView(binding.otherVideoView);
            otherVideoViewParent.addView(binding.selfVideoView);
        });
        binding.otherVideoView.setOnClickListener(v -> {
            ViewGroup selfVideoViewParent = (ViewGroup) binding.selfVideoView.getParent();
            ViewGroup otherVideoViewParent = (ViewGroup) binding.otherVideoView.getParent();
            if (selfVideoViewParent.getVisibility() != View.VISIBLE || callInfo.isVoiceCall()) {
                return;
            }
            selfVideoViewParent.removeView(binding.selfVideoView);
            otherVideoViewParent.removeView(binding.otherVideoView);
            selfVideoViewParent.addView(binding.otherVideoView);
            otherVideoViewParent.addView(binding.selfVideoView);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ZEGOCallInvitationManager.getInstance().endCall();
    }

    public void listenExpressEvent() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {

            @Override
            public void onUserEnter(List<ZEGOSDKUser> userList) {
                super.onUserEnter(userList);
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                    if (audioVideoView != null) {
                        audioVideoView.setUserID(zegosdkUser.userID);
                    }
                }
            }

            @Override
            public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                    if (audioVideoView != null) {
                        audioVideoView.setUserID(zegosdkUser.userID);
                        audioVideoView.setStreamID(zegosdkUser.getMainStreamID());
                        audioVideoView.startPlayRemoteAudioVideo();
                    }
                }
            }

            @Override
            public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                    if (audioVideoView != null) {
                        audioVideoView.setStreamID("");
                        ViewGroup parent = (ViewGroup) audioVideoView.getParent();
                        parent.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCameraOpen(String userID, boolean open) {
                super.onCameraOpen(userID, open);
                ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(userID);
                if (audioVideoView != null) {
                    ZEGOSDKUser user = ZEGOSDKManager.getInstance().expressService.getUser(userID);
                    if (user.isCameraOpen() || user.isMicrophoneOpen()) {
                        ViewGroup parent = (ViewGroup) audioVideoView.getParent();
                        parent.setVisibility(View.VISIBLE);
                        if (user.isCameraOpen()) {
                            audioVideoView.showVideoView();
                        } else {
                            audioVideoView.showAudioView();
                        }
                    } else {
                        ViewGroup parent = (ViewGroup) audioVideoView.getParent();
                        parent.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                super.onMicrophoneOpen(userID, open);
                ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(userID);
                if (audioVideoView != null) {
                    ZEGOSDKUser user = ZEGOSDKManager.getInstance().expressService.getUser(userID);
                    if (user.isCameraOpen() || user.isMicrophoneOpen()) {
                        ViewGroup parent = (ViewGroup) audioVideoView.getParent();
                        parent.setVisibility(View.VISIBLE);
                        if (user.isCameraOpen()) {
                            audioVideoView.showVideoView();
                        } else {
                            audioVideoView.showAudioView();
                        }
                    } else {
                        ViewGroup parent = (ViewGroup) audioVideoView.getParent();
                        parent.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onUserLeft(List<ZEGOSDKUser> userList) {
                finish();
            }

        });
    }

    private ZEGOAudioVideoView getAudioVideoViewByUserID(String userID) {
        if (Objects.equals(binding.selfVideoView.getUserID(), userID)) {
            return binding.selfVideoView;
        }
        if (Objects.equals(binding.otherVideoView.getUserID(), userID)) {
            return binding.otherVideoView;
        }
        return null;
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