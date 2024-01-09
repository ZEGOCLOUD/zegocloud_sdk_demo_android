package com.zegocloud.demo.bestpractice.internal.business.audioroom;

import android.widget.FrameLayout;
import com.zegocloud.demo.bestpractice.ZEGOSDKKeyCenter;
import com.zegocloud.demo.bestpractice.custom.TokenServerAssistant;
import com.zegocloud.demo.bestpractice.custom.TokenServerAssistant.TokenInfo;
import com.zegocloud.demo.bestpractice.custom.ZegoMiniGame;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.minigameengine.IZegoCommonCallback;
import im.zego.minigameengine.IZegoGameEngineHandler;
import im.zego.minigameengine.ZegoGameInfo;
import im.zego.minigameengine.ZegoGameInfoDetail;
import im.zego.minigameengine.ZegoGameLanguage;
import im.zego.minigameengine.ZegoGameLoadState;
import im.zego.minigameengine.ZegoGameMode;
import im.zego.minigameengine.ZegoGamePlayerState;
import im.zego.minigameengine.ZegoGameRobotConfig;
import im.zego.minigameengine.ZegoGameState;
import im.zego.minigameengine.ZegoGameUserInfo;
import im.zego.minigameengine.ZegoRobotSeatInfo;
import im.zego.minigameengine.ZegoStartGameConfig;
import im.zego.minigameengine.ZegoUserSeatInfo;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import timber.log.Timber;

public class MiniGameService {

    private ZegoMiniGame zegoMiniGame = new ZegoMiniGame();

    public void onRoomAttributesUpdated(List<Map<String, String>> setProperties,
        List<Map<String, String>> deleteProperties) {
        for (Map<String, String> setProperty : setProperties) {
            for (Map.Entry<String, String> entry : setProperty.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                if ("game_id".equals(key)) {
                    zegoMiniGame.loadGame(value, ZegoGameMode.ZegoGameModeHostsGame, new HashMap<>(),
                        new IZegoCommonCallback<String>() {
                            @Override
                            public void onResult(int i, String s) {

                            }
                        });
                }
            }
        }
        for (Map<String, String> deleteProperty : deleteProperties) {
            for (Map.Entry<String, String> entry : deleteProperty.entrySet()) {
                String key = entry.getKey();
                if ("game_id".equals(key)) {
                    zegoMiniGame.unloadGame();
                }
            }
        }
    }

