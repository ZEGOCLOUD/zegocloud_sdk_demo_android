package com.zegocloud.demo.bestpractice.internal.sdk.express;

import android.app.Application;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.ZegoMediaPlayer;
import im.zego.zegoexpress.callback.IZegoCustomVideoProcessHandler;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.callback.IZegoIMSendBarrageMessageCallback;
import im.zego.zegoexpress.callback.IZegoIMSendCustomCommandCallback;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.callback.IZegoMixerStopCallback;
import im.zego.zegoexpress.callback.IZegoPublisherSetStreamExtraInfoCallback;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.callback.IZegoRoomLogoutCallback;
import im.zego.zegoexpress.callback.IZegoRoomSetRoomExtraInfoCallback;
import im.zego.zegoexpress.callback.IZegoUploadLogResultCallback;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoCanvas;
import im.zego.zegoexpress.entity.ZegoCustomVideoProcessConfig;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoMixerTask;
import im.zego.zegoexpress.entity.ZegoPlayerConfig;
import im.zego.zegoexpress.entity.ZegoPublisherConfig;
import im.zego.zegoexpress.entity.ZegoRoomConfig;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zegoexpress.entity.ZegoVideoConfig;
import java.util.ArrayList;
import java.util.List;

class ExpressEngineProxy {

    private SimpleExpressEventHandler expressEventHandler;

    public void createEngine(Application application, long appID, String appSign, ZegoScenario scenario) {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.appSign = appSign;
        profile.scenario = scenario;
        profile.application = application;
        expressEventHandler = new SimpleExpressEventHandler();
        ZegoExpressEngine.createEngine(profile, expressEventHandler);
    }

    public ZegoExpressEngine getExpressEngine() {
        return ZegoExpressEngine.getEngine();
    }


    public void sendSEI(byte[] data) {
        ZegoExpressEngine.getEngine().sendSEI(data);
    }

    public void startPreview(ZegoCanvas canvas) {
        ZegoExpressEngine.getEngine().startPreview(canvas);
    }

    public void stopPreview() {
        ZegoExpressEngine.getEngine().stopPreview();
    }

    public void startSoundLevelMonitor(int millisecond) {
        ZegoExpressEngine.getEngine().startSoundLevelMonitor(millisecond);
    }

    public void stopSoundLevelMonitor() {
        ZegoExpressEngine.getEngine().stopSoundLevelMonitor();
    }

    public void startPublishingStream(String streamID) {
        ZegoExpressEngine.getEngine().startPublishingStream(streamID);
    }

    public void startPublishingStream(String streamID, ZegoPublishChannel channel) {
        ZegoExpressEngine.getEngine().startPublishingStream(streamID, channel);
    }

    public void startPublishingStream(String streamID, ZegoPublisherConfig config, ZegoPublishChannel channel) {
        ZegoExpressEngine.getEngine().startPublishingStream(streamID, config, channel);
    }

    public void stopPublishingStream() {
        ZegoExpressEngine.getEngine().stopPublishingStream();
    }

    public void stopPublishingStream(ZegoPublishChannel channel) {
        ZegoExpressEngine.getEngine().stopPublishingStream(channel);
    }

