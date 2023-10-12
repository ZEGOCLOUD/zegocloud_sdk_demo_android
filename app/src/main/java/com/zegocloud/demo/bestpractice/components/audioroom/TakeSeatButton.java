package com.zegocloud.demo.bestpractice.components.audioroom;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZTextButton;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequest;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequestCallback;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class TakeSeatButton extends ZTextButton {

    private String mRequestID;

    public TakeSeatButton(@NonNull Context context) {
        super(context);
    }

    public TakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TakeSeatButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
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
                if (Objects.equals(requestID, mRequestID)) {
                    updateUI();
                }
            }

            @Override
            public void onCancelOutgoingRoomRequest(int errorCode, String requestID, String extendedData) {
                if (errorCode == 0) {
                    if (Objects.equals(requestID, mRequestID)) {
                        mRequestID = null;
                        updateUI();
                    }
                }
            }

            @Override
            public void onOutgoingRoomRequestAccepted(String requestID, String extendedData) {
                if (Objects.equals(requestID, mRequestID)) {
                    mRequestID = null;
                    updateUI();
                }
            }

            @Override
            public void onOutgoingRoomRequestRejected(String requestID, String extendedData) {
                if (Objects.equals(requestID, mRequestID)) {
                    mRequestID = null;
                    updateUI();
                }
            }
        });
        updateUI();
    }

    @Override
    protected void afterClick() {
        super.afterClick();
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOSDKUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();
        if (localUser == null) {
            return;
        }
        if (hostUser == null) {
            RoomRequest roomRequest = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(mRequestID);
            if (roomRequest != null) {
                ZEGOSDKManager.getInstance().zimService.cancelRoomRequest(roomRequest.requestID,
                    new RoomRequestCallback() {
                        @Override
                        public void onRoomRequestSend(int errorCode, String requestID) {

                        }
                    });
                mRequestID = null;
            } else {
                mRequestID = null;
                updateUI();
            }

        } else {
            RoomRequest roomRequest = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(mRequestID);
            if (roomRequest == null) {
                int availableSeatIndex = ZEGOLiveAudioRoomManager.getInstance().findFirstAvailableSeatIndex();
                if (availableSeatIndex == -1) {
                    ToastUtil.show(getContext(), "cannot find available seat");
                    return;
                }
                RoomRequestExtendedData extendedData = new RoomRequestExtendedData();
                extendedData.roomRequestType = RoomRequestType.REQUEST_TAKE_SEAT;
                ZEGOSDKManager.getInstance().zimService.sendRoomRequest(hostUser.userID, extendedData.toString(),
                    new RoomRequestCallback() {
                        @Override
                        public void onRoomRequestSend(int errorCode, String requestID) {
                            if (errorCode == 0) {
                                mRequestID = requestID;
                            }
                        }
                    });
            } else {
                ZEGOSDKManager.getInstance().zimService.cancelRoomRequest(roomRequest.requestID,
                    new RoomRequestCallback() {
                        @Override
                        public void onRoomRequestSend(int errorCode, String requestID) {
                            mRequestID = null;
                            updateUI();
                        }
                    });
            }
        }
    }

    public void updateUI() {
        RoomRequest roomRequest = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(mRequestID);
        if (roomRequest == null) {
            setText("Apply to Take Seat");
        } else {
            setText("Cancel Take Seat");
        }
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setCompoundDrawablesWithIntrinsicBounds(R.drawable.liveaudioroom_bottombar_cohost, 0, 0, 0);
    }
}
