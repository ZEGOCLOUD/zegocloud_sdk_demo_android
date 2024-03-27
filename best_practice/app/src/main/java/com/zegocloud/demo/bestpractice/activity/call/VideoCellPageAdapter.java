package com.zegocloud.demo.bestpractice.activity.call;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayout.LayoutParams;
import com.zegocloud.demo.bestpractice.components.call.CallCellView;
import com.zegocloud.demo.bestpractice.internal.business.call.CallInviteUser;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class VideoCellPageAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<CallInviteUser> allCallUsers = new ArrayList<>();
    private static final int PAGE_MAX_CELL_COUNT = 9;
    private int pageWidth;
    private int pageHeight;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FlexboxLayout flexboxLayout = new FlexboxLayout(parent.getContext());
        flexboxLayout.setFlexDirection(FlexDirection.ROW);
        flexboxLayout.setFlexWrap(FlexWrap.WRAP);
        flexboxLayout.setLayoutParams(
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ViewHolder(flexboxLayout) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Timber.d("onBindViewHolder() called with: holder = [" + holder + "], position = [" + position + "]");
        List<CallInviteUser> pagerUserList;
        int subStart = position * PAGE_MAX_CELL_COUNT;
        int subEnd = (position + 1) * PAGE_MAX_CELL_COUNT;

        if (subEnd < allCallUsers.size()) {
            pagerUserList = allCallUsers.subList(subStart, subEnd);
        } else {
            pagerUserList = allCallUsers.subList(subStart, allCallUsers.size());
        }
        fillFlexboxLayout((FlexboxLayout) holder.itemView, pagerUserList, position);
    }

    public void setAllCallUsers(List<CallInviteUser> allCallUsers) {
        Timber.d("setAllCallUsers() called with: allCallUsers = [" + allCallUsers + "]");
        this.allCallUsers.clear();
        if (allCallUsers != null && !allCallUsers.isEmpty()) {
            this.allCallUsers.addAll(allCallUsers);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return 1 + ((allCallUsers.size() - 1) / PAGE_MAX_CELL_COUNT);
    }

    private void fillFlexboxLayout(FlexboxLayout flexboxLayout, List<CallInviteUser> callInviteUsers, int position) {
        Timber.d("fillFlexboxLayout() called with: flexboxLayout = [" + flexboxLayout + "], callInviteUsers = ["
            + callInviteUsers + "], position = [" + position + "]");
        flexboxLayout.removeAllViews();

        int width = flexboxLayout.getWidth();
        int height = flexboxLayout.getHeight();
        if (width == 0 || height == 0) {
            width = this.pageWidth;
            height = this.pageHeight;
        }
        Timber.d("fillFlexboxLayout() called with: width = [" + width + "], height = [" + height + "], position = ["
            + position + "]");

        if (position == 0) {
            if (callInviteUsers.size() == 3) {
                flexboxLayout.setFlexDirection(FlexDirection.COLUMN);
            } else {
                flexboxLayout.setFlexDirection(FlexDirection.ROW);
            }
        }
        for (int i = 0; i < callInviteUsers.size(); i++) {
            CallInviteUser callInviteUser = callInviteUsers.get(i);
            CallCellView callCellView = new CallCellView(flexboxLayout.getContext());
            callCellView.dismissLoading();
            callCellView.setCallUser(callInviteUser);
            if (!ZEGOSDKManager.getInstance().expressService.isCurrentUser(callInviteUser.getUserID())) {
                if (callInviteUser.isWaiting()) {
                    callCellView.loading();
                } else {
                    callCellView.dismissLoading();
                }
            }
            FlexboxLayout.LayoutParams layoutParams = new LayoutParams(width / 2, height);
            if (position == 0) {
                if (callInviteUsers.size() == 3) {
                    if (i == 0) {
                        layoutParams.width = width / 2;
                        layoutParams.height = height;
                    } else {
                        layoutParams.width = width / 2;
                        layoutParams.height = height / 2;
                    }
                } else if (callInviteUsers.size() == 4) {
                    layoutParams.width = width / 2;
                    layoutParams.height = height / 2;
                } else if (callInviteUsers.size() == 5) {
                    if (i <= 1) {
                        layoutParams.width = width / 2;
                        layoutParams.height = height / 2;
                    } else {
                        layoutParams.width = width / 3;
                        layoutParams.height = height / 2;
                    }
                } else if (callInviteUsers.size() == 6) {
                    layoutParams.width = width / 3;
                    layoutParams.height = height / 2;
                } else if (callInviteUsers.size() > 6) {
                    layoutParams.width = width / 3;
                    layoutParams.height = height / 3;
                }
            } else {
                layoutParams.width = width / 3;
                layoutParams.height = height / 3;
            }
            callCellView.setLayoutParams(layoutParams);
            flexboxLayout.addView(callCellView);
        }
    }

    public void setPageSize(int width, int height) {
        this.pageWidth = width;
        this.pageHeight = height;
    }
}
