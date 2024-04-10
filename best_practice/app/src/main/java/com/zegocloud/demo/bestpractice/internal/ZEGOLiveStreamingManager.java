package com.zegocloud.demo.bestpractice.internal;

import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.business.cohost.CoHostService;
import com.zegocloud.demo.bestpractice.internal.business.cohost.CoHostService.CoHostListener;
import com.zegocloud.demo.bestpractice.internal.business.pk.MixLayoutProvider;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKListener;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService.PKBattleInfo;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class ZEGOLiveStreamingManager {

    private static final class Holder {

        private static final ZEGOLiveStreamingManager INSTANCE = new ZEGOLiveStreamingManager();

    }

    private ZEGOLiveStreamingManager() {

    }

    public static ZEGOLiveStreamingManager getInstance() {
        return Holder.INSTANCE;
    }


    private PKService pkService = new PKService();
    private CoHostService coHostService = new CoHostService();
    private MixLayoutProvider mixLayoutProvider;

    /**
     * should be called only once after user signin
     */
    public void init() {
        pkService.addListenersForUserSignIn();
    }

    /**
     * should be called only once after user join room
     */
    public void addListenersForUserJoinRoom() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
                boolean needQuery = false;
                for (ZEGOSDKUser zegosdkUser : userList) {
                    ZIMUserFullInfo zimUserFullInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(
                        zegosdkUser.userID);
                    if (zimUserFullInfo == null) {
                        needQuery = true;
                        break;
                    }
                }
                if (needQuery) {
                    List<String> userIDList = new ArrayList<>();
                    for (ZEGOSDKUser user : userList) {
                        userIDList.add(user.userID);
                    }
                    ZEGOLiveStreamingManager.getInstance().queryUsersInfo(userIDList, null);
                }
                coHostService.onReceiveStreamAdd(userList);
                pkService.onReceiveStreamAdd(userList);
            }

            @Override
            public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
                coHostService.onReceiveStreamRemove(userList);
            }

            @Override
            public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode,
                JSONObject extendedData) {
                coHostService.onPublisherStateUpdate(streamID, state, errorCode);
            }

            @Override
            public void onPlayerRecvVideoFirstFrame(String streamID) {
                pkService.onPlayerRecvVideoFirstFrame(streamID);
            }

            @Override
            public void onPlayerSyncRecvSEI(String streamID, byte[] data) {
                pkService.onPlayerSyncRecvSEI(streamID, data);
            }

            @Override
            public void onUserLeft(List<ZEGOSDKUser> userList) {
                if (ZEGOLiveStreamingManager.getInstance().isCurrentUserHost()) {
                    return;
                }
                for (ZEGOSDKUser zegosdkUser : userList) {
                    if (pkService.isPKUser(zegosdkUser.userID)) {
                        // means this room's pk user left
                        ZEGOLiveStreamingManager.getInstance().endPKBattle();
                        break;
                    }
                }
            }
        });
        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onRoomAttributesUpdated2(List<Map<String, String>> setProperties,
                List<Map<String, String>> deleteProperties) {
                pkService.onRoomAttributesUpdated(setProperties, deleteProperties);
            }
        });

        pkService.addListener(new LiveStreamingListener() {

            @Override
            public void onPKStarted() {
                ZEGOSDKManager.getInstance().zimService.removeAllRequest();
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (ZEGOLiveStreamingManager.getInstance().isCoHost(currentUser.userID)) {
                    ZEGOSDKManager.getInstance().expressService.openCamera(false);
                    ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
                    ZEGOSDKManager.getInstance().expressService.stopPublishingStream();
                }
            }
        });
    }


    public void setHostUser(ZEGOSDKUser userInfo) {
        coHostService.setHostUser(userInfo);
    }

    public boolean isCurrentUserHost() {
        return coHostService.isLocalUserHost();
    }

    public ZEGOSDKUser getHostUser() {
        return coHostService.getHostUser();
    }

    public boolean isHost(String userID) {
        return coHostService.isHost(userID);
    }

    public boolean isCoHost(String userID) {
        return coHostService.isCoHost(userID);
    }

    public boolean isAudience(String userID) {
        return coHostService.isAudience(userID);
    }

    public void endCoHost() {
        coHostService.endCoHost();
    }

    public void startCoHost() {
        coHostService.startCoHost();
    }

    public void removeRoomListeners() {
        coHostService.removeRoomListeners();
        pkService.removeRoomListeners();
    }

    public void removeRoomData() {
        coHostService.removeRoomData();
        pkService.removeRoomData();
    }

    public void removeUserListeners() {
        coHostService.removeUserListeners();
        pkService.removeUserListeners();
        removeRoomListeners();
    }

    public void removeUserData() {
        coHostService.removeUserData();
        pkService.removeUserData();
    }

    public void startPublishingStream() {
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        String generateUserStreamID = generateUserStreamID(currentUser.userID, currentRoomID);
        ZEGOSDKManager.getInstance().expressService.startPublishingStream(generateUserStreamID);
    }

    public String generateUserStreamID(String userID, String roomID) {
        return coHostService.generateUserStreamID(userID, roomID);
    }

    public PKBattleInfo getPKBattleInfo() {
        return pkService.getPKBattleInfo();
    }

    public boolean isPKUser(String userID) {
        return pkService.isPKUser(userID);
    }

    public boolean isPKUserMuted(String userID) {
        return pkService.isPKUserMuted(userID);
    }

    public void mutePKUser(List<String> muteUserList, boolean mute, IZegoMixerStartCallback callback) {
        pkService.mutePKUser(muteUserList, mute, callback);
    }

    public void addLiveStreamingListener(LiveStreamingListener listener) {
        pkService.addListener(listener);
        coHostService.addListener(listener);
    }

    public void removeLiveStreamingListener(LiveStreamingListener listener) {
        pkService.removeListener(listener);
        coHostService.removeListener(listener);
    }

    public void queryUsersInfo(List<String> userIDList, ZIMUsersInfoQueriedCallback callback) {
        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList, callback);
    }

    public void invitePKBattle(String anotherHostID, UserRequestCallback callback) {
        pkService.invitePKBattle(Collections.singletonList(anotherHostID), false, callback);
    }

    public void invitePKBattle(List<String> anotherHostIDList, UserRequestCallback callback) {
        pkService.invitePKBattle(anotherHostIDList, false, callback);
    }

    public void startPKBattle(String anotherHostID, UserRequestCallback callback) {
        pkService.invitePKBattle(Collections.singletonList(anotherHostID), true, callback);
    }

    public void startPKBattle(List<String> anotherHostIDList, UserRequestCallback callback) {
        pkService.invitePKBattle(anotherHostIDList, true, callback);
    }

    public void cancelPKBattle(String requestID, List<String> userIDList) {
        pkService.cancelPKBattle(requestID, userIDList);
    }

    public void acceptPKBattle(String requestID) {
        pkService.acceptPKBattle(requestID);
    }

    public void rejectPKBattle(String requestID) {
        pkService.rejectPKBattle(requestID);
    }

    public void removeUserFromPKBattle(String userID) {
        PKBattleInfo pkBattleInfo = pkService.getPKBattleInfo();
        if (pkBattleInfo != null) {
            pkService.removePKBattle(userID);
        }
    }

    public void endPKBattle() {
        PKBattleInfo pkBattleInfo = pkService.getPKBattleInfo();
        if (pkBattleInfo != null) {
            pkService.endPKBattle(pkBattleInfo.requestID, null);
            pkService.stopPKBattle();
        }
    }

    public void quitPKBattle() {
        PKBattleInfo pkBattleInfo = pkService.getPKBattleInfo();
        if (pkBattleInfo != null) {
            pkService.quitPKBattle(pkBattleInfo.requestID, null);
            pkService.stopPKBattle();
        }
    }

    public void setMixLayoutProvider(MixLayoutProvider mixLayoutProvider) {
        this.mixLayoutProvider = mixLayoutProvider;
    }

    public MixLayoutProvider getMixLayoutProvider() {
        return mixLayoutProvider;
    }

    public void leave() {
        if (isCurrentUserHost()) {
            quitPKBattle();
        }
        removeRoomData();
        removeRoomListeners();
        ZEGOSDKManager.getInstance().expressService.setMediaPlayerEventHandler(null);
        ZEGOSDKManager.getInstance().logoutRoom(null);
    }

    public interface LiveStreamingListener extends CoHostListener, PKListener {

    }

}
