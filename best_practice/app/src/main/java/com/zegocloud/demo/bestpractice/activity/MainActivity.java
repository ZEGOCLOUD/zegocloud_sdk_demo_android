package com.zegocloud.demo.bestpractice.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.bestpractice.activity.call.CallEntryActivity;
import com.zegocloud.demo.bestpractice.activity.liveaudioroom.LiveAudioRoomEntryActivity;
import com.zegocloud.demo.bestpractice.activity.livestreaming.LiveStreamEntryActivity;
import com.zegocloud.demo.bestpractice.databinding.ActivityMainBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ZEGOSDKUser localUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        binding.liveUserinfoUserid.setText(localUser.userID);
        binding.liveUserinfoUsername.setText(localUser.userName);

        binding.buttonCall.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CallEntryActivity.class));
        });

        binding.buttonLivestreaming.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LiveStreamEntryActivity.class));
        });

        binding.buttonLiveaudioroom.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LiveAudioRoomEntryActivity.class));
        });
        // if use LiveStreaming,init after user login,can receive pk request.
        ZEGOLiveStreamingManager.getInstance().init();
        // if use Call invitation,init after user login,can receive call request.
        ZEGOCallInvitationManager.getInstance().init(this);
    }

    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOSDKManager.getInstance().disconnectUser();
            ZEGOLiveStreamingManager.getInstance().removeUserData();
            ZEGOLiveStreamingManager.getInstance().removeUserListeners();
            ZEGOCallInvitationManager.getInstance().removeUserData();
            ZEGOCallInvitationManager.getInstance().removeUserListeners();
        }
    }
}