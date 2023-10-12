package com.zegocloud.demo.bestpractice.internal.business;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomRequestExtendedData {

    public int roomRequestType;

    public static RoomRequestExtendedData parse(String extendedData) {
        try {
            JSONObject jsonObject = new JSONObject(extendedData);
            if (jsonObject.has("room_request_type")) {
                int type = (int) jsonObject.get("room_request_type");
                if (type == RoomRequestType.REQUEST_COHOST) {
                    RoomRequestExtendedData data = new RoomRequestExtendedData();
                    data.roomRequestType = RoomRequestType.REQUEST_COHOST;
                    return data;
                }else if (type == RoomRequestType.REQUEST_TAKE_SEAT) {
                    RoomRequestExtendedData data = new RoomRequestExtendedData();
                    data.roomRequestType = RoomRequestType.REQUEST_TAKE_SEAT;
                    return data;
                }
            }
        } catch (JSONException e) {
        }
        return null;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("room_request_type", roomRequestType);
        } catch (JSONException e) {

        }
        return jsonObject.toString();
    }
}
