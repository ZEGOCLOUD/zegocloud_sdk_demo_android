package com.zegocloud.demo.bestpractice.components.call;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.flexbox.FlexboxLayout;
import com.zegocloud.demo.bestpractice.activity.call.VideoCellPageAdapter;
import com.zegocloud.demo.bestpractice.components.ZEGOAudioVideoView;
import com.zegocloud.demo.bestpractice.databinding.LayoutCallMainLayoutBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.enums.ZIMCallUserState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;

public class CallMainLayout extends ConstraintLayout {

    private LayoutCallMainLayoutBinding binding;
    private CallChangedListener callChangedListener;
    private IExpressEngineEventHandler expressEngineEventHandler;
    private CallCellView[] callCellViews = new CallCellView[9];
    private VideoCellPageAdapter videoCellPageAdapter;

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

        // click to switch video view
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

        videoCellPageAdapter = new VideoCellPageAdapter();
        binding.viewPager.setAdapter(videoCellPageAdapter);

//        binding.addButton.setOnClickListener(v -> {
//            CallInviteUser user = new CallInviteUser("234234", ZIMCallUserState.ACCEPTED, "");
//            callInviteInfo.userList.add(user);
//            refreshLayout();
//        });
//        binding.removeButton.setOnClickListener(v -> {
//            callInviteInfo.userList.remove(callInviteInfo.userList.get(callInviteInfo.userList.size() - 1));
//            refreshLayout();
//        });

