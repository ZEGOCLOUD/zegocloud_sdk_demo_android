package com.zegocloud.demo.bestpractice.internal.sdk.zim;

import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMCallInvitationAcceptedInfo;
import im.zego.zim.entity.ZIMCallInvitationCancelledInfo;
import im.zego.zim.entity.ZIMCallInvitationEndedInfo;
import im.zego.zim.entity.ZIMCallInvitationReceivedInfo;
import im.zego.zim.entity.ZIMCallInvitationRejectedInfo;
import im.zego.zim.entity.ZIMCallInvitationTimeoutInfo;
import im.zego.zim.entity.ZIMCallUserStateChangeInfo;
import im.zego.zim.entity.ZIMConversationChangeInfo;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMGroupAttributesUpdateInfo;
import im.zego.zim.entity.ZIMGroupFullInfo;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageReaction;
import im.zego.zim.entity.ZIMMessageReceiptInfo;
import im.zego.zim.entity.ZIMMessageSentStatusChangeInfo;
import im.zego.zim.entity.ZIMRevokeMessage;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomMemberAttributesUpdateInfo;
import im.zego.zim.entity.ZIMRoomOperatedInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMGroupEvent;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import im.zego.zim.enums.ZIMGroupState;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;

class SimpleZIMEventHandler extends ZIMEventHandler {

    private final List<ZIMEventHandler> handlerList = new CopyOnWriteArrayList<>();

