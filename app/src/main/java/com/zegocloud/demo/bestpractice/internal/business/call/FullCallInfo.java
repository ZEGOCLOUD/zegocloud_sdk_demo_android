package com.zegocloud.demo.bestpractice.internal.business.call;

import androidx.annotation.NonNull;
import org.json.JSONException;
import org.json.JSONObject;

public class FullCallInfo {

    public String callID;
    public String callerUserID;
    public String callerUserName;
    public String calleeUserID;
    public int callType;
    public boolean isOutgoingCall;


    public static FullCallInfo parse(String string) {
        FullCallInfo callInfo = new FullCallInfo();
        try {
            JSONObject jsonObject = new JSONObject(string);
            callInfo.callID = jsonObject.getString("call_id");
            callInfo.callerUserID = jsonObject.getString("caller_user_id");
            callInfo.callerUserName = jsonObject.getString("caller_user_name");
            callInfo.calleeUserID = jsonObject.getString("callee_user_id");
            callInfo.callType = jsonObject.getInt("call_type");
            callInfo.isOutgoingCall = jsonObject.getBoolean("is_outgoing_call");
            return callInfo;
        } catch (JSONException e) {
        }
        return null;
    }

    public boolean isVideoCall() {
        return callType == CallExtendedData.VIDEO_CALL;
    }

    public boolean isVoiceCall() {
        return callType == CallExtendedData.VOICE_CALL;
    }

    @NonNull
    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("call_id", callID);
            jsonObject.put("caller_user_id", callerUserID);
            jsonObject.put("caller_user_name", callerUserName);
            jsonObject.put("callee_user_id", calleeUserID);
            jsonObject.put("call_type", callType);
            jsonObject.put("is_outgoing_call", isOutgoingCall);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }
}
