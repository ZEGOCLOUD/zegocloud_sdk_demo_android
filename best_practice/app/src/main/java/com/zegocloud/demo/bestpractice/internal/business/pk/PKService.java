package com.zegocloud.demo.bestpractice.internal.business.pk;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.callback.IZegoMixerStopCallback;
import im.zego.zegoexpress.constants.ZegoMixRenderMode;
import im.zego.zegoexpress.constants.ZegoMixerInputContentType;
import im.zego.zegoexpress.entity.ZegoMixerInput;
import im.zego.zegoexpress.entity.ZegoMixerOutput;
import im.zego.zegoexpress.entity.ZegoMixerTask;
import im.zego.zegoexpress.entity.ZegoMixerVideoConfig;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallRejectionSentCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallInvitationSentInfo;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallRejectConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class PKService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable syncSEIRunnable;
    private Runnable checkSEIRunnable;
    private PKInfo currentPKInfo;
    private Map<String, Long> seiTimeMap = new HashMap<>();
    private Map<String, Boolean> seiStateMap = new HashMap<>();
    private Map<String, String> roomProperties = new HashMap<>();
    private PKRequest sendPKStartRequest;
    private PKRequest recvPKStartRequest;
    private boolean hasNotified = false;
    private boolean mutePKUser = false;
    private ZegoMixerTask task;
    private IZIMEventHandler zimEventHandler;
    private List<PKListener> listenerList = new ArrayList<>();

    public void initWhenUserLogin() {
        syncSEIRunnable = new Runnable() {
            @Override
            public void run() {
                JSONObject jsonObject = new JSONObject();
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                boolean cameraEnabled = ZEGOSDKManager.getInstance().expressService.isCameraOpen();
                boolean microphoneOpen = ZEGOSDKManager.getInstance().expressService.isMicrophoneOpen();
                try {
                    jsonObject.put("type", 0);
                    jsonObject.put("sender_id", localUser.userID);
                    jsonObject.put("cam", cameraEnabled);
                    jsonObject.put("mic", microphoneOpen);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                ZEGOSDKManager.getInstance().expressService.sendSEI(jsonObject.toString());

                handler.postDelayed(syncSEIRunnable, 500);
            }
        };

        checkSEIRunnable = new Runnable() {
            @Override
            public void run() {
                for (Entry<String, Long> entry : seiTimeMap.entrySet()) {
                    String userID = entry.getKey();
                    Long timeStamp = entry.getValue();
                    boolean isTimeOut = System.currentTimeMillis() - timeStamp > 5000;
                    if (seiStateMap.containsKey(userID)) {
                        boolean lastTimeOutState = seiStateMap.get(userID);
                        seiStateMap.put(userID, isTimeOut);
                        if (isTimeOut != lastTimeOutState) {
                            for (PKListener listener : listenerList) {
                                listener.onPKSEITimeOut(userID, isTimeOut);
                            }
                        }
                    } else {
                        seiStateMap.put(userID, isTimeOut);
                        for (PKListener listener : listenerList) {
                            listener.onPKSEITimeOut(userID, isTimeOut);
                        }
                    }
                }
                handler.postDelayed(checkSEIRunnable, 1000);
            }
        };

        zimEventHandler = new IZIMEventHandler() {
            @Override
            public void onInComingUserRequestReceived(String requestID, String inviter, String extendedData) {
                PKExtendedData pkExtendedData = PKExtendedData.parse(extendedData);
                if (pkExtendedData != null) {
                    if (pkExtendedData.type == PKExtendedData.START_PK) {
                        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                        boolean userNotHost =
                            TextUtils.isEmpty(currentRoomID) || (!ZEGOLiveStreamingManager.getInstance()
                                .isCurrentUserHost());
                        boolean inPKStartRequest = sendPKStartRequest != null || recvPKStartRequest != null;
                        if (userNotHost || (currentPKInfo != null) || inPKStartRequest) {
                            rejectPKBattleStartRequest(requestID);
                            return;
                        }
                        recvPKStartRequest = new PKRequest();
                        recvPKStartRequest.requestID = requestID;
                        recvPKStartRequest.targetUserID = inviter;

                        for (PKListener listener : listenerList) {
                            listener.onReceiveStartPKRequest(requestID, inviter, pkExtendedData.userName,
                                pkExtendedData.roomID);
                        }
                    } else if (pkExtendedData.type == PKExtendedData.END_PK) {
                        acceptPKBattleStopRequest(requestID);
                        if (currentPKInfo == null) {
                            return;
                        }
                        for (PKListener listener : listenerList) {
                            listener.onReceiveStopPKRequest(requestID);
                        }
                        stopPKBattle();
                    }
                    if (pkExtendedData.type == PKExtendedData.RESUME_PK) {
                        // already stopped PK
                        if (currentPKInfo == null) {
                            rejectPKBattleResumeRequest(requestID);
                        } else {
                            acceptPKBattleResumeRequest(requestID);
                        }
                    }
                }
            }

            @Override
            public void onInComingUserRequestTimeout(String requestID) {
                if (recvPKStartRequest != null && requestID.equals(recvPKStartRequest.requestID)) {
                    recvPKStartRequest = null;
                    for (PKListener listener : listenerList) {
                        listener.onInComingStartPKRequestTimeout(requestID);
                    }
                }
            }

            @Override
            public void onInComingUserRequestCancelled(String requestID, String inviter, String extendedData) {
                if (recvPKStartRequest != null && requestID.equals(recvPKStartRequest.requestID)) {
                    recvPKStartRequest = null;
                    for (PKListener listener : listenerList) {
                        listener.onInComingStartPKRequestCancelled(requestID);
                    }
                }
            }

            @Override
            public void onOutgoingUserRequestTimeout(String requestID) {
                if (sendPKStartRequest != null && requestID.equals(sendPKStartRequest.requestID)) {
                    sendPKStartRequest = null;
                    for (PKListener listener : listenerList) {
                        listener.onOutgoingStartPKRequestTimeout();
                    }
                }
            }

            @Override
            public void onOutgoingUserRequestAccepted(String requestID, String invitee, String extendedData) {
                PKExtendedData pkExtendedData = PKExtendedData.parse(extendedData);
                if (pkExtendedData != null) {
                    if (pkExtendedData.type == PKExtendedData.START_PK) {
                        sendPKStartRequest = null;
                        PKInfo pkInfo = new PKInfo(new ZEGOSDKUser(invitee, pkExtendedData.userName),
                            pkExtendedData.roomID);
                        setCurrentPKInfo(pkInfo);
                        startPKBattle();
                    } else if (pkExtendedData.type == PKExtendedData.END_PK) {
                        stopPKBattle();
                    } else if (pkExtendedData.type == PKExtendedData.RESUME_PK) {
                        // re enter room
                        String pk_room = roomProperties.get("pk_room");
                        String pk_user_id = roomProperties.get("pk_user_id");
                        String pk_user_name = roomProperties.get("pk_user_name");
                        String pk_seq = roomProperties.get("pk_seq");
                        String host = roomProperties.get("host");
                        PKInfo pkInfo = new PKInfo(new ZEGOSDKUser(pk_user_id, pk_user_name), pk_room);
                        pkInfo.hostUserID = host;
                        pkInfo.seq = Long.parseLong(pk_seq);
                        setCurrentPKInfo(pkInfo);
                        startPKBattle();
                    }
                }
            }

            @Override
            public void onOutgoingUserRequestRejected(String requestID, String invitee, String extendedData) {
                if (sendPKStartRequest != null && requestID.equals(sendPKStartRequest.requestID)) {
                    sendPKStartRequest = null;
                    for (PKListener listener : listenerList) {
                        listener.onOutgoingStartPKRequestRejected();
                    }
                } else {
                    // if is resume be rejected,need to delete room attributes
                    deletePKRoomAttributes();
                }
            }
        };
        ZEGOSDKManager.getInstance().zimService.addEventHandler(zimEventHandler, false);
    }


    private void onReceivePKRoomAttribute(Map<String, String> roomProperties) {
        String pk_user_id = roomProperties.get("pk_user_id");
        String pk_user_name = roomProperties.get("pk_user_name");
        String pk_room = roomProperties.get("pk_room");
        String pk_seq = roomProperties.get("pk_seq");
        String host = roomProperties.get("host");

        PKInfo pkInfo = new PKInfo(new ZEGOSDKUser(pk_user_id, pk_user_name), pk_room);
        pkInfo.hostUserID = host;
        pkInfo.seq = Long.parseLong(pk_seq);

        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            // receive attribute but no pkInfo, resume PK
            if (currentPKInfo == null) {
                sendPKBattleResumeRequest(pk_user_id);
            }
            seiTimeMap.put(pk_user_id, System.currentTimeMillis());
        } else {
            seiTimeMap.put(host, System.currentTimeMillis());
            seiTimeMap.put(pk_user_id, System.currentTimeMillis());
            if (currentPKInfo == null) {
                // normal，audience receive Host PK action
                if (ZEGOLiveStreamingManager.getInstance().getHostUser() != null) {
                    setCurrentPKInfo(pkInfo);
                    startPKBattle();
                }
            }
        }
    }

    private void updatePKDeviceState(ZEGOSDKUser zegosdkUser, boolean isMicOpen, boolean isCameraOpen) {
        boolean micChanged = zegosdkUser.isMicrophoneOpen() != isMicOpen;
        if (micChanged) {
            zegosdkUser.setMicrophoneOpen(isMicOpen);
            handler.post(() -> {
                for (PKListener listener : listenerList) {
                    listener.onPKMicrophoneOpen(zegosdkUser.userID, isMicOpen);
                }
            });
        }
        boolean camChanged = zegosdkUser.isCameraOpen() != isCameraOpen;
        if (camChanged) {
            zegosdkUser.setCameraOpen(isCameraOpen);
            handler.post(() -> {
                for (PKListener listener : listenerList) {
                    listener.onPKCameraOpen(zegosdkUser.userID, isCameraOpen);
                }
            });
        }
    }

    public boolean isPKUser(String userID) {
        if (currentPKInfo == null) {
            return false;
        } else {
            return currentPKInfo.pkUser.userID.equals(userID);
        }
    }

    @NonNull
    private String getPKExtendedData(int type) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        PKExtendedData data = new PKExtendedData();
        data.roomID = currentRoomID;
        data.userName = localUser.userName;
        data.type = type;
        return data.toString();
    }

    public void sendPKBattlesStartRequest(String targetUserID, UserRequestCallback callback) {
        sendPKStartRequest = new PKRequest();
        String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
        sendUserRequest(targetUserID, pkExtendedData, new ZIMCallInvitationSentCallback() {
            @Override
            public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info, ZIMError errorInfo) {
                if (errorInfo.code.value() == 0) {
                    sendPKStartRequest.requestID = requestID;
                    sendPKStartRequest.targetUserID = targetUserID;
                } else {
                    sendPKStartRequest = null;
                }
                if (callback != null) {
                    callback.onUserRequestSend(errorInfo.code.value(), requestID);
                }
            }
        });
    }

    public void acceptPKBattleStartRequest(String requestID) {
        if (recvPKStartRequest != null && requestID.equals(recvPKStartRequest.requestID)) {
            recvPKStartRequest = null;
        }

        String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
        acceptUserRequest(requestID, pkExtendedData, new ZIMCallAcceptanceSentCallback() {
            @Override
            public void onCallAcceptanceSent(String callID, ZIMError errorInfo) {
                if (errorInfo.code.value() == 0) {
                    startPKBattle();
                } else {
                    sendPKBattlesStopRequestInner();
                }
            }
        });
    }

    public void rejectPKBattleStartRequest(String requestID) {
        if (recvPKStartRequest != null && requestID.equals(recvPKStartRequest.requestID)) {
            recvPKStartRequest = null;
        }

        String pkExtendedData = getPKExtendedData(PKExtendedData.START_PK);
        rejectUserRequest(requestID, pkExtendedData, null);
    }

    public void cancelPKBattleStartRequest(String requestID, String userID) {
        sendPKStartRequest = null;
        cancelUserRequest(userID, requestID, "", null);
    }

    public void sendPKBattlesStopRequest() {
        if (currentPKInfo != null) {
            sendPKBattlesStopRequestInner();
            stopPKBattle(); // end right now,no need to wait for answer because there
        }
    }

    private void sendPKBattlesStopRequestInner() {
        if (currentPKInfo != null) {
            String pkExtendedData = getPKExtendedData(PKExtendedData.END_PK);
            sendUserRequest(currentPKInfo.pkUser.userID, pkExtendedData, null);
            setCurrentPKInfo(null);
        }
    }

    public void sendPKBattleResumeRequest(String pk_user_id) {
        List<String> list = Collections.singletonList(pk_user_id);
        ZIMCallInviteConfig config = new ZIMCallInviteConfig();
        config.extendedData = getPKExtendedData(PKExtendedData.RESUME_PK);
        ZEGOSDKManager.getInstance().zimService.sendUserRequest(list, config, null);
    }

    public void rejectPKBattleResumeRequest(String requestID) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.RESUME_PK);
        rejectUserRequest(requestID, pkExtendedData, null);
    }

    public void acceptPKBattleResumeRequest(String requestID) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.RESUME_PK);
        acceptUserRequest(requestID, pkExtendedData, null);
        startMixStreamTask(false, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    setPKRoomAttributes();
                } else {
                    sendPKBattlesStopRequestInner();
                }
            }
        });
    }

    public void acceptPKBattleStopRequest(String requestID) {
        String pkExtendedData = getPKExtendedData(PKExtendedData.END_PK);
        acceptUserRequest(requestID, pkExtendedData, null);
    }

    public void setCurrentPKInfo(PKInfo currentPKInfo) {
        this.currentPKInfo = currentPKInfo;
    }

    public PKInfo getPKInfo() {
        return currentPKInfo;
    }

    public PKRequest getSendPKStartRequest() {
        return sendPKStartRequest;
    }

    private void startPKBattle() {
        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            startMixStreamTask(false, new IZegoMixerStartCallback() {
                @Override
                public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                    if (errorCode == 0) {
                        syncDeviceStatus();
                        setPKRoomAttributes();
                        hasNotified = false;

                        checkPKUserSEI();
                        for (PKListener listener : listenerList) {
                            listener.onPKStarted();
                        }
                    } else {
                        sendPKBattlesStopRequestInner();
                    }
                }
            });
        } else {
            hasNotified = false;

            checkPKUserSEI();
            for (PKListener listener : listenerList) {
                listener.onPKStarted();
            }
        }
    }


    private void setPKRoomAttributes() {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("host", localUser.userID);
        hashMap.put("pk_room", currentPKInfo.pkRoom);
        hashMap.put("pk_user_id", currentPKInfo.pkUser.userID);
        hashMap.put("pk_user_name", currentPKInfo.pkUser.userName);
        hashMap.put("pk_seq", String.valueOf(currentPKInfo.seq + 1));

        ZIMRoomAttributesSetConfig config = new ZIMRoomAttributesSetConfig();
        config.isDeleteAfterOwnerLeft = false;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes(hashMap, config,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {

                }
            });
    }

    public void stopPKBattle() {
        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            deletePKRoomAttributes();
            stopMixTask();
            stopSyncDeviceStatus();
        } else {
            muteHostAudioVideo(false);
        }

        setCurrentPKInfo(null);

        stopCheckPKUserSEI();

        hasNotified = false;
        mutePKUser = false;
        seiTimeMap.clear();
        seiStateMap.clear();

        for (PKListener listener : listenerList) {
            listener.onPKEnded();
        }
    }

    public void muteHostAudioVideo(boolean mute) {
        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        if (hostUser != null) {
            String hostMainStreamID = hostUser.getMainStreamID();
            ZEGOSDKManager.getInstance().expressService.mutePlayStreamAudio(hostMainStreamID, mute);
            ZEGOSDKManager.getInstance().expressService.mutePlayStreamVideo(hostMainStreamID, mute);
        }
    }

    private void deletePKRoomAttributes() {
        if (roomProperties != null && !roomProperties.isEmpty()) {
            String pk_user_id = roomProperties.get("pk_user_id");
            if (!TextUtils.isEmpty(pk_user_id)) {
                List<String> keys = new ArrayList<>();
                keys.add("host");
                keys.add("pk_room");
                keys.add("pk_user_id");
                keys.add("pk_user_name");
                keys.add("pk_seq");
                ZEGOSDKManager.getInstance().zimService.deleteRoomAttributes(keys,
                    new ZIMRoomAttributesOperatedCallback() {
                        @Override
                        public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys,
                            ZIMError errorInfo) {
                        }
                    });
            }
        }
    }

    private void startMixStreamTask(boolean muteAudio, IZegoMixerStartCallback callback) {
        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        String mixStreamID = currentRoomID + "_mix";
        if (task == null) {
            task = new ZegoMixerTask(mixStreamID);
            task.enableSoundLevel(true);
        }

        ZegoMixerVideoConfig videoConfig = new ZegoMixerVideoConfig();
        videoConfig.width = 1080;
        videoConfig.height = 960;
        task.videoConfig = videoConfig;

        ArrayList<ZegoMixerInput> inputList = new ArrayList<>();
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(localUser.userID, roomID);
        ZegoMixerInput input_1 = new ZegoMixerInput(streamID, ZegoMixerInputContentType.VIDEO,
            new Rect(0, 0, 540, 960));
        input_1.renderMode = ZegoMixRenderMode.FILL;
        inputList.add(input_1);
        ZegoMixerInput input_2 = new ZegoMixerInput(currentPKInfo.getPKStream(), ZegoMixerInputContentType.VIDEO,
            new Rect(540, 0, 1080, 960));
        input_2.renderMode = ZegoMixRenderMode.FILL;
        if (muteAudio) {
            input_2.contentType = ZegoMixerInputContentType.VIDEO_ONLY;
        } else {
            input_2.contentType = ZegoMixerInputContentType.VIDEO;
        }
        inputList.add(input_2);
        task.setInputList(inputList);

        ZegoMixerOutput mixerOutput = new ZegoMixerOutput(mixStreamID);
        ArrayList<ZegoMixerOutput> mixerOutputList = new ArrayList<>();
        mixerOutputList.add(mixerOutput);
        task.setOutputList(mixerOutputList);

        ZEGOSDKManager.getInstance().expressService.startMixerTask(task, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                // 1005026 non_exists_stream_list
                if (callback != null) {
                    callback.onMixerStartResult(errorCode, extendedData);
                }
            }
        });
    }

    public void stopMixTask() {
        if (task != null) {
            ZEGOSDKManager.getInstance().expressService.stopMixerTask(task, new IZegoMixerStopCallback() {
                @Override
                public void onMixerStopResult(int errorCode) {

                }
            });
            task = null;
        }
    }

    public boolean isPKUserMuted() {
        return mutePKUser;
    }

    public void mutePKUser(boolean mute, IZegoMixerStartCallback callback) {
        startMixStreamTask(mute, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    ZEGOSDKManager.getInstance().expressService.mutePlayStreamAudio(currentPKInfo.getPKStream(), mute);
                    mutePKUser = mute;
                    if (callback != null) {
                        callback.onMixerStartResult(errorCode, extendedData);
                    }
                }
            }
        });
    }

    public void syncDeviceStatus() {
        handler.removeCallbacks(syncSEIRunnable);
        handler.post(syncSEIRunnable);
    }

    public void stopSyncDeviceStatus() {
        handler.removeCallbacks(syncSEIRunnable);
    }

    public void checkPKUserSEI() {
        handler.removeCallbacks(checkSEIRunnable);
        handler.post(checkSEIRunnable);
    }

    public void stopCheckPKUserSEI() {
        handler.removeCallbacks(checkSEIRunnable);
    }


    public void addListener(PKListener listener) {
        listenerList.add(listener);
    }

    public void removeListener(PKListener listener) {
        listenerList.remove(listener);
    }

    public void removeRoomListeners() {
        task = null;
        listenerList.clear();
    }

    public void removeRoomData() {
        setCurrentPKInfo(null);
        seiTimeMap.clear();
        seiStateMap.clear();
        roomProperties.clear();
        hasNotified = false;
        mutePKUser = false;
        handler.removeCallbacksAndMessages(null);
    }

    public void removeUserListeners() {
        ZEGOSDKManager.getInstance().zimService.removeEventHandler(zimEventHandler);
    }

    public void removeUserData() {
        removeRoomData();
        removeRoomListeners();
    }

    public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
        for (ZEGOSDKUser zegosdkUser : userList) {
            String mainStreamID = zegosdkUser.getMainStreamID();
            if (!TextUtils.isEmpty(mainStreamID)) {
                if (mainStreamID.endsWith("_host")) {
                    if (roomProperties != null && !roomProperties.isEmpty()) {
                        String pk_user_id = roomProperties.get("pk_user_id");
                        if (!TextUtils.isEmpty(pk_user_id)) {
                            onReceivePKRoomAttribute(roomProperties);
                        }
                    }
                }
            }
        }
    }

    public void onPlayerRecvVideoFirstFrame(String streamID) {
        if (streamID.endsWith("_mix")) {
            muteHostAudioVideo(true);
        }
    }

    public void onPlayerSyncRecvSEI(String streamID, byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            int type = jsonObject.getInt("type");
            String senderID = jsonObject.getString("sender_id");
            seiTimeMap.put(senderID, System.currentTimeMillis());

            boolean isMicOpen = jsonObject.getBoolean("mic");
            boolean isCameraOpen = jsonObject.getBoolean("cam");

            boolean isPKUser =
                currentPKInfo != null && currentPKInfo.pkUser != null && Objects.equals(currentPKInfo.pkUser.userID,
                    senderID);

            ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
            boolean isHostUser = hostUser != null && Objects.equals(hostUser.userID, senderID);

            if (isPKUser) {
                boolean micChanged = currentPKInfo.pkUser.isMicrophoneOpen() != isMicOpen;
                if (micChanged || !hasNotified) {
                    currentPKInfo.pkUser.setMicrophoneOpen(isMicOpen);
                    handler.post(() -> {
                        for (PKListener listener : listenerList) {
                            listener.onPKMicrophoneOpen(currentPKInfo.pkUser.userID, isMicOpen);
                        }
                    });
                }
                boolean camChanged = currentPKInfo.pkUser.isCameraOpen() != isCameraOpen;
                if (camChanged || !hasNotified) {
                    currentPKInfo.pkUser.setCameraOpen(isCameraOpen);
                    handler.post(() -> {
                        for (PKListener listener : listenerList) {
                            listener.onPKCameraOpen(currentPKInfo.pkUser.userID, isCameraOpen);
                        }
                    });
                }
                hasNotified = true;
            } else if (isHostUser) {
                if (hostUser.isCameraOpen() != isCameraOpen) {
                    handler.post(() -> {
                        ZEGOSDKManager.getInstance().expressService.syncCameraState(hostUser.userID, isCameraOpen);
                    });
                }
                if (hostUser.isMicrophoneOpen() != isMicOpen) {
                    handler.post(() -> {
                        ZEGOSDKManager.getInstance().expressService.syncMicrophoneState(hostUser.userID, isMicOpen);
                    });
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void onRoomAttributesUpdated(List<Map<String, String>> setProperties,
        List<Map<String, String>> deleteProperties) {
        for (Map<String, String> deleteProperty : deleteProperties) {
            for (Entry<String, String> entry : deleteProperty.entrySet()) {
                roomProperties.remove(entry.getKey());
            }
        }
        for (Map<String, String> setPropertie : setProperties) {
            roomProperties.putAll(setPropertie);
        }

        for (Map<String, String> setPropertyMap : setProperties) {
            if (setPropertyMap.containsKey("pk_user_id")) {
                onReceivePKRoomAttribute(setPropertyMap);
            }
        }

        for (Map<String, String> deleteProperty : deleteProperties) {
            if (deleteProperty.containsKey("pk_user_id")) {
                // if already not in pk,return
                if (currentPKInfo == null) {
                    return;
                } else {
                    stopPKBattle();
                }
            }
        }
    }

    private void sendUserRequest(String userID, String extendedData, ZIMCallInvitationSentCallback callback) {
        ZIMCallInviteConfig config = new ZIMCallInviteConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.sendUserRequest(Collections.singletonList(userID), config, callback);
    }

    private void acceptUserRequest(String requestID, String extendedData, ZIMCallAcceptanceSentCallback callback) {
        ZIMCallAcceptConfig config = new ZIMCallAcceptConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.acceptUserRequest(requestID, config, callback);
    }

    private void rejectUserRequest(String requestID, String extendedData, ZIMCallRejectionSentCallback callback) {
        ZIMCallRejectConfig config = new ZIMCallRejectConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.rejectUserRequest(requestID, config, callback);
    }

    private void cancelUserRequest(String userID, String requestID, String extendedData,
        ZIMCallCancelSentCallback callback) {
        ZIMCallCancelConfig config = new ZIMCallCancelConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.cancelUserRequest(Collections.singletonList(userID), requestID, config,
            callback);
    }

    public static class PKRequest {

        public String requestID;
        public String targetUserID;
    }

    public static class PKInfo {

        public ZEGOSDKUser pkUser;
        public String pkRoom;
        public long seq;
        public String hostUserID;

        public PKInfo(ZEGOSDKUser pkUser, String pkRoom) {
            this.pkUser = pkUser;
            this.pkRoom = pkRoom;
        }

        public PKInfo(String targetUserID) {
            this.pkUser = new ZEGOSDKUser(targetUserID, "");
        }

        public String getPKStream() {
            return pkRoom + "_" + pkUser.userID + "_main" + "_host";
        }

        @Override
        public String toString() {
            return "PKInfo{" + "pkUser=" + pkUser + ", pkRoom='" + pkRoom + '\'' + '}';
        }
    }
}
