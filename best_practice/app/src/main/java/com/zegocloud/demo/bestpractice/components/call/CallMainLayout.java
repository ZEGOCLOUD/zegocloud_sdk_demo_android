package com.zegocloud.demo.bestpractice.components.call;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.flexbox.FlexboxLayout;
import com.zegocloud.demo.bestpractice.components.ZEGOAudioVideoView;
import com.zegocloud.demo.bestpractice.databinding.LayoutCallMainLayoutBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;

public class CallMainLayout extends ConstraintLayout {

    private LayoutCallMainLayoutBinding binding;
    private CallChangedListener callChangedListener;
    private IExpressEngineEventHandler expressEngineEventHandler;
    private CallCellView[] callCellViews = new CallCellView[9];

    public CallMainLayout(@NonNull Context context) {
        super(context);
        initView();
    }

    public CallMainLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CallMainLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        binding = LayoutCallMainLayoutBinding.inflate(LayoutInflater.from(getContext()), this, true);
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();

        binding.selfVideoView.setOnClickListener(v -> {
            ViewGroup selfVideoViewParent = (ViewGroup) binding.selfVideoView.getParent();
            ViewGroup otherVideoViewParent = (ViewGroup) binding.otherVideoView.getParent();
            if (otherVideoViewParent.getVisibility() != View.VISIBLE || callInviteInfo.isVoiceCall()) {
                return;
            }
            selfVideoViewParent.removeView(binding.selfVideoView);
            otherVideoViewParent.removeView(binding.otherVideoView);
            selfVideoViewParent.addView(binding.otherVideoView);
            otherVideoViewParent.addView(binding.selfVideoView);
        });
        binding.otherVideoView.setOnClickListener(v -> {
            ViewGroup selfVideoViewParent = (ViewGroup) binding.selfVideoView.getParent();
            ViewGroup otherVideoViewParent = (ViewGroup) binding.otherVideoView.getParent();
            if (selfVideoViewParent.getVisibility() != View.VISIBLE || callInviteInfo.isVoiceCall()) {
                return;
            }
            selfVideoViewParent.removeView(binding.selfVideoView);
            otherVideoViewParent.removeView(binding.otherVideoView);
            selfVideoViewParent.addView(binding.otherVideoView);
            otherVideoViewParent.addView(binding.selfVideoView);
        });

        for (int i = 0; i < callCellViews.length; i++) {
            callCellViews[i] = new CallCellView(getContext());
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(-2, -2);
            callCellViews[i].setLayoutParams(params);
        }

