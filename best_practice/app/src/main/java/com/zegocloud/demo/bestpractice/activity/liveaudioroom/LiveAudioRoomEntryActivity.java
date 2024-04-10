package com.zegocloud.demo.bestpractice.activity.liveaudioroom;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.activity.MainActivity;
import com.zegocloud.demo.bestpractice.databinding.ActivityLiveAudioRoomEntryBinding;
import com.zegocloud.demo.bestpractice.databinding.ActivityLiveStreamingEntryBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LiveAudioRoomEntryActivity extends AppCompatActivity {

    private ActivityLiveAudioRoomEntryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveAudioRoomEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.startLiveAudioroom.setOnClickListener(v -> {
            String liveID = binding.liveIdAudioRoom.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveIdAudioRoom.setError("please input liveID");
                return;
            }
            List<String> permissions = Arrays.asList(permission.RECORD_AUDIO);
            requestPermissionIfNeeded(permissions, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        Intent intent = new Intent(LiveAudioRoomEntryActivity.this, LiveAudioRoomActivity.class);
                        intent.putExtra("host", true);
                        intent.putExtra("liveID", liveID);
                        startActivity(intent);
                    }
                }
            });
        });

        binding.watchLiveAudioroom.setOnClickListener(v -> {
            String liveID = binding.liveIdAudioRoom.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveIdAudioRoom.setError("please input liveID");
                return;
            }
            Intent intent = new Intent(LiveAudioRoomEntryActivity.this, LiveAudioRoomActivity.class);
            intent.putExtra("host", false);
            intent.putExtra("liveID", liveID);
            startActivity(intent);
        });
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.permission_explain_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.permission_explain_mic);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera_mic);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.settings_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.settings_mic);
                    }
                } else {
                    message = this.getString(R.string.settings_camera_mic);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                getString(R.string.cancel));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }
}