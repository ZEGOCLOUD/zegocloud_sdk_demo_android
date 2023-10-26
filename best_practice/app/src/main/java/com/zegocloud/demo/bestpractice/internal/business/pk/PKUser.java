package com.zegocloud.demo.bestpractice.internal.business.pk;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import im.zego.zim.enums.ZIMCallUserState;
import org.json.JSONException;
import org.json.JSONObject;

public class PKUser {

    public String userID;
    public String userName;
    public String roomID;
    private boolean camera;
    private boolean microphone;
    private ZIMCallUserState callUserState = ZIMCallUserState.UNKNOWN;
    public Rect rect = new Rect();
    private boolean isMuted = false;
    private String extendedData;

    public PKUser(String userID) {
        this.userID = userID;
    }

    public PKUser(String userID, ZIMCallUserState callUserState, String extendedData) {
        this.userID = userID;
        this.callUserState = callUserState;
        this.extendedData = extendedData;
    }

    public boolean isCameraOpen() {
        return camera;
    }

    public void setCamera(boolean camera) {
        this.camera = camera;
    }

    public boolean isMicrophoneOpen() {
        return microphone;
    }

    public void setMicrophone(boolean microphone) {
        this.microphone = microphone;
    }

    public ZIMCallUserState getCallUserState() {
        return callUserState;
    }

    public boolean hasAccepted() {
        return callUserState == ZIMCallUserState.ACCEPTED;
    }

    public boolean isWaiting() {
        return callUserState == ZIMCallUserState.RECEIVED;
    }

    public void setCallUserState(ZIMCallUserState callUserState) {
        this.callUserState = callUserState;
    }

    public String getPKUserStream() {
        return roomID + "_" + userID + "_main" + "_host";
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public String getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(String extendedData) {
        this.extendedData = extendedData;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("uid", userID);
            jsonObject.put("rid", roomID);
            jsonObject.put("u_name", userName);
            JSONObject rectJson = new JSONObject();
            rectJson.put("top", rect.top);
            rectJson.put("left", rect.left);
            rectJson.put("right", rect.right);
            rectJson.put("bottom", rect.bottom);
            jsonObject.put("rect", rectJson);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    public String print() {
        return "PKUser{" + "userID='" + userID + '\'' + ", userName='" + userName + '\'' + ", roomID='" + roomID + '\''
            + ", camera=" + camera + ", microphone=" + microphone + ", callUserState=" + callUserState + ", rect="
            + rect + '}';
    }

    public static PKUser parse(String string) {
        try {
            JSONObject jsonObject = new JSONObject(string);
            String uid = jsonObject.getString("uid");
            String rid = jsonObject.getString("rid");
            String u_name = jsonObject.getString("u_name");
            JSONObject rectJson = jsonObject.getJSONObject("rect");
            int top = rectJson.getInt("top");
            int left = rectJson.getInt("left");
            int right = rectJson.getInt("right");
            int bottom = rectJson.getInt("bottom");
            PKUser pkUser = new PKUser(uid);
            pkUser.roomID = rid;
            pkUser.userName = u_name;
            pkUser.rect.set(left, top, right, bottom);
            return pkUser;
        } catch (JSONException e) {
            //            throw new RuntimeException(e);
            return null;
        }
    }

}
