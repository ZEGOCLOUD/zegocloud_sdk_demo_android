package com.zegocloud.demo.bestpractice.components.cohost;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.databinding.DialogPkInviteBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService.PKBattleInfo;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PKInviteDialog extends Dialog {

    private com.zegocloud.demo.bestpractice.databinding.DialogPkInviteBinding binding;

    public PKInviteDialog(@NonNull Context context) {
        super(context);
    }

    public PKInviteDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected PKInviteDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogPkInviteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        window.setGravity(Gravity.CENTER);
        window.setDimAmount(0.2f);
        window.setBackgroundDrawable(new ColorDrawable());

        registerClickListeners(binding.checkbox1, binding.edittext1);
        registerClickListeners(binding.checkbox2, binding.edittext2);
        registerClickListeners(binding.checkbox3, binding.edittext3);

        binding.invitePk.setOnClickListener(v -> {
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
                ZEGOLiveStreamingManager.getInstance().invitePKBattle(userIDList, new UserRequestCallback() {
                    @Override
                    public void onUserRequestSend(int errorCode, String requestID) {
                        if (errorCode != 0) {
                            ToastUtil.show(getContext(), "send pk request, return error :" + errorCode);
                        }
                    }
                });
            } else {
                ToastUtil.show(getContext(), "please input userID to invite");
            }
            dismiss();
        });

        binding.endPk.setOnClickListener(v -> {
            //            PKBattleInfo pkBattleInfo = ZEGOLiveStreamingManager.getInstance().getPKBattleInfo();
            //            if (pkBattleInfo != null) {
            //                List<String> cancelUsers = new ArrayList<>();
            //                for (PKUser pkUser : pkBattleInfo.pkUserList) {
            //                    ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            //                    if (!Objects.equals(currentUser.userID, pkUser.userID)) {
            //                        cancelUsers.add(pkUser.userID);
            //                    }
            //                }
            //                ZEGOLiveStreamingManager.getInstance().cancelPKBattle(pkBattleInfo.requestID, cancelUsers);
            //            }
            ZEGOLiveStreamingManager.getInstance().endPKBattle();
            dismiss();
        });

        binding.quitPk.setOnClickListener(v -> {
            ZEGOLiveStreamingManager.getInstance().quitPKBattle();
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
