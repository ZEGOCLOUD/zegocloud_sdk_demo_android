package com.zegocloud.demo.bestpractice.internal.business.audioroom;

import androidx.annotation.NonNull;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class LiveAudioRoomExtraInfo {

    private JSONObject extraInfoValueJson = new JSONObject();
    private ValuePairUpdateListener valuePairUpdateListener;

    public void setExtraInfoValueString(String extraInfoValue) {
        try {
            JSONObject jsonObject = new JSONObject(extraInfoValue);
            Iterator<String> keys = jsonObject.keys();
            Map<String, Object> updatePairs = new HashMap<>();
            Map<String, Object> deletedPairs = new HashMap<>();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);
                if (extraInfoValueJson.has(key)) {
                    Object oldValue = extraInfoValueJson.get(key);
                    if (!value.equals(oldValue)) {
                        updatePairs.put(key, value);
                    }
                } else {
                    updatePairs.put(key, value);
                }
            }
            Iterator<String> oldKeys = extraInfoValueJson.keys();
            while (oldKeys.hasNext()) {
                String oldKey = oldKeys.next();
                if (!jsonObject.has(oldKey)) {
                    deletedPairs.put(oldKey, extraInfoValueJson.get(oldKey));
                }
            }
            extraInfoValueJson = jsonObject;
            if (valuePairUpdateListener != null) {
                valuePairUpdateListener.onRoomExtraInfoValuePairUpdateListener(updatePairs, deletedPairs);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValuePairUpdateListener(ValuePairUpdateListener valuePairUpdateListener) {
        this.valuePairUpdateListener = valuePairUpdateListener;
    }

    public JSONObject getExtraInfoValueJson() {
        return extraInfoValueJson;
    }

    public void clear() {
        extraInfoValueJson = new JSONObject();
    }

    @NonNull
    @Override
    public String toString() {
        return extraInfoValueJson.toString();
    }

    public interface ValuePairUpdateListener {

        void onRoomExtraInfoValuePairUpdateListener(Map<String, Object> updatePairs, Map<String, Object> deletePairs);
    }
}
