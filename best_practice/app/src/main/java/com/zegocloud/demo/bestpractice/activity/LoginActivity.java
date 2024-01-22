package com.zegocloud.demo.bestpractice.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.bestpractice.ZEGOSDKKeyCenter;
import com.zegocloud.demo.bestpractice.databinding.ActivityLoginBinding;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKCallBack;
import im.zego.zim.callback.ZIMUserAvatarUrlUpdatedCallback;
import im.zego.zim.entity.ZIMError;
import java.util.Random;
import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Timber.plant(new DebugTree());

        binding.liveLoginUserid.getEditText().setText(Build.MANUFACTURER.toLowerCase());
        binding.liveLoginName.getEditText().setText(Build.MANUFACTURER.toLowerCase());
        binding.liveLoginUserid.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.liveLoginName.getEditText().setText(s + "_" + Build.MANUFACTURER.toLowerCase());
            }
        });

        binding.liveLoginBtn.setOnClickListener(v -> {
            String userID = binding.liveLoginUserid.getEditText().getText().toString();
            String userName = binding.liveLoginName.getEditText().getText().toString();
            if (TextUtils.isEmpty(userID) || TextUtils.isEmpty(userName)) {
                if (TextUtils.isEmpty(userID)) {
                    binding.liveLoginUserid.setError("please input userID");
                } else if (TextUtils.isEmpty(userName)) {
                    binding.liveLoginName.setError("please input username");
                }
                return;
            }
            signInZEGOSDK(userID, userName, (errorCode, message) -> {
                if (errorCode == 0) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

        });

        initZEGOSDK();
    }

    private void initZEGOSDK() {
        ZEGOSDKManager.getInstance().initSDK(getApplication(), ZEGOSDKKeyCenter.appID, ZEGOSDKKeyCenter.appSign);
    }

    /**
     * should be called only once after the user sign in to their own business account.
     */
    private void signInZEGOSDK(String userID, String userName, ZEGOSDKCallBack callback) {
        ZEGOSDKManager.getInstance().connectUser(userID, userName, new ZEGOSDKCallBack() {
            @Override
            public void onResult(int errorCode, String message) {
                if (errorCode == 0) {
                    String url = "https://robohash.org/" + userID + "?set=set4";
                    ZEGOSDKManager.getInstance().zimService.updateUserAvatarUrl(url,
                        new ZIMUserAvatarUrlUpdatedCallback() {
                            @Override
                            public void onUserAvatarUrlUpdated(String userAvatarUrl, ZIMError errorInfo) {
                                if (callback != null) {
                                    callback.onResult(errorCode, message);
                                }
                            }
                        });
                }

            }
        });
    }

    private static String generateUserID() {
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