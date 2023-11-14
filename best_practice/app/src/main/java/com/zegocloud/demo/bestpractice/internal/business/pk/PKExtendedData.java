package com.zegocloud.demo.bestpractice.internal.business.pk;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class PKExtendedData {

    public String roomID;
    public String userName;
    public int type;
    public String userID;
    public boolean autoAccept;

    public static final int START_PK = 91000;

    public static PKExtendedData parse(String extendedData) {
        try {
            JSONObject jsonObject = new JSONObject(extendedData);
            if (jsonObject.has("type")) {
                int type = (int) jsonObject.get("type");
                if (type == START_PK) {
                    PKExtendedData data = new PKExtendedData();
                    data.type = type;
                    if (jsonObject.has("room_id")) {
                        data.roomID = jsonObject.getString("room_id");
                    }
                    if (jsonObject.has("user_name")) {
                        data.userName = jsonObject.getString("user_name");
                    }
                    if (jsonObject.has("auto_accept")) {
                        data.autoAccept = jsonObject.getBoolean("auto_accept");
                    }
                    if (jsonObject.has("user_id")) {
                        data.userID = jsonObject.getString("user_id");
                    }
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
            jsonObject.put("room_id", roomID);
            jsonObject.put("user_name", userName);
            jsonObject.put("type", type);
            jsonObject.put("auto_accept", autoAccept);
            if (!TextUtils.isEmpty(userID)) {
                jsonObject.put("user_id", userID);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }
}
