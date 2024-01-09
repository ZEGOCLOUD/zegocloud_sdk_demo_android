package com.zegocloud.demo.bestpractice.custom;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.squareup.picasso.Picasso;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import im.zego.minigameengine.ZegoGameInfo;
import java.util.ArrayList;
import java.util.List;

public class GameListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ZegoGameInfo> gameInfoList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_info, parent, false);
        ImageView imageView = inflate.findViewById(R.id.game_icon);
        DisplayMetrics displayMetrics = parent.getContext().getResources().getDisplayMetrics();
        imageView.setLayoutParams(
            new LinearLayout.LayoutParams(Utils.dp2px(64, displayMetrics), Utils.dp2px(64, displayMetrics)));
        inflate.setLayoutParams(
            new RecyclerView.LayoutParams(Utils.dp2px(96, displayMetrics), Utils.dp2px(96, displayMetrics)));
        return new ViewHolder(inflate) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView textView = holder.itemView.findViewById(R.id.game_name);
        ImageView imageView = holder.itemView.findViewById(R.id.game_icon);
        ZegoGameInfo zegoGameInfo = gameInfoList.get(position);
        textView.setText(zegoGameInfo.gameName);
        Picasso.get().load(zegoGameInfo.thumbnail).fit().into(imageView);
    }

    @Override
    public int getItemCount() {
        return gameInfoList.size();
    }

    public void setGameInfoList(List<ZegoGameInfo> gameInfoList) {
        this.gameInfoList.clear();
        if (gameInfoList != null) {
            this.gameInfoList.addAll(gameInfoList);
        }
        notifyDataSetChanged();
    }

    public ZegoGameInfo getGameInfo(int position) {
        return this.gameInfoList.get(position);
    }
}
