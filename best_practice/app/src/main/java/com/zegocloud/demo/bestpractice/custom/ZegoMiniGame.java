package com.zegocloud.demo.bestpractice.custom;

import android.view.ViewGroup;
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
import im.zego.minigameengine.ZegoMiniGameEngine;
import im.zego.minigameengine.ZegoRobotSeatInfo;
import im.zego.minigameengine.ZegoStartGameConfig;
import im.zego.minigameengine.ZegoUserSeatInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import timber.log.Timber;

public class ZegoMiniGame {

    private ZegoMiniGameEngine miniGameEngine;
    private List<ZegoGameInfoDetail> gameFullInfoList = new ArrayList<>();
    private Map<ZegoGameMode, List<ZegoGameInfo>> gameListMap = new HashMap<>();
    private CopyOnWriteArrayList<IZegoGameEngineHandler> eventHandlerList = new CopyOnWriteArrayList<>();
    private String currentGame;

    public void init(long appID, String token, ZegoGameUserInfo userInfo, IZegoCommonCallback<String> callback) {
        miniGameEngine = ZegoMiniGameEngine.getInstance();

        miniGameEngine.init(appID, token, userInfo, new IZegoCommonCallback<String>() {
            @Override
            public void onResult(int i, String s) {
                Timber.d("miniGameEngine onResult() called with: i = [" + i + "], s = [" + s + "]");
                if (callback != null) {
                    callback.onResult(i, s);
                }
            }
        });
        miniGameEngine.setGameEngineHandler(new IZegoGameEngineHandler() {
            @Override
            public void onTokenWillExpire() {
                // token 过期前 30s 触发回调，通常配合方法 updateToken 函数使用，回调函数无参；
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onTokenWillExpire();
                }
            }

            @Override
            public void onGameLoadStateUpdate(ZegoGameLoadState gameLoadState) {
                // 通知游戏加载的状态
                Timber.d("onGameLoadStateUpdate() called with: gameLoadState = [" + gameLoadState + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onGameLoadStateUpdate(gameLoadState);
                }
            }

            @Override
            public void onGameStateUpdate(ZegoGameState gameState) {
                // 通知游戏运行的状态
                Timber.d("onGameStateUpdate() called with: gameState = [" + gameState + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onGameStateUpdate(gameState);
                }
            }

            @Override
            public void onPlayerStateUpdate(ZegoGamePlayerState playerState) {
                // 通知当前玩家的状态（游戏中/不在游戏中）
                Timber.d("onPlayerStateUpdate() called with: playerState = [" + playerState + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onPlayerStateUpdate(playerState);
                }
            }

            @Override
            public void onUnloaded(String gameID) {
                // 通知反加载游戏成功
                currentGame = null;
                Timber.d("onUnloaded() called with: gameID = [" + gameID + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onUnloaded(gameID);
                }
            }

            @Override
            public void onChargeRequire(String gameID) {
                // 通知玩家需要充值才能继续玩游戏
                Timber.d("onChargeRequire() called with: gameID = [" + gameID + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onChargeRequire(gameID);
                }
            }

            @Override
            public void onGameSoundPlay(String name, boolean isPlay, String url, boolean isLoop, int volume) {
                // 如果加载游戏时设置了自定义播放游戏声音，会收到这个回调，通知开发者需要控制声音文件的播放
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onGameSoundPlay(name, isPlay, url, isLoop, volume);
                }
            }

            @Override
            public void onGameSoundVolumeChange(String name, int volume) {
                // 如果加载游戏时设置了自定义播放游戏声音，此回调通知开发者需要修改 onGameSoundPlay 提及的声音的音量。
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onGameSoundVolumeChange(name, volume);
                }
            }

            @Override
            public void onGameError(int errorCode, String message) {
                Timber.d("onGameError() called with: errorCode = [" + errorCode + "], message = [" + message + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onGameError(errorCode, message);
                }
            }

            @Override
            public void onActionEventUpdate(int actionID, String data) {
                // 通知游戏按钮点击事件
                Timber.d("onActionEventUpdate() called with: actionID = [" + actionID + "], data = [" + data + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onActionEventUpdate(actionID, data);
                }
            }

            @Override
            public void onGameResult(String result) {
                // 游戏每局结算信息，Json 格式内容
                Timber.d("onGameResult() called with: result = [" + result + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onGameResult(result);
                }
            }

