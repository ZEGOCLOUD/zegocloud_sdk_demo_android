package com.zegocloud.demo.bestpractice.internal.sdk.express;

import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoAudioRoute;
import im.zego.zegoexpress.constants.ZegoAudioVADStableStateMonitorType;
import im.zego.zegoexpress.constants.ZegoAudioVADType;
import im.zego.zegoexpress.constants.ZegoDeviceExceptionType;
import im.zego.zegoexpress.constants.ZegoDeviceType;
import im.zego.zegoexpress.constants.ZegoEngineState;
import im.zego.zegoexpress.constants.ZegoNetworkMode;
import im.zego.zegoexpress.constants.ZegoNetworkSpeedTestType;
import im.zego.zegoexpress.constants.ZegoObjectSegmentationState;
import im.zego.zegoexpress.constants.ZegoPlayerMediaEvent;
import im.zego.zegoexpress.constants.ZegoPlayerState;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zegoexpress.constants.ZegoRemoteDeviceState;
import im.zego.zegoexpress.constants.ZegoRoomState;
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason;
import im.zego.zegoexpress.constants.ZegoScreenCaptureExceptionType;
import im.zego.zegoexpress.constants.ZegoStreamEvent;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoSuperResolutionState;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.constants.ZegoVideoCodecID;
import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoBroadcastMessageInfo;
import im.zego.zegoexpress.entity.ZegoMediaSideInfo;
import im.zego.zegoexpress.entity.ZegoNetworkSpeedTestQuality;
import im.zego.zegoexpress.entity.ZegoPerformanceStatus;
import im.zego.zegoexpress.entity.ZegoPlayStreamQuality;
import im.zego.zegoexpress.entity.ZegoPublishStreamQuality;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zegoexpress.entity.ZegoSoundLevelInfo;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zegoexpress.entity.ZegoStreamRelayCDNInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;

class SimpleExpressEventHandler extends IZegoEventHandler {

    private List<IZegoEventHandler> handlerList = new CopyOnWriteArrayList<>();

    @Override
    public void onDebugError(int errorCode, String funcName, String info) {
        super.onDebugError(errorCode, funcName, info);
        for (IZegoEventHandler handler : handlerList) {
            handler.onDebugError(errorCode, funcName, info);
        }
    }

    @Override
    public void onEngineStateUpdate(ZegoEngineState state) {
        super.onEngineStateUpdate(state);
        for (IZegoEventHandler handler : handlerList) {
            handler.onEngineStateUpdate(state);
        }
    }

