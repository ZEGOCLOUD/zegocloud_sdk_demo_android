package com.zegocloud.demo.bestpractice.internal.sdk.zim;

import android.app.Application;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallEndSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallQuitSentCallback;
import im.zego.zim.callback.ZIMCallRejectionSentCallback;
import im.zego.zim.callback.ZIMCallingInvitationSentCallback;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.callback.ZIMLogUploadedCallback;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesQueriedCallback;
import im.zego.zim.callback.ZIMRoomEnteredCallback;
import im.zego.zim.callback.ZIMRoomLeftCallback;
import im.zego.zim.callback.ZIMUserAvatarUrlUpdatedCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMAppConfig;
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallEndConfig;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallQuitConfig;
import im.zego.zim.entity.ZIMCallRejectConfig;
import im.zego.zim.entity.ZIMCallingInviteConfig;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomAttributesBatchOperationConfig;
import im.zego.zim.entity.ZIMRoomAttributesDeleteConfig;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConversationType;
import java.util.HashMap;
import java.util.List;

class ZIMProxy {

    private SimpleZIMEventHandler zimEventHandler;

    public void create(Application application, long appID, String appSign) {
        ZIMAppConfig zimAppConfig = new ZIMAppConfig();
        zimAppConfig.appID = appID;
        zimAppConfig.appSign = appSign;
        ZIM.create(zimAppConfig, application);

        zimEventHandler = new SimpleZIMEventHandler();
        if (getZIM() != null) {
            ZIM.getInstance().setEventHandler(zimEventHandler);
        }
    }

    public ZIM getZIM() {
        return ZIM.getInstance();
    }

    public void addEventHandler(ZIMEventHandler eventHandler) {
        zimEventHandler.addEventHandler(eventHandler);
    }

    public void removeEventHandler(ZIMEventHandler eventHandler) {
        zimEventHandler.removeEventHandler(eventHandler);
    }

    public void removeEventHandlerList(List<ZIMEventHandler> list) {
        zimEventHandler.removeEventHandlerList(list);
    }

    public void login(ZIMUserInfo zimUserInfo, String token, ZIMLoggedInCallback callback) {
        ZIM.getInstance().login(zimUserInfo, token, callback);
    }

    public void logout() {
        zimEventHandler.removeAllEventHandlers();
        ZIM.getInstance().logout();
    }

    public void enterRoom(ZIMRoomInfo roomInfo, ZIMRoomAdvancedConfig config, ZIMRoomEnteredCallback callback) {
        ZIM.getInstance().enterRoom(roomInfo, config, callback);
    }

    public void leaveRoom(String roomID, ZIMRoomLeftCallback callback) {
        ZIM.getInstance().leaveRoom(roomID, callback);
    }

    public void setRoomAttributes(HashMap<String, String> roomAttributes, String roomID,
        ZIMRoomAttributesSetConfig config, ZIMRoomAttributesOperatedCallback callback) {
        ZIM.getInstance().setRoomAttributes(roomAttributes, roomID, config, callback);
    }

    public void beginRoomPropertiesBatchOperation(String roomID, ZIMRoomAttributesBatchOperationConfig config) {
        ZIM.getInstance().beginRoomAttributesBatchOperation(roomID, config);
    }

    public void endRoomPropertiesBatchOperation(String roomID, ZIMRoomAttributesBatchOperatedCallback callback) {
        ZIM.getInstance().endRoomAttributesBatchOperation(roomID, callback);
    }

    public void deleteRoomAttributes(List<String> keys, String roomID, ZIMRoomAttributesDeleteConfig config,
        ZIMRoomAttributesOperatedCallback callback) {
        ZIM.getInstance().deleteRoomAttributes(keys, roomID, config, callback);
    }

    public void queryRoomAllAttributes(String roomID,
        ZIMRoomAttributesQueriedCallback zimRoomAttributesQueriedCallback) {
        ZIM.getInstance().queryRoomAllAttributes(roomID, zimRoomAttributesQueriedCallback);
    }

    public void updateUserAvatarUrl(String url, ZIMUserAvatarUrlUpdatedCallback callback) {
        ZIM.getInstance().updateUserAvatarUrl(url, callback);
    }

    public void queryUsersInfo(List<String> userIDList, ZIMUsersInfoQueryConfig config,
        ZIMUsersInfoQueriedCallback callback) {
        ZIM.getInstance().queryUsersInfo(userIDList, config, callback);
    }

    public void callInvite(List<String> list, ZIMCallInviteConfig config, ZIMCallInvitationSentCallback sentCallback) {
        ZIM.getInstance().callInvite(list, config, sentCallback);
    }

    public void callingInvite(List<String> invitees, String callID, ZIMCallingInviteConfig config,
        ZIMCallingInvitationSentCallback callback) {
        ZIM.getInstance().callingInvite(invitees, callID, config, callback);
    }

    public void callAccept(String callID, ZIMCallAcceptConfig config, ZIMCallAcceptanceSentCallback callback) {
        ZIM.getInstance().callAccept(callID, config, callback);
    }

    public void callReject(String callID, ZIMCallRejectConfig config, ZIMCallRejectionSentCallback callback) {
        ZIM.getInstance().callReject(callID, config, callback);
    }

    public void callCancel(List<String> list, String callID, ZIMCallCancelConfig config,
        ZIMCallCancelSentCallback callback) {
        ZIM.getInstance().callCancel(list, callID, config, callback);
    }

    public void callQuit(String callID, ZIMCallQuitConfig config, ZIMCallQuitSentCallback callback) {
        ZIM.getInstance().callQuit(callID, config, callback);
    }

    public void callEnd(String callID, ZIMCallEndConfig config, ZIMCallEndSentCallback callback) {
        ZIM.getInstance().callEnd(callID, config, callback);
    }

    public void sendMessage(ZIMMessage message, String toConversationID, ZIMConversationType conversationType,
        ZIMMessageSendConfig config, ZIMMessageSentCallback callback) {
        ZIM.getInstance().sendMessage(message, toConversationID, conversationType, config, callback);
    }

    public void uploadLog(ZIMLogUploadedCallback callback) {
        ZIM.getInstance().uploadLog(callback);
    }
}
