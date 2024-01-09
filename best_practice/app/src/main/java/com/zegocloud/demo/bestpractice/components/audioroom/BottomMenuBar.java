package com.zegocloud.demo.bestpractice.components.audioroom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.components.RoomRequestListDialog;
import com.zegocloud.demo.bestpractice.custom.GameListButton;
import com.zegocloud.demo.bestpractice.custom.GameListDialog;
import com.zegocloud.demo.bestpractice.custom.GameQuitButton;
import com.zegocloud.demo.bestpractice.custom.GameStartButton;
import com.zegocloud.demo.bestpractice.custom.InviteGameDialog;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager.LiveAudioRoomListener;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestType;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomSeat;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.MiniGameService;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.AudioOutputButton;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ToggleMicrophoneButton;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import im.zego.minigameengine.IZegoGameEngineHandler;
import im.zego.minigameengine.ZegoGameInfoDetail;
import im.zego.minigameengine.ZegoGameLoadState;
import im.zego.minigameengine.ZegoGamePlayerState;
import im.zego.minigameengine.ZegoGameRobotConfig;
import im.zego.minigameengine.ZegoGameState;
import java.util.List;

public class BottomMenuBar extends LinearLayout {


    private LinearLayout childLinearLayout;
    private ToggleMicrophoneButton microphoneButton;
    private LockSeatButton lockSeatButton;
    private TakeSeatButton takeSeatButton;
    private RoomRequestButton roomRequestListButton;
    private AudioOutputButton audioOutputButton;
    private GameListButton gameListButton;
    private GameStartButton gameStartButton;
    private GameQuitButton gameQuitButton;
    private ZegoGameLoadState gameLoadState;
    private ZegoGameState gameState;

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
        setGravity(Gravity.END);

        childLinearLayout = new LinearLayout(getContext());
        childLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
        childLinearLayout.setGravity(Gravity.END);
        LayoutParams params = new LayoutParams(0, -2, 1);
        addView(childLinearLayout, params);
        int paddingEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        childLinearLayout.setPadding(0, 0, paddingEnd, 0);

