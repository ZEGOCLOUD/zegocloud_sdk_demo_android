package com.zegocloud.demo.bestpractice.internal.business;

import com.zegocloud.demo.bestpractice.components.cohost.LiveRoom;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class FakeServerApi {

    private static int roomID = 1;
    private static int page = 10;

    public static List<LiveRoom> getRoomList() {
        List<LiveRoom> roomList = new ArrayList<>();
        for (int i = 0; i < page; i++) {
            LiveRoom liveRoom = new LiveRoom(String.valueOf(roomID), String.valueOf(roomID));
            roomID += 1;
            roomList.add(liveRoom);
        }
        Timber.d("getRoomList() called:" + roomList);
        return roomList;
    }

    public static void reset() {
        roomID = 1;
    }

    public static LiveRoom getNextLive(String liveID) {
        return null;
    }
}
