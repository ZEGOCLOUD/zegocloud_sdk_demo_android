package com.zegocloud.demo.screensharing.activity;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.screensharing.R;
import com.zegocloud.demo.screensharing.databinding.ActivityMainBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userID = getIntent().getStringExtra("userID");
        String userName = getIntent().getStringExtra("userName");
        binding.yourUserId.setText("Your userID:" + userID);
        binding.yourUserName.setText("Your userName:" + userName);
        binding.liveId.getEditText().setText(generateLiveID());
        binding.startLive.setOnClickListener(v -> {
            String liveID = binding.liveId.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveId.setError("please input liveID");
                return;
            }
            List<String> permissions = Arrays.asList(permission.RECORD_AUDIO, permission.CAMERA);
            requestPermissionIfNeeded(permissions, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
                        intent.putExtra("host", true);
                        intent.putExtra("roomID", liveID);
                        intent.putExtra("userID", userID);
                        intent.putExtra("userName", userName);
                        startActivity(intent);
                    }
                }
            });
        });

        binding.watchLive.setOnClickListener(v -> {
            String liveID = binding.liveId.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveId.setError("please input liveID");
                return;
            }
            Intent intent = new Intent(MainActivity.this, LiveActivity.class);
            intent.putExtra("host", false);
            intent.putExtra("roomID", liveID);
            intent.putExtra("userID", userID);
            intent.putExtra("userName", userName);
            startActivity(intent);

        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {

        }
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

    private static String generateLiveID() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 6) {
            int nextInt = random.nextInt(10);
            if (builder.length() == 0 && nextInt == 0) {
                continue;
            }
            builder.append(nextInt);
        }
        return builder.toString();
    }
}