package com.zegocloud.demo.bestpractice.activity.call;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.call.CallInviteDialog;
import com.zegocloud.demo.bestpractice.databinding.ActivityCallInvitationBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONObject;
import timber.log.Timber;

public class CallInvitationActivity extends AppCompatActivity {

    private ActivityCallInvitationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("CallInvitationActivity");

        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOCallInvitationManager.getInstance().setCallInviteUserComparator(new Comparator<CallInviteUser>() {
            @Override
            public int compare(CallInviteUser o1, CallInviteUser o2) {
                if (o1.getUserID().equals(currentUser.userID) && !o2.getUserID().equals(currentUser.userID)) {
                    return -1;
                } else if (!o1.getUserID().equals(currentUser.userID) && o2.getUserID().equals(currentUser.userID)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        ZEGOCallInvitationManager.getInstance().joinRoom(new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (errorCode == 0) {
                    onRoomJoinSuccess();
                } else {
                    ToastUtil.show(CallInvitationActivity.this, "Join Room failed,errorCode:" + errorCode);
                    finish();
                }
            }
        });
        listenSDKEvent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        Timber.d("onCreateOptionsMenu() called with: callInviteInfo = [" + callInviteInfo + "]");
        if (callInviteInfo == null || callInviteInfo.userList.isEmpty()) {
            return super.onCreateOptionsMenu(menu);
        } else {
            MenuItem add_new_user = menu.add("");
            add_new_user.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            add_new_user.setIcon(R.drawable.baseline_add_24);
            //        TextView textView = new TextView(this);
            //        textView.setText(R.string.add_new_user);
            //        textView.setTextColor(Color.WHITE);
            //        add_new_user.setActionView(textView);
            return true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        CallInviteDialog callInviteDialog = new CallInviteDialog(this);
        callInviteDialog.show();
        return super.onOptionsItemSelected(item);
    }

    private void onRoomJoinSuccess() {
        CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
        if (callInviteInfo.isVideoCall()) {
            binding.callCameraBtn.open();
            binding.callCameraBtn.setVisibility(View.VISIBLE);
            binding.callCameraSwitchBtn.setVisibility(View.VISIBLE);
        } else {
            binding.callCameraBtn.close();
            binding.callCameraBtn.setVisibility(View.GONE);
            binding.callCameraSwitchBtn.setVisibility(View.GONE);
        }

        binding.callHangupBtn.setOnClickListener(v -> {
            finish();
        });

        binding.callMicBtn.open();
        binding.callSpeakerBtn.open();

        List<String> permissions;
        if (callInviteInfo.isVideoCall()) {
            permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        } else {
            permissions = Collections.singletonList(permission.RECORD_AUDIO);
        }
        requestPermissionIfNeeded(this, permissions, new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                binding.layoutMain.onPermissionAnswered(allGranted, grantedList, deniedList);
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            ZEGOCallInvitationManager.getInstance().quitCallAndLeaveRoom();
        }
    }

    public void listenSDKEvent() {
        ZEGOSDKManager.getInstance().expressService.addEventHandler(new IExpressEngineEventHandler() {
            @Override
            public void onUserEnter(List<ZEGOSDKUser> userList) {
                super.onUserEnter(userList);
            }
        });
        ZEGOCallInvitationManager.getInstance().addCallListener(new CallChangedListener() {

            @Override
            public void onCallEnded(String requestID) {
                //
                finish();
            }
        });
    }

    private void requestPermissionIfNeeded(Context context, List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init((FragmentActivity) context).permissions(permissions)
            .onExplainRequestReason((scope, deniedList) -> {
                String message = "";
                if (permissions.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = context.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = context.getString(R.string.permission_explain_mic);
                    }
                } else {
                    if (deniedList.size() == 1) {
                        if (deniedList.contains(permission.CAMERA)) {
                            message = context.getString(R.string.permission_explain_camera);
                        } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                            message = context.getString(R.string.permission_explain_mic);
                        }
                    } else {
                        message = context.getString(R.string.permission_explain_camera_mic);
                    }
                }
                scope.showRequestReasonDialog(deniedList, message, context.getString(R.string.ok));
            }).onForwardToSettings((scope, deniedList) -> {
                String message = "";
                if (permissions.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = context.getString(R.string.settings_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = context.getString(R.string.settings_mic);
                    }
                } else {
                    if (deniedList.size() == 1) {
                        if (deniedList.contains(permission.CAMERA)) {
                            message = context.getString(R.string.settings_camera);
                        } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                            message = context.getString(R.string.settings_mic);
                        }
                    } else {
                        message = context.getString(R.string.settings_camera_mic);
                    }
                }
                scope.showForwardToSettingsDialog(deniedList, message, context.getString(R.string.settings),
                    context.getString(R.string.cancel));
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