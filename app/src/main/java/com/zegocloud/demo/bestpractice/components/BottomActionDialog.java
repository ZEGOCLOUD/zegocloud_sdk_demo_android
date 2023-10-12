package com.zegocloud.demo.bestpractice.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;
import java.util.List;

public class BottomActionDialog extends Dialog {

    private LinearLayout childParent;
    private List<String> stringList;
    private OnClickListener onDialogClickListener;

    public BottomActionDialog(@NonNull Context context, List<String> stringList) {
        super(context);
        this.stringList = stringList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout rootView = new FrameLayout(getContext());
        childParent = new LinearLayout(getContext());
        childParent.setOrientation(LinearLayout.VERTICAL);
        childParent.setBackgroundResource(R.drawable.bg_bottom_action_dialog);
        rootView.addView(childParent);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int cellHeight = Utils.dp2px(50, displayMetrics);
        for (int i = 0; i < stringList.size(); i++) {
            TextView button = new TextView(getContext());
            button.setText(stringList.get(i));
            button.setTextSize(14);
            button.setGravity(Gravity.CENTER);
            childParent.addView(button, new LinearLayout.LayoutParams(-1, cellHeight));
            final int index = i;
            button.setOnClickListener(v -> {
                if (onDialogClickListener != null) {
                    onDialogClickListener.onClick(this, index);
                }
            });
            if (i != stringList.size() - 1) {
                View view = new View(getContext());
                view.setBackgroundColor(Color.parseColor("#1affffff"));
                childParent.addView(view, new LinearLayout.LayoutParams(-1, Utils.dp2px(1, displayMetrics)));
            }
        }
        setContentView(rootView);

        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(attributes);
        window.setGravity(Gravity.BOTTOM);
        //        window.setDimAmount(0.2f);
        window.setBackgroundDrawable(new ColorDrawable());

        setCanceledOnTouchOutside(true);
    }

    public void setOnDialogClickListener(OnClickListener onClickListener) {
        this.onDialogClickListener = onClickListener;
    }
}
