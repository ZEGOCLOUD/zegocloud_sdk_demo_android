package com.zegocloud.demo.bestpractice.components.cohost;

import java.util.Objects;

public class LiveRoom {

    public String roomID;
    public String hostUserID;

    public LiveRoom(String roomID, String hostUserID) {
        this.roomID = roomID;
        this.hostUserID = hostUserID;
    }

    public LiveRoom(String liveID) {
        this.roomID = liveID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LiveRoom)) {
            return false;
        }
        LiveRoom liveRoom = (LiveRoom) o;
        return roomID.equals(liveRoom.roomID) && hostUserID.equals(liveRoom.hostUserID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomID, hostUserID);
    }

    @Override
    public String toString() {
        return "roomID='" + roomID + '\'' + ", hostUserID='" + hostUserID + '\'' + '}';
    }
}