    public void startPlayingStream(String streamID, ZegoCanvas canvas) {
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, canvas);
    }

    public void startPlayingStream(String streamID, ZegoCanvas canvas, ZegoPlayerConfig config) {
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, canvas, config);
    }


    public void startPlayingStream(String streamID, ZegoPlayerConfig config) {
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, config);
    }

    public void stopPlayingStream(String streamID) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID);
    }


    public void addEventHandler(IZegoEventHandler eventHandler) {
        expressEventHandler.addEventHandler(eventHandler);
    }

    public void removeEventHandler(IZegoEventHandler eventHandler) {
        expressEventHandler.removeEventHandler(eventHandler);
    }

    public void removeEventHandlerList(List<IZegoEventHandler> list) {
        if (list.isEmpty()) {
            return;
        }
        expressEventHandler.removeEventHandlerList(list);
    }

    public void removeAllEventHandlers() {
        expressEventHandler.removeAllEventHandlers();
    }

    public void loginRoom(String roomID, ZegoUser user, ZegoRoomConfig config, IZegoRoomLoginCallback callback) {
        ZegoExpressEngine.getEngine().loginRoom(roomID, user, config, callback);
    }

    public void switchRoom(String fromRoomID, String toRoomID) {
        ZegoExpressEngine.getEngine().switchRoom(fromRoomID, toRoomID);
    }

    public void logoutRoom(IZegoRoomLogoutCallback callback) {
        ZegoExpressEngine.getEngine().logoutRoom(callback);
    }

    public void enableCamera(boolean enable) {
        ZegoExpressEngine.getEngine().enableCamera(enable);
    }

    public void muteMicrophone(boolean mute) {
        ZegoExpressEngine.getEngine().muteMicrophone(mute);
    }

    public void useFrontCamera(boolean useFront) {
        ZegoExpressEngine.getEngine().useFrontCamera(useFront);
    }

    public void setAudioRouteToSpeaker(boolean routeToSpeaker) {
        ZegoExpressEngine.getEngine().setAudioRouteToSpeaker(routeToSpeaker);
    }

    public void setRoomExtraInfo(String roomID, String key, String value, IZegoRoomSetRoomExtraInfoCallback callback) {
        ZegoExpressEngine.getEngine().setRoomExtraInfo(roomID, key, value, callback);
    }

    public void enableCustomVideoProcessing(boolean enable, ZegoCustomVideoProcessConfig config,
        ZegoPublishChannel channel) {
        ZegoExpressEngine.getEngine().enableCustomVideoProcessing(enable, config, channel);
    }

    public void setCustomVideoProcessHandler(IZegoCustomVideoProcessHandler handler) {
        ZegoExpressEngine.getEngine().setCustomVideoProcessHandler(handler);
    }

    public ZegoVideoConfig getVideoConfig() {
        return ZegoExpressEngine.getEngine().getVideoConfig();
    }

    public void sendCustomVideoProcessedTextureData(int textureID, int width, int height,
        long referenceTimeMillisecond) {
        ZegoExpressEngine.getEngine()
            .sendCustomVideoProcessedTextureData(textureID, width, height, referenceTimeMillisecond);
    }

    public void sendCustomCommand(String roomID, String command, ArrayList<ZegoUser> toUserList,
        IZegoIMSendCustomCommandCallback callback) {
        ZegoExpressEngine.getEngine().sendCustomCommand(roomID, command, toUserList, callback);
    }

    public void sendBarrageMessage(String roomID, String message, IZegoIMSendBarrageMessageCallback callback) {
        if (ZegoExpressEngine.getEngine() == null) {
            return;
        }
        ZegoExpressEngine.getEngine().sendBarrageMessage(roomID, message, callback);
    }

    public void mutePlayStreamAudio(String streamID, boolean mute) {
        ZegoExpressEngine.getEngine().mutePlayStreamAudio(streamID, mute);
    }

    public void mutePlayStreamVideo(String streamID, boolean mute) {
        ZegoExpressEngine.getEngine().mutePlayStreamVideo(streamID, mute);
    }

    public void startMixerTask(ZegoMixerTask task, IZegoMixerStartCallback callback) {
        ZegoExpressEngine.getEngine().startMixerTask(task, callback);
    }

    public void stopMixerTask(ZegoMixerTask task, IZegoMixerStopCallback callback) {
        ZegoExpressEngine.getEngine().stopMixerTask(task, callback);
    }

    public void setRoomScenario(ZegoScenario scenario) {
        ZegoExpressEngine.getEngine().setRoomScenario(scenario);
    }

    public void setStreamExtraInfo(String extraInfo, IZegoPublisherSetStreamExtraInfoCallback callback) {
        ZegoExpressEngine.getEngine().setStreamExtraInfo(extraInfo, callback);
    }

    public void uploadLog(IZegoUploadLogResultCallback callback) {
        ZegoExpressEngine.getEngine().uploadLog(callback);
    }

    public void setVideoConfig(ZegoVideoConfig videoConfig) {
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig);
    }

    public void setVideoConfig(ZegoVideoConfig videoConfig, ZegoPublishChannel publishChannel) {
        ZegoExpressEngine.getEngine().setVideoConfig(videoConfig, publishChannel);
    }

    public ZegoMediaPlayer createMediaPlayer() {
        return ZegoExpressEngine.getEngine().createMediaPlayer();
    }

    public void destroyMediaPlayer(ZegoMediaPlayer mediaPlayer) {
        ZegoExpressEngine.getEngine().destroyMediaPlayer(mediaPlayer);
    }
}
