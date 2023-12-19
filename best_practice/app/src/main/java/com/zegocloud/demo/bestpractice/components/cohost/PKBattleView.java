package com.zegocloud.demo.bestpractice.components.cohost;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.LetterIconView;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKService;
import com.zegocloud.demo.bestpractice.internal.business.pk.PKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.express.ZEGOVideoView;
import im.zego.zegoexpress.callback.IZegoMixerStartCallback;
import java.util.Collections;
import java.util.Objects;
import org.json.JSONObject;

/**
 * cell view of each pk host,you can modify to add your custom widget
 */
public class PKBattleView extends FrameLayout {

    private FrameLayout connectTipsView;
    private LetterIconView iconView;
    private PKUser pkUser;
    private TextView defaultTips;
    private Button muteButton;

    public PKBattleView(@NonNull Context context) {
        super(context);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public PKBattleView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }


    private void initView() {
        iconView = new LetterIconView(getContext());
        addView(iconView);
        iconView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_444));

        muteButton = new Button(getContext());
        muteButton.setVisibility(GONE);
        muteButton.setText("Mute User");
        addView(muteButton, new LayoutParams(-2, -2));
        muteButton.setOnClickListener(v -> {
            if (pkUser != null) {
                boolean pkUserMuted = ZEGOLiveStreamingManager.getInstance().isPKUserMuted(pkUser.userID);
                ZEGOLiveStreamingManager.getInstance()
                    .mutePKUser(Collections.singletonList(pkUser.userID), !pkUserMuted, new IZegoMixerStartCallback() {
                        @Override
                        public void onMixerStartResult(int errorCode, JSONObject extendedData) {
                            if (!pkUserMuted) {
                                muteButton.setText("UnMute User");
                            } else {
                                muteButton.setText("Mute User");
                            }
                        }
                    });
            }
        });

        connectTipsView = new FrameLayout(getContext());
        addView(connectTipsView);
        defaultTips = new TextView(getContext());
        defaultTips.setGravity(Gravity.CENTER);
        defaultTips.setText("host reconnecting");
        defaultTips.setTextColor(Color.WHITE);
        defaultTips.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        defaultTips.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.gray_444));
        connectTipsView.addView(defaultTips, new LayoutParams(-1, -1));
        connectTipsView.setVisibility(GONE);
    }

    /**
     * @param pkUser
     * @param parent
     */
    public void setPKUser(PKUser pkUser, ViewGroup parent) {
        this.pkUser = pkUser;
        int mixVideoWidth = PKService.MIX_VIDEO_WIDTH;
        int mixVideoHeight = PKService.MIX_VIDEO_HEIGHT;
        if (pkUser != null) {
            float rateLeft = pkUser.rect.left / (mixVideoWidth * 1f);
            float rateTop = pkUser.rect.top / (mixVideoHeight * 1f);
            int left = (int) (parent.getWidth() * rateLeft);
            int top = (int) (parent.getHeight() * rateTop);

            int width = (int) (pkUser.rect.width() * parent.getWidth() / (mixVideoWidth * 1f));
            iconView.setCircleBackgroundRadius(width / 3);

            int height = (int) (pkUser.rect.height() * parent.getHeight() / (mixVideoHeight * 1f));

            //            Log.d(TAG, "setPKUser(0) called with: pkUser = [" + pkUser + "], parent = [" + getContext().getResources().getDisplayMetrics().density + "]");
            //            Log.d(TAG, "setPKUser(1) called with: getDisplayMetrics,widthPixels  = [" + getContext().getResources().getDisplayMetrics().widthPixels + "], heightPixels = [" + getContext().getResources().getDisplayMetrics().heightPixels + "]");
            //            Log.d(TAG, "setPKUser(2) called with: getWidth = [" + parent.getWidth() + "], getHeight = [" + parent.getHeight() + "]");
            //            Log.d(TAG, "setPKUser(3) called with: left = [" + left + "], top = [" + top + "]");
            //            Log.d(TAG, "setPKUser(4) called with: width = [" + width + "], height = [" + height + "]");

            FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(width, height);
            layoutParam.setMargins(left, top, 0, 0);
            setLayoutParams(layoutParam);

            boolean isCurrentUserHost = ZEGOLiveStreamingManager.getInstance().isCurrentUserHost();
            ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
            if (!Objects.equals(currentUser.userID, pkUser.userID)) {
                muteButton.setVisibility(VISIBLE);
            }
            // host play single stream of each pk user,need attach video view and hide mix video
            if (isCurrentUserHost) {
                ZEGOVideoView videoView = new ZEGOVideoView(getContext());
                videoView.setUserID(pkUser.userID);
                videoView.setStreamID(pkUser.getPKUserStream());
                addView(videoView, 0);

                if (Objects.equals(pkUser.userID, currentUser.userID)) {
                    videoView.startPreviewOnly();
                    videoView.startPublishAudioVideo();
                } else {
                    videoView.startPlayRemoteAudioVideo();
                }
            } else {
                if (getVideoView() != null) {
                    removeView(getVideoView());
                }
            }
            defaultTips.setText(pkUser.userName + " reconnecting");
            iconView.setLetter(pkUser.userName);
            onCameraUpdate(pkUser.isCameraOpen());
        } else {
            if (getVideoView() != null) {
                getVideoView().stopPlayRemoteAudioVideo();
            }
        }
    }

    public PKUser getPkUser() {
        return pkUser;
    }

    public void updatePKUser(PKUser pkUser) {
        FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(pkUser.rect.width(), pkUser.rect.height());
        layoutParam.setMargins(pkUser.rect.left, pkUser.rect.top, 0, 0);
        setLayoutParams(layoutParam);
    }

    public ZEGOVideoView getVideoView() {
        View childAt = getChildAt(0);
        if (childAt instanceof ZEGOVideoView) {
            return (ZEGOVideoView) childAt;
        }
        return null;
    }

    private static final String TAG = "PKBattleView";

    /**
     * host user or other pk user
     *
     * @param open
     */
    public void onCameraUpdate(boolean open) {
        if (getVisibility() == VISIBLE) {
            if (open) {
                iconView.setVisibility(INVISIBLE);
                if (getVideoView() != null) {
                    getVideoView().setVisibility(VISIBLE);
                }
            } else {
                iconView.setVisibility(VISIBLE);
                if (getVideoView() != null) {
                    getVideoView().setVisibility(GONE);
                }
            }
        }
    }


    public void onTimeOut(boolean timeout) {
        if (timeout) {
            connectTipsView.setVisibility(VISIBLE);
        } else {
            connectTipsView.setVisibility(GONE);
        }
    }

    public void mutePlayAudio(boolean mute) {
        if (getVideoView() != null) {
            getVideoView().mutePlayAudio(mute);
        }
        if (mute) {
            muteButton.setText("UnMute User");
        } else {
            muteButton.setText("Mute User");
        }
    }
}
