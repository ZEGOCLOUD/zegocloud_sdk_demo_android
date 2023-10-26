package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import java.util.List;

public class CoHostView extends RecyclerView {

    private CoHostAdapter coHostAdapter;

    public CoHostView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CoHostView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CoHostView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        coHostAdapter = new CoHostAdapter();
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(coHostAdapter);

        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onCameraOpen(String userID, boolean open) {
                if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                    coHostAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onMicrophoneOpen(String userID, boolean open) {
                if (ZEGOLiveStreamingManager.getInstance().getPKBattleInfo() == null) {
                    coHostAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void addUser(List<ZEGOSDKUser> coHostUserList) {
        coHostAdapter.addUser(coHostUserList);
    }

    public void addUser(ZEGOSDKUser user) {
        coHostAdapter.addUser(user);
    }

    public void removeUser(List<ZEGOSDKUser> coHostUserList) {
        coHostAdapter.removeUser(coHostUserList);
    }

    public void removeUser(ZEGOSDKUser user) {
        coHostAdapter.removeUser(user);
    }
}
