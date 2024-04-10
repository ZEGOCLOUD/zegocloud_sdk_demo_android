package com.zegocloud.demo.bestpractice.components.cohost;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.RoomRequestListDialog;
import com.zegocloud.demo.bestpractice.components.message.barrage.BottomInputDialog;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.business.cohost.CoHostService.Role;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService.PKBattleInfo;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.SwitchCameraButton;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ToggleCameraButton;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ToggleMicrophoneButton;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import java.util.Objects;
import timber.log.Timber;

public class BottomMenuBar extends LinearLayout {

    private RoomRequestButton coHostListButton;
    private CoHostButton coHostButton;
    private LinearLayout childLinearLayout;
    private ToggleCameraButton cameraButton;
    private ToggleMicrophoneButton microphoneButton;
    private SwitchCameraButton switchCameraButton;
    private PKButton pkButton;
    private RoomRequestListDialog roomRequestListDialog;
    private GiftButton giftButton;

    public BottomMenuBar(Context context) {
        super(context);
        initView();
    }

    public BottomMenuBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomMenuBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public BottomMenuBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(-1, -2));
        setGravity(Gravity.END | Gravity.CENTER_VERTICAL);

        ImageView messageButton = new ImageView(getContext());
        messageButton.setImageResource(R.drawable.audioroom_icon_im);
        messageButton.setScaleType(ScaleType.FIT_XY);
        addView(messageButton, generateChildImageLayoutParams());
        messageButton.setOnClickListener(v -> {
            if (getContext() instanceof Activity) {
                BottomInputDialog bottomInputDialog = new BottomInputDialog(getContext());
                bottomInputDialog.show();
            }
        });

        childLinearLayout = new LinearLayout(getContext());
        childLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        childLinearLayout.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LayoutParams params = new LayoutParams(0, -2, 1);
        addView(childLinearLayout, params);
        int paddingEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        childLinearLayout.setPadding(0, 0, paddingEnd, 0);

        pkButton = new PKButton(getContext());
        childLinearLayout.addView(pkButton, generateChildTextLayoutParams());

        coHostListButton = new RoomRequestButton(getContext());
        coHostListButton.setRoomRequestType(RoomRequestType.REQUEST_COHOST);
        coHostListButton.setOnClickListener(v -> {
            if (roomRequestListDialog == null) {
                roomRequestListDialog = new RoomRequestListDialog(getContext());
                roomRequestListDialog.setRoomRequestType(RoomRequestType.REQUEST_COHOST);
            }
            if (!roomRequestListDialog.isShowing()) {
                roomRequestListDialog.show();
            }
        });
        childLinearLayout.addView(coHostListButton, generateChildImageLayoutParams());

        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        cameraButton = new ToggleCameraButton(getContext());
        cameraButton.updateState(localUser.isCameraOpen());
        childLinearLayout.addView(cameraButton, generateChildImageLayoutParams());

        microphoneButton = new ToggleMicrophoneButton(getContext());
        microphoneButton.updateState(localUser.isMicrophoneOpen());
        childLinearLayout.addView(microphoneButton, generateChildImageLayoutParams());

        switchCameraButton = new SwitchCameraButton(getContext());
        childLinearLayout.addView(switchCameraButton, generateChildImageLayoutParams());

        coHostButton = new CoHostButton(getContext());
        childLinearLayout.addView(coHostButton, generateChildTextLayoutParams());

        giftButton = new GiftButton(getContext());
        childLinearLayout.addView(giftButton, generateChildTextLayoutParams());

        // init state
        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (currentUser == null) {
            onUserRoleChanged(Role.AUDIENCE);
        } else {
            if (Objects.equals(currentUser, hostUser)) {
                onUserRoleChanged(Role.HOST);
            } else {
                onUserRoleChanged(Role.AUDIENCE);
            }
        }

        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCameraOpen(String userID, boolean open) {
                if (userID.equals(localUser.userID)) {
                    cameraButton.updateState(open);
                }
            }

            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (userID.equals(localUser.userID)) {
                    microphoneButton.updateState(open);
                }
            }
        });

        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {
            @Override
            public void onRoleChanged(String userID, int after) {
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (Objects.equals(localUser.userID, userID)) {
                    onUserRoleChanged(after);
                }
            }

            @Override
            public void onPKStarted() {
                coHostButton.setVisibility(GONE);
                coHostListButton.setVisibility(GONE);
                if (roomRequestListDialog != null) {
                    roomRequestListDialog.dismiss();
                }
            }

            @Override
            public void onPKEnded() {
                if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                    coHostButton.setVisibility(GONE);
                    coHostListButton.setVisibility(VISIBLE);
                } else {
                    coHostButton.setVisibility(VISIBLE);
                    coHostListButton.setVisibility(GONE);
                }
            }
        });
    }

    private LayoutParams generateChildImageLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginStart = Utils.dp2px(4, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(4, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        layoutParams.leftMargin = marginStart;
        return layoutParams;
    }

    private LayoutParams generateChildTextLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginStart = Utils.dp2px(4, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(4, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        layoutParams.leftMargin = marginStart;
        return layoutParams;
    }

    public void onUserRoleChanged(@Role int role) {
        Timber.d("onUserRoleChanged() called with: role = [" + role + "]");
        if (role == Role.AUDIENCE) {
            coHostButton.setVisibility(VISIBLE);
            pkButton.setVisibility(GONE);
            coHostListButton.setVisibility(GONE);

            cameraButton.setVisibility(GONE);
            microphoneButton.setVisibility(GONE);
            switchCameraButton.setVisibility(GONE);

            giftButton.setVisibility(VISIBLE);
        } else if (role == Role.CO_HOST) {
            coHostButton.setVisibility(VISIBLE);
            pkButton.setVisibility(GONE);
            coHostListButton.setVisibility(GONE);

            cameraButton.setVisibility(VISIBLE);
            microphoneButton.setVisibility(VISIBLE);
            switchCameraButton.setVisibility(VISIBLE);
            giftButton.setVisibility(GONE);
        } else if (role == Role.HOST) {
            coHostButton.setVisibility(GONE);
            pkButton.setVisibility(VISIBLE);
            coHostListButton.setVisibility(VISIBLE);

            cameraButton.setVisibility(VISIBLE);
            microphoneButton.setVisibility(VISIBLE);
            switchCameraButton.setVisibility(VISIBLE);
            giftButton.setVisibility(GONE);
        }

        PKBattleInfo pkBattleInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
        if (pkBattleInfo != null) {
            coHostButton.setVisibility(GONE);
            coHostListButton.setVisibility(GONE);
        }
    }
}