    public void init() {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();

        //        GameTestApi.getToken(ZEGOSDKKeyCenter.appID, currentUser.userID, new Callback() {
        //            @Override
        //            public void onFailure(@NonNull Call call, @NonNull IOException e) {
        //
        //            }
        //
        //            @Override
        //            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
        //                token = response.body().string();
        //                initMiniGameEngine(token);
        //            }
        //        });

        // you should use GameTestApi.getToken to get token from server.
        // Here is for test only.
        TokenInfo tokenInfo = TokenServerAssistant.generateToken04(ZEGOSDKKeyCenter.appID, currentUser.userID,
            ZEGOSDKKeyCenter.serverSecret, 3600 * 24, "");
        String token = tokenInfo.data;
        initMiniGameEngine(token);

        zegoMiniGame.addMiniGameEventHandler(new IZegoGameEngineHandler() {
            @Override
            public void onTokenWillExpire() {

            }

            @Override
            public void onGameLoadStateUpdate(ZegoGameLoadState zegoGameLoadState) {
                if (zegoGameLoadState == ZegoGameLoadState.ZegoGameLoaded) {
                    ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                    ZEGOSDKUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();

                    ZegoGameInfoDetail currentGame = zegoMiniGame.getCurrentGame();

                    if (localUser.equals(hostUser)) {
                        syncHostLoadGameState(currentGame.gameID);
                    }
                }
            }

            @Override
            public void onGameStateUpdate(ZegoGameState zegoGameState) {

            }

            @Override
            public void onPlayerStateUpdate(ZegoGamePlayerState zegoGamePlayerState) {

            }

            @Override
            public void onUnloaded(String s) {
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                ZEGOSDKUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();

                if (localUser.equals(hostUser)) {
                    syncHostUnLoadGameState();
                }
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

    private void initMiniGameEngine(String token) {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(currentUser.userID);
        String userID = currentUser.userID;
        String userName = currentUser.userName;
        String userAvatar = "";
        if (zimUserFullInfo != null) {
            userAvatar = zimUserFullInfo.userAvatarUrl;
        }
        ZegoGameUserInfo userInfo = new ZegoGameUserInfo(userID, userName, userAvatar);
        zegoMiniGame.init(ZEGOSDKKeyCenter.appID, token, userInfo, new IZegoCommonCallback<String>() {
            @Override
            public void onResult(int i, String s) {
                if (i == 0) {
                    zegoMiniGame.setGameLanguage(ZegoGameLanguage.en);
                    zegoMiniGame.getGameList(ZegoGameMode.ZegoGameModeHostsGame, null);
                }
            }
        });
    }

    private void syncHostUnLoadGameState() {
        ZEGOSDKManager.getInstance().zimService.deleteRoomAttributes(Collections.singletonList("game_id"),
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                    Timber.d("syncHostLoadGameState() called with: roomID = [" + roomID + "], errorKeys = [" + errorKeys
                        + "], errorInfo = [" + errorInfo.getCode() + "]");
                }
            });
    }

    private void syncHostLoadGameState(String gameID) {
        ZIMRoomAttributesSetConfig config = new ZIMRoomAttributesSetConfig();
        config.isDeleteAfterOwnerLeft = true;
        config.isForce = true;
        config.isUpdateOwner = true;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes("game_id", gameID, config,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                    Timber.d("syncHostLoadGameState() called with: roomID = [" + roomID + "], errorKeys = [" + errorKeys
                        + "], errorInfo = [" + errorInfo.getCode() + "]");
                }
            });
    }

    public void unInit() {
        zegoMiniGame.unloadGame();
        zegoMiniGame.unInitMiniGame();
    }

    public void setGameContainer(FrameLayout miniGameContainer) {
        zegoMiniGame.setGameContainer(miniGameContainer);
    }

    public void unloadGame() {
        zegoMiniGame.unloadGame();
    }

    public ZegoGameInfoDetail getCurrentGame() {
        return zegoMiniGame.getCurrentGame();
    }

    public void addMiniGameEventHandler(IZegoGameEngineHandler handler) {
        zegoMiniGame.addMiniGameEventHandler(handler);
    }

    public List<ZegoGameInfo> getGameInfoList() {
        return zegoMiniGame.getGameInfoList(ZegoGameMode.ZegoGameModeHostsGame);
    }

    public void getGameList(IZegoCommonCallback<List<ZegoGameInfo>> listIZegoCommonCallback) {
        zegoMiniGame.getGameList(ZegoGameMode.ZegoGameModeHostsGame, listIZegoCommonCallback);
    }

    public void getGameFullInfo(String gameID,
        IZegoCommonCallback<ZegoGameInfoDetail> zegoGameInfoDetailIZegoCommonCallback) {
        zegoMiniGame.getGameFullInfo(gameID, zegoGameInfoDetailIZegoCommonCallback);
    }

    public void loadGame(String gameID, HashMap<String, Object> objectObjectHashMap,
        IZegoCommonCallback<String> stringIZegoCommonCallback) {
        String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        objectObjectHashMap.put("roomID", roomID);
        zegoMiniGame.loadGame(gameID, ZegoGameMode.ZegoGameModeHostsGame, objectObjectHashMap,
            stringIZegoCommonCallback);
    }

    public void startGame(String gameID, String roomID, ZegoStartGameConfig zegoStartGameConfig,
        List<ZegoUserSeatInfo> userSeatInfoList, List<ZegoRobotSeatInfo> robotSeatInfoList,
        IZegoCommonCallback<String> stringIZegoCommonCallback) {
        zegoMiniGame.startGame(gameID, roomID, zegoStartGameConfig, userSeatInfoList, robotSeatInfoList,
            stringIZegoCommonCallback);
    }
}