    @Override
    public void onRecvExperimentalAPI(String content) {
        super.onRecvExperimentalAPI(content);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRecvExperimentalAPI(content);
        }
    }

    @Override
    public void onFatalError(int errorCode) {
        super.onFatalError(errorCode);
        for (IZegoEventHandler handler : handlerList) {
            handler.onFatalError(errorCode);
        }
    }

    @Override
    public void onRoomStateUpdate(String roomID, ZegoRoomState state, int errorCode, JSONObject extendedData) {
        super.onRoomStateUpdate(roomID, state, errorCode, extendedData);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomStateUpdate(roomID, state, errorCode, extendedData);
        }
    }

    @Override
    public void onRoomStateChanged(String roomID, ZegoRoomStateChangedReason reason, int errorCode,
        JSONObject extendedData) {
        super.onRoomStateChanged(roomID, reason, errorCode, extendedData);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomStateChanged(roomID, reason, errorCode, extendedData);
        }
    }

    @Override
    public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
        super.onRoomUserUpdate(roomID, updateType, userList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomUserUpdate(roomID, updateType, userList);
        }
    }

    @Override
    public void onRoomOnlineUserCountUpdate(String roomID, int count) {
        super.onRoomOnlineUserCountUpdate(roomID, count);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomOnlineUserCountUpdate(roomID, count);
        }
    }

    @Override
    public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
        JSONObject extendedData) {
        super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
        }
    }

    @Override
    public void onRoomStreamExtraInfoUpdate(String roomID, ArrayList<ZegoStream> streamList) {
        super.onRoomStreamExtraInfoUpdate(roomID, streamList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomStreamExtraInfoUpdate(roomID, streamList);
        }
    }

    @Override
    public void onRoomExtraInfoUpdate(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
        super.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomExtraInfoUpdate(roomID, roomExtraInfoList);
        }
    }

    @Override
    public void onRoomTokenWillExpire(String roomID, int remainTimeInSecond) {
        super.onRoomTokenWillExpire(roomID, remainTimeInSecond);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRoomTokenWillExpire(roomID, remainTimeInSecond);
        }
    }

    @Override
    public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
        JSONObject extendedData) {
        super.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherStateUpdate(streamID, state, errorCode, extendedData);
        }
    }

    @Override
    public void onPublisherQualityUpdate(String streamID, ZegoPublishStreamQuality quality) {
        super.onPublisherQualityUpdate(streamID, quality);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherQualityUpdate(streamID, quality);
        }
    }

    @Override
    public void onPublisherCapturedAudioFirstFrame() {
        super.onPublisherCapturedAudioFirstFrame();
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherCapturedAudioFirstFrame();
        }
    }

    @Override
    public void onPublisherCapturedVideoFirstFrame(ZegoPublishChannel channel) {
        super.onPublisherCapturedVideoFirstFrame(channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherCapturedVideoFirstFrame(channel);
        }
    }

    @Override
    public void onPublisherSendAudioFirstFrame(ZegoPublishChannel channel) {
        super.onPublisherSendAudioFirstFrame(channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherSendAudioFirstFrame(channel);
        }
    }

    @Override
    public void onPublisherSendVideoFirstFrame(ZegoPublishChannel channel) {
        super.onPublisherSendVideoFirstFrame(channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherSendVideoFirstFrame(channel);
        }
    }

    @Override
    public void onPublisherRenderVideoFirstFrame(ZegoPublishChannel channel) {
        super.onPublisherRenderVideoFirstFrame(channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherRenderVideoFirstFrame(channel);
        }
    }

    @Override
    public void onPublisherVideoSizeChanged(int width, int height, ZegoPublishChannel channel) {
        super.onPublisherVideoSizeChanged(width, height, channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherVideoSizeChanged(width, height, channel);
        }
    }

    @Override
    public void onPublisherRelayCDNStateUpdate(String streamID, ArrayList<ZegoStreamRelayCDNInfo> infoList) {
        super.onPublisherRelayCDNStateUpdate(streamID, infoList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherRelayCDNStateUpdate(streamID, infoList);
        }
    }

    @Override
    public void onPublisherVideoEncoderChanged(ZegoVideoCodecID fromCodecID, ZegoVideoCodecID toCodecID,
        ZegoPublishChannel channel) {
        super.onPublisherVideoEncoderChanged(fromCodecID, toCodecID, channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherVideoEncoderChanged(fromCodecID, toCodecID, channel);
        }
    }

    @Override
    public void onPublisherStreamEvent(ZegoStreamEvent eventID, String streamID, String extraInfo) {
        super.onPublisherStreamEvent(eventID, streamID, extraInfo);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherStreamEvent(eventID, streamID, extraInfo);
        }
    }

    @Override
    public void onVideoObjectSegmentationStateChanged(ZegoObjectSegmentationState state, ZegoPublishChannel channel,
        int errorCode) {
        super.onVideoObjectSegmentationStateChanged(state, channel, errorCode);
        for (IZegoEventHandler handler : handlerList) {
            handler.onVideoObjectSegmentationStateChanged(state, channel, errorCode);
        }
    }

    @Override
    public void onPlayerStateUpdate(String streamID, ZegoPlayerState state, int errorCode, JSONObject extendedData) {
        super.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerStateUpdate(streamID, state, errorCode, extendedData);
        }
    }

    @Override
    public void onPlayerQualityUpdate(String streamID, ZegoPlayStreamQuality quality) {
        super.onPlayerQualityUpdate(streamID, quality);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerQualityUpdate(streamID, quality);
        }
    }

    @Override
    public void onPlayerMediaEvent(String streamID, ZegoPlayerMediaEvent event) {
        super.onPlayerMediaEvent(streamID, event);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerMediaEvent(streamID, event);
        }
    }

    @Override
    public void onPlayerRecvAudioFirstFrame(String streamID) {
        super.onPlayerRecvAudioFirstFrame(streamID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRecvAudioFirstFrame(streamID);
        }
    }

    @Override
    public void onPlayerRecvVideoFirstFrame(String streamID) {
        super.onPlayerRecvVideoFirstFrame(streamID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRecvVideoFirstFrame(streamID);
        }
    }

    @Override
    public void onPlayerRenderVideoFirstFrame(String streamID) {
        super.onPlayerRenderVideoFirstFrame(streamID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRenderVideoFirstFrame(streamID);
        }
    }

    @Override
    public void onPlayerRenderCameraVideoFirstFrame(String streamID) {
        super.onPlayerRenderCameraVideoFirstFrame(streamID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRenderCameraVideoFirstFrame(streamID);
        }
    }

    @Override
    public void onPlayerVideoSizeChanged(String streamID, int width, int height) {
        super.onPlayerVideoSizeChanged(streamID, width, height);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerVideoSizeChanged(streamID, width, height);
        }
    }

    @Override
    public void onPlayerRecvSEI(String streamID, byte[] data) {
        super.onPlayerRecvSEI(streamID, data);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRecvSEI(streamID, data);
        }
    }

    @Override
    public void onPlayerSyncRecvSEI(String streamID, byte[] data) {
        super.onPlayerSyncRecvSEI(streamID, data);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerSyncRecvSEI(streamID, data);
        }
    }

    @Override
    public void onPlayerRecvAudioSideInfo(String streamID, byte[] data) {
        super.onPlayerRecvAudioSideInfo(streamID, data);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRecvAudioSideInfo(streamID, data);
        }
    }

    @Override
    public void onPlayerLowFpsWarning(ZegoVideoCodecID codecID, String streamID) {
        super.onPlayerLowFpsWarning(codecID, streamID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerLowFpsWarning(codecID, streamID);
        }
    }

    @Override
    public void onPlayerStreamEvent(ZegoStreamEvent eventID, String streamID, String extraInfo) {
        super.onPlayerStreamEvent(eventID, streamID, extraInfo);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerStreamEvent(eventID, streamID, extraInfo);
        }
    }

    @Override
    public void onPlayerVideoSuperResolutionUpdate(String streamID, ZegoSuperResolutionState state, int errorCode) {
        super.onPlayerVideoSuperResolutionUpdate(streamID, state, errorCode);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerVideoSuperResolutionUpdate(streamID, state, errorCode);
        }
    }

    @Override
    public void onMixerRelayCDNStateUpdate(String taskID, ArrayList<ZegoStreamRelayCDNInfo> infoList) {
        super.onMixerRelayCDNStateUpdate(taskID, infoList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onMixerRelayCDNStateUpdate(taskID, infoList);
        }
    }

    @Override
    public void onMixerSoundLevelUpdate(HashMap<Integer, Float> soundLevels) {
        super.onMixerSoundLevelUpdate(soundLevels);
        for (IZegoEventHandler handler : handlerList) {
            handler.onMixerSoundLevelUpdate(soundLevels);
        }
    }

    @Override
    public void onAutoMixerSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        super.onAutoMixerSoundLevelUpdate(soundLevels);
        for (IZegoEventHandler handler : handlerList) {
            handler.onAutoMixerSoundLevelUpdate(soundLevels);
        }
    }

    @Override
    public void onCapturedSoundLevelUpdate(float soundLevel) {
        super.onCapturedSoundLevelUpdate(soundLevel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onCapturedSoundLevelUpdate(soundLevel);
        }
    }

    @Override
    public void onCapturedSoundLevelInfoUpdate(ZegoSoundLevelInfo soundLevelInfo) {
        super.onCapturedSoundLevelInfoUpdate(soundLevelInfo);
        for (IZegoEventHandler handler : handlerList) {
            handler.onCapturedSoundLevelInfoUpdate(soundLevelInfo);
        }
    }

    @Override
    public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
        super.onRemoteSoundLevelUpdate(soundLevels);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRemoteSoundLevelUpdate(soundLevels);
        }
    }

    @Override
    public void onRemoteSoundLevelInfoUpdate(HashMap<String, ZegoSoundLevelInfo> soundLevelInfos) {
        super.onRemoteSoundLevelInfoUpdate(soundLevelInfos);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRemoteSoundLevelInfoUpdate(soundLevelInfos);
        }
    }

    @Override
    public void onCapturedAudioSpectrumUpdate(float[] audioSpectrum) {
        super.onCapturedAudioSpectrumUpdate(audioSpectrum);
        for (IZegoEventHandler handler : handlerList) {
            handler.onCapturedAudioSpectrumUpdate(audioSpectrum);
        }
    }

    @Override
    public void onRemoteAudioSpectrumUpdate(HashMap<String, float[]> audioSpectrums) {
        super.onRemoteAudioSpectrumUpdate(audioSpectrums);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRemoteAudioSpectrumUpdate(audioSpectrums);
        }
    }

    @Override
    public void onLocalDeviceExceptionOccurred(ZegoDeviceExceptionType exceptionType, ZegoDeviceType deviceType,
        String deviceID) {
        super.onLocalDeviceExceptionOccurred(exceptionType, deviceType, deviceID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onLocalDeviceExceptionOccurred(exceptionType, deviceType, deviceID);
        }
    }

    @Override
    public void onRemoteCameraStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        super.onRemoteCameraStateUpdate(streamID, state);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRemoteCameraStateUpdate(streamID, state);
        }
    }

    @Override
    public void onRemoteMicStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        super.onRemoteMicStateUpdate(streamID, state);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRemoteMicStateUpdate(streamID, state);
        }
    }

    @Override
    public void onRemoteSpeakerStateUpdate(String streamID, ZegoRemoteDeviceState state) {
        super.onRemoteSpeakerStateUpdate(streamID, state);
        for (IZegoEventHandler handler : handlerList) {
            handler.onRemoteSpeakerStateUpdate(streamID, state);
        }
    }

    @Override
    public void onAudioRouteChange(ZegoAudioRoute audioRoute) {
        super.onAudioRouteChange(audioRoute);
        for (IZegoEventHandler handler : handlerList) {
            handler.onAudioRouteChange(audioRoute);
        }
    }

    @Override
    public void onAudioVADStateUpdate(ZegoAudioVADStableStateMonitorType type, ZegoAudioVADType state) {
        super.onAudioVADStateUpdate(type, state);
        for (IZegoEventHandler handler : handlerList) {
            handler.onAudioVADStateUpdate(type, state);
        }
    }

    @Override
    public void onIMRecvBroadcastMessage(String roomID, ArrayList<ZegoBroadcastMessageInfo> messageList) {
        super.onIMRecvBroadcastMessage(roomID, messageList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onIMRecvBroadcastMessage(roomID, messageList);
        }
    }

    @Override
    public void onIMRecvBarrageMessage(String roomID, ArrayList<ZegoBarrageMessageInfo> messageList) {
        super.onIMRecvBarrageMessage(roomID, messageList);
        for (IZegoEventHandler handler : handlerList) {
            handler.onIMRecvBarrageMessage(roomID, messageList);
        }
    }

    @Override
    public void onIMRecvCustomCommand(String roomID, ZegoUser fromUser, String command) {
        super.onIMRecvCustomCommand(roomID, fromUser, command);
        for (IZegoEventHandler handler : handlerList) {
            handler.onIMRecvCustomCommand(roomID, fromUser, command);
        }
    }

    @Override
    public void onPerformanceStatusUpdate(ZegoPerformanceStatus status) {
        super.onPerformanceStatusUpdate(status);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPerformanceStatusUpdate(status);
        }
    }

    @Override
    public void onNetworkModeChanged(ZegoNetworkMode mode) {
        super.onNetworkModeChanged(mode);
        for (IZegoEventHandler handler : handlerList) {
            handler.onNetworkModeChanged(mode);
        }
    }

    @Override
    public void onNetworkSpeedTestError(int errorCode, ZegoNetworkSpeedTestType type) {
        super.onNetworkSpeedTestError(errorCode, type);
        for (IZegoEventHandler handler : handlerList) {
            handler.onNetworkSpeedTestError(errorCode, type);
        }
    }

    @Override
    public void onNetworkSpeedTestQualityUpdate(ZegoNetworkSpeedTestQuality quality, ZegoNetworkSpeedTestType type) {
        super.onNetworkSpeedTestQualityUpdate(quality, type);
        for (IZegoEventHandler handler : handlerList) {
            handler.onNetworkSpeedTestQualityUpdate(quality, type);
        }
    }

    @Override
    public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality,
        ZegoStreamQualityLevel downstreamQuality) {
        super.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
        for (IZegoEventHandler handler : handlerList) {
            handler.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
        }
    }

    @Override
    public void onNetworkTimeSynchronized() {
        super.onNetworkTimeSynchronized();
        for (IZegoEventHandler handler : handlerList) {
            handler.onNetworkTimeSynchronized();
        }
    }

    @Override
    public void onScreenCaptureExceptionOccurred(ZegoScreenCaptureExceptionType exceptionType) {
        super.onScreenCaptureExceptionOccurred(exceptionType);
        for (IZegoEventHandler handler : handlerList) {
            handler.onScreenCaptureExceptionOccurred(exceptionType);
        }
    }

    @Override
    public void onPlayerRecvMediaSideInfo(ZegoMediaSideInfo info) {
        super.onPlayerRecvMediaSideInfo(info);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerRecvMediaSideInfo(info);
        }
    }

    @Override
    public void onPlayerSyncRecvVideoFirstFrame(String streamID) {
        super.onPlayerSyncRecvVideoFirstFrame(streamID);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPlayerSyncRecvVideoFirstFrame(streamID);
        }
    }

    @Override
    public void onPublisherDummyCaptureImagePathError(int errorCode, String path, ZegoPublishChannel channel) {
        super.onPublisherDummyCaptureImagePathError(errorCode, path, channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherDummyCaptureImagePathError(errorCode, path, channel);
        }
    }

    @Override
    public void onPublisherLowFpsWarning(ZegoVideoCodecID codecID, ZegoPublishChannel channel) {
        super.onPublisherLowFpsWarning(codecID, channel);
        for (IZegoEventHandler handler : handlerList) {
            handler.onPublisherLowFpsWarning(codecID, channel);
        }
    }

    public void addEventHandler(IZegoEventHandler eventHandler) {
        handlerList.add(eventHandler);
    }

    public void removeEventHandler(IZegoEventHandler eventHandler) {
        handlerList.remove(eventHandler);
    }

    public void removeEventHandlerList(List<IZegoEventHandler> list) {
        handlerList.removeAll(list);
    }

    public void removeAllEventHandlers() {
        handlerList.clear();
    }
}
