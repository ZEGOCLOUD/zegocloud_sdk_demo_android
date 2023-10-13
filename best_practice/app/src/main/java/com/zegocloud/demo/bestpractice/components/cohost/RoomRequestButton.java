package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequest;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMUserInfo;
import java.util.ArrayList;
import java.util.List;

public class RoomRequestButton extends FrameLayout {

    private ImageView imageView;
    private ImageFilterView redPoint;
    private int roomRequestType;

    public RoomRequestButton(@NonNull Context context) {
        super(context);
        initView();
    }

    public RoomRequestButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public RoomRequestButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public RoomRequestButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    public void setRoomRequestType(int roomRequestType) {
        this.roomRequestType = roomRequestType;
    }

    protected void initView() {
        imageView = new ImageView(getContext());
        addView(imageView);
        imageView.setImageResource(R.drawable.liveaudioroom_bottombar_cohost);
        imageView.setScaleType(ScaleType.CENTER);
        redPoint = new ImageFilterView(getContext());
        redPoint.setBackgroundColor(Color.parseColor("#FF0D23"));
        redPoint.setRoundPercent(1.0f);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        LayoutParams redPointParams = new LayoutParams(Utils.dp2px(8, displayMetrics), Utils.dp2px(8, displayMetrics));
        redPointParams.gravity = Gravity.TOP | Gravity.END;
        addView(redPoint, redPointParams);

        hideRedPoint();

        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onInComingRoomRequestReceived(String requestID, String extendedData) {
                checkRedPoint();
            }

            @Override
            public void onInComingRoomRequestCancelled(String requestID, String extendedData) {
                checkRedPoint();
            }

            @Override
            public void onAcceptIncomingRoomRequest(int errorCode, String requestID, String extendedData) {
                checkRedPoint();
            }

            @Override
            public void onRejectIncomingRoomRequest(int errorCode, String requestID, String extendedData) {
                checkRedPoint();
            }

            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);
                checkRedPoint();
            }
        });
        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {
            @Override
            public void onPKStarted() {
                hideRedPoint();
            }
        });
        setBackgroundResource(R.drawable.bg_cohost_btn);
    }

    private void showRedPoint() {
        redPoint.setVisibility(View.VISIBLE);
    }

    private void hideRedPoint() {
        redPoint.setVisibility(View.GONE);
    }

    public void checkRedPoint() {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (ZEGOLiveStreamingManager.getInstance().isHost(localUser.userID)) {
            List<RoomRequest> myReceivedRoomRequests = ZEGOSDKManager.getInstance().zimService.getMyReceivedRoomRequests();
            boolean showRedPoint = false;
            for (RoomRequest roomRequest : myReceivedRoomRequests) {
                String extendedData = roomRequest.extendedData;
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == roomRequestType) {
                    showRedPoint = true;
                    break;
                }
            }
            if (showRedPoint) {
                showRedPoint();
            } else {
                hideRedPoint();
            }
        }
    }
}
