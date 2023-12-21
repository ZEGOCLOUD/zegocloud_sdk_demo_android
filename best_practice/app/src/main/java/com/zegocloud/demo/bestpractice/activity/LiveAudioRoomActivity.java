package com.zegocloud.demo.bestpractice.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.bestpractice.databinding.ActivityLiveAudioRoomBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomLayoutConfig;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKCallBack;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamResourceMode;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import org.json.JSONObject;

public class LiveAudioRoomActivity extends AppCompatActivity {

    private ActivityLiveAudioRoomBinding binding;
    private LiveAudioRoomLayoutConfig seatLayoutConfig;

    String TAG = "LiveAudioRoomActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveAudioRoomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isHost = getIntent().getBooleanExtra("host", false);
        String roomID = getIntent().getStringExtra("liveID");
        if (TextUtils.isEmpty(roomID)) {
            finish();
            return;
        }

        getSupportActionBar().setTitle("Live Audio Room");

        binding.liveAudioroomTopbar.setRoomID(roomID);
        // two rows, four columns
        seatLayoutConfig = new LiveAudioRoomLayoutConfig();
        seatLayoutConfig.rowSpacing = Utils.dp2px(8, getResources().getDisplayMetrics());
        ZEGOLiveAudioRoomManager.getInstance().init(seatLayoutConfig);
        binding.seatContainer.setLayoutConfig(seatLayoutConfig);

        ZEGOSDKManager.getInstance().expressService.openCamera(false);
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                for (ZegoStream zegoStream : streamList) {
                    if (updateType == ZegoUpdateType.ADD) {
                        ZegoPlayerConfig config = new ZegoPlayerConfig();
                        config.resourceMode = ZegoStreamResourceMode.ONLY_RTC;
                        ZEGOSDKManager.getInstance().expressService.startPlayingStream(zegoStream.streamID, config);
                    } else {
                        ZEGOSDKManager.getInstance().expressService.stopPlayingStream(zegoStream.streamID);
                    }
                }
            }
        });
        ZegoScenario chatRoom = ZegoScenario.HIGH_QUALITY_CHATROOM;
        ZEGOSDKManager.getInstance().loginRoom(roomID, chatRoom, new ZEGOSDKCallBack() {
            @Override
            public void onResult(int errorCode, String message) {
                if (errorCode != 0) {
                    Log.e(TAG, "onRoomLoginResult: error: " + errorCode);
                    finish();
                } else {
                    if (isHost) {
                        ZEGOLiveAudioRoomManager.getInstance().setHostAndLockSeat();
                        ZEGOLiveAudioRoomManager.getInstance().takeSeat(0, new ZIMRoomAttributesOperatedCallback() {
                            @Override
                            public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                                ZIMError errorInfo) {

                            }
                        });
                    }
                    initListenerAfterLoginRoom();
                }
            }
        });
    }

    private void initListenerAfterLoginRoom() {
        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onOutgoingRoomRequestAccepted(String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == RoomRequestType.REQUEST_TAKE_SEAT) {
                    int seatIndex = ZEGOLiveAudioRoomManager.getInstance().findFirstAvailableSeatIndex();
                    if (seatIndex != -1) {
                        ZEGOLiveAudioRoomManager.getInstance()
                            .takeSeat(seatIndex, new ZIMRoomAttributesOperatedCallback() {
                                @Override
                                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                                    ZIMError errorInfo) {

                                }
                            });
                    } else {
                        ToastUtil.show(LiveAudioRoomActivity.this, "Cannot find available seat");
                    }
                }
            }

            @Override
            public void onUserInfoUpdated(ZIM zim, ZIMUserFullInfo info) {
                binding.seatContainer.onUserAvatarUpdated(info.baseInfo.userID, info.userAvatarUrl);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOLiveAudioRoomManager.getInstance().leave();
        }
    }
}