package com.zegocloud.demo.bestpractice.internal;


import android.text.TextUtils;
import com.zegocloud.demo.bestpractice.internal.business.call.CallExtendedData;
import com.zegocloud.demo.bestpractice.internal.business.call.ReceiveCallListener;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallRejectionSentCallback;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationSentInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallRejectConfig;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONException;
import org.json.JSONObject;

public class ZEGOCallInvitationManager {

    private static final class Holder {

        private static final ZEGOCallInvitationManager INSTANCE = new ZEGOCallInvitationManager();
    }

    private ZEGOCallInvitationManager() {

    }

    public static ZEGOCallInvitationManager getInstance() {
        return ZEGOCallInvitationManager.Holder.INSTANCE;
    }

    private ReceiveCallListener receiveCallListener;
    private CallRequest sendCallRequest;
    private CallRequest recvCallRequest;

    public void init() {
        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onInComingUserRequestReceived(String requestID, ZIMCallInvitationReceivedInfo info) {
                CallExtendedData callExtendedData = CallExtendedData.parse(info.extendedData);
                if (callExtendedData != null) {
                    if (callExtendedData.isVideoCall() || callExtendedData.isVoiceCall()) {
                        boolean inCallRequest = sendCallRequest != null || recvCallRequest != null;
                        String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                        boolean inRoom = !TextUtils.isEmpty(roomID);
                        if (inCallRequest || inRoom) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("type", callExtendedData.type);
                                jsonObject.put("callID", requestID);
                                jsonObject.put("reason", "busy");
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            busyRejectCallRequest(requestID, jsonObject.toString(), new UserRequestCallback() {
                                @Override
                                public void onUserRequestSend(int errorCode, String requestID) {

                                }
                            });
                            return;
                        }
                        recvCallRequest = new CallRequest();
                        recvCallRequest.requestID = requestID;
                        recvCallRequest.targetUserID = info.inviter;

                        if (receiveCallListener != null) {
                            receiveCallListener.onReceiveNewCall(requestID, info.inviter, callExtendedData.userName,
                                callExtendedData.type);
                        }
                    }
                }
            }

            @Override
            public void onInComingUserRequestTimeout(String requestID, ZIMCallInvitationTimeoutInfo info) {
                if (recvCallRequest != null && requestID.equals(recvCallRequest.requestID)) {
                    recvCallRequest = null;
                }
            }

            @Override
            public void onInComingUserRequestCancelled(String requestID, ZIMCallInvitationCancelledInfo info) {
                if (recvCallRequest != null && requestID.equals(recvCallRequest.requestID)) {
                    recvCallRequest = null;
                }
            }

            @Override
            public void onOutgoingUserRequestTimeout(String requestID) {
                if (sendCallRequest != null && requestID.equals(sendCallRequest.requestID)) {
                    sendCallRequest = null;
                }
            }

            @Override
            public void onOutgoingUserRequestAccepted(String requestID, String invitee, String extendedData) {
                if (sendCallRequest != null && requestID.equals(sendCallRequest.requestID)) {
                    sendCallRequest = null;
                }
            }

            @Override
            public void onOutgoingUserRequestRejected(String requestID, String invitee, String extendedData) {
                if (sendCallRequest != null && requestID.equals(sendCallRequest.requestID)) {
                    sendCallRequest = null;
                }
            }
        }, false);
    }

    public void startPublishingStream() {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        String generateUserStreamID = generateUserStreamID(currentUser.userID, currentRoomID);
        ZEGOSDKManager.getInstance().expressService.startPublishingStream(generateUserStreamID);
    }

    public String generateUserStreamID(String userID, String roomID) {
        return roomID + "_" + userID + "_main" + "_host";
    }

    public void setReceiveCallListener(ReceiveCallListener receiveCallListener) {
        this.receiveCallListener = receiveCallListener;
    }

    public void sendVideoCall(String targetUserID, ZIMCallInvitationSentCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        CallExtendedData extendedData = new CallExtendedData();
        extendedData.type = CallExtendedData.VIDEO_CALL;
        extendedData.userName = localUser.userName;

        ZIMCallInviteConfig config = new ZIMCallInviteConfig();
        config.extendedData = extendedData.toString();
        ZEGOSDKManager.getInstance().zimService.sendUserRequest(Collections.singletonList(targetUserID), config,
            new ZIMCallInvitationSentCallback() {
                @Override
                public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        sendCallRequest = new CallRequest();
                        sendCallRequest.requestID = requestID;
                    }
                    if (callback != null) {
                        callback.onCallInvitationSent(requestID, info, errorInfo);
                    }

                }
            });
    }


    public void sendVoiceCall(String targetUserID, ZIMCallInvitationSentCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        CallExtendedData extendedData = new CallExtendedData();
        extendedData.type = CallExtendedData.VOICE_CALL;
        extendedData.userName = localUser.userName;
        ZIMCallInviteConfig config = new ZIMCallInviteConfig();
        config.extendedData = extendedData.toString();
        ZEGOSDKManager.getInstance().zimService.sendUserRequest(Collections.singletonList(targetUserID), config,
            new ZIMCallInvitationSentCallback() {
                @Override
                public void onCallInvitationSent(String requestID, ZIMCallInvitationSentInfo info, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        sendCallRequest = new CallRequest();
                        sendCallRequest.requestID = requestID;
                    }
                    if (callback != null) {
                        callback.onCallInvitationSent(requestID, info, errorInfo);
                    }
                }
            });
    }

    public void rejectCallRequest(String requestID, UserRequestCallback callback) {
        ZEGOSDKManager.getInstance().zimService.rejectUserRequest(requestID, new ZIMCallRejectConfig(),
            new ZIMCallRejectionSentCallback() {
                @Override
                public void onCallRejectionSent(String callID, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        if (requestID.equals(recvCallRequest.requestID)) {
                            recvCallRequest = null;
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
                        if (recvCallRequest != null) {
                            if (requestID.equals(recvCallRequest.requestID)) {
                                recvCallRequest = null;
                            }
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
                        if (requestID.equals(recvCallRequest.requestID)) {
                            recvCallRequest = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    public void cancelCallRequest(String requestID, String userID, UserRequestCallback callback) {
        ZEGOSDKManager.getInstance().zimService.cancelUserRequest(Collections.singletonList(userID), requestID,
            new ZIMCallCancelConfig(), new ZIMCallCancelSentCallback() {
                @Override
                public void onCallCancelSent(String callID, ArrayList<String> errorInvitees, ZIMError errorInfo) {
                    if (errorInfo.code.value() == 0) {
                        if (requestID.equals(sendCallRequest.requestID)) {
                            sendCallRequest = null;
                        }
                    }
                    if (callback != null) {
                        callback.onUserRequestSend(errorInfo.code.value(), requestID);
                    }
                }
            });
    }

    public void endCall() {
        ZEGOSDKManager.getInstance().expressService.logoutRoom(null);
    }

    public void removeUserListeners() {
        receiveCallListener = null;
    }

    public void removeUserData() {
        sendCallRequest = null;
        recvCallRequest = null;
    }

    public static class CallRequest {

        public String requestID;
        public String targetUserID;
    }
}
