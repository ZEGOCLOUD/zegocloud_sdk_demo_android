package com.zegocloud.demo.bestpractice.components.cohost;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.ZEGOAudioVideoView;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class CoHostAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<ZEGOSDKUser> userList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_cohost_video, parent, false);

        DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(Utils.dp2px(93, displayMetrics),
            Utils.dp2px(124, displayMetrics));
        view.setLayoutParams(params);
        return new ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ZEGOSDKUser user = userList.get(position);
        ZEGOAudioVideoView videoView = holder.itemView.findViewById(R.id.cohost_video_view);
        TextView textView = holder.itemView.findViewById(R.id.cohost_video_name);
        videoView.setUserID(user.userID);
        if (ZEGOSDKManager.getInstance().expressService.isCurrentUser(user.userID)) {
            if (user.isCameraOpen()) {
                videoView.startPreviewOnly();
            }
            if (TextUtils.isEmpty(user.getMainStreamID())) {
                ZEGOSDKUser currentUser = ZEGOSDKManager.getInstance().expressService.getCurrentUser();
                String currentRoomID = ZEGOSDKManager.getInstance().expressService.getCurrentRoomID();
                String streamID = ZEGOLiveStreamingManager.getInstance()
                    .generateUserStreamID(currentUser.userID, currentRoomID);
                videoView.setStreamID(streamID);
                videoView.startPublishAudioVideo();
            }
        } else {
            String currentRoomID = ZEGOLiveStreamingManager.getInstance().getCurrentRoomID();
            String streamID = ZEGOLiveStreamingManager.getInstance().generateUserStreamID(user.userID, currentRoomID);
            videoView.setStreamID(streamID);
            videoView.startPlayRemoteAudioVideo(currentRoomID);
        }
        if (user.isCameraOpen()) {
            videoView.setVisibility(View.VISIBLE);
            videoView.showVideoView();
        } else if (user.isMicrophoneOpen()) {
            videoView.setVisibility(View.VISIBLE);
            videoView.showAudioView();
        } else {
            videoView.setVisibility(View.GONE);
        }
        textView.setText(user.userName);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public void addUser(List<ZEGOSDKUser> list) {
        if (list.isEmpty()) {
            return;
        }
        userList.addAll(list);
        notifyDataSetChanged();
    }

    public void addUser(ZEGOSDKUser user) {
        int position = userList.size();
        userList.add(user);
        notifyItemInserted(position);
    }

    public void removeUser(List<ZEGOSDKUser> list) {
        userList.removeAll(list);
        notifyDataSetChanged();
    }

    public void removeUser(ZEGOSDKUser user) {
        int position = userList.size();
        userList.remove(user);
        notifyItemRemoved(position);
    }
}