            @Override
            public ZegoGameRobotConfig onRobotConfigRequire(String gameID) {
                // 在开始游戏场随机匹配半屏或全屏模式游戏前，如果需要配置机器人，会收到这个回调，开发者需要返回机器人的配置信息。
                Timber.d("onRobotConfigRequire() called with: gameID = [" + gameID + "]");
                ZegoGameRobotConfig config = null;
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    config = handler.onRobotConfigRequire(gameID);
                }
                return config;
            }

            @Override
            public void onMicStateChange(boolean isMute) {
                // 加载游戏后，通知开发者要改变麦克风的状态。
                Timber.d("onMicStateChange() called with: isMute = [" + isMute + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onMicStateChange(isMute);
                }
            }

            @Override
            public void onSpeakerStateChange(List<String> userIDList, boolean isMute) {
                // 加载游戏后，通知开发者要改变扬声器的状态。
                Timber.d(
                    "onSpeakerStateChange() called with: userIDList = [" + userIDList + "], isMute = [" + isMute + "]");
                for (IZegoGameEngineHandler handler : eventHandlerList) {
                    handler.onSpeakerStateChange(userIDList, isMute);
                }
            }
        });
    }

    public void setGameLanguage(ZegoGameLanguage language) {
        miniGameEngine.setGameLanguage(language);
    }

    public void addMiniGameEventHandler(IZegoGameEngineHandler handler) {
        if (!eventHandlerList.contains(handler)) {
            eventHandlerList.add(handler);
        }
    }

    public void removeMiniGameEventHandler(IZegoGameEngineHandler handler) {
        eventHandlerList.remove(handler);
    }

    public void getGameList(ZegoGameMode gameMode, IZegoCommonCallback<List<ZegoGameInfo>> callback) {
        miniGameEngine.getGameList(gameMode, new IZegoCommonCallback<List<ZegoGameInfo>>() {
            @Override
            public void onResult(int errorCode, List<ZegoGameInfo> zegoGameInfos) {
                Timber.d("getGameList onResult() called with: errorCode = [" + errorCode + "], zegoGameInfos = ["
                    + zegoGameInfos + "]");
                if (errorCode == 0) {
                    List<ZegoGameInfo> modeGameList = gameListMap.get(gameMode);
                    if (modeGameList == null) {
                        modeGameList = new ArrayList<>();
                        gameListMap.put(gameMode, modeGameList);
                    }
                    for (ZegoGameInfo zegoGameInfo : zegoGameInfos) {
                        int gameInfoIndex = findGameInfoIndex(modeGameList, zegoGameInfo.gameID);
                        if (gameInfoIndex == -1) {
                            modeGameList.add(zegoGameInfo);
                        } else {
                            modeGameList.remove(gameInfoIndex);
                            modeGameList.add(gameInfoIndex, zegoGameInfo);
                        }
                        Timber.d("zegoGameInfo : " + zegoGameInfo);
                    }
                }
                if (callback != null) {
                    callback.onResult(errorCode, zegoGameInfos);
                }
            }
        });
    }

    private int findGameInfoIndex(List<ZegoGameInfo> gameInfoList, String gameID) {
        for (int i = 0; i < gameInfoList.size(); i++) {
            ZegoGameInfo zegoGameInfo = gameInfoList.get(i);
            if (Objects.equals(gameID, zegoGameInfo.gameID)) {
                return i;
            }
        }
        return -1;
    }

    private ZegoGameInfo findGameInfo(List<ZegoGameInfo> gameInfoList, String gameID) {
        for (int i = 0; i < gameInfoList.size(); i++) {
            ZegoGameInfo zegoGameInfo = gameInfoList.get(i);
            if (Objects.equals(gameID, zegoGameInfo.gameID)) {
                return zegoGameInfo;
            }
        }
        return null;
    }

    public ZegoGameInfo getGameInfo(String gameID) {
        for (List<ZegoGameInfo> value : gameListMap.values()) {
            ZegoGameInfo gameInfo = findGameInfo(value, gameID);
            if (gameInfo != null) {
                return gameInfo;
            }
        }
        return null;
    }

    public ZegoGameInfoDetail getCurrentGame() {
        return getGameFullInfo(currentGame);
    }

    public ZegoGameInfoDetail getGameFullInfo(String gameID) {
        for (ZegoGameInfoDetail gameInfoDetail : gameFullInfoList) {
            if (Objects.equals(gameID, gameInfoDetail.gameID)) {
                return gameInfoDetail;
            }
        }
        return null;
    }

    public List<ZegoGameInfo> getGameInfoList(ZegoGameMode gameMode) {
        return gameListMap.get(gameMode);
    }

    public void getGameFullInfo(String gameID, IZegoCommonCallback<ZegoGameInfoDetail> callback) {
        miniGameEngine.getGameInfo(gameID, new IZegoCommonCallback<ZegoGameInfoDetail>() {
            @Override
            public void onResult(int errorCode, ZegoGameInfoDetail zegoGameInfoDetail) {
                if (errorCode == 0) {
                    gameFullInfoList.add(zegoGameInfoDetail);
                }
                if (callback != null) {
                    callback.onResult(errorCode, zegoGameInfoDetail);
                }
            }
        });

    }

    public void loadGame(String gameID, ZegoGameMode gameMode, HashMap<String, Object> gameConfig,
        IZegoCommonCallback<String> callback) {
        miniGameEngine.loadGame(gameID, gameMode, gameConfig, new IZegoCommonCallback<String>() {
            @Override
            public void onResult(int i, String s) {
                if (i == 0) {
                    currentGame = gameID;
                }
                if (callback != null) {
                    callback.onResult(i, s);
                }
            }
        });
    }

    public void setGameContainer(ViewGroup viewGroup) {
        miniGameEngine.setGameContainer(viewGroup);
    }

    public void unloadGame() {
        Timber.d("unloadGame() called");
        miniGameEngine.unloadGame(true);
    }

    public void startGame(String gameID, String roomID, ZegoStartGameConfig gameConfig,
        List<ZegoUserSeatInfo> userSeatInfoList, List<ZegoRobotSeatInfo> robotSeatInfoList,
        IZegoCommonCallback<String> callback) {
        miniGameEngine.startGame(gameID, roomID, gameConfig, userSeatInfoList, robotSeatInfoList, callback);
    }

    public void unInitMiniGame() {
        gameFullInfoList.clear();
        eventHandlerList.clear();
        gameListMap.clear();
        miniGameEngine.unInit();
        currentGame = null;
    }
}
