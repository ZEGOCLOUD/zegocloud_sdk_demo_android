package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.databinding.ActivityLiveStreamingBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.ExpressService;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.ZegoUtil;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.constants.ZegoMediaPlayerState;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;

public class LiveStreamingView extends FrameLayout {

    private ActivityLiveStreamingBinding binding;
    private SurfaceView mediaPlayerView;
    private CoHostView coHostView;

    public LiveStreamingView(@NonNull Context context) {
        super(context);
        initView();
    }

    public LiveStreamingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public LiveStreamingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        binding = ActivityLiveStreamingBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }

    public void prepareForStartLive(View.OnClickListener onClickListener) {

        //prepare for host view
        binding.previewStart.setVisibility(View.VISIBLE);
        binding.liveAudioroomTopbar.setVisibility(GONE);
        binding.mainHostVideo.startPreviewOnly();
        binding.previewStart.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        });

        //prepare for view
        prepareForJoinLiveInner();
    }

    public void prepareForJoinLive() {
        //prepare for view
        prepareForJoinLiveInner();
    }

    private void prepareForJoinLiveInner() {
        // create new subviews to add sdk event for view itself when constructor
        binding.liveBottomMenuBarParent.removeAllViews();
        binding.liveBottomMenuBarParent.addView(new BottomMenuBar(getContext()));

        binding.liveCohostViewParent.removeAllViews();
        coHostView = new CoHostView(getContext());
        binding.liveCohostViewParent.addView(coHostView);

        binding.pkVideoLayoutParent.removeAllViews();
        binding.pkVideoLayoutParent.addView(new PKBattleLayout(getContext()));

        listenSDKEventForViews();
    }

    public void onJoinRoomSuccess(String roomID) {
        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            ZEGOLiveStreamingManager.getInstance().startPublishingStream();
        }

        binding.previewStart.setVisibility(View.GONE);
        binding.liveBottomMenuBarParent.setVisibility(View.VISIBLE);
        binding.liveAudioroomTopbar.setVisibility(VISIBLE);
        binding.liveAudioroomTopbar.setRoomID(roomID);

        ZegoVideoConfig videoConfig = new ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_360P);
        ZEGOSDKManager.getInstance().expressService.setVideoConfig(videoConfig);

        ZEGOSDKManager.getInstance().expressService.startSoundLevelMonitor();

        int width = binding.getRoot().getWidth() / 4;
        binding.mainHostVideoIcon.setCircleBackgroundRadius(width);

        initGiftView();
    }

    private void initGiftView() {
        ZegoMediaPlayer mediaPlayer = ZEGOSDKManager.getInstance().expressService.getMediaPlayer();
        mediaPlayerView = new SurfaceView(getContext());
        mediaPlayerView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mediaPlayerView.setZOrderOnTop(true);

        ZegoCanvas canvas = new ZegoCanvas(mediaPlayerView);
        canvas.alphaBlend = true;
        mediaPlayer.setPlayerCanvas(canvas);

        //        String giftUrl = "https://storage.zego.im/sdk-doc/Pics/zegocloud/gift/music_box.mp4";
        ZegoUtil.copyFileFromAssets(getContext(), "music_box.mp4",
            getContext().getExternalFilesDir(null) + "/music_box.mp4");
        String giftUrl = getContext().getExternalFilesDir(null) + "/music_box.mp4";

        ZEGOSDKManager.getInstance().expressService.loadResourceFile(giftUrl,
            new IZegoMediaPlayerLoadResourceCallback() {
                @Override
                public void onLoadResourceCallback(int errorCode) {
                    // load success first ,and then can display gift animation
                }
            });

        ZEGOSDKManager.getInstance().expressService.setMediaPlayerEventHandler(new IZegoMediaPlayerEventHandler() {

            @Override
            public void onMediaPlayerStateUpdate(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerState state,
                int errorCode) {
                super.onMediaPlayerStateUpdate(mediaPlayer, state, errorCode);
                if (state == ZegoMediaPlayerState.PLAY_ENDED) {
                    binding.giftParent.removeView(mediaPlayerView);
                }
            }
        });

        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onRoomCommandReceived(String senderID, String command) {
                super.onRoomCommandReceived(senderID, command);
                if (command.contains("gift_type")) {
                    if (mediaPlayerView.getParent() == null) {
                        binding.giftParent.addView(mediaPlayerView);
                        mediaPlayer.start();
                    }
                }
            }

            @Override
            public void onSendRoomCommand(int errorCode, String errorMessage, String command) {
                super.onSendRoomCommand(errorCode, errorMessage, command);
                if (errorCode == 0 && command.contains("gift_type")) {
                    if (mediaPlayerView.getParent() == null) {
                        binding.giftParent.addView(mediaPlayerView);
                        mediaPlayer.start();
                    }
                }
            }
        });
    }

    private void listenSDKEventForViews() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCameraOpen(String userID, boolean open) {
                onRoomUserCameraOpen(userID, open);
            }

            @Override
            public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
                List<ZEGOSDKUser> coHostUserList = new ArrayList<>();
                for (ZEGOSDKUser zegosdkUser : userList) {
                    if (ZEGOLiveStreamingManager.getInstance().isHost(zegosdkUser.userID)) {
                        onRoomUserCameraOpen(zegosdkUser.userID, zegosdkUser.isCameraOpen());
                        binding.mainHostVideo.setUserID(zegosdkUser.userID);
                        binding.mainHostVideoIcon.setLetter(zegosdkUser.userName);
                        ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(
                            zegosdkUser.userID);
                        if (zimUserFullInfo != null) {
                            binding.mainHostVideoIcon.setIconUrl(zimUserFullInfo.userAvatarUrl);
                        }
                        binding.mainHostVideo.setStreamID(zegosdkUser.getMainStreamID());
                        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                            binding.mainHostVideo.setVisibility(View.VISIBLE);
                            binding.mainHostVideoLayout.setVisibility(View.VISIBLE);
                            binding.mainHostVideo.startPlayRemoteAudioVideo();
                        }
                    } else {
                        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                            coHostUserList.add(zegosdkUser);
                        }
                    }
                }
                if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                    coHostView.addUser(coHostUserList);
                }
            }

            @Override
            public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
                List<ZEGOSDKUser> coHostUserList = new ArrayList<>();
                for (ZEGOSDKUser ZEGOSDKUser : userList) {
                    if (Objects.equals(binding.mainHostVideo.getUserID(), ZEGOSDKUser.userID)) {
                        binding.mainHostVideo.stopPlayRemoteAudioVideo();
                        binding.mainHostVideo.setStreamID("");
                        binding.mainHostVideo.setUserID("");
                        binding.mainHostVideoIcon.setLetter("");
                        binding.mainHostVideoIcon.setIconUrl("");
                        binding.mainHostVideoLayout.setVisibility(View.GONE);

                    } else {
                        coHostUserList.add(ZEGOSDKUser);
                    }
                }
                coHostView.removeUser(coHostUserList);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (state == ZegoPublisherState.PUBLISHING) {
                    if (ZEGOLiveStreamingManager.getInstance().isCoHost(currentUser.userID)) {
                        coHostView.addUser(currentUser);
                    } else if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                        binding.mainHostVideo.setUserID(currentUser.userID);
                        binding.mainHostVideoIcon.setLetter(currentUser.userName);
                        ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(
                            currentUser.userID);
                        binding.mainHostVideoIcon.setIconUrl(zimUserFullInfo.userAvatarUrl);
                        binding.mainHostVideo.setStreamID(streamID);
                        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                            binding.mainHostVideoLayout.setVisibility(View.VISIBLE);
                        }
                    }
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    if (streamID.endsWith("_host")) {
                        binding.mainHostVideo.setUserID("");
                        binding.mainHostVideoIcon.setLetter("");
                        binding.mainHostVideoIcon.setIconUrl("");
                        binding.mainHostVideo.setStreamID("");
                        binding.mainHostVideo.stopPublishAudioVideo();
                        binding.mainHostVideoLayout.setVisibility(View.GONE);
                    } else {
                        coHostView.removeUser(currentUser);
                    }
                }
            }
        });
        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {

            @Override
            public void onOutgoingRoomRequestAccepted(String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == RoomRequestType.REQUEST_COHOST) {
                    ExpressService expressService = ZEGOSDKManager.getInstance().expressService;
                    ZEGOSDKUser currentUser = expressService.getCurrentUser();
                    if (ZEGOLiveStreamingManager.getInstance().isAudience(currentUser.userID)) {
                        ZEGOLiveStreamingManager.getInstance().startCoHost();
                    }
                }
            }
        });

        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {
            @Override
            public void onPKEnded() {
                onRoomPKEnded();
            }

            @Override
            public void onPKStarted() {
                onRoomPKStarted();
            }

            @Override
            public void onPKUserConnecting(String userID, long duration) {
                if (duration >= 60_000) {
                    ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                    if (!Objects.equals(currentUser.userID, userID)) {
                        ZEGOLiveStreamingManager.getInstance().removeUserFromPKBattle(userID);
                    } else {
                        ZEGOLiveStreamingManager.getInstance().quitPKBattle();
                    }
                }
            }
        });
    }

    private void onRoomUserCameraOpen(String userID, boolean open) {
        if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
            if (ZEGOLiveStreamingManager.getInstance().isHost(userID)) {
                if (open) {
                    binding.mainHostVideo.setVisibility(View.VISIBLE);
                    binding.mainHostVideoIcon.setVisibility(View.GONE);
                } else {
                    binding.mainHostVideo.setVisibility(View.INVISIBLE);
                    binding.mainHostVideoIcon.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void onRoomPKStarted() {
        binding.liveCohostViewParent.setVisibility(View.GONE);
        binding.pkVideoLayoutParent.setVisibility(View.VISIBLE);
        binding.mainHostVideoLayout.setVisibility(View.GONE);
    }

    private void onRoomPKEnded() {
        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        binding.pkVideoLayoutParent.setVisibility(View.INVISIBLE);
        if (hostUser != null) {
            binding.mainHostVideoLayout.setVisibility(View.VISIBLE);
        }
        binding.liveCohostViewParent.setVisibility(View.VISIBLE);

        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            binding.mainHostVideo.startPreviewOnly();
        } else {
            if (hostUser != null) {
                String hostMainStreamID = hostUser.getMainStreamID();
                if (hostMainStreamID != null) {
                    binding.mainHostVideo.startPlayRemoteAudioVideo();
                }
            }
        }

        if (hostUser != null) {
            onRoomUserCameraOpen(hostUser.userID, hostUser.isCameraOpen());
        }
    }
}
