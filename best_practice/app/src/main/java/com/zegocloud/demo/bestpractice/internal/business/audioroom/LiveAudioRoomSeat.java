package com.zegocloud.demo.bestpractice.internal.business.audioroom;

import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import java.util.Objects;

public class LiveAudioRoomSeat {

    public int seatIndex = 0;
    public int rowIndex = 0;
    public int columnIndex = 0;
    private ZEGOSDKUser lastUser;
    private ZEGOSDKUser currentUser;

    public boolean isNotEmpty() {
        return currentUser != null;
    }

    public boolean isEmpty() {
        return currentUser == null;
    }

    public boolean isSeatChanged() {
        return !Objects.equals(currentUser, lastUser);
    }

    public boolean isTakenByUser(ZEGOSDKUser user) {
        return user.equals(currentUser);
    }

    public ZEGOSDKUser getUser() {
        return currentUser;
    }

    public ZEGOSDKUser getLastUser() {
        return lastUser;
    }

    public void setUser(ZEGOSDKUser user) {
        lastUser = currentUser;
        currentUser = user;
    }
}
