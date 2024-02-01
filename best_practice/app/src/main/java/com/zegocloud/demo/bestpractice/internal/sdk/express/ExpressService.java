package com.zegocloud.demo.bestpractice.internal.sdk.express;

import android.app.Application;
import android.text.TextUtils;
import android.view.TextureView;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoCustomVideoProcessHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBarrageMessageCallback;
import im.zego.zegoexpress.callback.IZegoMediaPlayerEventHandler;
import im.zego.zegoexpress.callback.IZegoMediaPlayerLoadResourceCallback;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.callback.IZegoMixerStopCallback;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.callback.IZegoRoomLogoutCallback;
import im.zego.zegoexpress.callback.IZegoRoomSetRoomExtraInfoCallback;
import im.zego.zegoexpress.callback.IZegoUploadLogResultCallback;
import im.zego.zegoexpress.constants.ZegoAlphaLayoutType;
import im.zego.zegoexpress.constants.ZegoMediaPlayerNetworkEvent;
import im.zego.zegoexpress.constants.ZegoMediaPlayerState;
import im.zego.zegoexpress.constants.ZegoMultimediaLoadType;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamEvent;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoProcessConfig;
import im.zego.zegoexpress.entity.ZegoEngineConfig;
import im.zego.zegoexpress.entity.ZegoMediaPlayerResource;
import im.zego.zegoexpress.entity.ZegoMixerTask;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoPublisherConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class ExpressService {

    private ZEGOSDKUser currentUser;
    private String currentRoomID;
    // order by default time sequence
    private List<String> roomRemoteUserIDList = new ArrayList<>();
    private Map<String, ZEGOSDKUser> roomRemoteUserMap = new HashMap<>();
    private Map<String, ZegoRoomExtraInfo> roomExtraInfoMap = new HashMap<>();

    private List<IExpressEngineEventHandler> handlerList = new CopyOnWriteArrayList<>();
    private List<IExpressEngineEventHandler> autoDeleteHandlerList = new CopyOnWriteArrayList<>();
    private ExpressEngineProxy engineProxy = new ExpressEngineProxy();
    private IZegoEventHandler initEventHandler;
    private ZegoMediaPlayer mediaPlayer;
    private Map<String, String> cachedMediaResourceMap = new HashMap<>();
    private IZegoMediaPlayerEventHandler mediaPlayerEvent;

    public void initSDK(Application application, long appID, String appSign, ZegoScenario scenario) {
        ZegoEngineConfig config = new ZegoEngineConfig();
        config.advancedConfig.put("notify_remote_device_unknown_status", "true");
        config.advancedConfig.put("notify_remote_device_init_status", "true");
        ZegoExpressEngine.setEngineConfig(config);
        engineProxy.createEngine(application, appID, appSign, scenario);

        initEventHandler = new IZegoEventHandler() {

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                Timber.d("onRoomStreamUpdate() called with: roomID = [" + roomID + "], updateType = [" + updateType
                    + "], streamList = [" + streamList + "], extendedData = [" + extendedData + "]");
                List<ZEGOSDKUser> userList = new ArrayList<>();
                List<ZEGOSDKUser> needNotifyCameraChangeUserList = new ArrayList<>();
                List<ZEGOSDKUser> notifyMicCHangeUserList = new ArrayList<>();
                if (updateType == ZegoUpdateType.ADD) {
                    for (ZegoStream zegoStream : streamList) {
                        ZEGOSDKUser liveUser = getUser(zegoStream.user.userID);
                        if (liveUser == null) {
                            liveUser = new ZEGOSDKUser(zegoStream.user.userID, zegoStream.user.userName);
                            saveUserInfo(liveUser);
                        }
                        liveUser.setStreamID(zegoStream.streamID);

                        if (!TextUtils.isEmpty(zegoStream.extraInfo)) {
                            try {
                                JSONObject jsonObject = new JSONObject(zegoStream.extraInfo);
                                if (jsonObject.has("cam")) {
                                    boolean isCameraOpen = jsonObject.getBoolean("cam");
                                    boolean changed = liveUser.isCameraOpen() != isCameraOpen;
                                    liveUser.setCameraOpen(isCameraOpen);
                                    if (changed) {
                                        needNotifyCameraChangeUserList.add(liveUser);
                                    }
                                }
                                if (jsonObject.has("mic")) {
                                    boolean isMicOpen = jsonObject.getBoolean("mic");
                                    boolean changed = liveUser.isMicrophoneOpen() != isMicOpen;
                                    liveUser.setMicrophoneOpen(isMicOpen);
                                    if (changed) {
                                        notifyMicCHangeUserList.add(liveUser);
                                    }
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        userList.add(liveUser);
                    }

                    for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                        eventHandler.onReceiveStreamAdd(userList);
                    }
                    for (IExpressEngineEventHandler eventHandler : handlerList) {
                        eventHandler.onReceiveStreamAdd(userList);
                    }

                    for (ZEGOSDKUser user : needNotifyCameraChangeUserList) {
                        for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                            eventHandler.onCameraOpen(user.userID, user.isCameraOpen());
                        }
                        for (IExpressEngineEventHandler eventHandler : handlerList) {
                            eventHandler.onCameraOpen(user.userID, user.isCameraOpen());
                        }
                    }
                    for (ZEGOSDKUser user : notifyMicCHangeUserList) {
                        for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                            eventHandler.onMicrophoneOpen(user.userID, user.isMicrophoneOpen());
                        }
                        for (IExpressEngineEventHandler eventHandler : handlerList) {
                            eventHandler.onMicrophoneOpen(user.userID, user.isMicrophoneOpen());
                        }
                    }
                } else {
                    for (ZegoStream zegoStream : streamList) {
                        ZEGOSDKUser liveUser = getUser(zegoStream.user.userID);
                        if (liveUser != null) {
                            liveUser.deleteStream(zegoStream.streamID);
                            syncCameraState(liveUser.userID, false);
                            syncMicrophoneState(liveUser.userID, false);
                        } else {
                            liveUser = new ZEGOSDKUser(zegoStream.user.userID, zegoStream.user.userName);
                        }
                        userList.add(liveUser);
                    }
                    for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                        eventHandler.onReceiveStreamRemove(userList);
                    }
                    for (IExpressEngineEventHandler eventHandler : handlerList) {
                        eventHandler.onReceiveStreamRemove(userList);
                    }
                }
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
                Timber.d("onPublisherStateUpdate: " + streamID + ", state:" + state + ", code:" + errorCode + ", data:"
                    + extendedData);
                //                ArrayList<ZegoStream> streamList = new ArrayList<>(1);
                //                ZegoStream zegoStream = new ZegoStream();
                //                zegoStream.user = new ZegoUser(currentUser.userID, currentUser.userName);
                //                zegoStream.streamID = streamID;
                //                zegoStream.extraInfo = extendedData.toString();
                //                streamList.add(zegoStream);

                if (state == ZegoPublisherState.PUBLISHING) {
                    currentUser.setStreamID(streamID);
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    currentUser.deleteStream(streamID);
                }
            }

            @Override
            public void onRoomStreamExtraInfoUpdate(String roomID, ArrayList<ZegoStream> streamList) {
                super.onRoomStreamExtraInfoUpdate(roomID, streamList);

                for (ZegoStream zegoStream : streamList) {
                    if (!TextUtils.isEmpty(zegoStream.extraInfo)) {
                        ZEGOSDKUser liveUser = getUser(zegoStream.user.userID);
                        if (liveUser == null) {
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(zegoStream.extraInfo);
                            if (jsonObject.has("cam")) {
                                boolean isCameraOpen = jsonObject.getBoolean("cam");
                                syncCameraState(liveUser.userID, isCameraOpen);
                            }
                            if (jsonObject.has("mic")) {
                                boolean isMicOpen = jsonObject.getBoolean("mic");
                                syncMicrophoneState(liveUser.userID, isMicOpen);
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

            }

            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                Timber.d("onRoomUserUpdate() called with: roomID = [" + roomID + "], updateType = [" + updateType
                    + "], userList = [" + userList + "]");
                List<ZEGOSDKUser> liveUserList = new ArrayList<>();
                for (ZegoUser zegoUser : userList) {
                    ZEGOSDKUser liveUser = getUser(zegoUser.userID);
                    if (liveUser != null) {
                        liveUserList.add(liveUser);
                    } else {
                        liveUserList.add(new ZEGOSDKUser(zegoUser.userID, zegoUser.userName));
                    }
                }

                if (updateType == ZegoUpdateType.ADD) {
                    for (ZEGOSDKUser liveUser : liveUserList) {
                        saveUserInfo(liveUser);
                    }

                    for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                        eventHandler.onUserEnter(liveUserList);
                    }
                    for (IExpressEngineEventHandler eventHandler : handlerList) {
                        eventHandler.onUserEnter(liveUserList);
                    }
                } else {
                    for (ZEGOSDKUser liveUser : liveUserList) {
                        removeUserInfo(liveUser.userID);
                    }

                    for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                        eventHandler.onUserLeft(liveUserList);
                    }
                    for (IExpressEngineEventHandler eventHandler : handlerList) {
                        eventHandler.onUserLeft(liveUserList);
                    }
                }
            }

            @Override
            public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteCameraStateUpdate(streamID, state);
                if (state == ZegoRemoteDeviceState.NOT_SUPPORT) {
                    return;
                }
                boolean isCameraOpen = state == ZegoRemoteDeviceState.OPEN;
                ZEGOSDKUser liveUser = getUserFromStreamID(streamID);
                if (liveUser == null) {
                    return;
                }
                syncCameraState(liveUser.userID, isCameraOpen);
            }

            @Override
            public void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
                super.onRemoteMicStateUpdate(streamID, state);
                if (state == ZegoRemoteDeviceState.NOT_SUPPORT) {
                    return;
                }
                boolean isMicOpen = state == ZegoRemoteDeviceState.OPEN;
                ZEGOSDKUser liveUser = getUserFromStreamID(streamID);
                if (liveUser == null) {
                    return;
                }
                syncMicrophoneState(liveUser.userID, isMicOpen);
            }

            @Override
            public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
                JSONObject extendedData) {
                super.onRoomStateChanged(roomID, reason, errorCode, extendedData);
                Timber.d("onRoomStateChanged() called with: roomID = [" + roomID + "], reason = [" + reason
                    + "], errorCode = [" + errorCode + "], extendedData = [" + extendedData + "]");
            }

            @Override
            public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                super.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
                for (ZegoRoomExtraInfo roomExtraInfo : roomExtraInfoList) {
                    ZegoRoomExtraInfo oldRoomExtraInfo = roomExtraInfoMap.get(roomExtraInfo.key);
                    if (oldRoomExtraInfo != null) {
                        if (Objects.equals(roomExtraInfo.updateUser.userID, getCurrentUser().userID)) {
                            continue;
                        }
                        if (roomExtraInfo.updateTime < oldRoomExtraInfo.updateTime) {
                            continue;
                        }
                    }
                    roomExtraInfoMap.put(roomExtraInfo.key, roomExtraInfo);

                    for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                        eventHandler.onRoomExtraInfoUpdate2(roomID, roomExtraInfoList);
                    }
                    for (IExpressEngineEventHandler eventHandler : handlerList) {
                        eventHandler.onRoomExtraInfoUpdate2(roomID, roomExtraInfoList);
                    }
                }
            }

            @Override
            public void onPlayerStreamEvent(ZegoStreamEvent eventID, String streamID, String extraInfo) {
                super.onPlayerStreamEvent(eventID, streamID, extraInfo);
                Timber.d("onPlayerStreamEvent() called with: eventID = [" + eventID + "], streamID = [" + streamID
                    + "], extraInfo = [" + extraInfo + "]");
            }

            @Override
            public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode,
                JSONObject extendedData) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
                Timber.d("onPlayerStateUpdate: " + streamID + ", state:" + state + ", code:" + errorCode + ", data:"
                    + extendedData);
            }
        };
        Timber.d(
            "initSDK() called with: application = [" + application + "], appID = [" + appID + "], appSign = [" + appSign
                + "], scenario = [" + scenario + "]");
        engineProxy.addEventHandler(initEventHandler);
    }

    public void sendSEI(String seiString) {
        engineProxy.sendSEI(seiString.getBytes());
    }

    public void startPreview(TextureView textureView, ZegoViewMode viewMode) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        ZegoCanvas canvas = new ZegoCanvas(textureView);
        canvas.viewMode = viewMode;
        engineProxy.startPreview(canvas);
    }

    public void stopPreview() {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.stopPreview();
    }

    public void startSoundLevelMonitor() {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.startSoundLevelMonitor(1000);
    }

    public void startSoundLevelMonitor(int mills) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.startSoundLevelMonitor(mills);
    }

    public void stopSoundLevelMonitor() {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.stopSoundLevelMonitor();
    }

    /**
     * preview before publish
     */
    public void startPublishingStream(String streamID) {
        if (engineProxy.getExpressEngine() == null || currentUser == null) {
            return;
        }
        engineProxy.startPublishingStream(streamID);
    }

    public void startPublishingStream(String streamID, ZegoPublishChannel channel) {
        if (engineProxy.getExpressEngine() == null || currentUser == null) {
            return;
        }
        engineProxy.startPublishingStream(streamID, channel);
    }

    public void startPublishingStream(String streamID, String roomID) {
        ZegoPublisherConfig publisherConfig = new ZegoPublisherConfig();
        publisherConfig.roomID = roomID;
        startPublishingStream(streamID, publisherConfig, ZegoPublishChannel.MAIN);
    }

    public void startPublishingStream(String streamID, ZegoPublisherConfig publisherConfig,
        ZegoPublishChannel channel) {
        if (engineProxy.getExpressEngine() == null || currentUser == null) {
            return;
        }
        engineProxy.startPublishingStream(streamID, publisherConfig, channel);
    }

    public void stopPublishingStream() {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.stopPublishingStream();
    }

    public void stopPublishingStream(ZegoPublishChannel channel) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.stopPublishingStream(channel);
    }

    public void startPlayingStream(TextureView textureView, String streamID, ZegoViewMode viewMode) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        ZegoCanvas canvas = new ZegoCanvas(textureView);
        canvas.viewMode = viewMode;
        engineProxy.startPlayingStream(streamID, canvas);
    }

    public void startPlayingStream(String streamID, ZegoPlayerConfig config) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.startPlayingStream(streamID, config);
    }

    public void stopPlayingStream(String streamID) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.stopPlayingStream(streamID);
    }

    public ZegoMediaPlayer getMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = engineProxy.createMediaPlayer();
            mediaPlayer.setEventHandler(new IZegoMediaPlayerEventHandler() {
                @Override
                public void onMediaPlayerStateUpdate(ZegoMediaPlayer mediaPlayer, ZegoMediaPlayerState state,
                    int errorCode) {
                    super.onMediaPlayerStateUpdate(mediaPlayer, state, errorCode);
                    if (mediaPlayerEvent != null) {
                        mediaPlayerEvent.onMediaPlayerStateUpdate(mediaPlayer, state, errorCode);
                    }
                }

                @Override
                public void onMediaPlayerLocalCache(ZegoMediaPlayer mediaPlayer, int errorCode, String resource,
                    String cachedFile) {
                    super.onMediaPlayerLocalCache(mediaPlayer, errorCode, resource, cachedFile);
                    if (errorCode == 0) {
                        cachedMediaResourceMap.put(resource, cachedFile);
                    }
                    if (mediaPlayerEvent != null) {
                        mediaPlayerEvent.onMediaPlayerLocalCache(mediaPlayer, errorCode, resource, cachedFile);
                    }
                }

                @Override
                public void onMediaPlayerNetworkEvent(ZegoMediaPlayer mediaPlayer,
                    ZegoMediaPlayerNetworkEvent networkEvent) {
                    super.onMediaPlayerNetworkEvent(mediaPlayer, networkEvent);
                    if (mediaPlayerEvent != null) {
                        mediaPlayerEvent.onMediaPlayerNetworkEvent(mediaPlayer, networkEvent);
                    }
                }
            });
        }
        return mediaPlayer;
    }

    public void setMediaPlayerEventHandler(IZegoMediaPlayerEventHandler mediaPlayerEvent) {
        this.mediaPlayerEvent = mediaPlayerEvent;
    }

    public void loadResourceFile(String url, IZegoMediaPlayerLoadResourceCallback callback) {
        ZegoMediaPlayer mediaPlayer = getMediaPlayer();
        ZegoMediaPlayerResource resource = new ZegoMediaPlayerResource();
        resource.loadType = ZegoMultimediaLoadType.FILE_PATH;
        if (cachedMediaResourceMap.containsKey(url)) {
            resource.filePath = cachedMediaResourceMap.get(url);
        } else {
            resource.filePath = url;
        }
        resource.alphaLayout = ZegoAlphaLayoutType.LEFT;
        mediaPlayer.loadResourceWithConfig(resource, new IZegoMediaPlayerLoadResourceCallback() {
            @Override
            public void onLoadResourceCallback(int errorCode) {
                if (callback != null) {
                    callback.onLoadResourceCallback(errorCode);
                }
            }
        });
    }

    public void loginRoom(String roomID, IZegoRoomLoginCallback callback) {
        loginRoom(roomID, "", callback);
    }

    public void loginRoom(String roomID, String token, IZegoRoomLoginCallback callback) {
        if (engineProxy.getExpressEngine() == null || currentUser == null) {
            return;
        }
        currentRoomID = roomID;
        ZegoRoomConfig config = new ZegoRoomConfig();
        config.token = token;
        config.isUserStatusNotify = true;
        ZegoUser zegoUser = new ZegoUser(currentUser.userID, currentUser.userName);
        engineProxy.loginRoom(roomID, zegoUser, config, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode != 0) {
                    currentRoomID = null;
                }
                if (callback != null) {
                    callback.onRoomLoginResult(errorCode, extendedData);
                }
            }
        });
    }

    public void logoutRoom(IZegoRoomLogoutCallback callback) {
        if (engineProxy.getExpressEngine() == null || currentRoomID == null) {
            return;
        }
        removeRoomData();
        removeAutoDeleteRoomListeners();
        engineProxy.logoutRoom(callback);
    }

    public void addEventHandler(IExpressEngineEventHandler eventHandler) {
        addEventHandler(eventHandler, true);
    }

    /**
     * @param eventHandler
     * @param autoDelete   delete or not when leave room
     */
    public void addEventHandler(IExpressEngineEventHandler eventHandler, boolean autoDelete) {
        if (autoDelete) {
            autoDeleteHandlerList.add(eventHandler);
        } else {
            handlerList.add(eventHandler);
        }
        engineProxy.addEventHandler(eventHandler);
    }

    public void removeEventHandler(IExpressEngineEventHandler eventHandler) {
        autoDeleteHandlerList.remove(eventHandler);
        handlerList.remove(eventHandler);
        engineProxy.removeEventHandler(eventHandler);
    }


    public void removeAutoDeleteRoomListeners() {
        engineProxy.removeEventHandlerList(new ArrayList<>(autoDeleteHandlerList));
        autoDeleteHandlerList.clear();
    }

    public void removeRoomData() {
        roomExtraInfoMap.clear();
        roomRemoteUserMap.clear();
        roomRemoteUserIDList.clear();
        currentRoomID = null;
        mediaPlayer = null;
        stopPreview();
        useFrontCamera(true);
        openCamera(false);
        setAudioRouteToSpeaker(true);
        stopSoundLevelMonitor();
    }

    private void removeUserListeners() {
        removeAutoDeleteRoomListeners();
        engineProxy.removeEventHandlerList(new ArrayList<>(handlerList));
        handlerList.clear();
    }

    public void removeUserData() {
        removeRoomData();
        currentUser = null;
    }

    public void connectUser(String userID, String userName) {
        currentUser = new ZEGOSDKUser(userID, userName);
    }

    public void disconnectUser() {
        removeUserData();
        removeUserListeners();
        // keep initEventHandler not cleared when user logout account
        Timber.d("disconnectUser: ");
    }

    public ZEGOSDKUser getCurrentUser() {
        return currentUser;
    }

    private void saveUserInfo(ZEGOSDKUser liveUser) {
        boolean contains = roomRemoteUserMap.containsKey(liveUser.userID);
        roomRemoteUserMap.put(liveUser.userID, liveUser);

        if (contains) {
            roomRemoteUserIDList.remove(liveUser.userID);
        }

        roomRemoteUserIDList.add(liveUser.userID);
    }

    private void removeUserInfo(String userID) {
        roomRemoteUserMap.remove(userID);
        roomRemoteUserIDList.remove(userID);
    }


    public void openCamera(boolean open) {
        if (currentUser == null || engineProxy.getExpressEngine() == null) {
            return;
        }
        if (currentUser != null) {
            boolean changed = open != currentUser.isCameraOpen();
            if (changed) {
                currentUser.setCameraOpen(open);
                for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                    eventHandler.onCameraOpen(currentUser.userID, open);
                }
                for (IExpressEngineEventHandler eventHandler : handlerList) {
                    eventHandler.onCameraOpen(currentUser.userID, open);
                }
            }
        }
        engineProxy.enableCamera(open);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cam", open);
            jsonObject.put("mic", currentUser.isMicrophoneOpen());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String extraInfo = jsonObject.toString();
        engineProxy.setStreamExtraInfo(extraInfo, null);
    }

    private void syncCameraState(String userID, boolean open) {
        if (!userID.equals(currentUser.userID)) {
            ZEGOSDKUser zegosdkUser = getUser(userID);
            boolean changed = zegosdkUser.isCameraOpen() != open;
            if (changed) {
                zegosdkUser.setCameraOpen(open);
                for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                    eventHandler.onCameraOpen(zegosdkUser.userID, open);
                }
                for (IExpressEngineEventHandler eventHandler : handlerList) {
                    eventHandler.onCameraOpen(zegosdkUser.userID, open);
                }
            }
        }
    }

    public void openMicrophone(boolean open) {
        if (currentUser == null || engineProxy.getExpressEngine() == null) {
            return;
        }
        if (currentUser != null) {
            boolean changed = currentUser.isMicrophoneOpen() != open;
            if (changed) {
                currentUser.setMicrophoneOpen(open);
                for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                    eventHandler.onMicrophoneOpen(currentUser.userID, open);
                }
                for (IExpressEngineEventHandler eventHandler : handlerList) {
                    eventHandler.onMicrophoneOpen(currentUser.userID, open);
                }
            }
        }
        engineProxy.muteMicrophone(!open);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cam", currentUser.isCameraOpen());
            jsonObject.put("mic", open);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String extraInfo = jsonObject.toString();
        engineProxy.setStreamExtraInfo(extraInfo, null);
    }

    public void syncMicrophoneState(String userID, boolean open) {
        if (!userID.equals(currentUser.userID)) {
            ZEGOSDKUser zegosdkUser = getUser(userID);
            boolean changed = zegosdkUser.isMicrophoneOpen() != open;
            if (changed) {
                zegosdkUser.setMicrophoneOpen(open);
                for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                    eventHandler.onMicrophoneOpen(zegosdkUser.userID, open);
                }
                for (IExpressEngineEventHandler eventHandler : handlerList) {
                    eventHandler.onMicrophoneOpen(zegosdkUser.userID, open);
                }
            }
        }
    }

    public ZEGOSDKUser getUser(String userID) {
        if (userID != null && currentUser != null && userID.equals(currentUser.userID)) {
            return currentUser;
        }
        return roomRemoteUserMap.get(userID);
    }

    public List<ZEGOSDKUser> getRoomUsers() {
        List<ZEGOSDKUser> roomUsers = new ArrayList<>();
        roomUsers.add(currentUser);
        for (String userID : roomRemoteUserIDList) {
            ZEGOSDKUser zegosdkUser = roomRemoteUserMap.get(userID);
            roomUsers.add(zegosdkUser);
        }
        return roomUsers;
    }


    public boolean isCameraOpen() {
        if (currentUser != null) {
            return currentUser.isCameraOpen();
        }
        return false;
    }

    public boolean isMicrophoneOpen() {
        if (currentUser != null) {
            return currentUser.isMicrophoneOpen();
        }
        return false;
    }

    public void useFrontCamera(boolean useFront) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.useFrontCamera(useFront);
    }

    private ZEGOSDKUser getUserFromStreamID(String streamID) {
        if (getCurrentUser() != null && Objects.equals(getCurrentUser().getMainStreamID(), streamID)) {
            return getCurrentUser();
        }
        for (ZEGOSDKUser liveUser : roomRemoteUserMap.values()) {
            if (Objects.equals(liveUser.getMainStreamID(), streamID)) {
                return liveUser;
            }
        }
        return null;
    }

    public String getCurrentRoomID() {
        return currentRoomID;
    }

    public void setAudioRouteToSpeaker(boolean routeToSpeaker) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.setAudioRouteToSpeaker(routeToSpeaker);
    }

    public boolean isCurrentUser(String userID) {
        if (currentUser != null) {
            return Objects.equals(userID, currentUser.userID);
        } else {
            return false;
        }
    }

    public void setRoomExtraInfo(String key, String value) {
        if (engineProxy.getExpressEngine() == null || currentRoomID == null) {
            return;
        }
        final String roomID = currentRoomID;
        engineProxy.setRoomExtraInfo(currentRoomID, key, value, new IZegoRoomSetRoomExtraInfoCallback() {
            @Override
            public void onRoomSetRoomExtraInfoResult(int errorCode) {
                if (errorCode == 0) {
                    ZegoRoomExtraInfo roomExtraInfo = roomExtraInfoMap.get(key);
                    if (roomExtraInfo == null) {
                        roomExtraInfo = new ZegoRoomExtraInfo();
                        roomExtraInfo.key = key;
                        roomExtraInfo.updateUser = new ZegoUser(getCurrentUser().userID, getCurrentUser().userName);
                    }
                    roomExtraInfo.updateTime = System.currentTimeMillis();
                    roomExtraInfo.value = value;
                    roomExtraInfoMap.put(roomExtraInfo.key, roomExtraInfo);

                    ArrayList<ZegoRoomExtraInfo> extraInfoList = new ArrayList<>();
                    extraInfoList.add(roomExtraInfo);

                    for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                        eventHandler.onRoomExtraInfoUpdate2(roomID, extraInfoList);
                    }
                    for (IExpressEngineEventHandler eventHandler : handlerList) {
                        eventHandler.onRoomExtraInfoUpdate2(roomID, extraInfoList);
                    }
                }
            }
        });
    }

    public void enableCustomVideoProcessing(boolean enable, ZegoCustomVideoProcessConfig config,
        ZegoPublishChannel channel) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.enableCustomVideoProcessing(enable, config, channel);
    }

    public void setCustomVideoProcessHandler(IZegoCustomVideoProcessHandler handler) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.setCustomVideoProcessHandler(handler);
    }

    public ZegoVideoConfig getVideoConfig() {
        if (engineProxy.getExpressEngine() == null) {
            return null;
        }
        return engineProxy.getVideoConfig();
    }

    public void setVideoConfig(ZegoVideoConfig videoConfig) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.setVideoConfig(videoConfig);
    }

    public void setVideoConfig(ZegoVideoConfig videoConfig, ZegoPublishChannel publishChannel) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.setVideoConfig(videoConfig, publishChannel);
    }

    public void sendCustomVideoProcessedTextureData(int textureID, int width, int height,
        long referenceTimeMillisecond) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.sendCustomVideoProcessedTextureData(textureID, width, height, referenceTimeMillisecond);
    }

    public void sendBarrageMessage(String message, IZegoIMSendBarrageMessageCallback callback) {
        if (engineProxy.getExpressEngine() == null || currentRoomID == null) {
            return;
        }
        engineProxy.sendBarrageMessage(currentRoomID, message, new IZegoIMSendBarrageMessageCallback() {
            @Override
            public void onIMSendBarrageMessageResult(int errorCode, String messageID) {
                if (callback != null) {
                    callback.onIMSendBarrageMessageResult(errorCode, messageID);
                }
                for (IExpressEngineEventHandler eventHandler : autoDeleteHandlerList) {
                    eventHandler.onIMSendBarrageMessageResult(errorCode, message, messageID);
                }
                for (IExpressEngineEventHandler eventHandler : handlerList) {
                    eventHandler.onIMSendBarrageMessageResult(errorCode, message, messageID);
                }
            }
        });
    }

    public void mutePlayStreamAudio(String streamID, boolean mute) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.mutePlayStreamAudio(streamID, mute);
    }

    public void mutePlayStreamVideo(String streamID, boolean mute) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.mutePlayStreamVideo(streamID, mute);
    }

    public void startMixerTask(ZegoMixerTask task, IZegoMixerStartCallback callback) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.startMixerTask(task, callback);
    }

    public void stopMixerTask(ZegoMixerTask task, IZegoMixerStopCallback callback) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.stopMixerTask(task, callback);
    }

    public void setRoomScenario(ZegoScenario scenario) {
        if (engineProxy.getExpressEngine() == null) {
            return;
        }
        engineProxy.setRoomScenario(scenario);
    }

    public void uploadLog(IZegoUploadLogResultCallback callback) {
        engineProxy.uploadLog(callback);
    }
}