        microphoneButton = new ToggleMicrophoneButton(getContext());
        boolean microphoneOpen = ZEGOSDKManager.getInstance().expressService.isMicrophoneOpen();
        microphoneButton.updateState(microphoneOpen);
        childLinearLayout.addView(microphoneButton, generateChildImageLayoutParams());
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (userID.equals(localUser.userID)) {
                    microphoneButton.updateState(open);
                }
            }
        });

        lockSeatButton = new LockSeatButton(getContext());
        childLinearLayout.addView(lockSeatButton, generateChildImageLayoutParams());

        takeSeatButton = new TakeSeatButton(getContext());
        childLinearLayout.addView(takeSeatButton, generateChildTextLayoutParams());

        roomRequestListButton = new RoomRequestButton(getContext());
        roomRequestListButton.setRoomRequestType(RoomRequestType.REQUEST_TAKE_SEAT);
        roomRequestListButton.setOnClickListener(v -> {
            RoomRequestListDialog dialog = new RoomRequestListDialog(getContext());
            dialog.setRoomRequestType(RoomRequestType.REQUEST_TAKE_SEAT);
            dialog.show();
        });
        childLinearLayout.addView(roomRequestListButton, generateChildImageLayoutParams());

        audioOutputButton = new AudioOutputButton(getContext());
        audioOutputButton.open();
        childLinearLayout.addView(audioOutputButton, generateChildImageLayoutParams());

        gameListButton = new GameListButton(getContext());
        gameListButton.setOnClickListener(v -> {
            GameListDialog dialog = new GameListDialog(getContext());
            dialog.show();
        });
        childLinearLayout.addView(gameListButton, generateChildTextLayoutParams());

        gameStartButton = new GameStartButton(getContext());
        gameStartButton.setOnClickListener(v -> {
            InviteGameDialog dialog = new InviteGameDialog(getContext());
            dialog.show();
        });
        childLinearLayout.addView(gameStartButton, generateChildTextLayoutParams());

        gameQuitButton = new GameQuitButton(getContext());
        gameQuitButton.setOnClickListener(v -> {
            ZEGOLiveAudioRoomManager.getInstance().unloadGame();
            updateWidgets();
        });
        childLinearLayout.addView(gameQuitButton, generateChildTextLayoutParams());

        ZEGOLiveAudioRoomManager.getInstance().addLiveAudioRoomListener(new LiveAudioRoomListener() {
            @Override
            public void onHostChanged(ZEGOSDKUser hostUser) {
                updateWidgets();
            }

            @Override
            public void onLockSeatStatusChanged(boolean lock) {
                updateWidgets();
                roomRequestListButton.checkRedPoint();
                takeSeatButton.updateUI();
            }

            @Override
            public void onSeatChanged(List<LiveAudioRoomSeat> changedSeats) {
                updateWidgets();
            }
        });

        microphoneButton.setVisibility(GONE);
        lockSeatButton.setVisibility(GONE);
        takeSeatButton.setVisibility(GONE);
        roomRequestListButton.setVisibility(GONE);
        audioOutputButton.setVisibility(GONE);
        gameListButton.setVisibility(GONE);
        gameStartButton.setVisibility(GONE);
        gameQuitButton.setVisibility(GONE);

        MiniGameService miniGameService = ZEGOLiveAudioRoomManager.getInstance().getMiniGameService();
        miniGameService.addMiniGameEventHandler(new IZegoGameEngineHandler() {
            @Override
            public void onTokenWillExpire() {

            }

            @Override
            public void onGameLoadStateUpdate(ZegoGameLoadState zegoGameLoadState) {
                gameLoadState = zegoGameLoadState;
                updateWidgets();
            }

            @Override
            public void onGameStateUpdate(ZegoGameState zegoGameState) {
                gameState = zegoGameState;
                updateWidgets();
            }

            @Override
            public void onPlayerStateUpdate(ZegoGamePlayerState zegoGamePlayerState) {

            }

            @Override
            public void onUnloaded(String s) {
                gameState = null;
                gameLoadState = null;
                updateWidgets();
            }

            @Override
            public void onChargeRequire(String s) {

            }

            @Override
            public void onGameSoundPlay(String s, boolean b, String s1, boolean b1, int i) {

            }

            @Override
            public void onGameSoundVolumeChange(String s, int i) {

            }

            @Override
            public void onGameError(int i, String s) {

            }

            @Override
            public void onActionEventUpdate(int i, String s) {

            }

            @Override
            public void onGameResult(String s) {

            }

            @Override
            public ZegoGameRobotConfig onRobotConfigRequire(String s) {
                return null;
            }

            @Override
            public void onMicStateChange(boolean b) {

            }

            @Override
            public void onSpeakerStateChange(List<String> list, boolean b) {

            }
        });
    }

    private void updateWidgets() {

        int myRoomSeatIndex = ZEGOLiveAudioRoomManager.getInstance().findMyRoomSeatIndex();
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOSDKUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();

        if (localUser.equals(hostUser)) {
            // host widget
            microphoneButton.setVisibility(VISIBLE);
            lockSeatButton.setVisibility(VISIBLE);
            takeSeatButton.setVisibility(GONE);
            roomRequestListButton.setVisibility(VISIBLE);
            audioOutputButton.setVisibility(VISIBLE);

            MiniGameService miniGameService = ZEGOLiveAudioRoomManager.getInstance().getMiniGameService();
            ZegoGameInfoDetail currentGame = miniGameService.getCurrentGame();
            if (currentGame != null) {
                if (gameLoadState == ZegoGameLoadState.ZegoGameLoading) {
                    gameListButton.setVisibility(GONE);
                    gameStartButton.setVisibility(GONE);
                    gameQuitButton.setVisibility(VISIBLE);
                } else if (gameLoadState == ZegoGameLoadState.ZegoGameLoaded) {
                    gameListButton.setVisibility(GONE);
                    gameStartButton.setVisibility(VISIBLE);
                    gameQuitButton.setVisibility(VISIBLE);
                } else {
                    gameListButton.setVisibility(VISIBLE);
                    gameListButton.setVisibility(GONE);
                    gameStartButton.setVisibility(GONE);
                }
                if (gameState == ZegoGameState.ZegoGamePlaying || gameState == ZegoGameState.ZegoGamePreparing) {
                    gameStartButton.setVisibility(GONE);
                } else if (gameState == ZegoGameState.ZegoGameOver || gameState == ZegoGameState.ZegoGameIdle) {
                    gameStartButton.setVisibility(VISIBLE);
                }
            } else {
                gameListButton.setVisibility(VISIBLE);
                gameStartButton.setVisibility(GONE);
                gameQuitButton.setVisibility(GONE);
            }
        } else {
            if (myRoomSeatIndex >= 0) {
                // speaker widget
                microphoneButton.setVisibility(VISIBLE);
                lockSeatButton.setVisibility(GONE);
                takeSeatButton.setVisibility(GONE);
                roomRequestListButton.setVisibility(GONE);
                audioOutputButton.setVisibility(VISIBLE);
            } else {
                // audience widget
                microphoneButton.setVisibility(GONE);
                lockSeatButton.setVisibility(GONE);
                if (ZEGOLiveAudioRoomManager.getInstance().isSeatLocked()) {
                    takeSeatButton.setVisibility(VISIBLE);
                } else {
                    takeSeatButton.setVisibility(GONE);
                }
                roomRequestListButton.setVisibility(GONE);
                audioOutputButton.setVisibility(GONE);
            }
        }

    }

    private LayoutParams generateChildImageLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(10f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        return layoutParams;
    }

    private LayoutParams generateChildTextLayoutParams() {
        int size = Utils.dp2px(36f, getResources().getDisplayMetrics());
        int marginTop = Utils.dp2px(10f, getResources().getDisplayMetrics());
        int marginBottom = Utils.dp2px(16f, getResources().getDisplayMetrics());
        int marginEnd = Utils.dp2px(8, getResources().getDisplayMetrics());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, size);
        layoutParams.topMargin = marginTop;
        layoutParams.bottomMargin = marginBottom;
        layoutParams.rightMargin = marginEnd;
        return layoutParams;
    }
}
