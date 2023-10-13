package com.zegocloud.demo.bestpractice.components;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequest;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequestCallback;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import java.util.ArrayList;
import java.util.List;

public class RoomRequestListAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<String> userIDList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_seatrequest, parent, false);
        int height = Utils.dp2px(70, parent.getContext().getResources().getDisplayMetrics());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        return new ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView memberName = holder.itemView.findViewById(R.id.live_member_item_name);
        TextView agree = holder.itemView.findViewById(R.id.live_member_item_agree);
        TextView disagree = holder.itemView.findViewById(R.id.live_member_item_disagree);

        ZEGOSDKUser cloudUser = ZEGOSDKManager.getInstance().expressService.getUser(userIDList.get(position));
        RoomRequest roomRequest = ZEGOSDKManager.getInstance().zimService.getRoomRequestBySenderID(cloudUser.userID);
        memberName.setText(cloudUser.userName);

        agree.setOnClickListener(v -> {
            ZEGOSDKManager.getInstance().zimService.acceptRoomRequest(roomRequest.requestID, null);
        });

        disagree.setOnClickListener(v -> {
            ZEGOSDKManager.getInstance().zimService.rejectRoomRequest(roomRequest.requestID, null);
        });
    }

    @Override
    public int getItemCount() {
        return userIDList.size();
    }

    public void addItem(String userID) {
        int position = userIDList.size();
        userIDList.add(userID);
        notifyItemInserted(position);
    }

    public void removeItem(String userID) {
        int position = userIDList.indexOf(userID);
        boolean removed = userIDList.remove(userID);
        if (removed) {
            notifyItemRemoved(position);
        }
    }

    public void setItems(List<String> userIDList) {
        this.userIDList.clear();
        this.userIDList.addAll(userIDList);
        notifyDataSetChanged();
    }
}
