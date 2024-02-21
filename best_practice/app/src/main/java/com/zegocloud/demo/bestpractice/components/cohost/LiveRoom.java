package com.zegocloud.demo.bestpractice.components.cohost;

public class LiveRoom {

    public String roomID;
    public String hostUserID;
    public boolean isInPK;

    public LiveRoom(String roomID, String hostUserID) {
        this.roomID = roomID;
        this.hostUserID = hostUserID;
    }

    @Override
    public String toString() {
        return "roomID='" + roomID + '\'' + ", hostUserID='" + hostUserID + '\'' + '}';
    }
}
