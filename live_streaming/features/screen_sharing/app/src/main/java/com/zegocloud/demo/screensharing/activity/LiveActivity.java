package com.zegocloud.demo.screensharing.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.screensharing.databinding.ActivityLiveBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoApiCalledEventHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoOrientation;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScreenCaptureExceptionType;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.constants.ZegoVideoSourceType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import org.json.JSONObject;

public class LiveActivity extends AppCompatActivity {

    private ActivityLiveBinding binding;
    private String userID;
    private String userName;
    private String roomID;
    private boolean isHost;
    private boolean isBroadCastScreen;
    private boolean isPlayScreenView;
    private boolean enableCamera = true;
    private IntentFilter configurationChangeFilter;
    private BroadcastReceiver configurationChangeReceiver;
    private ZegoVideoConfigPreset videoConfigPreset = ZegoVideoConfigPreset.PRESET_540P;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        roomID = getIntent().getStringExtra("roomID");
        isHost = getIntent().getBooleanExtra("host", false);

        if (!isHost) {
            binding.screenShareButton.setVisibility(View.GONE);
            binding.cameraButton.setVisibility(View.GONE);
        }

        binding.screenShareButton.setOnClickListener(v -> {
            if (isBroadCastScreen) {
                stopScreenSharing();
            } else {
                startPublishScreen();
            }

        });
        binding.cameraButton.setOnClickListener(v -> {
            enableCamera = !enableCamera;
            if (enableCamera) {
                binding.cameraButton.setText("Disable Camera");
                binding.videoView.setVisibility(View.VISIBLE);
            } else {
                binding.cameraButton.setText("Enable Camera");
                binding.videoView.setVisibility(View.GONE);
            }
            ZegoExpressEngine.getEngine().enableCamera(enableCamera);
        });

        startListenEvent();
        loginRoom();

        configurationChangeFilter = new IntentFilter();
        configurationChangeFilter.addAction("android.intent.action.CONFIGURATION_CHANGED");

        configurationChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ZegoOrientation orientation = ZegoOrientation.ORIENTATION_0;

