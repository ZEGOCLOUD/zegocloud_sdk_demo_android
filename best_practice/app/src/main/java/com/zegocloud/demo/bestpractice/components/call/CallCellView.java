package com.zegocloud.demo.bestpractice.components.call;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.ZEGOAudioVideoView;
import com.zegocloud.demo.bestpractice.internal.ZEGOCallInvitationManager;
import com.zegocloud.demo.bestpractice.internal.business.call.CallChangedListener;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.zim.entity.ZIMUserFullInfo;
import java.util.ArrayList;
import java.util.Objects;
import timber.log.Timber;

public class CallCellView extends FrameLayout {

    private ZEGOAudioVideoView audioVideoView;
    private CallInviteUser callInviteUser;
    private TextView textView;
    private ViewGroup progressBarParent;

    public CallCellView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CallCellView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public CallCellView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        setBackgroundResource(R.drawable.bg_gray_rect_stroke);
        audioVideoView = new ZEGOAudioVideoView(getContext());
        addView(audioVideoView);
        textView = new TextView(getContext());
        LayoutParams textViewParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
        textViewParams.gravity = Gravity.TOP | Gravity.END;
        textView.setTextColor(Color.WHITE);
        addView(textView, textViewParams);

        progressBarParent = (ViewGroup) LayoutInflater.from(getContext())
            .inflate(R.layout.layout_progress_cell_view, this, false);
        LayoutParams progressBarParentParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        progressBarParent.setLayoutParams(progressBarParentParams);
        ProgressBar progressBar = progressBarParent.findViewById(R.id.progress_bar);
        Sprite doubleBounce = new ThreeBounce();
        progressBar.setIndeterminateDrawable(doubleBounce);

        addView(progressBarParent);

        dismissLoading();

        ZEGOCallInvitationManager.getInstance().addCallListener(new CallChangedListener() {
            @Override
            public void onCallUserInfoUpdate(ArrayList<ZIMUserFullInfo> userList) {
                if(callInviteUser != null){
                    for (ZIMUserFullInfo userFullInfo : userList) {
                        if (Objects.equals(userFullInfo.baseInfo.userID, callInviteUser.getUserID())) {
                            audioVideoView.setUserID(callInviteUser.getUserID());
                            ZIMUserFullInfo zimUserInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(callInviteUser.getUserID());
                            if (zimUserInfo != null) {
                                textView.setText(zimUserInfo.baseInfo.userName);
                            }
                        }
                    }
                }
            }
        });
    }


    public void setCallUser(CallInviteUser callInviteUser) {
        this.callInviteUser = callInviteUser;
        audioVideoView.setUserID(callInviteUser.getUserID());
        ZIMUserFullInfo zimUserInfo = ZEGOSDKManager.getInstance().zimService.getUserInfo(callInviteUser.getUserID());
        if (zimUserInfo != null) {
            textView.setText(zimUserInfo.baseInfo.userName);
        }
        ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
        ZEGOSDKUser zegosdkUser = ZEGOSDKManager.getInstance().expressService.getUser(callInviteUser.getUserID());
        Timber.d("setCallUser() called with: callInviteUser = [" + callInviteUser + "],zegosdkUser:" + zegosdkUser);

        if (Objects.equals(currentUser.userID, callInviteUser.getUserID())) {
            if (currentUser.isCameraOpen()) {
                audioVideoView.startPreviewOnly();
                audioVideoView.showVideoView();
            } else {
                audioVideoView.stopPreview();
                audioVideoView.showAudioView();
            }
        } else {
            if (zegosdkUser != null) {
                audioVideoView.setStreamID(zegosdkUser.getMainStreamID());
                if (zegosdkUser.isCameraOpen()) {
                    audioVideoView.startPlayRemoteAudioVideo();
                    audioVideoView.showVideoView();
                } else {
                    //                    audioVideoView.stopPlayRemoteAudioVideo();
                    audioVideoView.showAudioView();
                }
            }
        }
    }

    public ZEGOAudioVideoView getAudioVideoView() {
        return audioVideoView;
    }

    public String getUserID() {
        if (callInviteUser == null) {
            return null;
        } else {
            return callInviteUser.getUserID();
        }
    }

    public void loading() {
        progressBarParent.setVisibility(VISIBLE);
    }

    public void dismissLoading() {
        progressBarParent.setVisibility(GONE);
    }
}
