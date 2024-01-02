package com.zegocloud.demo.bestpractice.components.call;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.databinding.DialogInviteBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteInfo;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.zim.callback.ZIMCallInvitationSentCallback;
import im.zego.zim.entity.ZIMCallInvitationSentInfo;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;
import java.util.List;

public class CallInviteDialog extends Dialog {

    private DialogInviteBinding binding;

    public CallInviteDialog(@NonNull Context context) {
        super(context);
    }

    public CallInviteDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected CallInviteDialog(@NonNull Context context, boolean cancelable,
        @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogInviteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.setDimAmount(0.2f);
        window.setBackgroundDrawable(new ColorDrawable());

        registerClickListeners(binding.checkbox1, binding.edittext1);
        registerClickListeners(binding.checkbox2, binding.edittext2);
        registerClickListeners(binding.checkbox3, binding.edittext3);

        binding.quit.setVisibility(View.GONE);
        binding.end.setVisibility(View.GONE);

        binding.invite.setOnClickListener(v -> {
            List<String> userIDList = new ArrayList<>();
            if (binding.checkbox1.isChecked() && !TextUtils.isEmpty(binding.edittext1.getText())) {
                userIDList.add(binding.edittext1.getText().toString());
            }
            if (binding.checkbox2.isChecked() && !TextUtils.isEmpty(binding.edittext2.getText())) {
                userIDList.add(binding.edittext2.getText().toString());
            }
            if (binding.checkbox3.isChecked() && !TextUtils.isEmpty(binding.edittext3.getText())) {
                userIDList.add(binding.edittext3.getText().toString());
            }
            if (!userIDList.isEmpty()) {
                CallInviteInfo callInviteInfo = ZEGOCallInvitationManager.getInstance().getCallInviteInfo();
                if (callInviteInfo.isVideoCall()) {
                    ZEGOCallInvitationManager.getInstance().inviteVideoCall(userIDList,
                        new ZIMCallInvitationSentCallback() {
                            @Override
                            public void onCallInvitationSent(String callID, ZIMCallInvitationSentInfo info,
                                ZIMError errorInfo) {
                                if (errorInfo.code.value() != 0) {
                                    ToastUtil.show(getContext(), "inviteVideoCall, return error :" + errorInfo.code.value());
                                }
                            }
                        });
                } else {
                    ZEGOCallInvitationManager.getInstance().inviteVoiceCall(userIDList,
                        new ZIMCallInvitationSentCallback() {
                            @Override
                            public void onCallInvitationSent(String callID, ZIMCallInvitationSentInfo info,
                                ZIMError errorInfo) {
                                if (errorInfo.code.value() != 0) {
                                    ToastUtil.show(getContext(), "inviteVoiceCall, return error :" + errorInfo.code.value());
                                }
                            }
                        });
                }
            } else {
                ToastUtil.show(getContext(), "please input userID to invite");
            }
            dismiss();
        });
    }

    private void registerClickListeners(CheckBox checkBox, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkBox.setChecked(s.length() > 0);
            }
        });
    }
}