                ZegoVideoConfig videoConfig = new ZegoVideoConfig(videoConfigPreset);
                ZegoVideoConfig preset540 = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_540P);

                if (Surface.ROTATION_0 == getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_0;
                    videoConfig.setEncodeResolution(preset540.encodeWidth, preset540.encodeHeight);

                } else if (Surface.ROTATION_180 == getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_180;
                    videoConfig.setEncodeResolution(preset540.encodeWidth, preset540.encodeHeight);

                } else if (Surface.ROTATION_270 == getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_270;
                    videoConfig.setEncodeResolution(preset540.encodeHeight, preset540.encodeWidth);

                } else if (Surface.ROTATION_90 == getWindowManager().getDefaultDisplay().getRotation()) {
                    orientation = ZegoOrientation.ORIENTATION_90;
                    videoConfig.setEncodeResolution(preset540.encodeHeight, preset540.encodeWidth);

                }

                ZegoExpressEngine.getEngine().setAppOrientation(orientation);
                ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
            }
        };
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            stopListenEvent();
            logoutRoom();
            stopPublishCamera();
            stopScreenSharing();
            if (configurationChangeReceiver != null) {
                unregisterReceiver(configurationChangeReceiver);
                configurationChangeReceiver = null;
            }
        }
    }

    void startPreview() {
        ZegoCanvas previewCanvas = new ZegoCanvas(binding.videoView);
        ZegoExpressEngine.getEngine().startPreview(previewCanvas);
    }

    void stopPreview() {
        ZegoExpressEngine.getEngine().stopPreview();
    }

    // Log in to a room.
    void loginRoom() {
        ZegoUser user = new ZegoUser(userID, userName);
        ZegoRoomConfig roomConfig = new ZegoRoomConfig();
        // The `onRoomUserUpdate` callback can be received only when
        // `ZegoRoomConfig` in which the `isUserStatusNotify` parameter is set to
        // `true` is passed.
        roomConfig.isUserStatusNotify = true;
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, roomConfig, (int error, JSONObject extendedData) -> {
            // Room login result. This callback is sufficient if you only need to
            // check the login result.
            if (error == 0) {
                // Login successful.
                // Start the preview and stream publishing.
                Toast.makeText(this, "Login successful.", Toast.LENGTH_LONG).show();

                if (isHost) {
                    startPreview();
                    startPublishCamera();
                }
                registerReceiver(configurationChangeReceiver, configurationChangeFilter);
            } else {
                // Login failed. For details, see [Error codes\|_blank](/404).
                Toast.makeText(this, "Login failed. error = " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    void logoutRoom() {
        ZegoExpressEngine.getEngine().logoutRoom();
    }


    void startPublishCamera() {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        String streamID = roomID + "_" + userID + "_main";
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);
    }


    void stopPublishCamera() {
        ZegoExpressEngine.getEngine().stopPublishingStream();
    }

    void startPublishScreen() {
        if (isBroadCastScreen) {
            return;
        }
        binding.screenShareButton.setText("Stop ScreenShare");
        binding.shareScreenTips.setVisibility(View.VISIBLE);
        showSmallVideoView();
        ZegoExpressEngine.getEngine().setVideoSource(ZegoVideoSourceType.SCREEN_CAPTURE, ZegoPublishChannel.AUX);
        ZegoExpressEngine.getEngine().startScreenCapture();
        ZegoExpressEngine.getEngine()
            .setVideoConfig(new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_720P), ZegoPublishChannel.AUX);
        String streamID = roomID + "_" + userID + "_share";
        ZegoExpressEngine.getEngine().startPublishingStream(streamID, ZegoPublishChannel.AUX);
        isBroadCastScreen = true;
    }

    void showSmallVideoView() {
        int viewWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
            getResources().getDisplayMetrics());
        int viewHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 144,
            getResources().getDisplayMetrics());
        LayoutParams params = new LayoutParams(viewWidth, viewHeight);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
            getResources().getDisplayMetrics());
        binding.videoView.setLayoutParams(params);
    }

    void showFullVideoView() {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        binding.videoView.setLayoutParams(params);
    }

    void stopScreenSharing() {
        if (!isBroadCastScreen) {
            return;
        }
        binding.screenShareButton.setText("Start ScreenShare");
        showFullVideoView();
        binding.shareScreenTips.setVisibility(View.GONE);
        ZegoExpressEngine.getEngine().setVideoSource(ZegoVideoSourceType.NONE, ZegoPublishChannel.AUX);
        ZegoExpressEngine.getEngine().stopPublishingStream(ZegoPublishChannel.AUX);
        ZegoExpressEngine.getEngine().stopScreenCapture();
        isBroadCastScreen = false;
    }

    void startPlayCameraStream(String streamID) {
        binding.videoView.setVisibility(View.VISIBLE);
        ZegoCanvas playCanvas = new ZegoCanvas(binding.videoView);
        playCanvas.viewMode = ZegoViewMode.ASPECT_FIT;
        ZegoPlayerConfig config = new ZegoPlayerConfig();
        config.resourceMode = ZegoStreamResourceMode.DEFAULT; // Live Streaming
        // config.resourceMode = ZegoStreamResourceMode.ONLY_L3; // Interactive Live Streaming
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, playCanvas, config);
    }

    void stopPlayCameraStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        binding.videoView.setVisibility(View.GONE);
    }

    private void startPlayScreenStream(String streamID) {
        ZegoCanvas playCanvas = new ZegoCanvas(binding.screenShareView);
        playCanvas.viewMode = ZegoViewMode.ASPECT_FIT;
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, playCanvas);
        binding.screenShareView.setVisibility(View.VISIBLE);
        isPlayScreenView = true;
    }

    void stopPlayScreenStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        binding.screenShareView.setVisibility(View.GONE);
        isPlayScreenView = false;
    }

    void startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            public void onScreenCaptureExceptionOccurred(ZegoScreenCaptureExceptionType exceptionType) {
                super.onScreenCaptureExceptionOccurred(exceptionType);
                stopScreenSharing();
            }

            @Override
            // Callback for updates on the status of the streams in the room.
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                // When `updateType` is set to `ZegoUpdateType.ADD`, an audio and video
                // stream is added, and you can call the `startPlayingStream` method to
                // play the stream.
                if (streamList.isEmpty()) {
                    return;
                }
                if (streamList.size() > 1) {
                    for (ZegoStream stream : streamList) {
                        onStreamUpdate(updateType, stream);
                    }
                } else {
                    ZegoStream stream = streamList.get(0);
                    onStreamUpdate(updateType, stream);
                }
            }

            @Override
            // Callback for updates on the status of other users in the room.
            // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                // You can implement service logic in the callback based on the login
                // and logout status of users.
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoUser user : userList) {
                        String text = user.userID + "logged in to the room.";
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                    }
                } else if (updateType == ZegoUpdateType.DELETE) {
                    for (ZegoUser user : userList) {
                        String text = user.userID + "logged out of the room.";
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            // Callback for updates on the current user's room connection status.
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int i,
                JSONObject jsonObject) {
                super.onRoomStateChanged(roomID, reason, i, jsonObject);
                if (reason == ZegoRoomStateChangedReason.LOGINING) {
                    // Logging in to a room. When `loginRoom` is called to log in to a
                    // room or `switchRoom` is called to switch to another room, the room
                    // enters this status, indicating that it is requesting a connection
                    // to the server. On the app UI, the status of logging in to the room
                    // is displayed.
                } else if (reason == ZegoRoomStateChangedReason.LOGINED) {
                    // Logging in to a room succeeds. When a user successfully logs in to
                    // a room or switches the room, the room enters this status. In this
                    // case, the user can receive notifications of addition or deletion of
                    // other users and their streams in the room. Only after a user
                    // successfully logs in to a room or switches the room,
                    // `startPublishingStream` and `startPlayingStream` can be called to
                    // publish and play streams properly.
                } else if (reason == ZegoRoomStateChangedReason.LOGIN_FAILED) {
                    // Logging in to a room fails. When a user fails to log in to a room
                    // or switch the room due to a reason such as incorrect AppID or
                    // Token, the room enters this status.
                    Toast.makeText(getApplicationContext(), "ZegoRoomStateChangedReason.LOGIN_FAILED",
                        Toast.LENGTH_LONG).show();
                } else if (reason == ZegoRoomStateChangedReason.RECONNECTING) {
                    // The room connection is temporarily interrupted. The SDK will retry
                    // internally if the interruption is caused by poor network quality.
                } else if (reason == ZegoRoomStateChangedReason.RECONNECTED) {
                    // Reconnecting a room succeeds. The SDK will retry internally if the
                    // interruption is caused by poor network quality. If the reconnection
                    // is successful, the room enters this status.
                } else if (reason == ZegoRoomStateChangedReason.RECONNECT_FAILED) {
                    // Reconnecting a room fails. The SDK will retry internally if the
                    // interruption is caused by poor network quality. If the reconnection
                    // fails, the room enters this status.
                    Toast.makeText(getApplicationContext(), "ZegoRoomStateChangedReason.RECONNECT_FAILED",
                        Toast.LENGTH_LONG).show();
                } else if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
                    // The server forces a user to log out of a room. If a user who has
                    // logged in to room A tries to log in to room B, the server forces
                    // the user to log out of room A and room A enters this status.
                    Toast.makeText(getApplicationContext(), "ZegoRoomStateChangedReason.KICK_OUT", Toast.LENGTH_LONG)
                        .show();
                } else if (reason == ZegoRoomStateChangedReason.LOGOUT) {
                    // Logging out of a room succeeds. This is the default status of a
                    // room before login. If a user successfully logs out of a room by
                    // calling `logoutRoom` or `switchRoom`, the room enters this status.
                } else if (reason == ZegoRoomStateChangedReason.LOGOUT_FAILED) {
                    // Logging out of a room fails. If a user fails to log out of a room
                    // by calling `logoutRoom` or `switchRoom`, the room enters this
                    // status.
                }
            }

            // Status notification of audio and video stream publishing.
            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                if (errorCode != 0) {
                    // Stream publishing exception.
                }
                if (state == ZegoPublisherState.PUBLISHING) {
                    // Publishing streams.
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    // Streams not published.
                    Toast.makeText(getApplicationContext(), "ZegoPublisherState.NO_PUBLISH", Toast.LENGTH_LONG).show();
                } else if (state == ZegoPublisherState.PUBLISH_REQUESTING) {
                    // Requesting stream publishing.
                }
            }

            // Status notifications of audio and video stream playing.
            // This callback is received when the status of audio and video stream
            // playing of a user changes. If an exception occurs during stream playing
            // due to a network interruption, the SDK automatically retries to play
            // the streams.
            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode,
                JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                if (errorCode != 0) {
                    // Stream playing exception.
                    Toast.makeText(getApplicationContext(),
                        "onPlayerStateUpdate, state:" + state + "errorCode:" + errorCode, Toast.LENGTH_LONG).show();
                }
                if (state == ZegoPlayerState.PLAYING) {
                    // Playing streams.
                } else if (state == ZegoPlayerState.NO_PLAY) {
                    // Streams not played.
                    Toast.makeText(getApplicationContext(), "ZegoPlayerState.NO_PLAY", Toast.LENGTH_LONG).show();
                } else if (state == ZegoPlayerState.PLAY_REQUESTING) {
                    // Requesting stream playing.
                }
            }

            @Override
            public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
                super.onPlayerVideoSizeChanged(streamID, width, height);
                if (streamID.endsWith("main")) {
                    if (isPlayScreenView) {
                        FrameLayout.LayoutParams params;
                        int maxSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128,
                            getResources().getDisplayMetrics());
                        if (width < height) {
                            int viewHeight = maxSize;
                            int viewWidth = maxSize * width / height;
                            params = new LayoutParams(viewWidth, viewHeight);
                            params.gravity = Gravity.BOTTOM | Gravity.END;
                        } else {
                            int viewWidth = maxSize;
                            int viewHeight = viewWidth * height / width;
                            params = new LayoutParams(viewWidth, viewHeight);
                            params.gravity = Gravity.BOTTOM | Gravity.END;
                        }
                        params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48,
                            getResources().getDisplayMetrics());
                        binding.videoView.setLayoutParams(params);
                    }
                }
            }

            @Override
            public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteCameraStateUpdate(streamID, state);
                if (state == ZegoRemoteDeviceState.DISABLE) {
                    binding.videoView.setVisibility(View.GONE);
                } else {
                    binding.videoView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void onStreamUpdate(ZegoUpdateType updateType, ZegoStream stream) {
        if (stream.streamID.endsWith("share")) {
            if (updateType == ZegoUpdateType.ADD) {
                showSmallVideoView();
                startPlayScreenStream(stream.streamID);
            } else {
                showFullVideoView();
                stopPlayScreenStream(stream.streamID);
            }
        } else {
            if (updateType == ZegoUpdateType.ADD) {
                startPlayCameraStream(stream.streamID);
            } else {
                stopPlayCameraStream(stream.streamID);
            }
        }
    }

    void stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }
}