package com.zegocloud.demo.screensharing.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.zegocloud.demo.screensharing.ZEGOKeyCenter;
import com.zegocloud.demo.screensharing.databinding.ActivityLoginBinding;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userID = generateUserID();
        String userName = userID + "_" + Build.MANUFACTURER.toLowerCase();
        binding.liveLoginId.getEditText().setText(userID);
        binding.liveLoginName.getEditText().setText(userName);

        binding.liveLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userID", userID);
            intent.putExtra("userName", userName);
            startActivity(intent);
        });

        initZEGOSDK();
    }

    private void initZEGOSDK() {
        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = ZEGOKeyCenter.appID;
        profile.appSign = ZEGOKeyCenter.appSign;
        profile.scenario = ZegoScenario.LIVE;
        profile.application = getApplication();
        ZegoExpressEngine.createEngine(profile, null);
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