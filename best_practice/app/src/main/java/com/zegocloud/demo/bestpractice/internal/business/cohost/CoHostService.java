package com.zegocloud.demo.bestpractice.internal.business.cohost;

import android.text.TextUtils;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.zegoexpress.constants.ZegoPublisherState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CoHostService {

    private ZEGOSDKUser hostUser;
    private List<ZEGOSDKUser> coHostUserList = new ArrayList<>();
    private List<CoHostListener> listenerList = new ArrayList<>();

    public String generateUserStreamID(String userID, String roomID) {
        if (hostUser != null && userID.equals(hostUser.userID)) {
            return roomID + "_" + userID + "_main" + "_host";
        } else {
            return roomID + "_" + userID + "_main" + "_cohost";
        }
    }

    public void setHostUser(ZEGOSDKUser user) {
        if (user == null && hostUser != null) {
            for (CoHostListener coHostListener : listenerList) {
                coHostListener.onRoleChanged(hostUser.userID, Role.AUDIENCE);
            }
        } else if (user != null && hostUser == null) {
            for (CoHostListener coHostListener : listenerList) {
                coHostListener.onRoleChanged(user.userID, Role.HOST);
            }
        } else if (user != null && hostUser != null) {
            if (!Objects.equals(user, hostUser)) {
                for (CoHostListener coHostListener : listenerList) {
                    coHostListener.onRoleChanged(user.userID, Role.HOST);
                }
                for (CoHostListener coHostListener : listenerList) {
                    coHostListener.onRoleChanged(hostUser.userID, Role.AUDIENCE);
                }
            } else {
                for (CoHostListener coHostListener : listenerList) {
                    coHostListener.onRoleChanged(user.userID, Role.HOST);
                }
            }
        }
        hostUser = user;
    }

    public ZEGOSDKUser getHostUser() {
        return hostUser;
    }

    public boolean isLocalUserHost() {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null) {
            return false;
        }
        return isHost(localUser.userID);
    }

    public boolean isHost(String userID) {
        if (hostUser == null) {
            return false;
        } else {
            return hostUser.userID.equals(userID);
        }
    }

    public boolean isCoHost(String userID) {
        for (ZEGOSDKUser zegosdkUser : coHostUserList) {
            if (zegosdkUser.userID.equals(userID)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAudience(String userID) {
        if (isHost(userID) || isCoHost(userID)) {
            return false;
        }
        return true;
    }

    public void removeRoomListeners() {
        listenerList.clear();
    }

    public void removeRoomData() {
        coHostUserList.clear();
        setHostUser(null);
    }

    public void removeUserListeners() {
        removeRoomListeners();
    }

    public void removeUserData() {
        removeRoomData();
    }

    public void onReceiveStreamAdd(List<ZEGOSDKUser> userList) {
        for (ZEGOSDKUser zegosdkUser : userList) {
            String mainStreamID = zegosdkUser.getMainStreamID();
            if (!TextUtils.isEmpty(mainStreamID)) {
                if (mainStreamID.endsWith("_host")) {
                    setHostUser(zegosdkUser);
                } else if (mainStreamID.endsWith("_cohost")) {
                    addCoHost(zegosdkUser);
                }
            }
        }
    }

    private void addCoHost(ZEGOSDKUser zegosdkUser) {
        if (!coHostUserList.contains(zegosdkUser)) {
            coHostUserList.add(zegosdkUser);
            for (CoHostListener coHostListener : listenerList) {
                coHostListener.onRoleChanged(zegosdkUser.userID, Role.CO_HOST);
            }
        }
    }

    private void removeCoHost(ZEGOSDKUser zegosdkUser) {
        boolean remove = coHostUserList.remove(zegosdkUser);
        if (remove) {
            for (CoHostListener coHostListener : listenerList) {
                coHostListener.onRoleChanged(zegosdkUser.userID, Role.AUDIENCE);
            }
        }
    }

    public void onReceiveStreamRemove(List<ZEGOSDKUser> userList) {
        for (ZEGOSDKUser zegosdkUser : userList) {
            if (zegosdkUser.equals(hostUser)) {
                setHostUser(null);
                ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                if (ZEGOLiveStreamingManager.getInstance().isCoHost(localUser.userID)) {
                    ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
                    ZEGOSDKManager.getInstance().expressService.openCamera(false);
                    ZEGOSDKManager.getInstance().expressService.stopPreview();
                    ZEGOSDKManager.getInstance().expressService.stopPublishingStream();
                }
            } else {
                removeCoHost(zegosdkUser);
            }
        }
    }

    public void onPublisherStateUpdate(String streamID, ZegoPublisherState state, int errorCode) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (state == ZegoPublisherState.PUBLISHING) {
            if (!TextUtils.isEmpty(streamID)) {
                if (streamID.endsWith("_host")) {
                    setHostUser(localUser);
                } else if (streamID.endsWith("_cohost")) {
                    addCoHost(localUser);
                }
            }
        } else if (state == ZegoPublisherState.NO_PUBLISH) {
            if (localUser.equals(hostUser)) {
                setHostUser(null);
            } else {
                removeCoHost(localUser);
            }
        }
    }

    public void addListener(CoHostListener listener) {
        listenerList.add(listener);
    }

    public void removeListener(CoHostListener listener) {
        listenerList.remove(listener);
    }

    public void endCoHost() {
        removeCoHost(ZEGOSDKManager.getInstance().expressService.getCurrentUser());
        ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
        ZEGOSDKManager.getInstance().expressService.openCamera(false);
        ZEGOSDKManager.getInstance().expressService.stopPreview();
        ZEGOSDKManager.getInstance().expressService.stopPublishingStream();
    }

    public void startCoHost() {
        addCoHost(ZEGOSDKManager.getInstance().expressService.getCurrentUser());
        ZEGOSDKManager.getInstance().expressService.openCamera(true);
        ZEGOSDKManager.getInstance().expressService.openMicrophone(true);
        ZEGOLiveStreamingManager.getInstance().startPublishingStream();
    }

    public @interface Role {

        int AUDIENCE = 0;
        int CO_HOST = 1;
        int HOST = 2;
    }

    public interface CoHostListener {

        default void onRoleChanged(String userID, @Role int after) {
        }
    }
}
