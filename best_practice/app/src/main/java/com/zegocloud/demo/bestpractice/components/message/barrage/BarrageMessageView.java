package com.zegocloud.demo.bestpractice.components.message.barrage;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import im.zego.zegoexpress.entity.ZegoBarrageMessageInfo;
import im.zego.zegoexpress.entity.ZegoUser;
import java.util.ArrayList;

public class BarrageMessageView extends FrameLayout {

    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private BarrageMessageAdapter messageAdapter;

    public BarrageMessageView(@NonNull Context context) {
        super(context);
        initView();
    }

    public BarrageMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BarrageMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public BarrageMessageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onIMRecvBarrageMessage(String roomID, ArrayList<ZegoBarrageMessageInfo> messageList) {
                if (messageAdapter != null) {
                    boolean reachBottom = !recyclerView.canScrollVertically(1);
                    messageAdapter.addMessages(messageList);
                    if (reachBottom) {
                        layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
            }

            @Override
            public void onIMSendBarrageMessageResult(int errorCode, String message, String messageID) {
                if (errorCode == 0) {
                    if (messageAdapter != null) {
                        ZegoBarrageMessageInfo messageInfo = new ZegoBarrageMessageInfo();
                        messageInfo.message = message;
                        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                        messageInfo.fromUser = new ZegoUser(localUser.userID, localUser.userName);
                        messageInfo.messageID = messageID;
                        messageInfo.sendTime = System.currentTimeMillis();
                        messageAdapter.addMessage(messageInfo);
                        layoutManager.scrollToPosition(messageAdapter.getItemCount() - 1);
                    }
                }
            }
        });

        recyclerView = new RecyclerView(getContext());
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        messageAdapter = new BarrageMessageAdapter();
        //        messageAdapter.addMessages(UIKitCore.getInstance().getInRoomMessages());
        recyclerView.setAdapter(messageAdapter);
        layoutManager.scrollToPosition(messageAdapter.getItemCount());
        LayoutParams params = new LayoutParams(-1, -2);
        params.gravity = Gravity.BOTTOM;
        addView(recyclerView, params);
    }
}
