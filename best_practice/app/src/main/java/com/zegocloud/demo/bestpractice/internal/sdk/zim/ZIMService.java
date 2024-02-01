package com.zegocloud.demo.bestpractice.internal.sdk.zim;

import android.app.Application;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMCallAcceptanceSentCallback;
import im.zego.zim.callback.ZIMCallCancelSentCallback;
import im.zego.zim.callback.ZIMCallEndSentCallback;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.callback.ZIMCallJoinSentCallback;
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
import im.zego.zim.entity.ZIMCallAcceptConfig;
import im.zego.zim.entity.ZIMCallCancelConfig;
import im.zego.zim.entity.ZIMCallEndConfig;
import im.zego.zim.entity.ZIMCallInvitationAcceptedInfo;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationEndedInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationRejectedInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import im.zego.zim.entity.ZIMCallInviteConfig;
import im.zego.zim.entity.ZIMCallJoinConfig;
import im.zego.zim.entity.ZIMCallQuitConfig;
import im.zego.zim.entity.ZIMCallRejectConfig;
import im.zego.zim.entity.ZIMCallUserStateChangeInfo;
import im.zego.zim.entity.ZIMCallingInviteConfig;
import im.zego.zim.entity.ZIMCommandMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMRoomAdvancedConfig;
import im.zego.zim.entity.ZIMRoomAttributesBatchOperationConfig;
import im.zego.zim.entity.ZIMRoomAttributesDeleteConfig;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomFullInfo;
import im.zego.zim.entity.ZIMRoomInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMRoomAttributesUpdateAction;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

public class ZIMService {

    private ZIMUserInfo currentUser;
    private ZIMRoomInfo currentRoom;
    private ZIMProxy zimProxy = new ZIMProxy();
    private List<IZIMEventHandler> handlerList = new CopyOnWriteArrayList<>();
    private List<IZIMEventHandler> autoDeleteHandlerList = new CopyOnWriteArrayList<>();
    private Map<String, RoomRequest> roomRequestMap = new HashMap<>();
    private ZIMEventHandler initEventHandler;
    private ArrayList<ZIMUserFullInfo> zimUserInfoList = new ArrayList<>();

    public void initSDK(Application application, long appID, String appSign) {
        zimProxy.create(application, appID, appSign);

        initEventHandler = new ZIMEventHandler() {

            @Override
            public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
                JSONObject extendedData) {
                super.onConnectionStateChanged(zim, state, event, extendedData);
                Timber.d(
                    "onConnectionStateChanged() called with: zim = [" + zim + "], state = [" + state + "], event = ["
                        + event + "], extendedData = [" + extendedData + "]");
            }

            @Override
            public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData,
                String roomID) {
                super.onRoomStateChanged(zim, state, event, extendedData, roomID);
                Timber.d(
                    "onRoomStateChanged() called with: zim = [" + zim + "], state = [" + state + "], event = [" + event
                        + "], extendedData = [" + extendedData + "], roomID = [" + roomID + "]");
            }

            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                super.onReceiveRoomMessage(zim, messageList, fromRoomID);