        initSDKEvents();
    }

    private void initSDKEvents() {
        callChangedListener = new CallChangedListener() {

            @Override
            public void onBusyRejectCall(String requestID) {

            }

            @Override
            public void onInvitedUserRejected(String requestID, CallInviteUser rejectUser) {
                refreshLayout();
            }

            @Override
            public void onInvitedUserTimeout(String requestID, CallInviteUser timeoutUser) {

                refreshLayout();
            }

            @Override
            public void onInvitedUserQuit(String requestID, CallInviteUser quitUser) {

                refreshLayout();
            }

            @Override
            public void onInvitedUserAccepted(String requestID, CallInviteUser acceptUser) {

                updateCellViews(acceptUser);
            }

            @Override
            public void onCallEnded(String requestID) {
                refreshLayout();
            }

            @Override
            public void onCallCancelled(String requestID) {
                refreshLayout();
            }

            @Override
            public void onCallTimeout(String requestID) {

                refreshLayout();
            }

            @Override
            public void onInviteNewUser(String requestID, CallInviteUser inviteUser) {

                refreshLayout();
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
                        refreshLayout();
                    } else {
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
                        refreshLayout();
                    } else {
                        ViewParent parent = audioVideoView.getParent();
                        if (parent instanceof CallCellView) {
                            ((CallCellView) parent).dismissLoading();
                        }
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
                    refreshLayout();
                } else {
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
                            audioVideoView.stopPlayRemoteAudioVideo();
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
                    refreshLayout();
                } else {
                    ZEGOSDKUser user = ZEGOSDKManager.getInstance().expressService.getUser(userID);
                    if (user.isCameraOpen()) {
                        audioVideoView.showVideoView();
                    } else {
                        audioVideoView.showAudioView();
                    }
                }
            }

            @Override
            public void onUserEnter(List<ZEGOSDKUser> userList) {
                super.onUserEnter(userList);

                refreshLayout();
            }

            @Override
            public void onUserLeft(List<ZEGOSDKUser> userList) {

                refreshLayout();
            }
        };
        ZEGOSDKManager.getInstance().expressService.addEventHandler(expressEngineEventHandler);
    }

    private void updateCellViews(CallInviteUser acceptUser) {
        int currentItem = binding.viewPager.getCurrentItem();
        if (currentItem != 0) {
            RecyclerView recyclerView = (RecyclerView) binding.viewPager.getChildAt(0);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(currentItem);
            if (viewHolder != null) {
                FlexboxLayout flexboxLayout = (FlexboxLayout) viewHolder.itemView;
                for (int i = 0; i < flexboxLayout.getChildCount(); i++) {
                    CallCellView cellView = (CallCellView) flexboxLayout.getChildAt(i);
                    if (Objects.equals(cellView.getUserID(), acceptUser.getUserID())) {
                        if (acceptUser.isWaiting()) {
                            cellView.loading();
                        } else {
                            cellView.dismissLoading();
                        }
                    }
                }
            }
        }
    }

    private void refreshLayout() {
        List<CallInviteUser> allCallUsers = checkAllCallUsers();
        List<String> userIDList = new ArrayList<>();
        for (CallInviteUser allCallUser : allCallUsers) {
            userIDList.add(allCallUser.getUserID());
        }
        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList, new ZIMUsersInfoQueriedCallback() {
            @Override
            public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                for (CallCellView callCellView : callCellViews) {
                    callCellView.updateUserIcon();
                }
            }
        });

        // less than two person,use pip more,use flexLayout.
        if (allCallUsers.size() <= 2) {
            binding.viewPager.setVisibility(GONE);
            binding.layoutPip.setVisibility(VISIBLE);
        } else {
            binding.layoutPip.setVisibility(GONE);
            binding.viewPager.setVisibility(VISIBLE);
        }

        if (isDisplayPip()) {
            refreshPipLayout(allCallUsers);
        } else {
            removeAllCellViews();
            refreshFlexLayoutCells(allCallUsers);
        }
    }

    private void refreshPipLayout(List<CallInviteUser> allCallUsers) {
        // if is pip,then only two users.
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        for (CallInviteUser callInviteUser : allCallUsers) {
            if (Objects.equals(callInviteUser.getUserID(), currentUser.userID)) {
                if (currentUser.isCameraOpen()) {
                    binding.selfVideoView.startPreviewOnly();
                    binding.selfVideoView.showVideoView();
                } else {
                    binding.selfVideoView.stopPreview();
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
                            binding.otherVideoView.showVideoView();
                        } else {
                            binding.otherVideoView.showAudioView();
                        }
                        binding.otherVideoView.startPlayRemoteAudioVideo();
                    }
                } else {
                    binding.otherVideoView.showAudioView();
                }
            }
        }
    }

    private void refreshFlexLayoutCells(List<CallInviteUser> allCallUsers) {
        int width = binding.viewPager.getWidth();
        int height = binding.viewPager.getHeight();

        if (width == 0 || height == 0) {
            return;
        }
        videoCellPageAdapter.setPageSize(width, height);
        videoCellPageAdapter.setAllCallUsers(allCallUsers);
    }

    @NonNull
    private List<CallInviteUser> checkAllCallUsers() {
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        List<ZEGOSDKUser> roomUsers = ZEGOSDKManager.getInstance().expressService.getRoomUsers();

        // include invite join users and directly join(without call-invite) users
        List<CallInviteUser> allCallUsers = new ArrayList<>();
        if (callInviteInfo != null) {
            for (CallInviteUser callInviteUser : callInviteInfo.userList) {
                if (callInviteUser.isWaiting() || callInviteUser.isAccepted()) {
                    allCallUsers.add(callInviteUser);
                }
            }
        }

        for (ZEGOSDKUser zegosdkUser : roomUsers) {
            boolean find = false;
            for (CallInviteUser callInviteUser : allCallUsers) {
                if (Objects.equals(callInviteUser.getUserID(), zegosdkUser.userID)) {
                    find = true;
                    break;
                }
            }
            if (!find) {
                CallInviteUser callInviteUser = new CallInviteUser(zegosdkUser.userID, null, "");
                allCallUsers.add(callInviteUser);
            }
        }
        return allCallUsers;
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
     * pip: 1v1 flexbox: multi
     *
     * @return
     */
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
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();

        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        String streamID = ZEGOCallInvitationManager.getInstance()
            .generateUserStreamID(currentUser.userID, currentRoomID);
        binding.selfVideoView.setStreamID(streamID);
        binding.selfVideoView.setUserID(currentUser.userID);
        binding.selfVideoView.startPublishAudioVideo();

        refreshLayout();
    }
}
