package com.zegocloud.demo.bestpractice.internal.sdk.zim;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class RoomRequest {

    public String requestID;
    public String receiver;
    public String sender;
    public String extendedData;
    public int actionType;

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("action_type", actionType);
            jsonObject.put("sender_id", sender);
            jsonObject.put("receiver_id", receiver);
            jsonObject.put("extended_data", extendedData);
            jsonObject.put("request_id", requestID);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    public RoomRequest copy() {
        RoomRequest roomRequest = new RoomRequest();
        roomRequest.requestID = requestID;
        roomRequest.receiver = receiver;
        roomRequest.sender = sender;
        roomRequest.extendedData = extendedData;
        roomRequest.actionType = actionType;
        return roomRequest;
    }

}
