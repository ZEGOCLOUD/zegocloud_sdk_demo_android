package com.zegocloud.demo.bestpractice.activity;

import android.Manifest.permission;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.ActivityLiveStreamingBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKCallBack;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.ExpressService;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import com.zegocloud.demo.bestpractice.internal.utils.ZegoUtil;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoMediaPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.callback.ZIMRoomEnteredCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomFullInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;

public class LiveStreamingActivity extends AppCompatActivity {

    private ActivityLiveStreamingBinding binding;
    private String liveID;
    //    private AlertDialog inviteCoHostDialog;
    private AlertDialog zimReconnectDialog;
    private SurfaceView mediaPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveStreamingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().setTitle("Live Streaming");

        boolean isHost = getIntent().getBooleanExtra("host", true);
        liveID = getIntent().getStringExtra("liveID");

        binding.liveAudioroomTopbar.setRoomID(liveID);

        ZEGOLiveStreamingManager.getInstance().addListenersForUserJoinRoom();

        listenSDKEvent();

        binding.previewStart.setOnClickListener(v -> {
            loginRoom();
        });

        if (isHost) {
            // join when click start
            ZEGOSDKManager.getInstance().expressService.openCamera(true);
            ZEGOSDKManager.getInstance().expressService.openMicrophone(true);
            binding.previewStart.setVisibility(View.VISIBLE);
            binding.mainHostVideo.startPreviewOnly();

            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            ZEGOLiveStreamingManager.getInstance().setHostUser(currentUser);
        } else {
            // join right now
            ZEGOSDKManager.getInstance().expressService.openCamera(false);
            ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
            binding.previewStart.setVisibility(View.GONE);
            loginRoom();
        }
    }

    private void loginRoom() {
        ZEGOSDKManager.getInstance().loginRoom(liveID, ZegoScenario.BROADCAST, new ZEGOSDKCallBack() {
            @Override
            public void onResult(int errorCode, String message) {
                if (errorCode != 0) {
                    onJoinRoomFailed(errorCode);
                } else {
                    onJoinRoomSuccess();
                }
            }
        });
    }

    private void onJoinRoomFailed(int errorCode) {
        ToastUtil.show(this, "Join room Failed,errorCode: " + errorCode);
        finish();
    }

    private void onJoinRoomSuccess() {
        binding.previewStart.setVisibility(View.GONE);
        binding.liveBottomMenuBar.setVisibility(View.VISIBLE);

        boolean isHost = getIntent().getBooleanExtra("host", true);
        if (isHost) {
            ZEGOLiveStreamingManager.getInstance().startPublishingStream();
        }

        ZegoVideoConfig videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);
        ZEGOSDKManager.getInstance().expressService.setVideoConfig(videoConfig);

        ZEGOSDKManager.getInstance().expressService.startSoundLevelMonitor();

        int width = binding.getRoot().getWidth() / 4;
        binding.mainHostVideoIcon.setCircleBackgroundRadius(width);

        prepareGiftView();

        //        ZEGOLiveStreamingManager.getInstance().setMixLayoutProvider(new MixLayoutProvider() {
        //            @Override
        //            public ArrayList<ZegoMixerInput> getMixVideoInputs(List<String> streamList,
        //                ZegoMixerVideoConfig videoConfig) {
        //                ArrayList<ZegoMixerInput> inputList = new ArrayList<>();
        //                if (streamList.size() < 4) {
        //                    for (int i = 0; i < streamList.size(); i++) {
        //                        int left = (videoConfig.width / streamList.size()) * i;
        //                        int top = 0;
        //                        int right = (videoConfig.width / streamList.size()) * (i + 1);
        //                        int bottom = videoConfig.height;
        //                        ZegoMixerInput input_1 = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
        //                            new Rect(left, top, right, bottom));
        //                        input_1.renderMode = ZegoMixRenderMode.FILL;
        //                        inputList.add(input_1);
        //                    }
        //                } else if (streamList.size() == 4) {
        //                    for (int i = 0; i < streamList.size(); i++) {
        //                        int left = (videoConfig.width / 2) * (i % 2);
        //                        int top = (videoConfig.height / 2) * (i < 2 ? 0 : 1);
        //                        int right = left + videoConfig.width / 2;
        //                        int bottom = top + videoConfig.height / 2;
        //                        ZegoMixerInput input_1 = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
        //                            new Rect(left, top, right, bottom));
        //                        input_1.renderMode = ZegoMixRenderMode.FILL;
        //                        inputList.add(input_1);
        //                    }
        //                }
        //                return inputList;
        //            }
        //        });
    }

    private void prepareGiftView() {
        ZegoMediaPlayer mediaPlayer = ZEGOSDKManager.getInstance().expressService.getMediaPlayer();
        mediaPlayerView = new SurfaceView(this);
        mediaPlayerView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mediaPlayerView.setZOrderOnTop(true);

        ZegoCanvas canvas = new ZegoCanvas(mediaPlayerView);
        canvas.alphaBlend = true;
        mediaPlayer.setPlayerCanvas(canvas);

//        String giftUrl = "https://storage.zego.im/sdk-doc/Pics/zegocloud/gift/music_box.mp4";

        ZegoUtil.copyFileFromAssets(this, "gift.mp4", getExternalFilesDir(null) + "/gift.mp4");
        String giftUrl = getExternalFilesDir(null) + "/gift.mp4";

        ZEGOSDKManager.getInstance().expressService.loadResourceFile(giftUrl,
            new IZegoMediaPlayerLoadResourceCallback() {
                @Override
                public void onLoadResourceCallback(int errorCode) {
                    // load success first ,and then can display gift animation
                }
            });

        ZEGOSDKManager.getInstance().expressService.setMediaPlayerEventHandler(new IZegoMediaPlayerEventHandler() {

            @Override
            public void onMediaPlayerStateUpdate(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerState state,
                int errorCode) {
                super.onMediaPlayerStateUpdate(mediaPlayer, state, errorCode);
                if (state == ZegoMediaPlayerState.PLAY_ENDED) {
                    binding.giftParent.removeView(mediaPlayerView);
                }
            }
        });

        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onRoomCommandReceived(String senderID, String command) {
                super.onRoomCommandReceived(senderID, command);
                if (command.contains("gift_type")) {
                    if (mediaPlayerView.getParent() == null) {
                        binding.giftParent.addView(mediaPlayerView);
                        mediaPlayer.start();
                    }
                }
            }

            @Override
            public void onSendRoomCommand(int errorCode, String errorMessage, String command) {
                super.onSendRoomCommand(errorCode, errorMessage, command);
                if (errorCode == 0 && command.contains("gift_type")) {
                    if (mediaPlayerView.getParent() == null) {
                        binding.giftParent.addView(mediaPlayerView);
                        mediaPlayer.start();
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOSDKManager.getInstance().expressService.setMediaPlayerEventHandler(null);
            ZEGOLiveStreamingManager.getInstance().leave();
        }
    }

    public void listenSDKEvent() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCameraOpen(String userID, boolean open) {
                onRoomUserCameraOpen(userID, open);
            }

            @Override
            public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
                List<ZEGOSDKUser> coHostUserList = new ArrayList<>();
                for (ZEGOSDKUser zegosdkUser : userList) {
                    if (ZEGOLiveStreamingManager.getInstance().isHost(zegosdkUser.userID)) {
                        binding.mainHostVideo.setUserID(zegosdkUser.userID);
                        binding.mainHostVideoIcon.setLetter(zegosdkUser.userName);
                        binding.mainHostVideo.setStreamID(zegosdkUser.getMainStreamID());
                        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                            binding.mainHostVideo.setVisibility(View.VISIBLE);
                            binding.mainHostVideoLayout.setVisibility(View.VISIBLE);
                            binding.mainHostVideo.startPlayRemoteAudioVideo();
                        }
                    } else {
                        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                            coHostUserList.add(zegosdkUser);
                        }
                    }
                }
                if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                    binding.liveCohostView.addUser(coHostUserList);
                }
            }

            @Override
            public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
                List<ZEGOSDKUser> coHostUserList = new ArrayList<>();
                for (ZEGOSDKUser ZEGOSDKUser : userList) {
                    if (Objects.equals(binding.mainHostVideo.getUserID(), ZEGOSDKUser.userID)) {
                        binding.mainHostVideo.stopPlayRemoteAudioVideo();
                        binding.mainHostVideo.setStreamID("");
                        binding.mainHostVideo.setUserID("");
                        binding.mainHostVideoIcon.setLetter("");
                        binding.mainHostVideoLayout.setVisibility(View.GONE);

                    } else {
                        coHostUserList.add(ZEGOSDKUser);
                    }
                }
                binding.liveCohostView.removeUser(coHostUserList);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (state == ZegoPublisherState.PUBLISHING) {
                    if (ZEGOLiveStreamingManager.getInstance().isCoHost(currentUser.userID)) {
                        binding.liveCohostView.addUser(currentUser);
                    } else if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                        binding.mainHostVideo.setUserID(currentUser.userID);
                        binding.mainHostVideoIcon.setLetter(currentUser.userName);
                        binding.mainHostVideo.setStreamID(streamID);
                        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                            binding.mainHostVideoLayout.setVisibility(View.VISIBLE);
                        }
                    }
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    if (streamID.endsWith("_host")) {
                        binding.mainHostVideo.setUserID("");
                        binding.mainHostVideoIcon.setLetter("");
                        binding.mainHostVideo.setStreamID("");
                        binding.mainHostVideo.stopPublishAudioVideo();
                        binding.mainHostVideoLayout.setVisibility(View.GONE);
                    } else {
                        binding.liveCohostView.removeUser(currentUser);
                    }
                }
            }

            @Override
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
                JSONObject extendedData) {
                if (reason == ZegoRoomStateChangedReason.RECONNECT_FAILED) {
                    if (zimReconnectDialog != null && zimReconnectDialog.isShowing()) {
                        zimReconnectDialog.dismiss();
                    }
                    AlertDialog.Builder builder = new Builder(LiveStreamingActivity.this);
                    builder.setTitle("ZEGO SDK Disconnected");
                    builder.setMessage("Reconnected ?");
                    builder.setPositiveButton(R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();

                            ZEGOSDKManager.getInstance().expressService.removeRoomData();
                            ZEGOSDKManager.getInstance().expressService.removeUserData();
                            ZEGOSDKManager.getInstance().zimService.removeRoomData();
                            ZEGOSDKManager.getInstance().zimService.removeUserData();
                            ZEGOLiveStreamingManager.getInstance().removeUserData();
                            ZEGOLiveStreamingManager.getInstance().removeRoomData();

                            ZEGOSDKManager.getInstance().zimService.connectUser(currentUser.userID,
                                currentUser.userName, errorInfo -> {
                                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                        ZEGOSDKManager.getInstance().zimService.loginRoom(roomID,
                                            new ZIMRoomEnteredCallback() {
                                                @Override
                                                public void onRoomEntered(ZIMRoomFullInfo roomInfo,
                                                    ZIMError errorInfo) {
                                                    ZEGOSDKManager.getInstance().expressService.loginRoom(roomID,
                                                        new IZegoRoomLoginCallback() {
                                                            @Override
                                                            public void onRoomLoginResult(int errorCode1,
                                                                JSONObject extendedData1) {

                                                            }
                                                        });
                                                }
                                            });
                                    }
                                });
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    builder.create().show();
                }
            }
        });
        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {

            @Override
            public void onOutgoingRoomRequestAccepted(String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == RoomRequestType.REQUEST_COHOST) {
                    ExpressService expressService = ZEGOSDKManager.getInstance().expressService;
                    ZEGOSDKUser currentUser = expressService.getCurrentUser();
                    if (ZEGOLiveStreamingManager.getInstance().isAudience(currentUser.userID)) {
                        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
                        requestPermissionIfNeeded(permissions, new RequestCallback() {

                            @Override
                            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                                @NonNull List<String> deniedList) {
                                ZEGOLiveStreamingManager.getInstance().startCoHost();
                            }
                        });
                    }
                }
            }

            @Override
            public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
                JSONObject extendedData) {
                if (state == ZIMConnectionState.DISCONNECTED) {
                    AlertDialog.Builder builder = new Builder(LiveStreamingActivity.this);
                    builder.setTitle("ZIM DisConnected");
                    builder.setMessage("Reconnected ?");
                    builder.setPositiveButton(R.string.ok, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                            String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();

                            ZEGOSDKManager.getInstance().zimService.removeRoomData();
                            ZEGOSDKManager.getInstance().zimService.removeUserData();
                            ZEGOLiveStreamingManager.getInstance().removeUserData();
                            ZEGOLiveStreamingManager.getInstance().removeRoomData();
                            ZEGOSDKManager.getInstance().zimService.connectUser(currentUser.userID,
                                currentUser.userName, new ZIMLoggedInCallback() {
                                    @Override
                                    public void onLoggedIn(ZIMError errorInfo) {
                                        ZEGOSDKManager.getInstance().zimService.loginRoom(roomID,
                                            new ZIMRoomEnteredCallback() {
                                                @Override
                                                public void onRoomEntered(ZIMRoomFullInfo roomInfo,
                                                    ZIMError errorInfo) {

                                                }
                                            });
                                    }
                                });
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    zimReconnectDialog = builder.create();
                    zimReconnectDialog.show();
                }
            }
        });

        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {
            @Override
            public void onPKEnded() {
                onRoomPKEnded();
            }

            @Override
            public void onPKStarted() {
                onRoomPKStarted();
            }

            @Override
            public void onPKUserConnecting(String userID, long duration) {
                if (duration >= 60_000) {
                    ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                    if (!Objects.equals(currentUser.userID, userID)) {
                        ZEGOLiveStreamingManager.getInstance().removeUserFromPKBattle(userID);
                    } else {
                        ZEGOLiveStreamingManager.getInstance().quitPKBattle();
                    }
                }
            }
        });
    }

    private void onRoomUserCameraOpen(String userID, boolean open) {
        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
            if (ZEGOLiveStreamingManager.getInstance().isHost(userID)) {
                if (open) {
                    binding.mainHostVideo.setVisibility(View.VISIBLE);
                    binding.mainHostVideoIcon.setVisibility(View.GONE);
                } else {
                    binding.mainHostVideo.setVisibility(View.INVISIBLE);
                    binding.mainHostVideoIcon.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void onRoomPKStarted() {
        binding.liveCohostView.setVisibility(View.GONE);
        binding.pkVideoLayout.setVisibility(View.VISIBLE);
        binding.mainHostVideoLayout.setVisibility(View.GONE);
    }

    private void onRoomPKEnded() {
        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        binding.pkVideoLayout.setVisibility(View.INVISIBLE);
        if (hostUser != null) {
            binding.mainHostVideoLayout.setVisibility(View.VISIBLE);
        }
        binding.liveCohostView.setVisibility(View.VISIBLE);

        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            binding.mainHostVideo.startPreviewOnly();
        } else {
            if (hostUser != null) {
                String hostMainStreamID = hostUser.getMainStreamID();
                if (hostMainStreamID != null) {
                    binding.mainHostVideo.startPlayRemoteAudioVideo();
                }
            }
        }

        if (hostUser != null) {
            onRoomUserCameraOpen(hostUser.userID, hostUser.isCameraOpen());
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