package com.zegocloud.demo.bestpractice.internal.business.pk;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallEndSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallQuitSentCallback;
import im.zego.zim.callback.ZIMCallRejectionSentCallback;
import im.zego.zim.callback.ZIMCallingInvitationSentCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallEndConfig;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationEndedInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationSentInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallQuitConfig;
import im.zego.zim.entity.ZIMCallRejectConfig;
import im.zego.zim.entity.ZIMCallUserInfo;
import im.zego.zim.entity.ZIMCallUserStateChangeInfo;
import im.zego.zim.entity.ZIMCallingInvitationSentInfo;
import im.zego.zim.entity.ZIMCallingInviteConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.enums.ZIMCallInvitationMode;
import im.zego.zim.enums.ZIMCallUserState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class PKService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable syncSEIRunnable;
    private Runnable checkSEIRunnable;
    private ConcurrentHashMap<String, Long> seiTimeMap = new ConcurrentHashMap<>();
    private Map<String, String> roomProperties = new HashMap<>();

    private PKBattleInfo pkBattleInfo;

    private ZegoMixerTask task;
    private IZIMEventHandler zimEventHandler;
    private List<PKListener> listenerList = new ArrayList<>();
    private boolean isPKStarted;
    public static final int MIX_VIDEO_WIDTH = 810;
    public static final int MIX_VIDEO_HEIGHT = 720;
    public static final int MIX_VIDEO_BITRATE = 1500;
    public static final int MIX_VIDEO_FPS = 15;

    public void addListenersForUserSignIn() {
        Timber.d("initWhenUserLogin() called");
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
                    long duration = System.currentTimeMillis() - timeStamp;
                    for (PKListener listener : listenerList) {
                        listener.onPKUserConnecting(userID, duration);
                    }
                }
                handler.postDelayed(checkSEIRunnable, 1000);
            }
        };

        zimEventHandler = new IZIMEventHandler() {

            @Override
            public void onInComingUserRequestReceived(String requestID, ZIMCallInvitationReceivedInfo info) {
                PKExtendedData inviterExtendedData = PKExtendedData.parse(info.extendedData);
                if (inviterExtendedData != null) {
                    if (inviterExtendedData.type == PKExtendedData.START_PK) {
                        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                        if (TextUtils.isEmpty(currentRoomID)) {
                            busyRejectPKBattle(requestID, "room");
                            return;
                        }
                        if (!ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                            busyRejectPKBattle(requestID, "host");
                            return;
                        }
                        if (pkBattleInfo != null) {
                            busyRejectPKBattle(requestID, "busy");
                            return;
                        }
                        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                        PKBattleInfo pkInfo = new PKBattleInfo();
                        pkInfo.requestID = requestID;
                        pkInfo.pkUserList = new ArrayList<>();
                        for (ZIMCallUserInfo zimCallUserInfo : info.callUserList) {
                            PKUser pkUser = new PKUser(zimCallUserInfo.userID, zimCallUserInfo.state,
                                zimCallUserInfo.extendedData);
                            // parse each user and it's extendedData
                            if (!TextUtils.isEmpty(zimCallUserInfo.extendedData)) {
                                PKExtendedData userData = PKExtendedData.parse(zimCallUserInfo.extendedData);
                                if (userData != null) {
                                    pkUser.userName = userData.userName;
                                    pkUser.roomID = userData.roomID;
                                }
                            }
                            //
                            if (Objects.equals(currentUser.userID, zimCallUserInfo.userID)) {
                                pkUser.userName = currentUser.userName;
                                pkUser.roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                                pkUser.setCamera(currentUser.isCameraOpen());
                                pkUser.setMicrophone(currentUser.isMicrophoneOpen());
                                pkInfo.pkUserList.add(0, pkUser);
                            } else {
                                if (Objects.equals(zimCallUserInfo.userID, inviterExtendedData.userID)) {
                                    pkUser.roomID = inviterExtendedData.roomID;
                                    pkUser.userName = inviterExtendedData.userName;
                                }
                                pkInfo.pkUserList.add(pkUser);
                            }
                        }
                        PKService.this.pkBattleInfo = pkInfo;

                        for (PKListener listener : listenerList) {
                            listener.onPKBattleReceived(requestID, info);
                        }
                    }
                }
            }

            @Override
            public void onInComingUserRequestTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {
                if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
                    pkBattleInfo = null;
                    for (PKListener listener : listenerList) {
                        listener.onInComingPKBattleTimeout(requestID, info);
                    }
                }
            }

            @Override
            public void onInComingUserRequestCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
                if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
                    pkBattleInfo = null;
                    for (PKListener listener : listenerList) {
                        listener.onPKBattleCancelled(requestID, info);
                    }
                }
            }

            @Override
            public void onUserRequestEnded(String requestID, ZIMCallInvitationEndedInfo info) {
                if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
                    stopPKBattle();
                }
            }

            @Override
            public void onUserRequestStateChanged(ZIMCallUserStateChangeInfo info, String requestID) {
                super.onUserRequestStateChanged(info, requestID);
                if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
                    ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                    for (ZIMCallUserInfo userInfo : info.callUserList) {
                        boolean findIfAlreadyAdded = false;
                        for (PKUser pkUser : pkBattleInfo.pkUserList) {
                            if (Objects.equals(pkUser.userID, userInfo.userID)) {
                                pkUser.setCallUserState(userInfo.state);
                                pkUser.setExtendedData(userInfo.extendedData);
                                if (!TextUtils.isEmpty(userInfo.extendedData)) {
                                    PKExtendedData userData = PKExtendedData.parse(userInfo.extendedData);
                                    if (userData != null) {
                                        pkUser.userName = userData.userName;
                                        pkUser.roomID = userData.roomID;
                                    }
                                }
                                if (Objects.equals(pkUser.userID, currentUser.userID)) {
                                    pkUser.roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                                    pkUser.userName = currentUser.userName;
                                    pkUser.setCamera(currentUser.isCameraOpen());
                                    pkUser.setMicrophone(currentUser.isMicrophoneOpen());
                                }
                                findIfAlreadyAdded = true;
                                break;
                            }
                        }
                        if (!findIfAlreadyAdded) {
                            PKUser pkUser = new PKUser(userInfo.userID, userInfo.state, userInfo.extendedData);
                            if (Objects.equals(pkUser.userID, currentUser.userID)) {
                                pkUser.roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                                pkUser.userName = currentUser.userName;
                                pkUser.setCamera(currentUser.isCameraOpen());
                                pkUser.setMicrophone(currentUser.isMicrophoneOpen());
                                pkBattleInfo.pkUserList.add(0, pkUser);
                            } else {
                                pkBattleInfo.pkUserList.add(pkUser);
                            }
                        }
                    }

                    for (ZIMCallUserInfo userInfo : info.callUserList) {
                        if (userInfo.state == ZIMCallUserState.ACCEPTED) {
                            PKUser pkUser = getPKUser(pkBattleInfo, userInfo.userID);
                            if (pkUser != null) {
                                for (PKListener listener : listenerList) {
                                    listener.onPKBattleAccepted(userInfo.userID, userInfo.extendedData);
                                }
                            }
                            onReceivePKUserAccepted(userInfo);
                        } else if (userInfo.state == ZIMCallUserState.REJECTED) {
                            for (PKListener listener : listenerList) {
                                listener.onPKBattleRejected(userInfo.userID, userInfo.extendedData);
                            }
                            checkIfPKEnd(requestID, currentUser);
                        } else if (userInfo.state == ZIMCallUserState.TIMEOUT) {
                            for (PKListener listener : listenerList) {
                                listener.onOutgoingPKBattleTimeout(userInfo.userID, userInfo.extendedData);
                            }
                            checkIfPKEnd(requestID, currentUser);
                        } else if (userInfo.state == ZIMCallUserState.QUITED) {
                            onReceivePKUserQuit(requestID, userInfo);
                            seiTimeMap.remove(userInfo.userID);
                        }
                    }
                }
            }

            @Override
            public void onCallInvitationEnded(ZIM zim, ZIMCallInvitationEndedInfo info, String callID) {
                super.onCallInvitationEnded(zim, info, callID);
                if (pkBattleInfo != null) {
                    endPKBattle(pkBattleInfo.requestID, null);
                    stopPKBattle();
                }
            }
        };

        /**
         * listen to zim call events to get PK invitation states.
         */
        ZEGOSDKManager.getInstance().zimService.addEventHandler(zimEventHandler, false);
    }

    private void checkIfPKEnd(String requestID, ZEGOSDKUser currentUser) {
        PKUser self = getPKUser(pkBattleInfo, currentUser.userID);
        if (self.hasAccepted()) {
            boolean hasWaitingUser = false;
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (!Objects.equals(pkUser.userID, currentUser.userID)) {
                    // except self
                    if (pkUser.hasAccepted() || pkUser.isWaiting()) {
                        hasWaitingUser = true;
                    }
                }
            }
            if (!hasWaitingUser) {
                quitPKBattle(requestID, null);
                stopPKBattle();
            }
        }
    }

    public void removePKBattle(String userID) {
        if (pkBattleInfo != null) {
            List<PKUser> timeoutQuitUsers = new ArrayList<>();
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (Objects.equals(userID, pkUser.userID)) {
                    pkUser.setCallUserState(ZIMCallUserState.QUITED);
                    timeoutQuitUsers.add(pkUser);
                }
            }
            if (!timeoutQuitUsers.isEmpty()) {
                for (PKUser timeoutQuitUser : timeoutQuitUsers) {
                    ZIMCallUserInfo callUserInfo = new ZIMCallUserInfo();
                    callUserInfo.userID = timeoutQuitUser.userID;
                    callUserInfo.extendedData = timeoutQuitUser.getExtendedData();
                    callUserInfo.state = timeoutQuitUser.getCallUserState();

                    if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                        onReceivePKUserQuit(pkBattleInfo.requestID, callUserInfo);
                    } else {
                        for (PKListener listener : listenerList) {
                            listener.onPKUserQuit(userID, callUserInfo.extendedData);
                        }
                    }
                }
            }
        }
        handler.post(() -> {
            seiTimeMap.remove(userID);
        });
    }

    private void onReceivePKUserQuit(String requestID, ZIMCallUserInfo userInfo) {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        PKUser self = getPKUser(pkBattleInfo, currentUser.userID);
        if (self.hasAccepted()) {
            boolean moreThanOneAcceptedExceptMe = false;
            boolean hasWaitingUser = false;
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (!Objects.equals(pkUser.userID, currentUser.userID)) {
                    // except self
                    if (pkUser.hasAccepted() || pkUser.isWaiting()) {
                        hasWaitingUser = true;
                    }
                    if (pkUser.hasAccepted()) {
                        moreThanOneAcceptedExceptMe = true;
                    }
                }
            }

            if (hasWaitingUser) {
                if (isPKStarted) {
                    updatePKMixTask(new IZegoMixerStartCallback() {
                        @Override
                        public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                            for (PKListener listener : listenerList) {
                                listener.onPKUserQuit(userInfo.userID, userInfo.extendedData);
                            }
                        }
                    });
                }else {
                    // if not start,still notify
                    for (PKListener listener : listenerList) {
                        listener.onPKUserQuit(userInfo.userID, userInfo.extendedData);
                    }
                }
            } else {
                for (PKListener listener : listenerList) {
                    listener.onPKUserQuit(userInfo.userID, userInfo.extendedData);
                }
                quitPKBattle(requestID, null);
                stopPKBattle();
            }
        }
    }

    private void onReceivePKUserAccepted(ZIMCallUserInfo userInfo) {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        PKExtendedData pkExtendedData = PKExtendedData.parse(userInfo.extendedData);
        if (pkExtendedData == null) {
            return;
        }
        if (pkExtendedData.type == PKExtendedData.START_PK) {
            boolean moreThanOneAcceptedExceptMe = false;
            boolean meHasAccepted = false;
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (Objects.equals(pkUser.userID, currentUser.userID)) {
                    meHasAccepted = pkUser.hasAccepted();
                } else {
                    if (pkUser.hasAccepted()) {
                        moreThanOneAcceptedExceptMe = true;
                    }
                }

            }
            if (meHasAccepted && moreThanOneAcceptedExceptMe && !isPKStarted) {
                isPKStarted = true;
                updatePKMixTask(new IZegoMixerStartCallback() {
                    @Override
                    public void onMixerStartResult(int errorCode, JSONObject mixData) {
                        if (errorCode == 0) {
                            // ended when waiting for mix result
                            if (pkBattleInfo == null) {
                                return;
                            }
                            syncDeviceStatus();
                            checkPKUserSEI();
                            for (PKListener listener : listenerList) {
                                listener.onPKStarted();
                            }
                            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                                if (pkUser.hasAccepted()) {
                                    for (PKListener listener : listenerList) {
                                        listener.onPKUserJoin(pkUser.userID, pkUser.getExtendedData());
                                    }
                                }
                            }
                        } else {
                            isPKStarted = false;
                            if (pkBattleInfo != null) {
                                quitPKBattle(pkBattleInfo.requestID, null);
                            }
                            for (PKListener listener : listenerList) {
                                listener.onPKMixStreamError(errorCode, mixData.toString());
                            }
                        }
                    }
                });
            } else {
                updatePKMixTask(new IZegoMixerStartCallback() {
                    @Override
                    public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                        if (errorCode == 0) {
                            for (PKListener listener : listenerList) {
                                listener.onPKUserJoin(userInfo.userID, userInfo.extendedData);
                            }
                        }
                    }
                });
            }
        }
    }

    private void onReceivePKRoomAttribute(Map<String, String> roomProperties) {
        Timber.d("onReceivePKRoomAttribute() called with: roomProperties = [" + roomProperties + "]");
        String request_id = roomProperties.get("request_id");
        List<PKUser> pkUserList = new ArrayList<>();
        try {
            JSONArray pkUsers = new JSONArray(roomProperties.get("pk_users"));
            for (int i = 0; i < pkUsers.length(); i++) {
                PKUser pkUser = PKUser.parse(pkUsers.getString(i));
                if (!ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                    // for audience,all is ACCEPTED,he can't get call user state.
                    pkUser.setCallUserState(ZIMCallUserState.ACCEPTED);
                }
                pkUserList.add(pkUser);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            // receive attribute but no pkInfo,clear
            if (pkBattleInfo == null) {
                deletePKRoomAttributes();
            }
        } else {
            for (PKUser pkUser : pkUserList) {
                seiTimeMap.put(pkUser.userID, System.currentTimeMillis());
            }
            if (pkBattleInfo == null) {
                // normal，audience receive Host PK action
                if (ZEGOLiveStreamingManager.getInstance().getHostUser() != null) {
                    pkBattleInfo = new PKBattleInfo();
                    pkBattleInfo.requestID = request_id;
                    pkBattleInfo.pkUserList = pkUserList;

                    checkPKUserSEI();
                    isPKStarted = true;
                    for (PKListener listener : listenerList) {
                        listener.onPKStarted();
                    }
                    for (PKUser pkUser : pkBattleInfo.pkUserList) {
                        if (pkUser.hasAccepted()) {
                            for (PKListener listener : listenerList) {
                                listener.onPKUserJoin(pkUser.userID, pkUser.getExtendedData());
                            }
                        }
                    }
                }
            } else {
                // only audience will receive ,update pk infos
                for (PKListener listener : listenerList) {
                    listener.onPKUserUpdate();
                }
            }
        }
    }

    public boolean isPKUser(String userID) {
        if (pkBattleInfo == null) {
            return false;
        } else {
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (pkUser.userID.equals(userID)) {
                    return true;
                }
            }
            return false;
        }
    }

    private String getPKExtendedData(boolean needUserID, boolean autoAccept) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        PKExtendedData data = new PKExtendedData();
        data.roomID = currentRoomID;
        data.userName = localUser.userName;
        data.type = PKExtendedData.START_PK;
        if (needUserID) {
            data.userID = localUser.userID;
        }
        if (autoAccept) {
            data.autoAccept = true;
        }
        return data.toString();
    }

    private static final String TAG = "PKService";

    public void invitePKBattle(List<String> targetUserIDList, boolean autoAccept, UserRequestCallback callback) {
        if (pkBattleInfo == null) {
            pkBattleInfo = new PKBattleInfo();
            String pkExtendedData = getPKExtendedData(true, autoAccept);
            // inviter extended data will keep while different person invite each other,so
            // we need recognize who's data when first invite
            sendUserRequest(targetUserIDList, pkExtendedData, true, new ZIMCallInvitationSentCallback() {
                @Override
                public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        PKBattleInfo pkInfo = new PKBattleInfo();
                        pkInfo.requestID = requestID;
                        pkInfo.pkUserList = new ArrayList<>();
                        pkBattleInfo = pkInfo;
                    } else {
                        pkBattleInfo = null;
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
        } else {
            if (!TextUtils.isEmpty(pkBattleInfo.requestID)) {
                addUserToRequest(targetUserIDList, pkBattleInfo.requestID, new ZIMCallingInvitationSentCallback() {
                    @Override
                    public void onCallingInvitationSent(String callID, ZIMCallingInvitationSentInfo info,
                        ZIMError errorInfo) {
                        if (callback != null) {
                            callback.onUserRequestSend(errorInfo.code.value(), callID);
                        }
                    }
                });
            }
        }
    }

    public void acceptPKBattle(String requestID) {
        if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
            String pkExtendedData = getPKExtendedData(false, false);
            acceptUserRequest(requestID, pkExtendedData, new ZIMCallAcceptanceSentCallback() {
                @Override
                public void onCallAcceptanceSent(String callID, ZIMError errorInfo) {
                    // will receive self state changed to accepted in advanced mode,so do nothing when success
                    if (errorInfo.code.value() == 0) {
                    } else {
                        pkBattleInfo = null;
                    }
                }
            });
        }
    }

    private void updatePKMixTask(IZegoMixerStartCallback callback) {
        if (pkBattleInfo != null) {
            List<String> pkUserStreamList = new ArrayList<>();
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (pkUser.getCallUserState() == ZIMCallUserState.ACCEPTED) {
                    pkUserStreamList.add(pkUser.getPKUserStream());
                }
            }

            ZegoMixerVideoConfig videoConfig = new ZegoMixerVideoConfig();
            videoConfig.width = MIX_VIDEO_WIDTH;
            videoConfig.height = MIX_VIDEO_HEIGHT;
            videoConfig.bitrate = MIX_VIDEO_BITRATE;
            videoConfig.fps = MIX_VIDEO_FPS;

            MixLayoutProvider mixLayoutProvider = ZEGOLiveStreamingManager.getInstance().getMixLayoutProvider();
            ArrayList<ZegoMixerInput> mixVideoInputs;
            if (mixLayoutProvider == null) {
                mixVideoInputs = getMixVideoInputs(pkUserStreamList, videoConfig);
            } else {
                mixVideoInputs = mixLayoutProvider.getMixVideoInputs(pkUserStreamList, videoConfig);
            }

            if (task == null) {
                String mixStreamID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID() + "_mix";

                task = new ZegoMixerTask(mixStreamID);
                task.videoConfig = videoConfig;

                task.setInputList(mixVideoInputs);

                ZegoMixerOutput mixerOutput = new ZegoMixerOutput(mixStreamID);
                ArrayList<ZegoMixerOutput> mixerOutputList = new ArrayList<>();
                mixerOutputList.add(mixerOutput);
                task.setOutputList(mixerOutputList);

                task.enableSoundLevel(true);
            } else {
                task.inputList = mixVideoInputs;
            }

            ZEGOSDKManager.getInstance().expressService.startMixerTask(task, new IZegoMixerStartCallback() {
                @Override
                public void onMixerStartResult(int errorCode, JSONObject data) {
                    // 1005026 non_exists_stream_list
                    if (errorCode == 0) {
                        updatePKRoomAttributes();
                    }
                    if (callback != null) {
                        callback.onMixerStartResult(errorCode, data);
                    }

                }
            });
        }
    }

    private ArrayList<ZegoMixerInput> getMixVideoInputs(List<String> streamList, ZegoMixerVideoConfig videoConfig) {
        ArrayList<ZegoMixerInput> inputList = new ArrayList<>();
        if (streamList.size() == 1) {
            int left = 0;
            int top = 0;
            int right = (videoConfig.width / streamList.size());
            int bottom = videoConfig.height;
            ZegoMixerInput input = new ZegoMixerInput(streamList.get(0), ZegoMixerInputContentType.VIDEO,
                new Rect(left, top, right, bottom));
            input.renderMode = ZegoMixRenderMode.FILL;
            inputList.add(input);
        } else if (streamList.size() == 2) {
            for (int i = 0; i < streamList.size(); i++) {
                int left = (videoConfig.width / streamList.size()) * i;
                int top = 0;
                int right = (videoConfig.width / streamList.size()) * (i + 1);
                int bottom = videoConfig.height;
                ZegoMixerInput input = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
                    new Rect(left, top, right, bottom));
                input.renderMode = ZegoMixRenderMode.FILL;
                inputList.add(input);
            }
        } else if (streamList.size() == 3) {
            for (int i = 0; i < streamList.size(); i++) {
                int left, top, right, bottom;
                if (i == 0) {
                    left = 0;
                    top = 0;
                    right = videoConfig.width / 2;
                    bottom = videoConfig.height;
                } else if (i == 1) {
                    left = videoConfig.width / 2;
                    top = 0;
                    right = left + videoConfig.width / 2;
                    bottom = top + videoConfig.height / 2;
                } else {
                    left = videoConfig.width / 2;
                    top = videoConfig.height / 2;
                    right = left + videoConfig.width / 2;
                    bottom = top + videoConfig.height / 2;
                }
                ZegoMixerInput input = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
                    new Rect(left, top, right, bottom));
                input.renderMode = ZegoMixRenderMode.FILL;
                inputList.add(input);
            }
        } else if (streamList.size() == 4 || streamList.size() == 6) {
            int row = 2;
            int maxCellCount = streamList.size() % 2 == 0 ? streamList.size() : (streamList.size() + 1);
            int column = maxCellCount / row;
            int cellWidth = videoConfig.width / column;
            int cellHeight = videoConfig.height / row;
            int left, top, right, bottom;
            for (int i = 0; i < streamList.size(); i++) {
                left = cellWidth * (i % column);
                top = cellHeight * (i < column ? 0 : 1);
                right = left + cellWidth;
                bottom = top + cellHeight;
                ZegoMixerInput input = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
                    new Rect(left, top, right, bottom));
                input.renderMode = ZegoMixRenderMode.FILL;
                inputList.add(input);
            }
        } else if (streamList.size() == 5) {
            for (int i = 0; i < streamList.size(); i++) {
                int left, top, right, bottom;
                if (i == 0) {
                    left = 0;
                    top = 0;
                    right = videoConfig.width / 2;
                    bottom = videoConfig.height / 2;
                } else if (i == 1) {
                    left = videoConfig.width / 2;
                    top = 0;
                    right = left + videoConfig.width / 2;
                    bottom = top + videoConfig.height / 2;
                } else if (i == 2) {
                    left = 0;
                    top = videoConfig.height / 2;
                    right = left + videoConfig.width / 3;
                    bottom = top + videoConfig.height / 2;
                } else if (i == 3) {
                    left = videoConfig.width / 3;
                    top = videoConfig.height / 2;
                    right = left + videoConfig.width / 3;
                    bottom = top + videoConfig.height / 2;
                } else {
                    left = (videoConfig.width / 3) * 2;
                    top = videoConfig.height / 2;
                    right = left + videoConfig.width / 3;
                    bottom = top + videoConfig.height / 2;
                }
                ZegoMixerInput input = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
                    new Rect(left, top, right, bottom));
                input.renderMode = ZegoMixRenderMode.FILL;
                inputList.add(input);
            }
        } else {
            int row = 3;
            int column = 3;
            int cellWidth = videoConfig.width / column;
            int cellHeight = videoConfig.height / row;
            int left, top, right, bottom;
            for (int i = 0; i < streamList.size(); i++) {
                left = cellWidth * (i % column);
                top = cellHeight * (i / column);
                right = left + cellWidth;
                bottom = top + cellHeight;
                ZegoMixerInput input = new ZegoMixerInput(streamList.get(i), ZegoMixerInputContentType.VIDEO,
                    new Rect(left, top, right, bottom));
                input.renderMode = ZegoMixRenderMode.FILL;
                inputList.add(input);
            }
        }

        return inputList;
    }

    public void rejectPKBattle(String requestID) {
        String pkExtendedData = getPKExtendedData(false, false);
        rejectUserRequest(requestID, pkExtendedData, null);
        if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
            pkBattleInfo = null;
        }
    }

    public void busyRejectPKBattle(String requestID, String reason) {
        String pkExtendedData = getPKExtendedData(false, false);
        try {
            JSONObject jsonObject = new JSONObject(pkExtendedData);
            jsonObject.put("reason", reason);
            rejectUserRequest(requestID, jsonObject.toString(), null);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    public void cancelPKBattle(String requestID, List<String> userIDList) {
        if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
            cancelUserRequest(userIDList, requestID, "", null);
            pkBattleInfo = null;
        }
    }

    /**
     * just send request,no local actions
     *
     * @param requestID
     * @param callback
     */
    public void endPKBattle(String requestID, ZIMCallEndSentCallback callback) {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (isPKUser(currentUser.userID)) {
            endUserRequest(requestID, "", callback);
        }
        if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
            pkBattleInfo = null;
        }
    }

    /**
     * just send request,no local actions
     *
     * @param requestID
     * @param callback
     */
    public void quitPKBattle(String requestID, ZIMCallQuitSentCallback callback) {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (isPKUser(currentUser.userID)) {
            quitUserRequest(requestID, "", callback);
        }
        if (pkBattleInfo != null && requestID.equals(pkBattleInfo.requestID)) {
            pkBattleInfo = null;
        }
    }

    public PKBattleInfo getPKBattleInfo() {
        return pkBattleInfo;
    }

    private void updatePKRoomAttributes() {
        HashMap<String, String> hashMap = new HashMap<>();

        if (ZEGOLiveStreamingManager.getInstance().getHostUser() != null) {
            hashMap.put("host_user_id", ZEGOLiveStreamingManager.getInstance().getHostUser().userID);
        }

        if (pkBattleInfo == null) {
            Timber.e("updatePKRoomAttributes: pkBattleInfo == null ");
            return;
        }
        hashMap.put("request_id", pkBattleInfo.requestID);

        List<PKUser> acceptedUsers = new ArrayList<>();
        for (PKUser pkUser : pkBattleInfo.pkUserList) {
            if (pkUser.hasAccepted()) {
                acceptedUsers.add(pkUser);
            }
        }
        for (PKUser pkUser : acceptedUsers) {
            for (ZegoMixerInput zegoMixerInput : task.inputList) {
                if (Objects.equals(pkUser.getPKUserStream(), zegoMixerInput.streamID)) {
                    pkUser.rect = zegoMixerInput.layout;
                }
            }
        }
        hashMap.put("pk_users", acceptedUsers.toString());

        ZIMRoomAttributesSetConfig config = new ZIMRoomAttributesSetConfig();
        config.isDeleteAfterOwnerLeft = false;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes(hashMap, config,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {

                }
            });
    }

    /**
     * means stop pk locally,update views and finish local logic
     */
    public void stopPKBattle() {
        if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
            deletePKRoomAttributes();
            stopMixTask();
            stopSyncDeviceStatus();
        } else {
            muteHostAudioVideo(false);
        }

        pkBattleInfo = null;
        stopCheckPKUserSEI();

        seiTimeMap.clear();
        isPKStarted = false;
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
        if (roomProperties.containsKey("pk_users")) {
            List<String> keys = new ArrayList<>();
            keys.add("request_id");
            keys.add("host_user_id");
            keys.add("pk_users");
            ZEGOSDKManager.getInstance().zimService.deleteRoomAttributes(keys, new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                }
            });
        }

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

    public boolean isPKUserMuted(String userID) {
        if (pkBattleInfo != null) {
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (Objects.equals(userID, pkUser.userID)) {
                    return pkUser.isMuted();
                }
            }
        }
        return false;
    }

    public void mutePKUser(List<String> muteUserList, boolean mute, IZegoMixerStartCallback callback) {
        if (task == null || task.inputList == null) {
            return;
        }
        List<String> muteStreamList = new ArrayList<>();

        List<Integer> userIndexList = new ArrayList<>();
        for (String userID : muteUserList) {
            for (int i = 0; i < pkBattleInfo.pkUserList.size(); i++) {
                PKUser pkUser = pkBattleInfo.pkUserList.get(i);
                if (Objects.equals(pkUser.userID, userID)) {
                    userIndexList.add(i);
                    break;
                }
            }
        }

        for (Integer index : userIndexList) {
            if (index < task.inputList.size()) {
                ZegoMixerInput zegoMixerInput = task.inputList.get(index);
                if (mute) {
                    zegoMixerInput.contentType = ZegoMixerInputContentType.VIDEO_ONLY;
                    muteStreamList.add(zegoMixerInput.streamID);
                } else {
                    zegoMixerInput.contentType = ZegoMixerInputContentType.VIDEO;
                }
            }
        }
        ZEGOSDKManager.getInstance().expressService.startMixerTask(task, new IZegoMixerStartCallback() {
            @Override
            public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                // 1005026 non_exists_stream_list
                if (errorCode == 0) {
                    for (String userID : muteUserList) {
                        for (int i = 0; i < pkBattleInfo.pkUserList.size(); i++) {
                            PKUser pkUser = pkBattleInfo.pkUserList.get(i);
                            if (Objects.equals(pkUser.userID, userID)) {
                                pkUser.setMuted(mute);
                                ZEGOSDKManager.getInstance().expressService.mutePlayStreamAudio(
                                    pkUser.getPKUserStream(), mute);
                            }
                        }
                    }
                }
                if (callback != null) {
                    callback.onMixerStartResult(errorCode, extendedData);
                }
            }
        });
    }

    /**
     * While pushing the stream to transmit the audio and video stream data,the stream media supplementary enhancement
     * information(SEI) is sent to synchronize some other additional information.
     * <p>
     * syncDeviceStatus will call ZegoExpressEngine.getEngine().sendSEI() every 500 ms to sync user devices states and
     * connect state.
     * <p>
     * others will receive data by onPlayerSyncRecvSEI() callback.
     */
    public void syncDeviceStatus() {
        handler.removeCallbacks(syncSEIRunnable);
        handler.post(syncSEIRunnable);
    }

    public void stopSyncDeviceStatus() {
        handler.removeCallbacks(syncSEIRunnable);
    }

    /**
     * check the data received by onPlayerSyncRecvSEI() callback every 1000 ms and trigger
     * PKListener.onPKUserConnecting() to notify the duration from last check. if any host is disconnected,you can check
     * his connection states here.
     */
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
        seiTimeMap.clear();
        roomProperties.clear();
        handler.removeCallbacksAndMessages(null);
        pkBattleInfo = null;
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
                        String pkUsers = roomProperties.get("pk_users");
                        if (!TextUtils.isEmpty(pkUsers)) {
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

    /**
     * trigger by other host call ZegoExpressEngine.getEngine().sendSEI(), if any host is disconnected,you can check his
     * connection states by checkPKUserSEI.
     *
     * @param streamID
     * @param data
     */
    public void onPlayerSyncRecvSEI(String streamID, byte[] data) {
        try {
            JSONObject jsonObject = new JSONObject(new String(data));
            int type = jsonObject.getInt("type");
            String senderID = jsonObject.getString("sender_id");
            seiTimeMap.put(senderID, System.currentTimeMillis());

            boolean isMicOpen = jsonObject.getBoolean("mic");
            boolean isCameraOpen = jsonObject.getBoolean("cam");

            PKUser pkUser = getPKUser(pkBattleInfo, senderID);

            if (pkUser != null) {
                boolean micChanged = pkUser.isMicrophoneOpen() != isMicOpen;
                if (micChanged) {
                    pkUser.setMicrophone(isMicOpen);
                    handler.post(() -> {
                        for (PKListener listener : listenerList) {
                            listener.onPKUserMicrophoneOpen(pkUser.userID, isMicOpen);
                        }
                    });
                }
                boolean camChanged = pkUser.isCameraOpen() != isCameraOpen;
                if (camChanged) {
                    pkUser.setCamera(isCameraOpen);
                    handler.post(() -> {
                        for (PKListener listener : listenerList) {
                            listener.onPKUserCameraOpen(pkUser.userID, isCameraOpen);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * audience will receive zimService.onRoomAttributesUpdated when join PK room. depend on the attributes,will trigger
     * PKListener.onPKStarted or PKListener.onPKEnded
     *
     * @param setProperties
     * @param deleteProperties
     */
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
            if (setPropertyMap.containsKey("pk_users")) {
                onReceivePKRoomAttribute(setPropertyMap);
            }
        }

        for (Map<String, String> deleteProperty : deleteProperties) {
            if (deleteProperty.containsKey("pk_users")) {
                // if already not in pk,return
                if (pkBattleInfo == null) {
                    return;
                } else {
                    stopPKBattle();
                }
            }
        }
    }

    private boolean containsUser(PKBattleInfo pkBattleInfo, String userID) {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (!Objects.equals(currentUser.userID, userID)) {
            if (pkBattleInfo != null) {
                for (PKUser pkUser : pkBattleInfo.pkUserList) {
                    if (Objects.equals(userID, pkUser.userID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private PKUser getPKUser(PKBattleInfo pkBattleInfo, String userID) {
        if (pkBattleInfo != null) {
            for (PKUser pkUser : pkBattleInfo.pkUserList) {
                if (Objects.equals(userID, pkUser.userID)) {
                    return pkUser;
                }
            }
        }
        return null;
    }

    private void sendUserRequest(List<String> userIDList, String extendedData, boolean advanced,
        ZIMCallInvitationSentCallback callback) {
        ZIMCallInviteConfig config = new ZIMCallInviteConfig();
        if (advanced) {
            config.mode = ZIMCallInvitationMode.ADVANCED;
        }
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.sendUserRequest(userIDList, config, callback);
    }

    private void addUserToRequest(List<String> invitees, String requestID, ZIMCallingInvitationSentCallback callback) {
        ZIMCallingInviteConfig config = new ZIMCallingInviteConfig();
        ZEGOSDKManager.getInstance().zimService.addUserToRequest(invitees, requestID, config, callback);
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

    private void cancelUserRequest(List<String> userIDList, String requestID, String extendedData,
        ZIMCallCancelSentCallback callback) {
        ZIMCallCancelConfig config = new ZIMCallCancelConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.cancelUserRequest(userIDList, requestID, config, callback);
    }

    private void quitUserRequest(String requestID, String extendedData, ZIMCallQuitSentCallback callback) {
        ZIMCallQuitConfig config = new ZIMCallQuitConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.quitUserRequest(requestID, config, callback);
    }

    private void endUserRequest(String requestID, String extendedData, ZIMCallEndSentCallback callback) {
        ZIMCallEndConfig config = new ZIMCallEndConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.endUserRequest(requestID, config, callback);
    }

    public static class PKBattleInfo {

        public String requestID;
        public List<PKUser> pkUserList;

        @Override
        public String toString() {
            return "PKBattleInfo{" + "requestID='" + requestID + '\'' + ", pkUserList=" + pkUserList
                + ", pkExtendedData=" + '}';
        }
    }
}
