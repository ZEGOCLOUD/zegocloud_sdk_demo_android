package com.zegocloud.demo.bestpractice.components.message.barrage;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.utils.Utils;

public class BottomInputDialog extends Dialog {

    private boolean enableChat = true;

    public BottomInputDialog(@NonNull Context context) {
        super(context);
    }

    public BottomInputDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ViewGroup contentView = (ViewGroup) LayoutInflater.from(getContext())
            .inflate(R.layout.layout_bottom_input, null, false);
        FrameLayout contentViewParent = new FrameLayout(getContext());
        contentViewParent.addView(contentView);
        setContentView(contentViewParent);

        int corner = Utils.dp2px(8f, getContext().getResources().getDisplayMetrics());
        float[] outerR = new float[]{corner, corner, corner, corner, corner, corner, corner, corner};
        RoundRectShape roundRectShape = new RoundRectShape(outerR, null, null);

        ShapeDrawable shapeDrawable = new ShapeDrawable();
        shapeDrawable.getPaint().setColor(Color.parseColor("#222222"));
        contentView.setBackground(shapeDrawable);

        ImageView sendBtn = findViewById(R.id.send_message);
        sendBtn.setEnabled(false);

        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_enabled},
            ContextCompat.getDrawable(getContext(), R.drawable.zego_uikit_icon_send_normal));
        sld.addState(new int[]{}, ContextCompat.getDrawable(getContext(), R.drawable.zego_uikit_icon_send_disable));
        sendBtn.setImageDrawable(sld);

        EditText editText = findViewById(R.id.edit_message_input);

        sendBtn.setOnClickListener(v -> {
            String message = editText.getText().toString();
            ZEGOSDKManager.getInstance().expressService.sendBarrageMessage(message, (errorCode, messageID) -> {

            });
            editText.setText("");
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendBtn.setEnabled(editText.getText().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        RoundRectShape roundRectShape2 = new RoundRectShape(outerR, null, null);
        ShapeDrawable shapeDrawable2 = new ShapeDrawable(roundRectShape2);
        shapeDrawable2.getPaint().setColor(Color.parseColor("#1AFFFFFF"));
        editText.setBackground(shapeDrawable2);

        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());

        int mode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        window.setSoftInputMode(mode);

        setCanceledOnTouchOutside(true);

        setOnDismissListener(dialog -> {
            hideInputWindow(contentView);
        });

        requestInputWindow(editText);
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
            dismiss();
            return true;
        }
        return false;
    }

    public static void hideInputWindow(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void requestInputWindow(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean input = false;
        if (view.isAttachedToWindow()) {
            input = imm.showSoftInput(view, 0);
        }
    }
}
