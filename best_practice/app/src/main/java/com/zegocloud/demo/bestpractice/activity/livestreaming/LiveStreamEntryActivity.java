package com.zegocloud.demo.bestpractice.activity.livestreaming;

import android.Manifest.permission;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.ActivityLiveStreamingEntryBinding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LiveStreamEntryActivity extends AppCompatActivity {

    private ActivityLiveStreamingEntryBinding binding;
    private List<Activity> audienceActivityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLiveStreamingEntryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getApplication().registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity instanceof LiveStreamAudienceActivity) {
                    audienceActivityList.add(activity);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {

            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (audienceActivityList.contains(activity)) {
                    audienceActivityList.remove(activity);
                }
            }
        });

        //        binding.liveIdStreaming.getEditText().setText(Build.MANUFACTURER.toLowerCase());
        binding.startLiveStreaming.setOnClickListener(v -> {
            String liveID = binding.liveIdStreaming.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveIdStreaming.setError("please input liveID");
                return;
            }
            List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
            requestPermissionIfNeeded(permissions, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        Intent intent = new Intent(LiveStreamEntryActivity.this, LiveStreamHostActivity.class);
                        intent.putExtra("liveID", liveID);
                        startActivity(intent);
                    }
                }
            });
        });

        binding.slideLiveStreaming.setOnClickListener(v -> {
            String liveID = binding.liveIdStreaming.getEditText().getText().toString();
            if (TextUtils.isEmpty(liveID)) {
                binding.liveIdStreaming.setError("please input liveID");
                return;
            }

            if(!audienceActivityList.isEmpty()){
                audienceActivityList.forEach(Activity::finish);
                audienceActivityList.clear();
            }
            Intent intent = new Intent(LiveStreamEntryActivity.this, LiveStreamAudienceActivity.class);
            intent.putExtra("liveID", liveID);
            startActivity(intent);
        });
    }

    private static final String TAG = "LiveStreamEntryActivity";

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