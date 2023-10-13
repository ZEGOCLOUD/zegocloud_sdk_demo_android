package com.zegocloud.demo.quickstart;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;

import java.util.ArrayList;

import org.json.JSONObject;

public class CallPageActivity extends AppCompatActivity {
    private String userID;
    private String userName;

    // The value of `roomID` is generated locally and must be globally unique.
    // Users must log in to the same room to call each other.
    private String roomID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_page);
        userID = getIntent().getStringExtra("userID");
        userName = getIntent().getStringExtra("userName");
        roomID = getIntent().getStringExtra("roomID");

        startListenEvent();
        loginRoom();
        // Set a listener for the call stopping button.
        findViewById(R.id.callEndButton).setOnClickListener(new View.OnClickListener() {
            // Click to stop a call.
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            stopListenEvent();
            logoutRoom();
        }
    }

    void startPreview() {
        ZegoCanvas previewCanvas = new ZegoCanvas(findViewById(R.id.preview));
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
                startPreview();
                startPublish();
            } else {
                // Login failed. For details, see [Error codes\|_blank](/404).
                Toast.makeText(this, "Login failed. error = " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    void logoutRoom() {
        ZegoExpressEngine.getEngine().logoutRoom();
    }


    void startPublish() {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        String streamID = roomID + "_" + userID + "_call";
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);
    }


    void stopPublish() {
        ZegoExpressEngine.getEngine().stopPublishingStream();
    }


    void startPlayStream(String streamID) {
        findViewById(R.id.remoteUserView).setVisibility(View.VISIBLE);
        ZegoCanvas playCanvas = new ZegoCanvas(findViewById(R.id.remoteUserView));
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, playCanvas);
    }

    void stopPlayStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
        findViewById(R.id.remoteUserView).setVisibility(View.GONE);
    }

    void startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(new IZegoEventHandler() {
            @Override
            // Callback for updates on the status of the streams in the room.
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList, JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                // When `updateType` is set to `ZegoUpdateType.ADD`, an audio and video
                // stream is added, and you can call the `startPlayingStream` method to
                // play the stream.
                if (updateType == ZegoUpdateType.ADD) {
                    startPlayStream(streamList.get(0).streamID);
                } else {
                    stopPlayStream(streamList.get(0).streamID);
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
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int i, JSONObject jsonObject) {
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
                    Toast.makeText(getApplicationContext(), "ZegoRoomStateChangedReason.LOGIN_FAILED", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "ZegoRoomStateChangedReason.RECONNECT_FAILED", Toast.LENGTH_LONG).show();
                } else if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
                    // The server forces a user to log out of a room. If a user who has
                    // logged in to room A tries to log in to room B, the server forces
                    // the user to log out of room A and room A enters this status.
                    Toast.makeText(getApplicationContext(), "ZegoRoomStateChangedReason.KICK_OUT", Toast.LENGTH_LONG).show();
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
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode, JSONObject extendedData) {
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
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                if (errorCode != 0) {
                    // Stream playing exception.
                    Toast.makeText(getApplicationContext(), "onPlayerStateUpdate, state:" + state + "errorCode:" + errorCode, Toast.LENGTH_LONG).show();
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
        });
    }

    void stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null);
    }

}