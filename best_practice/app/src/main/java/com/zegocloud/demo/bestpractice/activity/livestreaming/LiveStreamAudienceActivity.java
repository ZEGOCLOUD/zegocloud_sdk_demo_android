package com.zegocloud.demo.bestpractice.activity.livestreaming;

import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.viewpager2.widget.ViewPager2;
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.demo.bestpractice.R;
import com.zegocloud.demo.bestpractice.components.cohost.LiveRoom;
import com.zegocloud.demo.bestpractice.components.cohost.LiveStreamingView;
import com.zegocloud.demo.bestpractice.internal.ZEGOLiveStreamingManager;
import com.zegocloud.demo.bestpractice.internal.business.FakeApi;
import com.zegocloud.demo.bestpractice.internal.sdk.ZEGOSDKManager;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKCallBack;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoViewMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import timber.log.Timber;

public class LiveStreamAudienceActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private List<LiveRoom> roomList = new ArrayList<>();
    private int currentState;
    private Set<String> loadingRooms = new HashSet<>();
    private SlideAdapter slideAdapter;
    private float pageOffset;
    private boolean enablePreLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this liveID will be displayed when join room
        String liveID = getIntent().getStringExtra("liveID");
        roomList.add(new LiveRoom(liveID));

        // to slide watch livestreaming,need get next LiveID to switch .
        LiveRoom nextLive = FakeApi.getNextLive(liveID);
        if (nextLive != null) {
            roomList.add(nextLive);
        }

        setContentView(R.layout.activity_live_slide);

        viewPager2 = findViewById(R.id.view_pager2);
        viewPager2.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        slideAdapter = new SlideAdapter(roomList);
        viewPager2.setAdapter(slideAdapter);

        viewPager2.registerOnPageChangeCallback(new OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (enablePreLoad) {
                    if (currentState == ViewPager2.SCROLL_STATE_DRAGGING) {
                        if (positionOffset < pageOffset) {
                            preLoadItem(position);
                        } else {
                            preLoadItem(position + 1);
                        }
                    }
                }
                pageOffset = positionOffset;
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // once page is selected,need to get next liveID and add to roomlist,
                // so that is ready for slide to next one.
                // if is slide to previous,do nothing to room list
                if (position == roomList.size() - 1) {
                    LiveRoom nextLive = FakeApi.getNextLive(liveID);
                    if (nextLive != null) {
                        int position1 = roomList.size() - 1;
                        if (!roomList.contains(nextLive)) {
                            roomList.add(nextLive);
                            slideAdapter.notifyItemInserted(position1);
                        }
                    }
                }

                onItemPageSelected(position);
                Timber.d("onPageSelected() called with: position = [" + position + "]");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                currentState = state;
                if (enablePreLoad) {
                    if (currentState == ViewPager2.SCROLL_STATE_IDLE) {
                        stopPreLoadItems(viewPager2.getCurrentItem());
                    }
                }
                Timber.d("onPageScrollStateChanged() called with: state = [" + state + "]");
            }
        });

        List<String> permissions = Arrays.asList(permission.CAMERA, permission.RECORD_AUDIO);
        requestPermissionIfNeeded(permissions, new RequestCallback() {

            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {

            }
        });
    }

    // preload +1 or -1 position when start dragging slide
    private void preLoadItem(int position) {
        if (position < 0 || position > roomList.size() - 1) {
            return;
        }
        LiveRoom liveRoom = slideAdapter.getItem(position);
        // if no live room
        if (liveRoom == null || liveRoom.hostUserID == null) {
            return;
        }
        // if this room has loaded
        if (loadingRooms.contains(liveRoom.roomID)) {
            return;
        }

        Timber.d("preLoadItem() called with: position = [" + position + "]");

        loadingRooms.add(liveRoom.roomID);

        String streamID = liveRoom.roomID + "_" + liveRoom.hostUserID + "_main" + "_host";

        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            ViewGroup simpleViewParent = (ViewGroup) viewHolder.itemView.findViewById(R.id.simple_view_layout);
            simpleViewParent.setVisibility(View.VISIBLE);

            TextureView textureView = viewHolder.itemView.findViewById(R.id.texture_view);
            ZEGOSDKManager.getInstance().expressService.startPlayingStream(textureView, streamID,
                ZegoViewMode.ASPECT_FIT);
        }
    }

    private void onItemPageSelected(int position) {
        if (position < 0 || position > roomList.size() - 1) {
            return;
        }
        LiveRoom liveRoom = slideAdapter.getItem(position);
        // if no live room
        if (liveRoom == null) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            joinRoom(position, viewHolder, new ZEGOSDKCallBack() {
                @Override
                public void onResult(int errorCode, String message) {
                    if (errorCode == 0) {
                        loadingRoomFullViews(position);
                        stopPreLoadItems(position);
                    }
                }
            });
        }
    }

    private void joinRoom(int position, ViewHolder viewHolder, ZEGOSDKCallBack callBack) {
        LiveRoom liveRoom = slideAdapter.getItem(position);

        // leave room will auto stop streams and remove subview's sdk event listeners.
        ZEGOLiveStreamingManager.getInstance().leave();

        ViewGroup simpleViewParent = (ViewGroup) viewHolder.itemView.findViewById(R.id.simple_view_layout);
        simpleViewParent.setVisibility(View.GONE);
        ViewGroup fullViewParent = (ViewGroup) viewHolder.itemView.findViewById(R.id.full_view_layout);
        fullViewParent.removeAllViews();

        LiveStreamingView liveStreamingView = new LiveStreamingView(this);
        fullViewParent.addView(liveStreamingView);

        // create subviews now,because their sdk event listeners was add when constructor,
        // and removed when leave room(internal logic).
        ZEGOSDKManager.getInstance().expressService.openCamera(false);
        ZEGOSDKManager.getInstance().expressService.openMicrophone(false);
        ZEGOLiveStreamingManager.getInstance().addListenersForUserJoinRoom();

        liveStreamingView.prepareForJoinLive();

        ZEGOSDKManager.getInstance().loginRoom(liveRoom.roomID, ZegoScenario.BROADCAST, new ZEGOSDKCallBack() {
            @Override
            public void onResult(int errorCode, String message) {
                Timber.d("loginRoom " + liveRoom.roomID + " onResult() called with: errorCode = [" + errorCode
                    + "], message = [" + message + "]");
                if (callBack != null) {
                    callBack.onResult(errorCode, message);
                }
            }
        });
    }

    //when ensured a room to load,unload other preloaded rooms
    private void stopPreLoadItems(int position) {
        if (position < 0 || position > roomList.size() - 1) {
            return;
        }
        LiveRoom liveRoom = slideAdapter.getItem(position);
        if (liveRoom == null) {
            return;
        }
        loadingRooms.remove(liveRoom.roomID);
        for (String loadingRoom : loadingRooms) {
            for (LiveRoom room : roomList) {
                if (Objects.equals(room.roomID, loadingRoom)) {
                    String streamID = room.roomID + "_" + room.hostUserID + "_main" + "_host";
                    ZEGOSDKManager.getInstance().expressService.stopPlayingStream(streamID);
                    break;
                }
            }
        }
        loadingRooms.clear();

        loadingRooms.add(liveRoom.roomID);


    }

    private void loadingRoomFullViews(int position) {
        if (position < 0 || position > roomList.size() - 1) {
            return;
        }

        LiveRoom liveRoom = slideAdapter.getItem(position);
        // if no live room
        if (liveRoom == null) {
            return;
        }
        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);

        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if (viewHolder != null) {
            ViewGroup fullViewParent = (ViewGroup) viewHolder.itemView.findViewById(R.id.full_view_layout);
            LiveStreamingView liveStreamingView = (LiveStreamingView) fullViewParent.getChildAt(0);
            if (liveStreamingView != null) {
                liveStreamingView.onJoinRoomSuccess(liveRoom.roomID);
            }

            Timber.d("loadItem() called with: liveRoom.roomID = [" + liveRoom.roomID + "]");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            loadingRooms.clear();
            roomList.clear();
            ZEGOLiveStreamingManager.getInstance().leave();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!loadingRooms.isEmpty()) {
            loadingRooms.clear();
            roomList.clear();
            ZEGOLiveStreamingManager.getInstance().leave();
        }
    }

    private void requestPermissionIfNeeded(List<String> permissions, RequestCallback requestCallback) {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        if (allGranted) {
            requestCallback.onResult(true, permissions, new ArrayList<>());
            return;
        }

        PermissionX.init(this).permissions(permissions).onExplainRequestReason((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.permission_explain_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.permission_explain_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.permission_explain_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.permission_explain_mic);
                    }
                } else {
                    message = this.getString(R.string.permission_explain_camera_mic);
                }
            }
            scope.showRequestReasonDialog(deniedList, message, getString(R.string.ok));
        }).onForwardToSettings((scope, deniedList) -> {
            String message = "";
            if (permissions.size() == 1) {
                if (deniedList.contains(permission.CAMERA)) {
                    message = this.getString(R.string.settings_camera);
                } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                    message = this.getString(R.string.settings_mic);
                }
            } else {
                if (deniedList.size() == 1) {
                    if (deniedList.contains(permission.CAMERA)) {
                        message = this.getString(R.string.settings_camera);
                    } else if (deniedList.contains(permission.RECORD_AUDIO)) {
                        message = this.getString(R.string.settings_mic);
                    }
                } else {
                    message = this.getString(R.string.settings_camera_mic);
                }
            }
            scope.showForwardToSettingsDialog(deniedList, message, getString(R.string.settings),
                getString(R.string.cancel));
        }).request(new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (requestCallback != null) {
                    requestCallback.onResult(allGranted, grantedList, deniedList);
                }
            }
        });
    }
}