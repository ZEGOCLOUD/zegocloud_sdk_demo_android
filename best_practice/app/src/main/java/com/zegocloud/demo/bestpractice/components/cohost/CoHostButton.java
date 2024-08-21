package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.business.cohost.CoHostService.Role;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZTextButton;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequest;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequestCallback;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.ZIMService;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import java.util.Objects;

public class CoHostButton extends ZTextButton {

    private String mRoomRequestID;

    public CoHostButton(@NonNull Context context) {
        super(context);
    }

    public CoHostButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CoHostButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void initView() {
        super.initView();

        setTextColor(Color.WHITE);
        setTextSize(13);
        setGravity(Gravity.CENTER);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        setPadding(Utils.dp2px(14, displayMetrics), 0, Utils.dp2px(16, displayMetrics), 0);
        setCompoundDrawablePadding(Utils.dp2px(6, displayMetrics));

        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onSendRoomRequest(int errorCode, String requestID, String extendedData) {
                if (errorCode == 0) {
                    if (Objects.equals(requestID, mRoomRequestID)) {
                        requestCoHostUI();
                    }
                }
            }

            @Override
            public void onCancelOutgoingRoomRequest(int errorCode, String requestID, String extendedData) {
                if (errorCode == 0) {
                    if (Objects.equals(requestID, mRoomRequestID)) {
                        removeRoomRequestID();
                        audienceUI();
                    }
                }
            }

            @Override
            public void onOutgoingRoomRequestAccepted(String requestID, String extendedData) {
                if (Objects.equals(requestID, mRoomRequestID)) {
                    removeRoomRequestID();
                    coHostUI();
                }
            }

            @Override
            public void onOutgoingRoomRequestRejected(String requestID, String extendedData) {
                if (Objects.equals(requestID, mRoomRequestID)) {
                    removeRoomRequestID();
                    audienceUI();
                }
            }
        });

        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {
            @Override
            public void onRoleChanged(String userID, int after) {
                Log.d(TAG, "onRoleChanged() called with: userID = [" + userID + "], after = [" + after + "]");
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (currentUser.userID.equals(userID)) {
                    if (after == Role.AUDIENCE) {
                        mRoomRequestID = null;
                    }
                    updateUI();
                }
            }
        });
        audienceUI();
    }

    private static final String TAG = "CoHostButton";

    @Override
    protected void afterClick() {
        super.afterClick();
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        if (localUser == null || hostUser == null) {
            return;
        }

        if (ZEGOLiveStreamingManager.getInstance().isCoHost(localUser.userID)) {
            ZEGOLiveStreamingManager.getInstance().endCoHost();
            audienceUI();
        } else {
            RoomRequest roomRequest = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(mRoomRequestID);
            RoomRequestExtendedData extendedData = new RoomRequestExtendedData();
            extendedData.roomRequestType = RoomRequestType.REQUEST_COHOST;
            if (roomRequest == null) {
                ZEGOSDKManager.getInstance().zimService.sendRoomRequest(hostUser.userID, extendedData.toString(),
                    new RoomRequestCallback() {
                        @Override
                        public void onRoomRequestSend(int errorCode, String requestID) {
                            if (errorCode == 0) {
                                mRoomRequestID = requestID;
                            }
                        }
                    });
            } else {
                ZEGOSDKManager.getInstance().zimService.cancelRoomRequest(roomRequest.requestID, null);
            }
        }

    }

    public void updateUI() {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZIMService zimService = ZEGOSDKManager.getInstance().zimService;
        if (ZEGOLiveStreamingManager.getInstance().isCoHost(localUser.userID)) {
            coHostUI();
        } else if (ZEGOLiveStreamingManager.getInstance().isAudience(localUser.userID)) {
            RoomRequest roomRequest = zimService.getRoomRequestByRequestID(mRoomRequestID);
            if (roomRequest == null) {
                audienceUI();
            } else {
                requestCoHostUI();
            }
        }
    }

    private void coHostUI() {
        setText("End");
        setBackgroundResource(R.drawable.livestreaming_bg_end_cohost_btn);
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
    }

    private void requestCoHostUI() {
        setText("Cancel CoHost");
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
    }

    private void audienceUI() {
        setText("Request Co-host");
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
    }

    public void removeRoomRequestID() {
        mRoomRequestID = null;
    }
}
