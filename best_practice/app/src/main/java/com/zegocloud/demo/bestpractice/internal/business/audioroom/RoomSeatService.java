package com.zegocloud.demo.bestpractice.internal.business.audioroom;

import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.zim.callback.ZIMRoomAttributesBatchOperatedCallback;
import im.zego.zim.callback.ZIMRoomAttributesOperatedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomAttributesBatchOperationConfig;
import im.zego.zim.entity.ZIMRoomAttributesSetConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RoomSeatService {

    private List<RoomSeatServiceListener> seatServiceListenerList = new ArrayList<>();
    private List<LiveAudioRoomSeat> seatList = new ArrayList<>();
    private boolean batchOperation = false;
    private boolean isTakeSeat = false;
    private int hostPresetSeatIndex = 0;

    public void init(LiveAudioRoomLayoutConfig layoutConfig) {
        seatList.clear();
        for (int rowIndex = 0; rowIndex < layoutConfig.rowConfigs.size(); rowIndex++) {
            LiveAudioRoomLayoutRowConfig rowConfig = layoutConfig.rowConfigs.get(rowIndex);
            for (int columnIndex = 0; columnIndex < rowConfig.count; columnIndex++) {
                LiveAudioRoomSeat audioRoomSeat = new LiveAudioRoomSeat();
                audioRoomSeat.rowIndex = rowIndex;
                audioRoomSeat.columnIndex = columnIndex;
                audioRoomSeat.seatIndex = seatList.size();
                seatList.add(audioRoomSeat);
            }
        }
    }

    public void onUserEnter(List<ZEGOSDKUser> userList) {
        List<LiveAudioRoomSeat> changedSeatList = new ArrayList<>();
        for (ZEGOSDKUser audioRoomUser : userList) {
            for (LiveAudioRoomSeat seat : seatList) {
                if (audioRoomUser.equals(seat.getUser())) {
                    if (!Objects.equals(audioRoomUser.userName, seat.getUser().userName)) {
                        seat.setUser(audioRoomUser);
                        changedSeatList.add(seat);
                    }
                }
            }
        }
        if (!changedSeatList.isEmpty()) {
            for (RoomSeatServiceListener listener : seatServiceListenerList) {
                listener.onSeatChanged(changedSeatList);
            }
        }
    }

    public void onRoomAttributesUpdated(List<Map<String, String>> setProperties,
        List<Map<String, String>> deleteProperties) {
        int seatIndexBefore = findMyRoomSeatIndex();

        List<LiveAudioRoomSeat> changedSeats = new ArrayList<>();
        for (Map<String, String> setProperty : setProperties) {
            for (Map.Entry<String, String> entry : setProperty.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                try {
                    int index = Integer.parseInt(key);
                    if (isSeatIndexValid(index)) {
                        LiveAudioRoomSeat seat = seatList.get(index);
                        ZEGOSDKUser cloudUser = ZEGOSDKManager.getInstance().expressService.getUser(value);
                        if (cloudUser == null) {
                            cloudUser = new ZEGOSDKUser(value, value);
                        }
                        seat.setUser(cloudUser);
                        changedSeats.add(seat);
                    }
                } catch (Exception e) {

                }
            }
        }
        for (Map<String, String> deleteProperty : deleteProperties) {
            for (Map.Entry<String, String> entry : deleteProperty.entrySet()) {
                String key = entry.getKey();
                int index = Integer.parseInt(key);
                if (isSeatIndexValid(index)) {
                    LiveAudioRoomSeat seat = seatList.get(index);
                    seat.setUser(null);
                    changedSeats.add(seat);
                }
            }
        }

        int seatIndexAfter = findMyRoomSeatIndex();
        if (seatIndexBefore >= 0 && seatIndexAfter == -1) {
            ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
            ZEGOSDKManager.getInstance().expressService.stopPublishingStream();
        } else if (seatIndexBefore == -1 && seatIndexAfter >= 0) {
            ZEGOSDKManager.getInstance().expressService.openMicrophone(true);
            ZEGOLiveAudioRoomManager.getInstance().startPublishingStream();
        }
        for (RoomSeatServiceListener listener : seatServiceListenerList) {
            listener.onSeatChanged(changedSeats);
        }
    }

    public List<LiveAudioRoomSeat> getAudioRoomSeatList() {
        return seatList;
    }

    public int getSeatCount() {
        return seatList.size();
    }

    public int findFirstAvailableSeatIndex() {
        int firstEmptyIndex = -1;
        for (int i = 0; i < seatList.size(); i++) {
            if (i == hostPresetSeatIndex) {
                continue;
            }
            if (seatList.get(i).isEmpty()) {
                firstEmptyIndex = i;
                break;
            }
        }
        return firstEmptyIndex;
    }

    public LiveAudioRoomSeat findUserRoomSeat(String userID) {
        LiveAudioRoomSeat seat = null;
        for (int i = 0; i < seatList.size(); i++) {
            LiveAudioRoomSeat audioRoomSeat = seatList.get(i);
            if (audioRoomSeat.isNotEmpty() && Objects.equals(userID, audioRoomSeat.getUser().userID)) {
                seat = audioRoomSeat;
                break;
            }
        }
        return seat;
    }

    public int findMyRoomSeatIndex() {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        LiveAudioRoomSeat userRoomSeat = findUserRoomSeat(localUser.userID);
        if (userRoomSeat != null) {
            return userRoomSeat.seatIndex;
        } else {
            return -1;
        }
    }

    public LiveAudioRoomSeat getAudioRoomSeat(int seatIndex) {
        if (isSeatIndexValid(seatIndex)) {
            return null;
        }
        return seatList.get(seatIndex);
    }

    private boolean isSeatIndexValid(int seatIndex) {
        if (seatIndex < 0 || seatIndex >= seatList.size()) {
            return false;
        }
        return true;
    }

    public void takeSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null || isTakeSeat) {
            return;
        }
        isTakeSeat = true;
        String key = String.valueOf(seatIndex);
        String value = localUser.userID;

        ZIMRoomAttributesSetConfig config = new ZIMRoomAttributesSetConfig();
        config.isDeleteAfterOwnerLeft = true;
        config.isForce = true;
        config.isUpdateOwner = true;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes(key, value, config,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                    isTakeSeat = false;
                    if (callback != null) {
                        callback.onRoomAttributesOperated(roomID, errorKeys, errorInfo);
                    }
                }
            });
    }

    public void switchSeat(int fromSeatIndex, int toSeatIndex, ZIMRoomAttributesBatchOperatedCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null) {
            return;
        }
        if (!batchOperation) {
            ZIMRoomAttributesBatchOperationConfig config = new ZIMRoomAttributesBatchOperationConfig();
            config.isForce = true;
            config.isDeleteAfterOwnerLeft = false;
            config.isUpdateOwner = false;
            ZEGOSDKManager.getInstance().zimService.beginRoomPropertiesBatchOperation(config);
            batchOperation = true;
            tryTakeSeat(toSeatIndex, null);
            leaveSeat(fromSeatIndex, null);
            ZEGOSDKManager.getInstance().zimService.endRoomPropertiesBatchOperation(
                new ZIMRoomAttributesBatchOperatedCallback() {
                    @Override
                    public void onRoomAttributesBatchOperated(String roomID, ZIMError errorInfo) {
                        batchOperation = false;
                        if (callback != null) {
                            callback.onRoomAttributesBatchOperated(roomID, errorInfo);
                        }
                    }
                });
        }
    }

    public void tryTakeSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null || isTakeSeat) {
            return;
        }
        isTakeSeat = true;
        String key = String.valueOf(seatIndex);
        String value = localUser.userID;
        ZIMRoomAttributesSetConfig config = new ZIMRoomAttributesSetConfig();
        config.isDeleteAfterOwnerLeft = true;
        config.isForce = false;
        config.isUpdateOwner = false;
        ZEGOSDKManager.getInstance().zimService.setRoomAttributes(key, value, config,
            new ZIMRoomAttributesOperatedCallback() {
                @Override
                public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                    isTakeSeat = false;
                    if (callback != null) {
                        callback.onRoomAttributesOperated(roomID, errorKeys, errorInfo);
                    }
                }
            });
    }

    public void leaveSeat(int seatIndex, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null) {
            return;
        }
        List<String> list = Collections.singletonList(String.valueOf(seatIndex));
        ZEGOSDKManager.getInstance().zimService.deleteRoomAttributes(list, new ZIMRoomAttributesOperatedCallback() {
            @Override
            public void onRoomAttributesOperated(String roomID, ArrayList<String> errorKeys, ZIMError errorInfo) {
                if (callback != null) {
                    callback.onRoomAttributesOperated(roomID, errorKeys, errorInfo);
                }
            }
        });
    }

    public void removeSpeakerFromSeat(String userID, ZIMRoomAttributesOperatedCallback callback) {
        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        if (localUser == null) {
            return;
        }
        for (LiveAudioRoomSeat seat : seatList) {
            int seatIndex = seat.seatIndex;
            ZEGOSDKUser seatUser = seat.getUser();
            if (seatUser != null) {
                String seatUserID = seatUser.userID;
                if (Objects.equals(userID, seatUserID)) {
                    leaveSeat(seatIndex, callback);
                    break;
                }
            }
        }
    }

    public void addListener(RoomSeatServiceListener listener) {
        this.seatServiceListenerList.add(listener);
    }

    public void removeListener(RoomSeatServiceListener listener) {
        this.seatServiceListenerList.remove(listener);
    }

    public void removeRoomData() {
        batchOperation = false;
        seatList.clear();
        isTakeSeat = false;
        hostPresetSeatIndex = 0;
    }

    public void removeRoomListeners() {
        seatServiceListenerList.clear();
    }

    public int getHostPresetSeatIndex() {
        return hostPresetSeatIndex;
    }

    public void setHostPresetSeatIndex(int hostSeatIndex) {
        this.hostPresetSeatIndex = hostSeatIndex;
    }
}
