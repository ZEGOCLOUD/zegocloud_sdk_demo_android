package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZTextButton;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomCommandCallback;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * show pk dialog
 */
public class GiftButton extends ZTextButton {

    public GiftButton(@NonNull Context context) {
        super(context);
    }

    public GiftButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GiftButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setText("Gift");
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setGravity(Gravity.CENTER);
        setTextColor(Color.parseColor("#cccccc"));
        setMinWidth(Utils.dp2px(36, getResources().getDisplayMetrics()));
        int padding = Utils.dp2px(8, getResources().getDisplayMetrics());
        setPadding(padding, 0, padding, 0);
    }

    @Override
    protected void afterClick() {
        super.afterClick();

        ZEGOSDKUser hostUser = ZEGOLiveStreamingManager.getInstance().getHostUser();
        String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
        if (hostUser != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("room_id", roomID);
                jsonObject.put("user_id", hostUser.userID);
                jsonObject.put("user_name", hostUser.userName);
                jsonObject.put("gift_type", 1001);
                jsonObject.put("gift_count", 1);
                jsonObject.put("timestamp", System.currentTimeMillis());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ZEGOSDKManager.getInstance().zimService.sendRoomCommand(jsonObject.toString(), new RoomCommandCallback() {
                @Override
                public void onSendRoomCommand(int errorCode, String errorMessage, String command) {
                    Timber.d("onSendRoomCommand() called with: errorCode = [" + errorCode + "], errorMessage = ["
                        + errorMessage + "]");
                }
            });
        }
    }
}
