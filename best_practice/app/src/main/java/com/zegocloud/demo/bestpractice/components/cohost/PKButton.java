package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import com.google.android.material.textfield.TextInputLayout;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager.LiveStreamingListener;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService.PKInfo;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService.PKRequest;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZTextButton;
import com.zegocloud.demo.bestpractice.internal.business.UserRequestCallback;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;

public class PKButton extends ZTextButton {

    public PKButton(@NonNull Context context) {
        super(context);
    }

    public PKButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PKButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView() {
        super.initView();
        setText("PK");
        setBackgroundResource(R.drawable.bg_cohost_btn);
        setGravity(Gravity.CENTER);
        setTextColor(Color.parseColor("#cccccc"));
        setMinWidth(Utils.dp2px(36, getResources().getDisplayMetrics()));
        int padding = Utils.dp2px(8, getResources().getDisplayMetrics());
        setPadding(padding, 0, padding, 0);

        ZEGOLiveStreamingManager.getInstance().addLiveStreamingListener(new LiveStreamingListener() {
            @Override
            public void onPKStarted() {
                updateUI();
            }

            @Override
            public void onPKEnded() {
                updateUI();
            }

            @Override
            public void onOutgoingStartPKRequestTimeout() {
                updateUI();
                ToastUtil.show(getContext(), "send pk request,but no reply");
            }

            @Override
            public void onOutgoingStartPKRequestRejected() {
                updateUI();
                ToastUtil.show(getContext(), "send pk request,but rejected");
            }
        });
    }

    @Override
    protected void afterClick() {
        super.afterClick();
        PKRequest pkRequest = ZEGOLiveStreamingManager.getInstance().getSendPKStartRequest();
        if (pkRequest == null) {
            if (ZEGOLiveStreamingManager.getInstance().getPKInfo() == null) {
                AlertDialog.Builder builder = new Builder(getContext());
                View layout = LayoutInflater.from(getContext()).inflate(R.layout.dialog_input, null, false);
                TextInputLayout inputLayout = layout.findViewById(R.id.dialog_pk_edittext);
                Button button = layout.findViewById(R.id.dialog_pk_button);
                builder.setView(layout);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                button.setOnClickListener(view -> {
                    EditText editText = inputLayout.getEditText();
                    if (!TextUtils.isEmpty(editText.getText())) {
                        ZEGOLiveStreamingManager.getInstance()
                            .sendPKBattlesStartRequest(editText.getText().toString(), new UserRequestCallback() {
                                @Override
                                public void onUserRequestSend(int errorCode, String requestID) {
                                    if (errorCode != 0) {
                                        ToastUtil.show(getContext(), "send pk request, return error :" + errorCode);
                                    }
                                    updateUI();
                                }
                            });
                        alertDialog.dismiss();
                    }
                });
            } else {
                ZEGOLiveStreamingManager.getInstance().sendPKBattlesStopRequest();
            }
        } else {
            ZEGOLiveStreamingManager.getInstance()
                .cancelPKBattleStartRequest(pkRequest.requestID, pkRequest.targetUserID);
            updateUI();
        }
    }

    public void updateUI() {
        PKInfo pkInfo = ZEGOLiveStreamingManager.getInstance().getPKInfo();
        if (pkInfo != null) {
            setText("End PK");
        } else {
            PKRequest sendPKStartRequest = ZEGOLiveStreamingManager.getInstance().getSendPKStartRequest();
            if (sendPKStartRequest == null) {
                setText("PK");
            } else {
                setText("Cancel PK");
            }
        }
    }
}