        initSDKEvents();
    }

    private void initSDKEvents() {
        callChangedListener = new CallChangedListener() {

            @Override
            public void onBusyRejectCall(String requestID) {

            }

            @Override
            public void onInvitedUserRejected(String requestID, CallInviteUser rejectUser) {

                updateLayoutVisibility();

                updateCellViewParams();
            }

            @Override
            public void onInvitedUserTimeout(String requestID, CallInviteUser timeoutUser) {
                updateLayoutVisibility();

                updateCellViewParams();
            }

            @Override
            public void onInvitedUserQuit(String requestID, CallInviteUser quitUser) {
                updateLayoutVisibility();

                updateCellViewParams();
            }

            @Override
            public void onInvitedUserAccepted(String requestID, CallInviteUser acceptUser) {
                updateLayoutVisibility();

                updateCellViewParams();
            }

            @Override
            public void onCallEnded(String requestID) {
                if (!isDisplayPip()) {
                    removeAllCellViews();
                }
                binding.layoutFlexbox.setVisibility(GONE);
                binding.layoutPip.setVisibility(VISIBLE);
            }

            @Override
            public void onCallCancelled(String requestID) {
                if (!isDisplayPip()) {
                    removeAllCellViews();
                }
                binding.layoutFlexbox.setVisibility(GONE);
                binding.layoutPip.setVisibility(VISIBLE);
            }

            @Override
            public void onCallTimeout(String requestID) {
                updateLayoutVisibility();
                updateCellViewParams();
            }

            @Override
            public void onInviteNewUser(String requestID, CallInviteUser inviteUser) {
                updateLayoutVisibility();
                updateCellViewParams();
            }
        };
        ZEGOCallInvitationManager.getInstance().addCallListener(callChangedListener);

        expressEngineEventHandler = new IExpressEngineEventHandler() {

            @Override
            public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
                Timber.d("onReceiveStreamAdd() called with: userList = [" + userList + "]");
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                    if (audioVideoView == null) {

                        updateLayoutVisibility();

                        updateCellViewParams();

                        if (!isDisplayPip()) {
                            audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                        }
                    }
                    if (audioVideoView != null) {
                        ViewParent parent = audioVideoView.getParent();
                        if (parent instanceof CallCellView) {
                            ((CallCellView) parent).dismissLoading();
                        }
                        audioVideoView.setStreamID(zegosdkUser.getMainStreamID());
                        audioVideoView.startPlayRemoteAudioVideo();
                    }

                }
            }

            @Override
            public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                    if (audioVideoView == null) {
                        if (!isDisplayPip()) {
                            updateLayoutVisibility();

                            updateCellViewParams();

                            audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                        }
                    }
                    if (audioVideoView != null) {
                        audioVideoView.stopPlayRemoteAudioVideo();
                        audioVideoView.setStreamID("");
                    }
                }
            }

            @Override
            public void onCameraOpen(String userID, boolean open) {
                super.onCameraOpen(userID, open);
                Timber.d("onCameraOpen() called with: userID = [" + userID + "], open = [" + open + "]");
                ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(userID);
                if (audioVideoView == null) {
                    if (!isDisplayPip()) {
                        updateLayoutVisibility();

                        updateCellViewParams();

                        audioVideoView = getAudioVideoViewByUserID(userID);
                    }
                }
                if (audioVideoView != null) {
                    ZEGOSDKUser user = ZEGOSDKManager.getInstance().expressService.getUser(userID);
                    if (user.isCameraOpen()) {
                        if (ZEGOSDKManager.getInstance().expressService.isCurrentUser(userID)) {
                            audioVideoView.startPreviewOnly();
                        } else {
                            audioVideoView.startPlayRemoteAudioVideo();
                        }
                        audioVideoView.showVideoView();
                    } else {
                        if (ZEGOSDKManager.getInstance().expressService.isCurrentUser(userID)) {
                            audioVideoView.stopPreview();
                        } else {
                            //                            audioVideoView.stopPlayRemoteAudioVideo();
                        }
                        audioVideoView.showAudioView();
                    }
                }
            }

            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                super.onMicrophoneOpen(userID, open);
                ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(userID);
                if (audioVideoView == null) {
                    if (!isDisplayPip()) {
                        updateLayoutVisibility();

                        updateCellViewParams();

                        audioVideoView = getAudioVideoViewByUserID(userID);
                    }
                }
                if (audioVideoView != null) {
                    ZEGOSDKUser user = ZEGOSDKManager.getInstance().expressService.getUser(userID);
                    if (user.isCameraOpen()) {
                        audioVideoView.showVideoView();
                    } else {
                        audioVideoView.showAudioView();
                    }
                }
            }

            @Override
            public void onUserLeft(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZEGOAudioVideoView audioVideoView = getAudioVideoViewByUserID(zegosdkUser.userID);
                    if (audioVideoView != null) {
                        audioVideoView.stopPlayRemoteAudioVideo();
                        audioVideoView.setUserID("");
                        audioVideoView.setStreamID("");
                        ViewGroup parent = (ViewGroup) audioVideoView.getParent();
                        if (parent instanceof CallCellView) {
                            ViewGroup grandParent = (ViewGroup) parent.getParent();
                            if (grandParent != null) {
                                grandParent.removeView(parent);
                            }
                        }
                    }
                }
            }
        };
        ZEGOSDKManager.getInstance().expressService.addEventHandler(expressEngineEventHandler);
    }

    private void updateCellViewParams() {
        removeAllCellViews();

        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        Timber.d("updateCellViewParams() called:" + callInviteInfo.userList);

        List<CallInviteUser> notFinishedUsers = new ArrayList<>();
        for (CallInviteUser callInviteUser : callInviteInfo.userList) {
            if (callInviteUser.isWaiting() || callInviteUser.isAccepted()) {
                notFinishedUsers.add(callInviteUser);
            }
        }

        if (!isDisplayPip()) {
            int width = binding.layoutFlexbox.getWidth();
            int height = binding.layoutFlexbox.getHeight();

            for (int i = 0; i < notFinishedUsers.size(); i++) {
                CallInviteUser callInviteUser = notFinishedUsers.get(i);
                callCellViews[i].dismissLoading();
                callCellViews[i].setCallUser(callInviteUser);
                if (!ZEGOSDKManager.getInstance().expressService.isCurrentUser(callInviteUser.getUserID())) {
                    if (callInviteUser.isWaiting()) {
                        callCellViews[i].loading();
                    } else {
                        callCellViews[i].dismissLoading();
                    }
                }
                FlexboxLayout.LayoutParams layoutParams = (FlexboxLayout.LayoutParams) callCellViews[i].getLayoutParams();
                if (notFinishedUsers.size() == 3) {
                    if (i == 0) {
                        layoutParams.width = width / 2;
                        layoutParams.height = height;
                        callCellViews[i].setLayoutParams(layoutParams);
                        binding.layoutFlexbox.addView(callCellViews[i], 0);
                    } else {
                        layoutParams.width = width / 2;
                        layoutParams.height = height / 2;
                        callCellViews[i].setLayoutParams(layoutParams);
                        binding.layoutChildFlexbox.addView(callCellViews[i]);
                    }
                } else if (notFinishedUsers.size() == 4) {
                    layoutParams.width = width / 2;
                    layoutParams.height = height / 2;
                    callCellViews[i].setLayoutParams(layoutParams);
                    binding.layoutFlexbox.addView(callCellViews[i]);
                } else if (notFinishedUsers.size() == 5) {
                    if (i <= 1) {
                        layoutParams.width = width / 2;
                        layoutParams.height = height / 2;
                    } else {
                        layoutParams.width = width / 3;
                        layoutParams.height = height / 2;
                    }
                    callCellViews[i].setLayoutParams(layoutParams);
                    binding.layoutFlexbox.addView(callCellViews[i]);
                } else if (notFinishedUsers.size() == 6) {
                    layoutParams.width = width / 3;
                    layoutParams.height = height / 2;
                    callCellViews[i].setLayoutParams(layoutParams);
                    binding.layoutFlexbox.addView(callCellViews[i]);
                } else if (notFinishedUsers.size() > 6) {
                    layoutParams.width = width / 3;
                    layoutParams.height = height / 3;
                    callCellViews[i].setLayoutParams(layoutParams);
                    binding.layoutFlexbox.addView(callCellViews[i]);
                }
            }
        } else {
            // if is pip,then only two users,other is waiting.
            for (CallInviteUser callInviteUser : notFinishedUsers) {
                if (ZEGOSDKManager.getInstance().expressService.isCurrentUser(callInviteUser.getUserID())) {
                    if (currentUser.isCameraOpen()) {
                        binding.selfVideoView.startPreviewOnly();
                        binding.selfVideoView.showVideoView();
                    } else {
                        binding.selfVideoView.showAudioView();
                    }
                } else {
                    binding.otherVideoView.setUserID(callInviteUser.getUserID());
                    ZEGOSDKUser zegosdkUser = ZEGOSDKManager.getInstance().expressService.getUser(
                        callInviteUser.getUserID());
                    if (zegosdkUser != null) {
                        if (!TextUtils.isEmpty(zegosdkUser.getMainStreamID())) {
                            binding.otherVideoView.setStreamID(zegosdkUser.getMainStreamID());
                            if (zegosdkUser.isCameraOpen()) {
                                binding.otherVideoView.startPlayRemoteAudioVideo();
                                binding.otherVideoView.showVideoView();
                            } else {
                                binding.otherVideoView.showAudioView();
                                //                                binding.otherVideoView.stopPlayRemoteAudioVideo();
                            }
                        }
                    } else {
                        binding.otherVideoView.showAudioView();
                    }
                }
            }
        }
    }

    private void removeAllCellViews() {
        for (CallCellView callCellView : callCellViews) {
            ViewGroup parent = (ViewGroup) callCellView.getParent();
            if (parent != null) {
                parent.removeView(callCellView);
            }
        }
    }

    /**
     * less than two person,use pip more,use flexLayout.
     */
    private void updateLayoutVisibility() {
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        if (callInviteInfo == null) {
            binding.layoutFlexbox.setVisibility(GONE);
            binding.layoutPip.setVisibility(VISIBLE);
        } else {
            int notFinishedCount = 0;
            for (CallInviteUser callInviteUser : callInviteInfo.userList) {
                if (callInviteUser.isWaiting() || callInviteUser.isAccepted()) {
                    notFinishedCount++;
                }
            }
            if (notFinishedCount <= 2) {
                binding.layoutFlexbox.setVisibility(GONE);
                binding.layoutPip.setVisibility(VISIBLE);
            } else {
                binding.layoutPip.setVisibility(GONE);
                binding.layoutFlexbox.setVisibility(VISIBLE);
                if (notFinishedCount == 3) {
                    binding.layoutChildFlexbox.setVisibility(VISIBLE);
                } else {
                    binding.layoutChildFlexbox.setVisibility(GONE);
                }
            }
        }

    }

    private boolean isDisplayPip() {
        return binding.layoutPip.getVisibility() == VISIBLE;
    }

    private ZEGOAudioVideoView getAudioVideoViewByUserID(String userID) {
        if (isDisplayPip()) {
            if (Objects.equals(binding.selfVideoView.getUserID(), userID)) {
                return binding.selfVideoView;
            }
            if (Objects.equals(binding.otherVideoView.getUserID(), userID)) {
                return binding.otherVideoView;
            }
            return null;
        } else {
            for (CallCellView callCellView : callCellViews) {
                if (Objects.equals(callCellView.getUserID(), userID)) {
                    return callCellView.getAudioVideoView();
                }
            }
            return null;
        }
    }

    public void onPermissionAnswered(boolean allGranted, List<String> grantedList, List<String> deniedList) {
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();

        CallInviteUser inviteUser = callInviteInfo.userList.get(0);
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (Objects.equals(inviteUser.getUserID(), currentUser.userID)) {
            inviteUser = callInviteInfo.userList.get(1);
        }

        binding.selfVideoView.setUserID(currentUser.userID);
        binding.otherVideoView.setUserID(inviteUser.getUserID());

        updateLayoutVisibility();
        updateCellViewParams();
        if (isDisplayPip()) {
            List<ZEGOSDKUser> roomUsers = ZEGOSDKManager.getInstance().expressService.getRoomUsers();
            for (ZEGOSDKUser roomUser : roomUsers) {
                if (!Objects.equals(roomUser.userID, currentUser.userID)) {
                    binding.otherVideoView.setStreamID(roomUser.getMainStreamID());
                    binding.otherVideoView.startPlayRemoteAudioVideo();
                    binding.otherVideoView.showVideoView();
                } else {
                    String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                    String streamID = ZEGOCallInvitationManager.getInstance()
                        .generateUserStreamID(currentUser.userID, currentRoomID);
                    binding.selfVideoView.setStreamID(streamID);
                    binding.selfVideoView.startPublishAudioVideo();
                    if (currentUser.isCameraOpen()) {
                        binding.selfVideoView.startPreviewOnly();
                        binding.selfVideoView.showVideoView();
                    } else {
                        binding.selfVideoView.showAudioView();
                    }
                }
            }
        } else {
            for (CallCellView callCellView : callCellViews) {
                if (callCellView.getParent() != null) {
                    if (Objects.equals(callCellView.getUserID(), currentUser.userID)) {
                        if (currentUser.isCameraOpen()) {
                            callCellView.getAudioVideoView().startPreviewOnly();
                        }
                        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                        String streamID = ZEGOCallInvitationManager.getInstance()
                            .generateUserStreamID(currentUser.userID, currentRoomID);
                        callCellView.getAudioVideoView().setStreamID(streamID);
                        callCellView.getAudioVideoView().startPublishAudioVideo();
                    } else {
                        callCellView.getAudioVideoView().startPlayRemoteAudioVideo();
                    }
                }
            }
        }
    }
}
