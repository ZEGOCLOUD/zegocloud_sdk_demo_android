package com.zegocloud.demo.bestpractice.activity.livestreaming;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.cohost.LiveRoom;
import java.util.List;

public class SlideAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String TAG = "SlideAdapter";
    private List<LiveRoom> roomIDList;

    public SlideAdapter(List<LiveRoom> roomIDList) {
        this.roomIDList = roomIDList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slide_page, null, false);
        inflate.setLayoutParams(
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(inflate) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LiveRoom liveRoom = roomIDList.get(position);

        TextView textView = holder.itemView.findViewById(R.id.text_view);
        String text = "RoomID:" + " " + liveRoom.toString();
        textView.setText(text);
        textView.setTextColor(Color.WHITE);

//        Random random = new Random();
//        int red = random.nextInt(256);
//        int green = random.nextInt(256);
//        int blue = random.nextInt(256);
//        holder.itemView.setBackgroundColor(Color.rgb(red, green, blue));
    }

    @Override
    public int getItemCount() {
        return roomIDList.size();
    }

    public LiveRoom getItem(int position) {
        if (position < 0 || position > roomIDList.size() - 1) {
            return null;
        }
        return roomIDList.get(position);
    }
}
