package com.zegocloud.demo.bestpractice.components.audioroom;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;

public class TopMenuBar extends LinearLayout {

    private TextView roomIDTextView;

    public TopMenuBar(@NonNull Context context) {
        super(context);
        initView();
    }

    public TopMenuBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TopMenuBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public TopMenuBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {

        setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        int marginEnd = Utils.dp2px(12, getResources().getDisplayMetrics());
        params.setMargins(marginEnd, marginEnd, 0, 0);

        TextView roomIDPrefix = new TextView(getContext());
        roomIDPrefix.setText("RoomID:");
        addView(roomIDPrefix, params);

        roomIDTextView = new TextView(getContext());
        roomIDTextView.setMaxLines(1);
        roomIDTextView.setEllipsize(TruncateAt.END);
        roomIDTextView.setSingleLine(true);
        roomIDTextView.getPaint().setFakeBoldText(true);
        roomIDTextView.setTextColor(Color.parseColor("#ff1b1b1b"));
        roomIDTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        roomIDTextView.setMaxWidth(Utils.dp2px(200, getResources().getDisplayMetrics()));
        addView(roomIDTextView, params);
    }

    public void setRoomID(String roomID) {
        roomIDTextView.setText(roomID);
    }
}
