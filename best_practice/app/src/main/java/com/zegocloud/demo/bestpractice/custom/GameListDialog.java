package com.zegocloud.demo.bestpractice.custom;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.ZEGOSDKKeyCenter;
import com.zegocloud.demo.bestpractice.databinding.DialogGameListBinding;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveAudioRoomManager;
import com.zegocloud.demo.bestpractice.internal.business.audioroom.MiniGameService;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.components.OnRecyclerViewItemTouchListener;
import com.zegocloud.demo.bestpractice.internal.utils.ToastUtil;
import im.zego.minigameengine.IZegoCommonCallback;
import im.zego.minigameengine.ZegoGameInfo;
import im.zego.minigameengine.ZegoGameInfoDetail;
import im.zego.minigameengine.ZegoGameMode;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import timber.log.Timber;

public class GameListDialog extends Dialog {

    private DialogGameListBinding binding;
    private GameListAdapter gameListAdapter;

    public GameListDialog(@NonNull Context context) {
        super(context, R.style.TransparentDialog);
    }

    public GameListDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected GameListDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogGameListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.3f);

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = height;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        binding.liveRequestListTitle.setText("GameList");

        gameListAdapter = new GameListAdapter();
        MiniGameService miniGameService = ZEGOLiveAudioRoomManager.getInstance().getMiniGameService();
        gameListAdapter.setGameInfoList(miniGameService.getGameInfoList());
        binding.requestRecyclerview.setAdapter(gameListAdapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        binding.requestRecyclerview.setLayoutManager(gridLayoutManager);

        binding.requestRecyclerview.addOnItemTouchListener(
            new OnRecyclerViewItemTouchListener(binding.requestRecyclerview) {
                @Override
                public void onItemClick(ViewHolder vh) {
                    if (vh.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }
                    ZegoGameInfo gameInfo = gameListAdapter.getGameInfo(vh.getAdapterPosition());
                    ToastUtil.show(getContext(), gameInfo.gameName);

                    miniGameService.getGameFullInfo(gameInfo.gameID, new IZegoCommonCallback<ZegoGameInfoDetail>() {
                        @Override
                        public void onResult(int errorCode, ZegoGameInfoDetail zegoGameInfoDetail) {
                            Timber.d("getGameFullInfo onResult() called with: errorCode = [" + errorCode
                                + "], zegoGameInfoDetail = [" + zegoGameInfoDetail + "]");
                            if (errorCode == 0) {

                                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();

                                GameTestApi.exchangeUserCurrency(ZEGOSDKKeyCenter.appID, currentUser.userID,
                                    zegoGameInfoDetail.gameID, String.valueOf(System.currentTimeMillis()), 1000,
                                    new Callback() {
                                        @Override
                                        public void onFailure(@NonNull Call call, @NonNull IOException e) {

                                        }

                                        @Override
                                        public void onResponse(@NonNull Call call, @NonNull Response response)
                                            throws IOException {
                                            GameTestApi.getUserCurrency(ZEGOSDKKeyCenter.appID, currentUser.userID,
                                                zegoGameInfoDetail.gameID);
                                        }
                                    });

                                miniGameService.loadGame(gameInfo.gameID, new HashMap<>(),
                                    new IZegoCommonCallback<String>() {
                                        @Override
                                        public void onResult(int i, String s) {
                                            dismiss();
                                        }
                                    });
                            }
                        }
                    });
                }
            });

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                MiniGameService miniGameService = ZEGOLiveAudioRoomManager.getInstance().getMiniGameService();
                List<ZegoGameInfo> gameInfoList = miniGameService.getGameInfoList();
                if (gameInfoList.isEmpty()) {
                    miniGameService.getGameList(new IZegoCommonCallback<List<ZegoGameInfo>>() {
                        @Override
                        public void onResult(int errorCode, List<ZegoGameInfo> zegoGameInfos) {
                            if (isShowing()) {
                                gameListAdapter.setGameInfoList(gameInfoList);
                            }
                        }
                    });
                } else {
                    gameListAdapter.setGameInfoList(gameInfoList);
                }
            }
        });
    }
}