                for (ZIMMessage zimMessage : messageList) {
                    if (zimMessage instanceof ZIMCommandMessage) {
                        ZIMCommandMessage commandMessage = (ZIMCommandMessage) zimMessage;
                        String message = new String(commandMessage.message, StandardCharsets.UTF_8);
                        try {
                            JSONObject jsonObject = new JSONObject(message);
                            if (jsonObject.has("action_type")) {
                                jsonObject.put("message_id", String.valueOf(commandMessage.getMessageID()));
                                if (currentUser != null) {
                                    onReceiveRoomRequestMessage(jsonObject);
                                }
                            } else {
                                // onRoomCommand
                                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                                    handler.onRoomCommandReceived(zimMessage.getSenderUserID(), message);
                                }
                                for (IZIMEventHandler handler : handlerList) {
                                    handler.onRoomCommandReceived(zimMessage.getSenderUserID(), message);
                                }

                            }
                        } catch (JSONException e) {
                            //  onRoomCommand
                            for (IZIMEventHandler handler : autoDeleteHandlerList) {
                                handler.onRoomCommandReceived(zimMessage.getSenderUserID(), message);
                            }
                            for (IZIMEventHandler handler : handlerList) {
                                handler.onRoomCommandReceived(zimMessage.getSenderUserID(), message);
                            }
                        }
                    }
                }
            }

            @Override
            public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
                super.onRoomAttributesUpdated(zim, info, roomID);
                HashMap<String, String> roomAttributes = info.roomAttributes;
                List<Map<String, String>> setProperties;
                List<Map<String, String>> deleteProperties;
                if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                    setProperties = Collections.singletonList(roomAttributes);
                } else {
                    setProperties = new ArrayList<>();
                }
                if (info.action == ZIMRoomAttributesUpdateAction.DELETE) {
                    deleteProperties = Collections.singletonList(roomAttributes);
                } else {
                    deleteProperties = new ArrayList<>();
                }

                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onRoomAttributesUpdated2(setProperties, deleteProperties);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onRoomAttributesUpdated2(setProperties, deleteProperties);
                }
            }

            @Override
            public void onRoomAttributesBatchUpdated(ZIM zim, ArrayList<ZIMRoomAttributesUpdateInfo> infos,
                String roomID) {
                super.onRoomAttributesBatchUpdated(zim, infos, roomID);

                List<Map<String, String>> setProperties = new ArrayList<>();
                List<Map<String, String>> deleteProperties = new ArrayList<>();
                for (ZIMRoomAttributesUpdateInfo info : infos) {
                    if (info.action == ZIMRoomAttributesUpdateAction.SET) {
                        setProperties.add(info.roomAttributes);
                    } else if (info.action == ZIMRoomAttributesUpdateAction.DELETE) {
                        deleteProperties.add(info.roomAttributes);
                    }
                }

                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onRoomAttributesUpdated2(setProperties, deleteProperties);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onRoomAttributesUpdated2(setProperties, deleteProperties);
                }
            }

            @Override
            public void onCallInvitationReceived(ZIM zim, ZIMCallInvitationReceivedInfo info, String callID) {
                super.onCallInvitationReceived(zim, info, callID);
                Timber.d("onCallInvitationReceived() called with: zim = [" + zim + "], info = [" + info + "], callID = ["
                        + callID + "]");
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onInComingUserRequestReceived(callID, info);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onInComingUserRequestReceived(callID, info);
                }
            }

            @Override
            public void onUserInfoUpdated(ZIM zim, ZIMUserFullInfo info) {
                super.onUserInfoUpdated(zim, info);
                Timber.d("onUserInfoUpdated() called with: zim = [" + zim + "], info = [" + info + "]");
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onUserInfoUpdated(zim, info);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onUserInfoUpdated(zim, info);
                }
            }

            @Override
            public void onCallInvitationAccepted(ZIM zim, ZIMCallInvitationAcceptedInfo info, String callID) {
                super.onCallInvitationAccepted(zim, info, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onOutgoingUserRequestAccepted(callID, info.invitee, info.extendedData);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onOutgoingUserRequestAccepted(callID, info.invitee, info.extendedData);
                }
            }

            @Override
            public void onCallInvitationCancelled(ZIM zim, ZIMCallInvitationCancelledInfo info, String callID) {
                super.onCallInvitationCancelled(zim, info, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onInComingUserRequestCancelled(callID, info);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onInComingUserRequestCancelled(callID, info);
                }
            }

            @Override
            public void onCallInvitationRejected(ZIM zim, ZIMCallInvitationRejectedInfo info, String callID) {
                super.onCallInvitationRejected(zim, info, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onOutgoingUserRequestRejected(callID, info.invitee, info.extendedData);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onOutgoingUserRequestRejected(callID, info.invitee, info.extendedData);
                }
            }

            @Override
            public void onCallInvitationTimeout(ZIM zim, String callID) {
                super.onCallInvitationTimeout(zim, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onInComingUserRequestTimeout(callID, null);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onInComingUserRequestTimeout(callID, null);
                }
            }

            @Override
            public void onCallInviteesAnsweredTimeout(ZIM zim, ArrayList<String> invitees, String callID) {
                super.onCallInviteesAnsweredTimeout(zim, invitees, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onOutgoingUserRequestTimeout(callID);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onOutgoingUserRequestTimeout(callID);
                }
            }

            @Override
            public void onCallUserStateChanged(ZIM zim, ZIMCallUserStateChangeInfo info, String callID) {
                super.onCallUserStateChanged(zim, info, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onUserRequestStateChanged(info, callID);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onUserRequestStateChanged(info, callID);
                }
            }

            @Override
            public void onCallInvitationEnded(ZIM zim, ZIMCallInvitationEndedInfo info, String callID) {
                super.onCallInvitationEnded(zim, info, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onUserRequestEnded(callID, info);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onUserRequestEnded(callID, info);
                }
            }

            @Override
            public void onCallInvitationTimeout(ZIM zim, ZIMCallInvitationTimeoutInfo info, String callID) {
                super.onCallInvitationTimeout(zim, info, callID);
                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onInComingUserRequestTimeout(callID, info);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onInComingUserRequestTimeout(callID, info);
                }
            }

            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);

                for (ZIMUserInfo zimUserInfo : memberList) {
                    RoomRequest roomRequest = getRoomRequestBySenderID(zimUserInfo.userID);
                    if (roomRequest != null) {
                        roomRequestMap.remove(roomRequest.requestID);
                    }
                }
            }
        };
        zimProxy.addEventHandler(initEventHandler);
    }

    private void onReceiveRoomRequestMessage(JSONObject jsonObject) throws JSONException {
        String sender = jsonObject.getString("sender_id");
        String receiver = jsonObject.getString("receiver_id");
        int actionType = jsonObject.getInt("action_type");
        String extendedData = jsonObject.getString("extended_data");
        String messageID = jsonObject.getString("message_id");
        if (currentUser.userID.equals(receiver)) {
            if (actionType == RoomRequestAction.ACTION_REQUEST) {
                RoomRequest request = new RoomRequest();
                request.requestID = messageID;
                request.receiver = receiver;
                request.sender = sender;
                request.actionType = actionType;
                request.extendedData = extendedData;
                roomRequestMap.put(request.requestID, request);

                for (IZIMEventHandler handler : autoDeleteHandlerList) {
                    handler.onInComingRoomRequestReceived(request.requestID, request.extendedData);
                }
                for (IZIMEventHandler handler : handlerList) {
                    handler.onInComingRoomRequestReceived(request.requestID, request.extendedData);
                }
            } else if (actionType == RoomRequestAction.ACTION_ACCEPT) {
                String requestID = jsonObject.getString("request_id");
                RoomRequest request = roomRequestMap.remove(requestID);
                if (request != null) {
                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onOutgoingRoomRequestAccepted(request.requestID, request.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onOutgoingRoomRequestAccepted(request.requestID, request.extendedData);
                    }
                }
            } else if (actionType == RoomRequestAction.ACTION_CANCEL) {
                String requestID = jsonObject.getString("request_id");
                RoomRequest request = roomRequestMap.remove(requestID);
                if (request != null) {
                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onInComingRoomRequestCancelled(request.requestID, request.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onInComingRoomRequestCancelled(request.requestID, request.extendedData);
                    }
                }
            } else if (actionType == RoomRequestAction.ACTION_REJECT) {
                String requestID = jsonObject.getString("request_id");
                RoomRequest request = roomRequestMap.remove(requestID);
                if (request != null) {
                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onOutgoingRoomRequestRejected(request.requestID, request.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onOutgoingRoomRequestRejected(request.requestID, request.extendedData);
                    }
                }
            }
        }
    }

    public void connectUser(String userID, String userName, ZIMLoggedInCallback callback) {
        connectUser(userID, userName, "", callback);
    }

    public void connectUser(String userID, String userName, String token, ZIMLoggedInCallback callback) {
        ZIMUserInfo zimUserInfo = new ZIMUserInfo();
        zimUserInfo.userID = userID;
        zimUserInfo.userName = userName;
        if (zimProxy.getZIM() == null) {
            return;
        }
        zimProxy.login(zimUserInfo, token, new ZIMLoggedInCallback() {
            @Override
            public void onLoggedIn(ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    currentUser = zimUserInfo;
                }
                if (callback != null) {
                    callback.onLoggedIn(errorInfo);
                }
            }
        });
    }

    public void disconnectUser() {
        if (zimProxy.getZIM() == null) {
            return;
        }
        currentUser = null;
        removeUserData();
        removeUserListeners();
        zimProxy.logout();
        // keep initEventHandler not cleared when user logout account
        zimProxy.addEventHandler(initEventHandler);
    }

    public void loginRoom(String roomID, ZIMRoomEnteredCallback callback) {
        if (zimProxy.getZIM() == null) {
            return;
        }
        ZIMRoomInfo zimRoomInfo = new ZIMRoomInfo();
        zimRoomInfo.roomID = roomID;
        ZIMRoomAdvancedConfig config = new ZIMRoomAdvancedConfig();

        zimProxy.enterRoom(zimRoomInfo, config, new ZIMRoomEnteredCallback() {
            @Override
            public void onRoomEntered(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    currentRoom = zimRoomInfo;
                }
                if (callback != null) {
                    callback.onRoomEntered(roomInfo, errorInfo);
                }
            }
        });
    }

    public void logoutRoom(ZIMRoomLeftCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        removeRoomData();
        removeAutoDeleteRoomListeners();
        zimProxy.leaveRoom(currentRoom.roomID, callback);
        currentRoom = null;
    }

    public void setRoomAttributes(String key, String value, ZIMRoomAttributesSetConfig config,
        ZIMRoomAttributesOperatedCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(key, value);
        zimProxy.setRoomAttributes(attributes, currentRoom.roomID, config, callback);
    }

    public void setRoomAttributes(HashMap<String, String> attributes, ZIMRoomAttributesSetConfig config,
        ZIMRoomAttributesOperatedCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        zimProxy.setRoomAttributes(attributes, currentRoom.roomID, config, callback);
    }

    public void beginRoomPropertiesBatchOperation(ZIMRoomAttributesBatchOperationConfig config) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        zimProxy.beginRoomPropertiesBatchOperation(currentRoom.roomID, config);
    }

    public void endRoomPropertiesBatchOperation(ZIMRoomAttributesBatchOperatedCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        zimProxy.endRoomPropertiesBatchOperation(currentRoom.roomID, callback);
    }

    public void deleteRoomAttributes(List<String> keys, ZIMRoomAttributesOperatedCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        ZIMRoomAttributesDeleteConfig config = new ZIMRoomAttributesDeleteConfig();
        config.isForce = true;
        zimProxy.deleteRoomAttributes(keys, currentRoom.roomID, config, callback);
    }

    public void queryRoomProperties(String roomID, ZIMRoomAttributesQueriedCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null) {
            return;
        }
        zimProxy.queryRoomAllAttributes(roomID, callback);
    }

    public void updateUserAvatarUrl(String url, ZIMUserAvatarUrlUpdatedCallback callback) {
        Timber.d("updateUserAvatarUrl() called with: url = [" + url + "], callback = [" + callback + "]");
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.updateUserAvatarUrl(url, new ZIMUserAvatarUrlUpdatedCallback() {
            @Override
            public void onUserAvatarUrlUpdated(String userAvatarUrl, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    boolean find = false;
                    for (ZIMUserFullInfo userFullInfo : zimUserInfoList) {
                        if (Objects.equals(userFullInfo.baseInfo.userID, currentUser.userID)) {
                            userFullInfo.userAvatarUrl = userAvatarUrl;
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        ZIMUserFullInfo userFullInfo = new ZIMUserFullInfo();
                        userFullInfo.baseInfo = currentUser;
                        userFullInfo.userAvatarUrl = userAvatarUrl;
                        zimUserInfoList.add(userFullInfo);
                    }
                }
                if (callback != null) {
                    callback.onUserAvatarUrlUpdated(userAvatarUrl, errorInfo);
                }
            }
        });
    }

    public void queryUsersInfo(List<String> userIDList, ZIMUsersInfoQueriedCallback callback) {
        if (zimProxy.getZIM() == null) {
            return;
        }
        ZIMUsersInfoQueryConfig config = new ZIMUsersInfoQueryConfig();
        zimProxy.queryUsersInfo(userIDList, config, new ZIMUsersInfoQueriedCallback() {
            @Override
            public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                Timber.d(
                    "onUsersInfoQueried() called with: userList = [" + userList + "], errorUserList = [" + errorUserList
                        + "], errorInfo = [" + errorInfo + "]");
                for (ZIMUserFullInfo zimUserFullInfo : userList) {
                    boolean find = false;
                    for (ZIMUserFullInfo userFullInfo : zimUserInfoList) {
                        if (Objects.equals(userFullInfo.baseInfo.userID, zimUserFullInfo.baseInfo.userID)) {
                            userFullInfo.baseInfo.userName = zimUserFullInfo.baseInfo.userName;
                            userFullInfo.userAvatarUrl = zimUserFullInfo.userAvatarUrl;
                            userFullInfo.extendedData = zimUserFullInfo.extendedData;
                            find = true;
                            break;
                        }
                    }
                    if (!find) {
                        zimUserInfoList.add(zimUserFullInfo);
                    }
                }
                if (callback != null) {
                    callback.onUsersInfoQueried(userList, errorUserList, errorInfo);
                }
            }
        });
    }

    @Nullable
    public String getUserAvatar(String userID) {
        for (ZIMUserFullInfo userFullInfo : zimUserInfoList) {
            if (Objects.equals(userFullInfo.baseInfo.userID, userID)) {
                return userFullInfo.userAvatarUrl;
            }
        }
        return null;
    }


    public ZIMUserFullInfo getUserInfo(String userID) {
        for (ZIMUserFullInfo userFullInfo : zimUserInfoList) {
            if (Objects.equals(userFullInfo.baseInfo.userID, userID)) {
                return userFullInfo;
            }
        }
        return null;
    }

    public void addEventHandler(IZIMEventHandler zimEventHandler) {
        addEventHandler(zimEventHandler, true);
    }

    public void addEventHandler(IZIMEventHandler zimEventHandler, boolean autoDelete) {
        Timber.d(
            "addEventHandler() called with: zimEventHandler = [" + zimEventHandler + "], autoDelete = [" + autoDelete
                + "]");
        if (autoDelete) {
            autoDeleteHandlerList.add(zimEventHandler);
        } else {
            handlerList.add(zimEventHandler);
        }
        zimProxy.addEventHandler(zimEventHandler);
    }

    public void removeEventHandler(IZIMEventHandler zimEventHandler) {
        autoDeleteHandlerList.remove(zimEventHandler);
        handlerList.remove(zimEventHandler);
        zimProxy.removeEventHandler(zimEventHandler);
    }

    public void sendUserRequest(List<String> list, ZIMCallInviteConfig config,
        ZIMCallInvitationSentCallback sentCallback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callInvite(list, config, sentCallback);
    }

    public void addUserToRequest(List<String> invitees, String requestID, ZIMCallingInviteConfig config,
        ZIMCallingInvitationSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callingInvite(invitees, requestID, config, callback);
    }

    public void acceptUserRequest(String requestID, ZIMCallAcceptConfig config,
        ZIMCallAcceptanceSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callAccept(requestID, config, callback);
    }

    public void joinUserRequest(String requestID, ZIMCallJoinConfig config, ZIMCallJoinSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callJoin(requestID, config, callback);
    }

    public void rejectUserRequest(String requestID, ZIMCallRejectConfig config, ZIMCallRejectionSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callReject(requestID, config, callback);
    }

    public void cancelUserRequest(List<String> list, String requestID, ZIMCallCancelConfig config,
        ZIMCallCancelSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callCancel(list, requestID, config, callback);
    }

    public void quitUserRequest(String callID, ZIMCallQuitConfig config, ZIMCallQuitSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callQuit(callID, config, callback);
    }

    public void endUserRequest(String callID, ZIMCallEndConfig config, ZIMCallEndSentCallback callback) {
        if (zimProxy.getZIM() == null || currentUser == null) {
            return;
        }
        zimProxy.callEnd(callID, config, callback);
    }


    public void sendRoomCommand(String command, RoomCommandCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null || currentUser == null) {
            return;
        }
        byte[] bytes = command.getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        zimProxy.sendMessage(commandMessage, currentRoom.roomID, ZIMConversationType.ROOM, new ZIMMessageSendConfig(),
            new ZIMMessageSentCallback() {
                @Override
                public void onMessageAttached(ZIMMessage message) {

                }

                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    if (callback != null) {
                        callback.onSendRoomCommand(errorInfo.code.value(), errorInfo.message,command);
                    }
                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onSendRoomCommand(errorInfo.code.value(), errorInfo.message,command);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onSendRoomCommand(errorInfo.code.value(), errorInfo.message,command);
                    }
                }
            });
    }

    public void sendRoomRequest(String receiverID, String extendedData, RoomRequestCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null || currentUser == null) {
            return;
        }
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.receiver = receiverID;
        roomRequest.sender = currentUser.userID;
        roomRequest.extendedData = extendedData;
        roomRequest.actionType = RoomRequestAction.ACTION_REQUEST;

        byte[] bytes = roomRequest.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        zimProxy.sendMessage(commandMessage, currentRoom.roomID, ZIMConversationType.ROOM, new ZIMMessageSendConfig(),
            new ZIMMessageSentCallback() {
                @Override
                public void onMessageAttached(ZIMMessage message) {

                }

                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        roomRequest.requestID = String.valueOf(message.getMessageID());
                        roomRequestMap.put(roomRequest.requestID, roomRequest);
                    }
                    if (callback != null) {
                        callback.onRoomRequestSend(errorInfo.code.value(), roomRequest.requestID);
                    }

                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onSendRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onSendRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                }
            });
    }

    public void acceptRoomRequest(String requestID, RoomRequestCallback callback) {
        acceptRoomRequest(requestID, null, callback);
    }

    public void acceptRoomRequest(String requestID, String extendedData, RoomRequestCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null || currentUser == null) {
            return;
        }
        RoomRequest roomRequest = getRoomRequestByRequestID(requestID);
        if (roomRequest == null) {
            return;
        }
        RoomRequest copyRequest = roomRequest.copy();
        copyRequest.actionType = RoomRequestAction.ACTION_ACCEPT;
        copyRequest.receiver = copyRequest.sender;
        copyRequest.sender = currentUser.userID;
        if (extendedData != null) {
            copyRequest.extendedData = extendedData;
        }

        byte[] bytes = copyRequest.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        zimProxy.sendMessage(commandMessage, currentRoom.roomID, ZIMConversationType.ROOM, new ZIMMessageSendConfig(),
            new ZIMMessageSentCallback() {
                @Override
                public void onMessageAttached(ZIMMessage message) {

                }

                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        roomRequest.actionType = copyRequest.actionType;
                        roomRequest.receiver = copyRequest.receiver;
                        roomRequest.sender = copyRequest.sender;
                    }
                    if (callback != null) {
                        callback.onRoomRequestSend(errorInfo.code.value(), roomRequest.requestID);
                    }

                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onAcceptIncomingRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onAcceptIncomingRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    roomRequestMap.remove(roomRequest.requestID);
                }
            });
    }

    public void rejectRoomRequest(String requestID, RoomRequestCallback callback) {
        rejectRoomRequest(requestID, null, callback);
    }

    public void rejectRoomRequest(String requestID, String extendedData, RoomRequestCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null || currentUser == null) {
            return;
        }
        RoomRequest roomRequest = getRoomRequestByRequestID(requestID);
        if (roomRequest == null) {
            return;
        }
        RoomRequest copyRequest = roomRequest.copy();
        copyRequest.actionType = RoomRequestAction.ACTION_REJECT;
        copyRequest.receiver = roomRequest.sender;
        copyRequest.sender = currentUser.userID;
        if (extendedData != null) {
            copyRequest.extendedData = extendedData;
        }

        byte[] bytes = copyRequest.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        zimProxy.sendMessage(commandMessage, currentRoom.roomID, ZIMConversationType.ROOM, new ZIMMessageSendConfig(),
            new ZIMMessageSentCallback() {
                @Override
                public void onMessageAttached(ZIMMessage message) {

                }

                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        roomRequest.actionType = copyRequest.actionType;
                        roomRequest.receiver = copyRequest.receiver;
                        roomRequest.sender = copyRequest.sender;
                    }
                    if (callback != null) {
                        callback.onRoomRequestSend(errorInfo.code.value(), roomRequest.requestID);
                    }

                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onRejectIncomingRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onRejectIncomingRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    roomRequestMap.remove(roomRequest.requestID);
                }
            });
    }

    public void cancelRoomRequest(String requestID, RoomRequestCallback callback) {
        cancelRoomRequest(requestID, null, callback);
    }

    public void cancelRoomRequest(String requestID, String extendedData, RoomRequestCallback callback) {
        if (zimProxy.getZIM() == null || currentRoom == null || currentUser == null) {
            return;
        }
        RoomRequest roomRequest = getRoomRequestByRequestID(requestID);
        if (roomRequest == null) {
            return;
        }
        RoomRequest copyRequest = roomRequest.copy();
        copyRequest.actionType = RoomRequestAction.ACTION_CANCEL;
        if (extendedData != null) {
            copyRequest.extendedData = extendedData;
        }

        byte[] bytes = copyRequest.toString().getBytes(StandardCharsets.UTF_8);
        ZIMCommandMessage commandMessage = new ZIMCommandMessage(bytes);
        zimProxy.sendMessage(commandMessage, currentRoom.roomID, ZIMConversationType.ROOM, new ZIMMessageSendConfig(),
            new ZIMMessageSentCallback() {
                @Override
                public void onMessageAttached(ZIMMessage message) {

                }

                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        roomRequest.actionType = copyRequest.actionType;
                    }
                    if (callback != null) {
                        callback.onRoomRequestSend(errorInfo.code.value(), roomRequest.requestID);
                    }

                    for (IZIMEventHandler handler : autoDeleteHandlerList) {
                        handler.onCancelOutgoingRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    for (IZIMEventHandler handler : handlerList) {
                        handler.onCancelOutgoingRoomRequest(errorInfo.code.value(), roomRequest.requestID,
                            roomRequest.extendedData);
                    }
                    roomRequestMap.remove(roomRequest.requestID);
                }
            });
    }

    public RoomRequest getRoomRequestBySenderID(String userID) {
        for (RoomRequest roomRequest : roomRequestMap.values()) {
            if (roomRequest.sender.equals(userID)) {
                return roomRequest;
            }
        }
        return null;
    }

    public void uploadLog(ZIMLogUploadedCallback callback) {
        zimProxy.uploadLog(callback);
    }

    public RoomRequest getRoomRequestByRequestID(String requestID) {
        return roomRequestMap.get(requestID);
    }

    public List<RoomRequest> getMyReceivedRoomRequests() {
        List<RoomRequest> list = new ArrayList<>();
        if (currentUser != null) {
            for (RoomRequest roomRequest : roomRequestMap.values()) {
                if (roomRequest.receiver.equals(currentUser.userID)) {
                    list.add(roomRequest);
                }
            }
        }
        return list;
    }

    public void removeAllRequest() {
        roomRequestMap.clear();
    }

    public void removeRoomData() {
        roomRequestMap.clear();
    }

    public void removeUserData() {
        currentUser = null;
        zimUserInfoList.clear();
    }

    public void removeAutoDeleteRoomListeners() {
        zimProxy.removeEventHandlerList(new ArrayList<>(autoDeleteHandlerList));
        autoDeleteHandlerList.clear();
    }

    public void removeUserListeners() {
        removeAutoDeleteRoomListeners();
        zimProxy.removeEventHandlerList(new ArrayList<>(handlerList));
        handlerList.clear();
    }

    public @interface RoomRequestAction {

        int ACTION_REQUEST = 0;
        int ACTION_ACCEPT = 1;
        int ACTION_REJECT = 2;
        int ACTION_CANCEL = 3;
    }
}
