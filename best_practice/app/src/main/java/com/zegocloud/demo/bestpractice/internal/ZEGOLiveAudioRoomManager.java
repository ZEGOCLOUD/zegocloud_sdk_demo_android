package com.zegocloud.demo.bestpractice.internal;

import android.text.TextUtils;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomExtraInfo;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomExtraInfo.ValuePairUpdateListener;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomLayoutConfig;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomSeat;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.RoomSeatService;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.RoomSeatServiceListener;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import im.zego.zegoexpress.entity.ZegoRoomExtraInfo;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.callback.ZIMUserAvatarUrlUpdatedCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class ZEGOLiveAudioRoomManager {

    private static final class Holder {

        private static final ZEGOLiveAudioRoomManager INSTANCE = new ZEGOLiveAudioRoomManager();
    }

    private ZEGOLiveAudioRoomManager(){

    }
    public static ZEGOLiveAudioRoomManager getInstance() {
        return Holder.INSTANCE;
    }

    private static final String EXTRA_INFO_KEY = "audioRoom";
    private static final String EXTRA_INFO_VALUE_HOST = "host";
    private static final String EXTRA_INFO_VALUE_LOCK_SEAT = "lockseat";
    private List<LiveAudioRoomListener> audioRoomListenerList = new ArrayList<>();
    private RoomSeatService seatService = new RoomSeatService();
    private String hostUserID;
    private boolean lockSeat;
    private LiveAudioRoomExtraInfo audioRoomExtraInfo = new LiveAudioRoomExtraInfo();

    public void init(LiveAudioRoomLayoutConfig layoutConfig) {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onRoomExtraInfoUpdate2(String roomID, ArrayList<ZegoRoomExtraInfo> roomExtraInfoList) {
                for (ZegoRoomExtraInfo extraInfo : roomExtraInfoList) {
                    if (extraInfo.key.equals(EXTRA_INFO_KEY)) {
                        audioRoomExtraInfo.setExtraInfoValueString(extraInfo.value);
                    }
                }
            }

            @Override
            public void onUserEnter(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser audioRoomUser : userList) {
                    // if RoomExtraInfo callback is first,and userEnter is next
                    if (!TextUtils.isEmpty(hostUserID) && Objects.equals(audioRoomUser.userID, hostUserID)) {
                        for (LiveAudioRoomListener listener : audioRoomListenerList) {
                            listener.onHostChanged(audioRoomUser);
                        }
                    }
                }
                seatService.onUserEnter(userList);
            }
        });

        ZEGOSDKManager.getInstance().zimService.addEventHandler(new IZIMEventHandler() {
            @Override
            public void onRoomAttributesUpdated2(List<Map<String, String>> setProperties,
                List<Map<String, String>> deleteProperties) {
                seatService.onRoomAttributesUpdated(setProperties, deleteProperties);
            }
        });

        audioRoomExtraInfo.setValuePairUpdateListener(new ValuePairUpdateListener() {
            @Override
            public void onRoomExtraInfoValuePairUpdateListener(Map<String, Object> updatePairs,
                Map<String, Object> deletePairs) {
                if (updatePairs.containsKey(EXTRA_INFO_VALUE_HOST)) {
                    String lastHostUserID = hostUserID;
                    hostUserID = (String) updatePairs.get(EXTRA_INFO_VALUE_HOST);
                    if (!Objects.equals(lastHostUserID, hostUserID) && getHostUser() != null) {
                        for (LiveAudioRoomListener listener : audioRoomListenerList) {
                            listener.onHostChanged(getHostUser());
                        }
                    }
                }
                if (updatePairs.containsKey(EXTRA_INFO_VALUE_LOCK_SEAT)) {
                    lockSeat = (boolean) updatePairs.get(EXTRA_INFO_VALUE_LOCK_SEAT);
                    if (!lockSeat) {
                        ZEGOSDKManager.getInstance().zimService.removeAllRequest();
                    }
                    for (LiveAudioRoomListener listener : audioRoomListenerList) {
                        listener.onLockSeatStatusChanged(lockSeat);
                    }
                }
                if (deletePairs.containsKey(EXTRA_INFO_VALUE_LOCK_SEAT)) {
                    lockSeat = false;
                    for (LiveAudioRoomListener listener : audioRoomListenerList) {
                        listener.onLockSeatStatusChanged(lockSeat);
                    }
                }
            }
        });
        seatService.init(layoutConfig);
    }

    public void lockSeat(boolean lock) {
        JSONObject extraInfoValueJson = audioRoomExtraInfo.getExtraInfoValueJson();
        try {
            JSONObject jsonObject = new JSONObject(extraInfoValueJson.toString());
            jsonObject.put(EXTRA_INFO_VALUE_LOCK_SEAT, lock);
            ZEGOSDKManager.getInstance().expressService.setRoomExtraInfo(EXTRA_INFO_KEY, jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isSeatLocked() {
        return lockSeat;
    }

    public void setHostAndLockSeat() {
        JSONObject extraInfoValueJson = audioRoomExtraInfo.getExtraInfoValueJson();
        try {
            ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            JSONObject jsonObject = new JSONObject(extraInfoValueJson.toString());
            jsonObject.put(EXTRA_INFO_VALUE_HOST, localUser.userID);
            jsonObject.put(EXTRA_INFO_VALUE_LOCK_SEAT, true);
            ZEGOSDKManager.getInstance().expressService.setRoomExtraInfo(EXTRA_INFO_KEY, jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearHost() {
        JSONObject extraInfoValueJson = audioRoomExtraInfo.getExtraInfoValueJson();
        try {
            ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            JSONObject jsonObject = new JSONObject(extraInfoValueJson.toString());
            jsonObject.put(EXTRA_INFO_VALUE_HOST, "");
            ZEGOSDKManager.getInstance().expressService.setRoomExtraInfo(EXTRA_INFO_KEY, jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void startPublishingStream() {
        String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        String streamID = generateUserStreamID(currentUser.userID, roomID);
        ZEGOSDKManager.getInstance().expressService.startPublishingStream(streamID);
    }

    public String generateUserStreamID(String userID, String roomID) {
        return roomID + "_" + userID + "_main" + "_host";
    }

    public ZEGOSDKUser getHostUser() {
        if (TextUtils.isEmpty(hostUserID)) {
            return null;
        }
        return ZEGOSDKManager.getInstance().expressService.getUser(hostUserID);
    }

    public void addLiveAudioRoomListener(LiveAudioRoomListener listener) {
        this.audioRoomListenerList.add(listener);
        seatService.addListener(listener);
    }

    public void removeLiveAudioRoomListener(LiveAudioRoomListener listener) {
        this.audioRoomListenerList.remove(listener);
        seatService.removeListener(listener);
    }

    public int getHostPresetSeatIndex() {
        return seatService.getHostPresetSeatIndex();
    }

    public void setHostPresetSeatIndex(int hostSeatIndex) {
        seatService.setHostPresetSeatIndex(hostSeatIndex);
    }

    public void updateUserAvatarUrl(String url, ZIMUserAvatarUrlUpdatedCallback callback) {
        ZEGOSDKManager.getInstance().zimService.updateUserAvatarUrl(url, callback);
    }

    public void queryUsersInfo(List<String> userIDList, ZIMUsersInfoQueriedCallback callback) {
        ZEGOSDKManager.getInstance().zimService.queryUsersInfo(userIDList, callback);
    }

    public String getUserAvatar(String userID) {
        return ZEGOSDKManager.getInstance().zimService.getUserAvatar(userID);
    }

    public List<LiveAudioRoomSeat> getAudioRoomSeatList() {
        return seatService.getAudioRoomSeatList();
    }

    public int findFirstAvailableSeatIndex() {
        return seatService.findFirstAvailableSeatIndex();
    }

    public int findMyRoomSeatIndex() {
        return seatService.findMyRoomSeatIndex();
    }

    public void takeSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        seatService.takeSeat(seatIndex, callback);
    }

    public void switchSeat(int fromSeatIndex, int toSeatIndex, ZIMRoomAttributesBatchOperatedCallback callback) {
        seatService.switchSeat(fromSeatIndex, toSeatIndex, callback);
    }

    public void leaveSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        seatService.leaveSeat(seatIndex, callback);
    }

    public void removeSpeakerFromSeat(String userID, ZIMRoomAttributesOperatedCallback callback) {
        seatService.removeSpeakerFromSeat(userID, callback);
    }

    public void removeRoomData() {
        lockSeat = false;
        audioRoomExtraInfo.clear();
        hostUserID = null;
        seatService.removeRoomData();
    }

    public void removeRoomListeners() {
        audioRoomListenerList.clear();
        seatService.removeRoomListeners();
    }

    public void leave() {
        clearHost();
        removeRoomData();
        removeRoomListeners();
        ZEGOSDKManager.getInstance().logoutRoom(null);
    }

    public interface LiveAudioRoomListener extends RoomSeatServiceListener {

        default void onHostChanged(ZEGOSDKUser hostUser){}

        default void onLockSeatStatusChanged(boolean lock){}
    }

}
