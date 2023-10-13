package com.zegocloud.demo.bestpractice.components;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.databinding.DialogRoomReqestListBinding;
import com.zegocloud.demo.bestpractice.internal.business.RoomRequestExtendedData;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKUser;
import com.zegocloud.demo.bestpractice.internal.sdk.express.IExpressEngineEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.IZIMEventHandler;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.RoomRequest;
import java.util.ArrayList;
import java.util.List;

public class RoomRequestListDialog extends BottomSheetDialog {

    private DialogRoomReqestListBinding binding;
    private RoomRequestListAdapter roomRequestListAdapter;
    private IZIMEventHandler incomingRoomRequestListener;
    private IExpressEngineEventHandler roomUserChangeListener;
    private int roomRequestType;

    public RoomRequestListDialog(@NonNull Context context) {
        super(context, R.style.TransparentDialog);
    }

    public RoomRequestListDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected RoomRequestListDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public void setRoomRequestType(int roomRequestType) {
        this.roomRequestType = roomRequestType;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogRoomReqestListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.1f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        // both need setPeekHeight & setLayoutParams
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int height = (int) (displayMetrics.heightPixels * 0.6f);
        getBehavior().setPeekHeight(height);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(-1, height);
        binding.liveRequestListLayout.setLayoutParams(params);

        binding.requestRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        roomRequestListAdapter = new RoomRequestListAdapter();

        binding.requestRecyclerview.setAdapter(roomRequestListAdapter);
        incomingRoomRequestListener = new IZIMEventHandler() {
            @Override
            public void onInComingRoomRequestReceived(String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == roomRequestType) {
                    RoomRequest request = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(requestID);
                    if (request != null) {
                        roomRequestListAdapter.addItem(request.sender);
                    }
                }
            }

            @Override
            public void onInComingRoomRequestCancelled(String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == roomRequestType) {
                    RoomRequest request = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(requestID);
                    if (request != null) {
                        roomRequestListAdapter.removeItem(request.sender);
                    }
                }
            }

            @Override
            public void onAcceptIncomingRoomRequest(int errorCode, String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == roomRequestType) {
                    RoomRequest request = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(requestID);
                    if (request != null) {
                        roomRequestListAdapter.removeItem(request.receiver);
                    }
                }
            }

            @Override
            public void onRejectIncomingRoomRequest(int errorCode, String requestID, String extendedData) {
                RoomRequestExtendedData data = RoomRequestExtendedData.parse(extendedData);
                if (data != null && data.roomRequestType == roomRequestType) {
                    RoomRequest request = ZEGOSDKManager.getInstance().zimService.getRoomRequestByRequestID(requestID);
                    if (request != null) {
                        roomRequestListAdapter.removeItem(request.receiver);
                    }
                }
            }
        };
        roomUserChangeListener = new IExpressEngineEventHandler() {

            @Override
            public void onUserLeft(List<ZEGOSDKUser> userList) {
                for (ZEGOSDKUser zegosdkUser : userList) {
                    roomRequestListAdapter.removeItem(zegosdkUser.userID);
                }
            }
        };

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                List<String> requestUserList = new ArrayList<>();
                List<RoomRequest> myReceivedRoomRequests = ZEGOSDKManager.getInstance().zimService.getMyReceivedRoomRequests();
                for (RoomRequest roomRequest : myReceivedRoomRequests) {
                    RoomRequestExtendedData data = RoomRequestExtendedData.parse(roomRequest.extendedData);
                    if (data != null && data.roomRequestType == roomRequestType) {
                        requestUserList.add(roomRequest.sender);
                    }
                }
                roomRequestListAdapter.setItems(requestUserList);
                ZEGOSDKManager.getInstance().expressService.addEventHandler(roomUserChangeListener);
                ZEGOSDKManager.getInstance().zimService.addEventHandler(incomingRoomRequestListener);
            }
        });
        setOnDismissListener(dialog -> {
            ZEGOSDKManager.getInstance().zimService.removeEventHandler(incomingRoomRequestListener);
            ZEGOSDKManager.getInstance().expressService.removeEventHandler(roomUserChangeListener);
        });
    }
}
