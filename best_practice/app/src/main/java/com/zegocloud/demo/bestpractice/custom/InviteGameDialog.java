package com.zegocloud.demo.bestpractice.custom;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import androidx.annotation.NonNull;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.DialogGameInviteBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.LiveAudioRoomSeat;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.MiniGameService;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import im.zego.minigameengine.IZegoCommonCallback;
import im.zego.minigameengine.ZegoGameInfoDetail;
import im.zego.minigameengine.ZegoRobotSeatInfo;
import im.zego.minigameengine.ZegoStartGameConfig;
import im.zego.minigameengine.ZegoUserSeatInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import timber.log.Timber;

public class InviteGameDialog extends Dialog {

    private DialogGameInviteBinding binding;

    public InviteGameDialog(@NonNull Context context) {
        super(context);
    }

    public InviteGameDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGameInviteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MiniGameService miniGameService = ZEGOLiveAudioRoomManager.getInstance().getMiniGameService();
        ZegoGameInfoDetail currentGame = miniGameService.getCurrentGame();
        ZEGOSDKUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();

        String supportUser = getContext().getString(R.string.support_player_count, currentGame.playerNum.toString());
        binding.supportUserCount.setText(supportUser);

        List<LiveAudioRoomSeat> audioRoomSeatList = ZEGOLiveAudioRoomManager.getInstance().getAudioRoomSeatList();
        for (LiveAudioRoomSeat seat : audioRoomSeatList) {
            if (!seat.isEmpty()) {
                ZEGOSDKUser seatUser = seat.getUser();
                CheckLetterIconView letterIconView = new CheckLetterIconView(getContext());
                letterIconView.setUserID(seatUser.userID);
                if (Objects.equals(seatUser.userID, hostUser.userID)) {
                    letterIconView.setChecked(true);
                    letterIconView.setEnabled(false);
                }
                binding.userList.addView(letterIconView);

                letterIconView.setCheckListener(new OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        List<String> checkedUserList = getCheckedUserList();
                        int max = currentGame.playerNum.get(currentGame.playerNum.size() - 1);

                        if (checkedUserList.size() >= max) {
                            cannotSelectMore();
                        } else {
                            canSelectAny();
                        }
                    }
                });
            }
        }
        binding.cancel.setOnClickListener(v -> {
            dismiss();
        });

        binding.ok.setOnClickListener(v -> {
            List<ZegoUserSeatInfo> userSeatInfoList = new ArrayList<>();
            List<ZegoRobotSeatInfo> robotSeatInfoList = new ArrayList<>();

            List<String> checkedUserList = getCheckedUserList();

            for (String userID : checkedUserList) {
                ZegoUserSeatInfo seatInfo = new ZegoUserSeatInfo(userID, userSeatInfoList.size());
                userSeatInfoList.add(seatInfo);
            }

            int appropriatePlayerNum = currentGame.playerNum.get(currentGame.playerNum.size() - 1);
            ;
            //            int appropriatePlayerNum = currentGame.playerNum.get(0);
            //            for (Integer integer : currentGame.playerNum) {
            //                if (userSeatInfoList.size() <= integer) {
            //                    appropriatePlayerNum = integer;
            //                    break;
            //                }
            //            }

            if (userSeatInfoList.size() < appropriatePlayerNum) {
                int robotSize = appropriatePlayerNum - userSeatInfoList.size();
                for (int i = 0; i < robotSize; i++) {
                    ZegoRobotSeatInfo robotSeatInfo = new ZegoRobotSeatInfo("robot" + (i + 1),
                        userSeatInfoList.size() + i, null, 0, 999);
                    robotSeatInfoList.add(robotSeatInfo);
                }
            }

            Timber.d("userSeatInfoList.size: " + userSeatInfoList.size());
            for (ZegoUserSeatInfo seatInfo : userSeatInfoList) {
                Timber.d("userSeatInfoList: " + seatInfo.userID);
            }
            Timber.d("robotSeatInfoList.size: " + robotSeatInfoList.size());
            for (ZegoRobotSeatInfo seatInfo : robotSeatInfoList) {
                Timber.d("robotSeatInfoList: " + seatInfo.robotName);
            }

            String roomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
            miniGameService.startGame(currentGame.gameID, roomID, new ZegoStartGameConfig(), userSeatInfoList,
                robotSeatInfoList, new IZegoCommonCallback<String>() {
                    @Override
                    public void onResult(int i, String s) {
                        Timber.d("onResult() called with: i = [" + i + "], s = [" + s + "]");
                    }
                });
            dismiss();
        });
    }

    @NonNull
    private List<String> getCheckedUserList() {
        List<String> checkedUserList = new ArrayList<>();
        int childCount = binding.userList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            CheckLetterIconView letterIconView = (CheckLetterIconView) binding.userList.getChildAt(i);
            if(letterIconView.isChecked()){
                checkedUserList.add(letterIconView.getUserID());
            }
        }
        return checkedUserList;
    }

    private void cannotSelectMore() {
        int childCount = binding.userList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            CheckLetterIconView letterIconView = (CheckLetterIconView) binding.userList.getChildAt(i);
            if (!letterIconView.isChecked()) {
                letterIconView.setEnabled(false);
            }
        }
    }

    private void canSelectAny() {
        int childCount = binding.userList.getChildCount();
        ZEGOSDKUser hostUser = ZEGOLiveAudioRoomManager.getInstance().getHostUser();
        for (int i = 0; i < childCount; i++) {
            CheckLetterIconView letterIconView = (CheckLetterIconView) binding.userList.getChildAt(i);
            if (!Objects.equals(letterIconView.getUserID(), hostUser.userID)) {
                letterIconView.setEnabled(true);
            }
        }
    }
}
