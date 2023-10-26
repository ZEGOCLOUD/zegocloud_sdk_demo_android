package com.zegocloud.demo.bestpractice.components.audioroom;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.BottomActionDialog;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager.LiveAudioRoomListener;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomLayoutAlignment;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomLayoutConfig;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomLayoutRowConfig;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomSeat;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LiveAudioRoomSeatContainer extends LinearLayout {

    private long lastClickTime;
    private ZEGOSDKUser roomHostUser;
    private BottomActionDialog switchSeatDialog;
    private BottomActionDialog leaveSeatDialog;
    private BottomActionDialog kickUserSeatDialog;
    private BottomActionDialog requestTakeSeatDialog;

    public LiveAudioRoomSeatContainer(@NonNull Context context) {
        super(context);
        initView();
    }

    public LiveAudioRoomSeatContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LiveAudioRoomSeatContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setOrientation(LinearLayout.VERTICAL);
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onUserLeft(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser zegosdkUser : userList) {
                    for (LiveAudioRoomSeat audioRoomSeat : ZEGOLiveAudioRoomManager.getInstance()
                        .getAudioRoomSeatList()) {
                        if (audioRoomSeat.isTakenByUser(zegosdkUser)) {
                            ZEGOLiveAudioRoomManager.getInstance().leaveSeat(audioRoomSeat.seatIndex, new ZIMRoomAttributesOperatedCallback() {
                                    @Override
                                    public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                                        ZIMError errorInfo) {

                                    }
                                });
                        }
                    }
                }

            }
        });
        ZEGOLiveAudioRoomManager.getInstance().addLiveAudioRoomListener(new LiveAudioRoomListener() {
            @Override
            public void onHostChanged(ZEGOSDKUser hostUser) {
                onHostUserChanged(hostUser);
            }

            @Override
            public void onLockSeatStatusChanged(boolean lock) {
                List<LiveAudioRoomSeat> seatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
                for (LiveAudioRoomSeat seat : seatList) {
                    ZEGOLiveAudioRoomSeatView seatView = getSeatView(seat);
                    seatView.onLockChanged(lock);
                }
            }

            @Override
            public void onSeatChanged(List<LiveAudioRoomSeat> changedSeats) {
                for (LiveAudioRoomSeat changedSeat : changedSeats) {
                    ZEGOLiveAudioRoomSeatView seatView = getSeatView(changedSeat);
                    seatView.onUserUpdate(changedSeat.getUser());
                    if (roomHostUser != null && roomHostUser.equals(changedSeat.getUser())) {
                        seatView.showHostTag();
                    } else {
                        seatView.hideHostTag();
                    }
                }

                checkUsersAvatar();
            }
        });
    }

    private void checkUsersAvatar() {
        List<String> speakerUserIDs = new ArrayList<>();
        List<LiveAudioRoomSeat> seatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
        for (LiveAudioRoomSeat seat : seatList) {
            if (seat.isNotEmpty()) {
                String userAvatar = ZEGOLiveAudioRoomManager.getInstance().getUserAvatar(seat.getUser().userID);
                if (TextUtils.isEmpty(userAvatar)) {
                    speakerUserIDs.add(seat.getUser().userID);
                }
            }
        }
        if (!speakerUserIDs.isEmpty()) {
            ZEGOLiveAudioRoomManager.getInstance().queryUsersInfo(speakerUserIDs, null);
        }
    }

    public void onHostUserChanged(ZEGOSDKUser hostUser) {
        roomHostUser = hostUser;
        for (LiveAudioRoomSeat audioRoomSeat : ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList()) {
            ZEGOLiveAudioRoomSeatView seatView = getSeatView(audioRoomSeat);
            if (audioRoomSeat.isNotEmpty() && audioRoomSeat.getUser().equals(hostUser)) {
                seatView.showHostTag();
            } else {
                seatView.hideHostTag();
            }
        }
    }

    public void setLayoutConfig(LiveAudioRoomLayoutConfig layoutConfig) {
        int seatIndex = 0;
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            LiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            FlexboxLayout flexboxLayout = new FlexboxLayout(getContext());
            LayoutParams params = new LayoutParams(-1, -2);
            params.bottomMargin = layoutConfig.rowSpacing;
            addView(flexboxLayout, params);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                ZEGOLiveAudioRoomSeatView seatView = new ZEGOLiveAudioRoomSeatView(getContext());
                seatView.setOnClickListener(v -> {
                    if (System.currentTimeMillis() - lastClickTime < 500) {
                        return;
                    }
                    onSeatViewClicked(seatView);
                    lastClickTime = System.currentTimeMillis();
                });
                seatView.setTag(seatIndex);
                flexboxLayout.addView(seatView);
                seatIndex = seatIndex + 1;

                FlexboxLayout.LayoutParams layoutParams = new FlexboxLayout.LayoutParams(-2, -2);
                if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.SPACE_EVENLY
                    || rowConfig.alignment == LiveAudioRoomLayoutAlignment.SPACE_BETWEEN
                    || rowConfig.alignment == LiveAudioRoomLayoutAlignment.SPACE_AROUND) {
                    layoutParams.rightMargin = 0;
                } else {
                    layoutParams.rightMargin = rowConfig.seatSpacing;
                }
                seatView.setLayoutParams(layoutParams);
            }

            if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.SPACE_EVENLY) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_EVENLY);
            } else if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.SPACE_BETWEEN) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_BETWEEN);
            } else if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.SPACE_AROUND) {
                flexboxLayout.setJustifyContent(JustifyContent.SPACE_AROUND);
            } else if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.CENTER) {
                flexboxLayout.setJustifyContent(JustifyContent.CENTER);
            } else if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.FLEX_START) {
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_START);
            } else if (rowConfig.alignment == LiveAudioRoomLayoutAlignment.FLEX_END) {
                flexboxLayout.setJustifyContent(JustifyContent.FLEX_END);
            }
        }
    }

    private ZEGOLiveAudioRoomSeatView getSeatView(LiveAudioRoomSeat audioRoomSeat) {
        FlexboxLayout flexboxLayout = (FlexboxLayout) getChildAt(audioRoomSeat.rowIndex);
        return ((ZEGOLiveAudioRoomSeatView) flexboxLayout.getChildAt(audioRoomSeat.columnIndex));
    }


    private LiveAudioRoomSeat getSeat(ZEGOLiveAudioRoomSeatView seatView) {
        int seatIndex = (int) seatView.getTag();
        return ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList().get(seatIndex);
    }

    private void onSeatViewClicked(ZEGOLiveAudioRoomSeatView clickSeatView) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null) {
            return;
        }
        LiveAudioRoomSeat clickSeat = getSeat(clickSeatView);
        int hostSeatIndex = ZEGOLiveAudioRoomManager.getInstance().getHostPresetSeatIndex();
        if (clickSeat.isEmpty()) {
            if (localUser.equals(roomHostUser) || clickSeat.seatIndex == hostSeatIndex
                || ZEGOLiveAudioRoomManager.getInstance().isSeatLocked()) {
            } else {
                int mySeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                if (mySeatIndex == -1) {
                    showTakeSeatDialog(clickSeat);
                } else {
                    switchToTheSeat(clickSeat);
                }
            }
        } else {
            int mySeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
            if (!localUser.equals(roomHostUser) && clickSeat.seatIndex == mySeatIndex) {
                showLeaveSeatDialog(clickSeat);
            } else if (localUser.equals(roomHostUser) && clickSeat.seatIndex != mySeatIndex) {
                showKickUserDialog(clickSeat);
            }
        }
    }

    private void showKickUserDialog(LiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Kick user from seat", getContext().getString(R.string.cancel));
        int seatIndex = clickSeat.seatIndex;
        ZEGOSDKUser clickSeatUser = clickSeat.getUser();
        kickUserSeatDialog = new BottomActionDialog(getContext(), stringList);
        kickUserSeatDialog.show();
        kickUserSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                LiveAudioRoomSeat currentSeat = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList()
                    .get(seatIndex);
                if (Objects.equals(currentSeat.getUser(), clickSeatUser)) {
                    ZEGOLiveAudioRoomManager.getInstance()
                        .removeSpeakerFromSeat(clickSeat.getUser().userID, new ZIMRoomAttributesOperatedCallback() {
                            @Override
                            public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                                ZIMError errorInfo) {
                            }
                        });
                } else {
                    ToastUtil.show(getContext(), "Seat user has changed");
                }

            }
            dialog.dismiss();
        });
    }

    private void showLeaveSeatDialog(LiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Leave the Seat", getContext().getString(R.string.cancel));
        leaveSeatDialog = new BottomActionDialog(getContext(), stringList);
        leaveSeatDialog.show();
        leaveSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                int seatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                if (seatIndex == clickSeat.seatIndex) {
                    ZEGOLiveAudioRoomManager.getInstance()
                        .leaveSeat(clickSeat.seatIndex, new ZIMRoomAttributesOperatedCallback() {
                            @Override
                            public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                                ZIMError errorInfo) {

                            }
                        });
                } else {
                    ToastUtil.show(getContext(), "Your seat has already changed");
                }
            }
            dialog.dismiss();
        });
    }

    private void switchToTheSeat(LiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Switch to the Seat", getContext().getString(R.string.cancel));

        int myRoomSeatIndexWhenClick = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();

        switchSeatDialog = new BottomActionDialog(getContext(), stringList);
        switchSeatDialog.show();
        switchSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                boolean seatLocked = ZEGOLiveAudioRoomManager.getInstance().isSeatLocked();
                if (seatLocked) {
                    ToastUtil.show(getContext(), "This seat is locked by host");
                } else {
                    int myRoomSeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
                    if (myRoomSeatIndex != myRoomSeatIndexWhenClick) {
                        ToastUtil.show(getContext(), "Your seat has already changed");
                    } else {
                        ZEGOLiveAudioRoomManager.getInstance()
                            .switchSeat(myRoomSeatIndex, clickSeat.seatIndex,
                                new ZIMRoomAttributesBatchOperatedCallback() {
                                    @Override
                                    public void onRoomAttributesBatchOperated(String roomID, ZIMError errorInfo) {

                                    }
                                });
                    }
                }

            }
            dialog.dismiss();
        });
    }

    private void showTakeSeatDialog(LiveAudioRoomSeat clickSeat) {
        List<String> stringList = Arrays.asList("Take the Seat", getContext().getString(R.string.cancel));
        requestTakeSeatDialog = new BottomActionDialog(getContext(), stringList);
        requestTakeSeatDialog.show();
        requestTakeSeatDialog.setOnDialogClickListener((dialog, which) -> {
            if (which == 0) {
                List<String> permissions = Arrays.asList(permission.RECORD_AUDIO);
                requestPermissionIfNeeded(permissions, new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                        @NonNull List<String> deniedList) {
                        if (allGranted) {
                            boolean seatLocked = ZEGOLiveAudioRoomManager.getInstance().isSeatLocked();
                            if (seatLocked) {
                                ToastUtil.show(getContext(), "This seat is locked by host");
                            } else {
                                ZEGOLiveAudioRoomManager.getInstance()
                                    .takeSeat(clickSeat.seatIndex, new ZIMRoomAttributesOperatedCallback() {
                                        @Override
                                        public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                                            ZIMError errorInfo) {

                                        }
                                    });
                            }
                        }
                    }
                });
            }
            dialog.dismiss();
        });
    }

    public void onUserAvatarUpdated(String userID, String url) {
        List<LiveAudioRoomSeat> seatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
        for (LiveAudioRoomSeat seat : seatList) {
            if (seat.isNotEmpty() && seat.getUser().userID.equals(userID)) {
                ZEGOLiveAudioRoomSeatView seatView = getSeatView(seat);
                seatView.onUserAvatarUpdated(url);
            }
        }
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        if (getContext() instanceof Activity) {
            PermissionX.init((FragmentActivity) getContext()).permissions(permissions)
                .onExplainRequestReason((scope, deniedList) -> {
                    String message = "";
                    if (permissions.size() == 1) {
                        if (deniedList.contains(permission.CAMERA)) {
                            message = getContext().getString(R.string.permission_explain_camera);
                        } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                            message = getContext().getString(R.string.permission_explain_mic);
                        }
                    } else {
                        if (deniedList.size() == 1) {
                            if (deniedList.contains(permission.CAMERA)) {
                                message = getContext().getString(R.string.permission_explain_camera);
                            } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                                message = getContext().getString(R.string.permission_explain_mic);
                            }
                        } else {
                            message = getContext().getString(R.string.permission_explain_camera_mic);
                        }
                    }
                    scope.showRequestReasonDialog(deniedList, message, getContext().getString(R.string.ok));
                }).onForwardToSettings((scope, deniedList) -> {
                    String message = "";
                    if (permissions.size() == 1) {
                        if (deniedList.contains(permission.CAMERA)) {
                            message = getContext().getString(R.string.settings_camera);
                        } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                            message = getContext().getString(R.string.settings_mic);
                        }
                    } else {
                        if (deniedList.size() == 1) {
                            if (deniedList.contains(permission.CAMERA)) {
                                message = getContext().getString(R.string.settings_camera);
                            } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                                message = getContext().getString(R.string.settings_mic);
                            }
                        } else {
                            message = getContext().getString(R.string.settings_camera_mic);
                        }
                    }
                    scope.showForwardToSettingsDialog(deniedList, message, getContext().getString(R.string.settings),
                        getContext().getString(R.string.cancel));
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
}
