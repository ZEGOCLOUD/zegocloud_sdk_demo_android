package com.zegocloud.demo.bestpractice.internal;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.zegocloud.demo.bestpractice.components.call.IncomingCallDialog;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoUser;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallEndSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallRejectionSentCallback;
import im.zego.zim.callback.ZIMCallingInvitationSentCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallEndConfig;
import im.zego.zim.entity.ZIMCallEndedSentInfo;
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
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.enums.ZIMCallInvitationMode;
import im.zego.zim.enums.ZIMCallUserState;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class ZEGOCallInvitationManager {

    private static final class Holder {

        private static final ZEGOCallInvitationManager INSTANCE = new ZEGOCallInvitationManager();
    }

    private ZEGOCallInvitationManager() {

    }

    public static ZEGOCallInvitationManager getInstance() {
        return ZEGOCallInvitationManager.Holder.INSTANCE;
    }

    private IZIMEventHandler zimEventHandler;
    private CallInviteInfo callInviteInfo;
    private CopyOnWriteArrayList<CallChangedListener> callListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<CallChangedListener> autoRemoveCallListeners = new CopyOnWriteArrayList<>();
    private IExpressEngineEventHandler expressEventHandler;
    private Comparator<CallInviteUser> callInviteUserComparator;
    private Context applicationContext;
    private boolean oneOnOneCall = true;

    public void init(Context context) {
        applicationContext = context.getApplicationContext();
        zimEventHandler = new IZIMEventHandler() {
            @Override
            public void onInComingUserRequestReceived(String requestID, ZIMCallInvitationReceivedInfo info) {
                Timber.d("onInComingUserRequestReceived() called with: requestID = [" + requestID + "], info = [" + info
                    + "]");
                CallExtendedData originalExtendedData = CallExtendedData.parse(info.extendedData);
                if (originalExtendedData != null) {
                    if (originalExtendedData.isVideoCall() || originalExtendedData.isVoiceCall()) {
                        boolean inCallRequest = callInviteInfo != null;
                        String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                        boolean inRoom = !TextUtils.isEmpty(roomID);
                        if (inCallRequest || inRoom) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("type", originalExtendedData.type);
                                jsonObject.put("callID", requestID);
                                jsonObject.put("reason", "busy");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            busyRejectCallRequest(requestID, jsonObject.toString(), new UserRequestCallback() {
                                @Override
                                public void onUserRequestSend(int errorCode, String requestID) {
                                    if (errorCode == 0) {
                                        for (CallChangedListener listener : callListeners) {
                                            listener.onBusyRejectCall(requestID);
                                        }
                                        for (CallChangedListener listener : autoRemoveCallListeners) {
                                            listener.onBusyRejectCall(requestID);
                                        }

                                    }

                                }
                            });
                            return;
                        }

                        callInviteInfo = new CallInviteInfo();
                        callInviteInfo.requestID = requestID;
                        callInviteInfo.inviter = info.inviter;
                        callInviteInfo.firstInviter = info.caller;
                        callInviteInfo.userList = new ArrayList<>();
                        callInviteInfo.type = originalExtendedData.type;
                        callInviteInfo.isOutgoingCall = false;
                        List<String> userIDList = new ArrayList<>();

                        for (ZIMCallUserInfo zimCallUserInfo : info.callUserList) {
                            CallInviteUser callInviteUser = new CallInviteUser(zimCallUserInfo.userID,
                                zimCallUserInfo.state, zimCallUserInfo.extendedData);

                            callInviteInfo.userList.add(callInviteUser);
                            userIDList.add(zimCallUserInfo.userID);
                        }

                        if (callInviteUserComparator != null) {
                            Collections.sort(callInviteInfo.userList, callInviteUserComparator);
                        }
                        Timber.d("callInviteInfo.userList: " + callInviteInfo.userList);

                        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList,
                            new ZIMUsersInfoQueriedCallback() {
                                @Override
                                public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                                    ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {

                                    Intent intent = new Intent(applicationContext, IncomingCallDialog.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    applicationContext.startActivity(intent);

                                    for (CallChangedListener listener : callListeners) {
                                        listener.onReceiveNewCall(requestID, callInviteInfo.userList);
                                    }
                                    for (CallChangedListener listener : autoRemoveCallListeners) {
                                        listener.onReceiveNewCall(requestID, callInviteInfo.userList);
                                    }
                                }
                            });
                    }
                }
            }

            @Override
            public void onUserRequestStateChanged(ZIMCallUserStateChangeInfo info, String requestID) {
                super.onUserRequestStateChanged(info, requestID);
                Timber.d(
                    "onUserRequestStateChanged() called with: info = [" + info + "], requestID = [" + requestID + "]");

                if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                    List<CallInviteUser> stateChangedUsers = new ArrayList<>();
                    for (ZIMCallUserInfo userInfo : info.callUserList) {
                        CallInviteUser changedUser = new CallInviteUser(userInfo.userID, userInfo.state,
                            userInfo.extendedData);
                        stateChangedUsers.add(changedUser);
                    }

                    for (CallInviteUser changedUser : stateChangedUsers) {
                        boolean ifAlreadyAdded = false;
                        for (CallInviteUser inviteUser : callInviteInfo.userList) {
                            if (Objects.equals(inviteUser.getUserID(), changedUser.getUserID())) {
                                inviteUser.setCallUserState(changedUser.getCallUserState());
                                inviteUser.setExtendedData(changedUser.getExtendedData());
                                ifAlreadyAdded = true;
                                break;
                            }
                        }
                        if (!ifAlreadyAdded) {
                            callInviteInfo.userList.add(changedUser);
                        }
                    }
                    if (callInviteUserComparator != null) {
                        Collections.sort(callInviteInfo.userList, callInviteUserComparator);
                    }

                    List<String> queryList = new ArrayList<>();
                    for (ZIMCallUserInfo userInfo : info.callUserList) {
                        ZIMUserFullInfo fullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(userInfo.userID);
                        if (fullInfo == null || userInfo.state == ZIMCallUserState.INVITING) {
                            queryList.add(userInfo.userID);
                        }
                    }

                    if (queryList.size() > 0) {
                        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(queryList,
                            new ZIMUsersInfoQueriedCallback() {
                                @Override
                                public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                                    ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                                    queryList.clear();
                                    for (CallChangedListener listener : callListeners) {
                                        listener.onCallUserInfoUpdate(userList);
                                    }
                                    for (CallChangedListener listener : autoRemoveCallListeners) {
                                        listener.onCallUserInfoUpdate(userList);
                                    }
                                }
                            });
                    }
                    for (CallInviteUser stateChangedUser : stateChangedUsers) {
                        if (stateChangedUser.getCallUserState() == ZIMCallUserState.ACCEPTED) {
                            for (CallChangedListener listener : callListeners) {
                                listener.onInvitedUserAccepted(requestID, stateChangedUser);
                            }
                            for (CallChangedListener listener : autoRemoveCallListeners) {
                                listener.onInvitedUserAccepted(requestID, stateChangedUser);
                            }
                        } else if (stateChangedUser.getCallUserState() == ZIMCallUserState.INVITING) {
                            for (CallChangedListener listener : callListeners) {
                                listener.onInviteNewUser(requestID, stateChangedUser);
                            }
                            for (CallChangedListener listener : autoRemoveCallListeners) {
                                listener.onInviteNewUser(requestID, stateChangedUser);
                            }
                        } else if (stateChangedUser.getCallUserState() == ZIMCallUserState.REJECTED) {
                            for (CallChangedListener listener : callListeners) {
                                listener.onInvitedUserRejected(requestID, stateChangedUser);
                            }
                            for (CallChangedListener listener : autoRemoveCallListeners) {
                                listener.onInvitedUserRejected(requestID, stateChangedUser);
                            }
                            checkIfCallEnded();
                        } else if (stateChangedUser.getCallUserState() == ZIMCallUserState.TIMEOUT) {
                            for (CallChangedListener listener : callListeners) {
                                listener.onInvitedUserTimeout(requestID, stateChangedUser);
                            }
                            for (CallChangedListener listener : autoRemoveCallListeners) {
                                listener.onInvitedUserTimeout(requestID, stateChangedUser);
                            }
                            checkIfCallEnded();
                        } else if (stateChangedUser.getCallUserState() == ZIMCallUserState.QUITED) {
                            for (CallChangedListener listener : callListeners) {
                                listener.onInvitedUserQuit(requestID, stateChangedUser);
                            }
                            for (CallChangedListener listener : autoRemoveCallListeners) {
                                listener.onInvitedUserQuit(requestID, stateChangedUser);
                            }
                            CallInviteUser selfInviteInfo = getSelfInviteInfo();
                            if (selfInviteInfo != null) {
                                if (selfInviteInfo.isAccepted() || selfInviteInfo.isWaiting()) {
                                    checkIfCallEnded();
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onUserRequestEnded(String requestID, ZIMCallInvitationEndedInfo info) {
                super.onUserRequestEnded(requestID, info);
                Timber.d("onUserRequestEnded() called with: requestID = [" + requestID + "], info = [" + info + "]");
                if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                    for (CallChangedListener listener : callListeners) {
                        listener.onCallEnded(requestID);
                    }
                    for (CallChangedListener listener : autoRemoveCallListeners) {
                        listener.onCallEnded(requestID);
                    }
                    removeCallData();
                }
            }

            @Override
            public void onInComingUserRequestTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {
                if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                    for (CallChangedListener listener : callListeners) {
                        listener.onCallTimeout(requestID);
                    }
                    for (CallChangedListener listener : autoRemoveCallListeners) {
                        listener.onCallTimeout(requestID);
                    }
                    removeCallData();
                }
            }

            @Override
            public void onInComingUserRequestCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
                if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                    for (CallChangedListener listener : callListeners) {
                        listener.onCallCancelled(requestID);
                    }
                    for (CallChangedListener listener : autoRemoveCallListeners) {
                        listener.onCallCancelled(requestID);
                    }
                    removeCallData();
                }
            }
        };
        ZEGOSDKManager.getInstance().zimService.addEventHandler(zimEventHandler, false);

        expressEventHandler = new IExpressEngineEventHandler() {
            @Override
            public void onRoomUserUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoUser> userList) {
                super.onRoomUserUpdate(roomID, updateType, userList);
                if (updateType == ZegoUpdateType.ADD) {
                    List<ZEGOSDKUser> roomUsers = ZEGOSDKManager.getInstance().expressService.getRoomUsers();
                    if (roomUsers.size() > 2) {
                        oneOnOneCall = false;
                    }
                }
            }
        };
        ZEGOSDKManager.getInstance().expressService.addEventHandler(expressEventHandler);
    }

    private boolean checkIfSelfAccepted() {
        boolean accepted = false;
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        if (callInviteInfo != null && callInviteInfo.userList != null) {
            for (CallInviteUser callInviteUser : callInviteInfo.userList) {
                if (Objects.equals(callInviteUser.getUserID(), currentUser.userID)) {
                    accepted = callInviteUser.getCallUserState() == ZIMCallUserState.ACCEPTED;
                    break;
                }
            }
        }
        return accepted;
    }

    private CallInviteUser getSelfInviteInfo() {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (currentUser == null) {
            return null;
        }
        if (callInviteInfo != null && callInviteInfo.userList != null) {
            for (CallInviteUser callInviteUser : callInviteInfo.userList) {
                if (Objects.equals(callInviteUser.getUserID(), currentUser.userID)) {
                    return callInviteUser;
                }
            }
        }
        return null;
    }

    public void setCallInviteInfo(String callID, int type) {
        callInviteInfo = new CallInviteInfo();
        callInviteInfo.requestID = callID;
        callInviteInfo.type = type;
        callInviteInfo.userList = new ArrayList<>();
        return;
    }

    public CallInviteInfo getCallInviteInfo() {
        return callInviteInfo;
    }

    private void checkIfCallEnded() {
        if (callInviteInfo == null) {
            // already end
            return;
        }
        boolean shouldEndCall = true;
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        for (CallInviteUser callInviteUser : callInviteInfo.userList) {
            if (!Objects.equals(callInviteUser.getUserID(), currentUser.userID)) {
                // except self
                if (callInviteUser.isAccepted() || callInviteUser.isWaiting()) {
                    shouldEndCall = false;
                }
            }
        }
        if (shouldEndCall) {
            if (oneOnOneCall) {
                for (CallChangedListener listener : callListeners) {
                    listener.onCallEnded(callInviteInfo.requestID);
                }
                for (CallChangedListener listener : autoRemoveCallListeners) {
                    listener.onCallEnded(callInviteInfo.requestID);
                }
            }
            quitCall();
        }
    }

    public String generateUserStreamID(String userID, String roomID) {
        return roomID + "_" + userID + "_main";
    }

    public void inviteVideoCall(List<String> userIDList, ZIMCallInvitationSentCallback callback) {
        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList, new ZIMUsersInfoQueriedCallback() {
            @Override
            public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    sendCall(userIDList, true, callback);
                } else {
                    if (callback != null) {
                        ZIMError zimError = new ZIMError();
                        zimError.code = ZIMErrorCode.FAILED;
                        callback.onCallInvitationSent(null, null, zimError);
                    }
                }
            }
        });
    }

    public void inviteVoiceCall(List<String> userIDList, ZIMCallInvitationSentCallback callback) {
        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList, new ZIMUsersInfoQueriedCallback() {
            @Override
            public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    sendCall(userIDList, false, callback);
                }
                if (callback != null) {
                    ZIMError zimError = new ZIMError();
                    zimError.code = ZIMErrorCode.FAILED;
                    callback.onCallInvitationSent(null, null, zimError);
                }
            }
        });
    }

    private void sendCall(List<String> userIDList, boolean video, ZIMCallInvitationSentCallback callback) {
        if (callInviteInfo == null) {
            callInviteInfo = new CallInviteInfo();
            CallExtendedData extendedData = new CallExtendedData();
            if (video) {
                extendedData.type = CallExtendedData.VIDEO_CALL;
            } else {
                extendedData.type = CallExtendedData.VOICE_CALL;
            }

            ZIMCallInviteConfig config = new ZIMCallInviteConfig();
            config.extendedData = extendedData.toString();
            config.mode = ZIMCallInvitationMode.ADVANCED;
            ZEGOSDKManager.getInstance().zimService.sendUserRequest(userIDList, config,
                new ZIMCallInvitationSentCallback() {
                    @Override
                    public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info,
                        ZIMError errorInfo) {
                        if (errorInfo.code.value() == 0) {
                            callInviteInfo.requestID = requestID;
                            callInviteInfo.userList = new ArrayList<>();
                            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                            callInviteInfo.inviter = currentUser.userID;
                            callInviteInfo.firstInviter = currentUser.userID;
                            callInviteInfo.isOutgoingCall = true;
                            if (video) {
                                callInviteInfo.type = CallExtendedData.VIDEO_CALL;
                            } else {
                                callInviteInfo.type = CallExtendedData.VOICE_CALL;
                            }
                        } else {
                            callInviteInfo = null;
                        }
                        if (callback != null) {
                            callback.onCallInvitationSent(requestID, info, errorInfo);
                        }

                    }
                });
        } else {
            if (!TextUtils.isEmpty(callInviteInfo.requestID)) {
                ZIMCallingInviteConfig config = new ZIMCallingInviteConfig();
                ZEGOSDKManager.getInstance().zimService.addUserToRequest(userIDList, callInviteInfo.requestID, config,
                    new ZIMCallingInvitationSentCallback() {
                        @Override
                        public void onCallingInvitationSent(String callID, ZIMCallingInvitationSentInfo info,
                            ZIMError errorInfo) {
                            if (callback != null) {
                                ZIMCallInvitationSentInfo sentInfo = new ZIMCallInvitationSentInfo();
                                sentInfo.errorUserList = info.errorUserList;
                                callback.onCallInvitationSent(callInviteInfo.requestID, sentInfo, errorInfo);
                            }
                        }
                    });
            }
        }
    }

    public void rejectCallRequest(String requestID, UserRequestCallback callback) {
        ZEGOSDKManager.getInstance().zimService.rejectUserRequest(requestID, new ZIMCallRejectConfig(),
            new ZIMCallRejectionSentCallback() {
                @Override
                public void onCallRejectionSent(String callID, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                            callInviteInfo = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    private void busyRejectCallRequest(String requestID, String extendedData, UserRequestCallback callback) {
        ZIMCallRejectConfig config = new ZIMCallRejectConfig();
        config.extendedData = extendedData;
        ZEGOSDKManager.getInstance().zimService.rejectUserRequest(requestID, config,
            new ZIMCallRejectionSentCallback() {
                @Override
                public void onCallRejectionSent(String callID, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                            callInviteInfo = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    public void acceptCallRequest(String requestID, UserRequestCallback callback) {
        ZEGOSDKManager.getInstance().zimService.acceptUserRequest(requestID, new ZIMCallAcceptConfig(),
            new ZIMCallAcceptanceSentCallback() {
                @Override
                public void onCallAcceptanceSent(String callID, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {

                    } else {
                        if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                            callInviteInfo = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    public void cancelCallRequest(String requestID, List<String> userIDList, UserRequestCallback callback) {
        ZEGOSDKManager.getInstance().zimService.cancelUserRequest(userIDList, requestID, new ZIMCallCancelConfig(),
            new ZIMCallCancelSentCallback() {
                @Override
                public void onCallCancelSent(String callID, ArrayList<String> errorInvitees, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                            callInviteInfo = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    public void endCallRequest(String requestID, UserRequestCallback callback) {
        ZEGOSDKManager.getInstance().zimService.endUserRequest(requestID, new ZIMCallEndConfig(),
            new ZIMCallEndSentCallback() {
                @Override
                public void onCallEndSent(String callID, ZIMCallEndedSentInfo info, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        if (callInviteInfo != null && requestID.equals(callInviteInfo.requestID)) {
                            callInviteInfo = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    public void joinRoom(IZegoRoomLoginCallback callback) {
        ZEGOSDKManager.getInstance().expressService.loginRoom(callInviteInfo.requestID, callback);
    }

    public void leaveRoom() {
        autoRemoveCallListeners.clear();
        oneOnOneCall = true;
        ZEGOSDKManager.getInstance().expressService.logoutRoom(null);
    }

    public void quitCallAndLeaveRoom() {
        Timber.d("quitCallAndLeaveRoom() called");
        quitCall();
        leaveRoom();
    }

    public void quitCall() {
        if (callInviteInfo != null) {
            ZEGOSDKManager.getInstance().zimService.quitUserRequest(callInviteInfo.requestID, new ZIMCallQuitConfig(),
                null);
            removeCallData();
        }
    }

    public void setCallInviteUserComparator(Comparator<CallInviteUser> comparator) {
        this.callInviteUserComparator = comparator;
        if (callInviteInfo != null && comparator != null) {
            Collections.sort(callInviteInfo.userList, callInviteUserComparator);
        }
    }

    public void removeUserListeners() {
        Timber.d("removeUserListeners() called");
        ZEGOSDKManager.getInstance().zimService.removeEventHandler(zimEventHandler);
        callListeners.clear();
        autoRemoveCallListeners.clear();
    }

    public void removeUserData() {
        removeCallData();
    }

    public void removeCallData() {
        callInviteInfo = null;
    }

    public void addCallListener(CallChangedListener listener) {
        addCallListener(listener, true);
    }

    /**
     * @param listener
     * @param autoRemove if true, the listener will be removed when leave room(End Call).
     */
    public void addCallListener(CallChangedListener listener, boolean autoRemove) {
        Timber.d("addCallListener() called with: listener = [" + listener + "], autoRemove = [" + autoRemove + "]");
        if (autoRemove) {
            autoRemoveCallListeners.add(listener);
        } else {
            callListeners.add(listener);
        }
    }

    public void removeCallListener(CallChangedListener listener) {
        Timber.d("removeCallListener() called with: listener = [" + listener + "]");
        callListeners.remove(listener);
        autoRemoveCallListeners.remove(listener);
    }
}