    @Override
    public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData) {
        super.onConnectionStateChanged(zim, state, event, extendedData);
        for (ZIMEventHandler handler : handlerList) {
            handler.onConnectionStateChanged(zim, state, event, extendedData);
        }
    }

    @Override
    public void onError(ZIM zim, ZIMError errorInfo) {
        super.onError(zim, errorInfo);
        for (ZIMEventHandler handler : handlerList) {
            handler.onError(zim, errorInfo);
        }
    }

    @Override
    public void onTokenWillExpire(ZIM zim, int second) {
        super.onTokenWillExpire(zim, second);
        for (ZIMEventHandler handler : handlerList) {
            handler.onTokenWillExpire(zim, second);
        }
    }

    @Override
    public void onConversationChanged(ZIM zim, ArrayList<ZIMConversationChangeInfo> conversationChangeInfoList) {
        super.onConversationChanged(zim, conversationChangeInfoList);
        for (ZIMEventHandler handler : handlerList) {
            handler.onConversationChanged(zim, conversationChangeInfoList);
        }
    }

    @Override
    public void onConversationTotalUnreadMessageCountUpdated(ZIM zim, int totalUnreadMessageCount) {
        super.onConversationTotalUnreadMessageCountUpdated(zim, totalUnreadMessageCount);
        for (ZIMEventHandler handler : handlerList) {
            handler.onConversationTotalUnreadMessageCountUpdated(zim, totalUnreadMessageCount);
        }
    }

    @Override
    public void onConversationMessageReceiptChanged(ZIM zim, ArrayList<ZIMMessageReceiptInfo> infos) {
        super.onConversationMessageReceiptChanged(zim, infos);
        for (ZIMEventHandler handler : handlerList) {
            handler.onConversationMessageReceiptChanged(zim, infos);
        }
    }

    @Override
    public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
        super.onReceivePeerMessage(zim, messageList, fromUserID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onReceivePeerMessage(zim, messageList, fromUserID);
        }
    }

    @Override
    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        super.onReceiveRoomMessage(zim, messageList, fromRoomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onReceiveRoomMessage(zim, messageList, fromRoomID);
        }
    }

    @Override
    public void onReceiveGroupMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromGroupID) {
        super.onReceiveGroupMessage(zim, messageList, fromGroupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onReceiveGroupMessage(zim, messageList, fromGroupID);
        }
    }

    @Override
    public void onMessageRevokeReceived(ZIM zim, ArrayList<ZIMRevokeMessage> messageList) {
        super.onMessageRevokeReceived(zim, messageList);
        for (ZIMEventHandler handler : handlerList) {
            handler.onMessageRevokeReceived(zim, messageList);
        }
    }

    @Override
    public void onMessageReceiptChanged(ZIM zim, ArrayList<ZIMMessageReceiptInfo> infos) {
        super.onMessageReceiptChanged(zim, infos);
        for (ZIMEventHandler handler : handlerList) {
            handler.onMessageReceiptChanged(zim, infos);
        }
    }

    @Override
    public void onMessageSentStatusChanged(ZIM zim,
        ArrayList<ZIMMessageSentStatusChangeInfo> messageSentStatusChangeInfoList) {
        super.onMessageSentStatusChanged(zim, messageSentStatusChangeInfoList);
        for (ZIMEventHandler handler : handlerList) {
            handler.onMessageSentStatusChanged(zim, messageSentStatusChangeInfoList);
        }
    }

    @Override
    public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        super.onRoomMemberJoined(zim, memberList, roomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onRoomMemberJoined(zim, memberList, roomID);
        }
    }

    @Override
    public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
        super.onRoomMemberLeft(zim, memberList, roomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onRoomMemberLeft(zim, memberList, roomID);
        }
    }

    @Override
    public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData,
        String roomID) {
        super.onRoomStateChanged(zim, state, event, extendedData, roomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onRoomStateChanged(zim, state, event, extendedData, roomID);
        }
    }

    @Override
    public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
        super.onRoomAttributesUpdated(zim, info, roomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onRoomAttributesUpdated(zim, info, roomID);
        }
    }

    @Override
    public void onRoomAttributesBatchUpdated(ZIM zim, ArrayList<ZIMRoomAttributesUpdateInfo> infos, String roomID) {
        super.onRoomAttributesBatchUpdated(zim, infos, roomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onRoomAttributesBatchUpdated(zim, infos, roomID);
        }
    }

    @Override
    public void onRoomMemberAttributesUpdated(ZIM zim, ArrayList<ZIMRoomMemberAttributesUpdateInfo> infos,
        ZIMRoomOperatedInfo operatedInfo, String roomID) {
        super.onRoomMemberAttributesUpdated(zim, infos, operatedInfo, roomID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onRoomMemberAttributesUpdated(zim, infos, operatedInfo, roomID);
        }
    }

    @Override
    public void onGroupStateChanged(ZIM zim, ZIMGroupState state, ZIMGroupEvent event,
        ZIMGroupOperatedInfo operatedInfo, ZIMGroupFullInfo groupInfo) {
        super.onGroupStateChanged(zim, state, event, operatedInfo, groupInfo);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupStateChanged(zim, state, event, operatedInfo, groupInfo);
        }
    }

    @Override
    public void onGroupNameUpdated(ZIM zim, String groupName, ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupNameUpdated(zim, groupName, operatedInfo, groupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupNameUpdated(zim, groupName, operatedInfo, groupID);
        }
    }

    @Override
    public void onGroupAvatarUrlUpdated(ZIM zim, String groupAvatarUrl, ZIMGroupOperatedInfo operatedInfo,
        String groupID) {
        super.onGroupAvatarUrlUpdated(zim, groupAvatarUrl, operatedInfo, groupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupAvatarUrlUpdated(zim, groupAvatarUrl, operatedInfo, groupID);
        }
    }

    @Override
    public void onGroupNoticeUpdated(ZIM zim, String groupNotice, ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupNoticeUpdated(zim, groupNotice, operatedInfo, groupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupNoticeUpdated(zim, groupNotice, operatedInfo, groupID);
        }
    }

    @Override
    public void onGroupAttributesUpdated(ZIM zim, ArrayList<ZIMGroupAttributesUpdateInfo> infos,
        ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupAttributesUpdated(zim, infos, operatedInfo, groupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupAttributesUpdated(zim, infos, operatedInfo, groupID);
        }
    }

    @Override
    public void onGroupMemberStateChanged(ZIM zim, ZIMGroupMemberState state, ZIMGroupMemberEvent event,
        ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupMemberStateChanged(zim, state, event, userList, operatedInfo, groupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupMemberStateChanged(zim, state, event, userList, operatedInfo, groupID);
        }
    }

    @Override
    public void onGroupMemberInfoUpdated(ZIM zim, ArrayList<ZIMGroupMemberInfo> userList,
        ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupMemberInfoUpdated(zim, userList, operatedInfo, groupID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onGroupMemberInfoUpdated(zim, userList, operatedInfo, groupID);
        }
    }

    @Override
    public void onCallInvitationReceived(ZIM zim, ZIMCallInvitationReceivedInfo info, String callID) {
        super.onCallInvitationReceived(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationReceived(zim, info, callID);
        }
    }

    @Override
    public void onCallInvitationCancelled(ZIM zim, ZIMCallInvitationCancelledInfo info, String callID) {
        super.onCallInvitationCancelled(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationCancelled(zim, info, callID);
        }
    }

    @Override
    public void onCallInvitationAccepted(ZIM zim, ZIMCallInvitationAcceptedInfo info, String callID) {
        super.onCallInvitationAccepted(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationAccepted(zim, info, callID);
        }
    }

    @Override
    public void onCallInvitationRejected(ZIM zim, ZIMCallInvitationRejectedInfo info, String callID) {
        super.onCallInvitationRejected(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationRejected(zim, info, callID);
        }
    }

    @Override
    public void onCallInvitationTimeout(ZIM zim, String callID) {
        super.onCallInvitationTimeout(zim, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationTimeout(zim, callID);
        }
    }

    @Override
    public void onCallInviteesAnsweredTimeout(ZIM zim, ArrayList<String> invitees, String callID) {
        super.onCallInviteesAnsweredTimeout(zim, invitees, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInviteesAnsweredTimeout(zim, invitees, callID);
        }
    }

    @Override
    public void onCallInvitationEnded(ZIM zim, ZIMCallInvitationEndedInfo info, String callID) {
        super.onCallInvitationEnded(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationEnded(zim, info, callID);
        }
    }

    @Override
    public void onCallInvitationTimeout(ZIM zim, ZIMCallInvitationTimeoutInfo info, String callID) {
        super.onCallInvitationTimeout(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallInvitationTimeout(zim, info, callID);
        }
    }

    @Override
    public void onCallUserStateChanged(ZIM zim, ZIMCallUserStateChangeInfo info, String callID) {
        super.onCallUserStateChanged(zim, info, callID);
        for (ZIMEventHandler handler : handlerList) {
            handler.onCallUserStateChanged(zim, info, callID);
        }
    }

    @Override
    public void onBroadcastMessageReceived(ZIM zim, ZIMMessage message) {
        super.onBroadcastMessageReceived(zim, message);
        for (ZIMEventHandler handler : handlerList) {
            handler.onBroadcastMessageReceived(zim, message);
        }
    }

    @Override
    public void onMessageReactionsChanged(ZIM zim, ArrayList<ZIMMessageReaction> reactions) {
        super.onMessageReactionsChanged(zim, reactions);
        for (ZIMEventHandler handler : handlerList) {
            handler.onMessageReactionsChanged(zim, reactions);
        }
    }

    public void addEventHandler(ZIMEventHandler eventHandler) {
        handlerList.add(eventHandler);
    }

    public void removeEventHandler(ZIMEventHandler eventHandler) {
        handlerList.remove(eventHandler);
    }

    public void removeEventHandlerList(List<ZIMEventHandler> list) {
        if (list.isEmpty()) {
            return;
        }
        handlerList.removeAll(list);
    }

    public void removeAllEventHandlers() {
        handlerList.clear();
    }
}
