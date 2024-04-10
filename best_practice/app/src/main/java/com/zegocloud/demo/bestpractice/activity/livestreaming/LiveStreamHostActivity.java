package com.zegocloud.demo.bestpractice.activity.livestreaming;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.cohost.LiveStreamingView;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKCallBack;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zegoexpress.constants.ZegoScenario;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LiveStreamHostActivity extends AppCompatActivity {

    private LiveStreamingView liveStreamingView;
    private AlertDialog zimReconnectDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        liveStreamingView = new LiveStreamingView(this);
        setContentView(liveStreamingView);

        getSupportActionBar().setTitle("Live Streaming");

        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        requestPermissionIfNeeded(permissions, new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
            }
        });

        //prepare for ZEGOLiveStreamingManager do something
        ZEGOSDKManager.getInstance().expressService.openCamera(true);
        ZEGOSDKManager.getInstance().expressService.openMicrophone(true);
        ZEGOLiveStreamingManager.getInstance().addListenersForUserJoinRoom();

        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOLiveStreamingManager.getInstance().setHostUser(currentUser);

        liveStreamingView.prepareForStartLive(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginRoom();
            }
        });
    }

    private void loginRoom() {
        //        listenSDKEvent();

        String liveID = getIntent().getStringExtra("liveID");

        ZEGOSDKManager.getInstance().loginRoom(liveID, ZegoScenario.BROADCAST, new ZEGOSDKCallBack() {
            @Override
            public void onResult(int errorCode, String message) {
                if (errorCode != 0) {
                    onJoinRoomFailed(errorCode);
                } else {
                    liveStreamingView.onJoinRoomSuccess(liveID);
                }
            }
        });
    }

    private void onJoinRoomFailed(int errorCode) {
        ToastUtil.show(this, "Join room Failed,errorCode: " + errorCode);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOLiveStreamingManager.getInstance().leave();
        }
    }

    //    public void listenSDKEvent() {
    //        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
    //
    //            @Override
    //            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
    //                JSONObject extendedData) {
    //                if (reason == ZegoRoomStateChangedReason.RECONNECT_FAILED) {
    //                    if (zimReconnectDialog != null && zimReconnectDialog.isShowing()) {
    //                        zimReconnectDialog.dismiss();
    //                    }
    //                    AlertDialog.Builder builder = new Builder(LiveStreamingActivity.this);
    //                    builder.setTitle("ZEGO SDK Disconnected");
    //                    builder.setMessage("Reconnected ?");
    //                    builder.setPositiveButton(R.string.ok, new OnClickListener() {
    //                        @Override
    //                        public void onClick(DialogInterface dialog, int which) {
    //                            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
    //
    //                            ZEGOSDKManager.getInstance().expressService.removeRoomData();
    //                            ZEGOSDKManager.getInstance().expressService.removeUserData();
    //                            ZEGOSDKManager.getInstance().zimService.removeRoomData();
    //                            ZEGOSDKManager.getInstance().zimService.removeUserData();
    //                            ZEGOLiveStreamingManager.getInstance().removeUserData();
    //                            ZEGOLiveStreamingManager.getInstance().removeRoomData();
    //
    //                            ZEGOSDKManager.getInstance().zimService.connectUser(currentUser.userID,
    //                                currentUser.userName, errorInfo -> {
    //                                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
    //                                        ZEGOSDKManager.getInstance().zimService.loginRoom(roomID,
    //                                            new ZIMRoomEnteredCallback() {
    //                                                @Override
    //                                                public void onRoomEntered(ZIMRoomFullInfo roomInfo,
    //                                                    ZIMError errorInfo) {
    //                                                    ZEGOSDKManager.getInstance().expressService.loginRoom(roomID,
    //                                                        new IZegoRoomLoginCallback() {
    //                                                            @Override
    //                                                            public void onRoomLoginResult(int errorCode1,
    //                                                                JSONObject extendedData1) {
    //
    //                                                            }
    //                                                        });
    //                                                }
    //                                            });
    //                                    }
    //                                });
    //                        }
    //                    });
    //                    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
    //                        @Override
    //                        public void onClick(DialogInterface dialog, int which) {
    //                            dialog.dismiss();
    //                            finish();
    //                        }
    //                    });
    //                    builder.create().show();
    //                }
    //            }
    //        });
    //        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
    //
    //            @Override
    //            public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
    //                JSONObject extendedData) {
    //                if (state == ZIMConnectionState.DISCONNECTED) {
    //                    AlertDialog.Builder builder = new Builder(LiveStreamingActivity.this);
    //                    builder.setTitle("ZIM DisConnected");
    //                    builder.setMessage("Reconnected ?");
    //                    builder.setPositiveButton(R.string.ok, new OnClickListener() {
    //                        @Override
    //                        public void onClick(DialogInterface dialog, int which) {
    //                            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
    //                            String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
    //
    //                            ZEGOSDKManager.getInstance().zimService.removeRoomData();
    //                            ZEGOSDKManager.getInstance().zimService.removeUserData();
    //                            ZEGOLiveStreamingManager.getInstance().removeUserData();
    //                            ZEGOLiveStreamingManager.getInstance().removeRoomData();
    //                            ZEGOSDKManager.getInstance().zimService.connectUser(currentUser.userID,
    //                                currentUser.userName, new ZIMLoggedInCallback() {
    //                                    @Override
    //                                    public void onLoggedIn(ZIMError errorInfo) {
    //                                        ZEGOSDKManager.getInstance().zimService.loginRoom(roomID,
    //                                            new ZIMRoomEnteredCallback() {
    //                                                @Override
    //                                                public void onRoomEntered(ZIMRoomFullInfo roomInfo,
    //                                                    ZIMError errorInfo) {
    //
    //                                                }
    //                                            });
    //                                    }
    //                                });
    //                        }
    //                    });
    //                    builder.setNegativeButton(R.string.cancel, new OnClickListener() {
    //                        @Override
    //                        public void onClick(DialogInterface dialog, int which) {
    //                            dialog.dismiss();
    //                            finish();
    //                        }
    //                    });
    //                    zimReconnectDialog = builder.create();
    //                    zimReconnectDialog.show();
    //                }
    //            }
    //        });
    //    }

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