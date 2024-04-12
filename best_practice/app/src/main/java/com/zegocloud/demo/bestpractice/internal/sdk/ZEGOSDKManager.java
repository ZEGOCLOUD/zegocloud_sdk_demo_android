package com.zegocloud.demo.bestpractice.internal.sdk;

import android.app.Application;
import android.text.TextUtils;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.MergeCallBack;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZEGOSDKCallBack;
import com.zegocloud.demo.bestpractice.internal.sdk.basic.ZegoTokenExpireListener;
import com.zegocloud.demo.bestpractice.internal.sdk.express.ExpressService;
import com.zegocloud.demo.bestpractice.internal.sdk.zim.ZIMService;
import im.zego.zegoexpress.callback.IZegoRoomLoginCallback;
import im.zego.zegoexpress.callback.IZegoRoomLogoutCallback;
import im.zego.zegoexpress.callback.IZegoUploadLogResultCallback;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zim.callback.ZIMLogUploadedCallback;
import im.zego.zim.callback.ZIMLoggedInCallback;
import im.zego.zim.callback.ZIMRoomEnteredCallback;
import im.zego.zim.callback.ZIMRoomLeftCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMRoomFullInfo;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.Objects;
import org.json.JSONObject;
import timber.log.Timber;

public class ZEGOSDKManager {

    public ExpressService expressService = new ExpressService();
    public ZIMService zimService = new ZIMService();
    private String token;
    private ZegoTokenExpireListener tokenExpireListener;
    private long lastNotifyTokenTime;

    private static final class Holder {

        private static final ZEGOSDKManager INSTANCE = new ZEGOSDKManager();
    }

    private ZEGOSDKManager() {

    }

    public static ZEGOSDKManager getInstance() {
        return Holder.INSTANCE;
    }

    public void initSDK(Application application, long appID, String appSign) {
        initSDK(application, appID, appSign, ZegoScenario.DEFAULT);
    }

    public void initSDK(Application application, long appID, String appSign, ZegoScenario scenario) {
        expressService.initSDK(application, appID, appSign, scenario);
        zimService.initSDK(application, appID, appSign);
    }

    public void initSDKWithToken(Application application, long appID, String token) {
        this.token = token;
        initSDK(application, appID, "", ZegoScenario.DEFAULT);
    }

    public void connectUser(String userID, String userName, ZEGOSDKCallBack callback) {
        connectUser(userID, userName, token, callback);
    }

    public void connectUser(String userID, String userName, String token, ZEGOSDKCallBack callback) {
        expressService.connectUser(userID, userName);
        zimService.connectUser(userID, userName, token, new ZIMLoggedInCallback() {
            @Override
            public void onLoggedIn(ZIMError errorInfo) {
                if (callback != null) {
                    callback.onResult(errorInfo.code.value(), errorInfo.message);
                }
            }
        });
    }

    public void disconnectUser() {
        zimService.logoutRoom(null);
        expressService.logoutRoom(null);
        zimService.disconnectUser();
        expressService.disconnectUser();

    }

    public void loginRTCRoom(String roomID, IZegoRoomLoginCallback callback) {
        expressService.loginRoom(roomID, token, new IZegoRoomLoginCallback() {
            @Override
            public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                if (callback != null) {
                    callback.onRoomLoginResult(errorCode, extendedData);
                }
            }
        });
    }

    /**
     * if you init with valid appSign,token is not needed,else token is required.
     *
     * @param roomID
     * @param scenario
     * @param callback
     */
    public void loginRoom(String roomID, ZegoScenario scenario, ZEGOSDKCallBack callback) {
        Timber.d(
            "loginRoom() called with: roomID = [" + roomID + "], scenario = [" + scenario + "], callback = [" + callback
                + "]");
        zimService.loginRoom(roomID, new ZIMRoomEnteredCallback() {
            @Override
            public void onRoomEntered(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    expressService.setRoomScenario(scenario);
                    expressService.loginRoom(roomID, token, new IZegoRoomLoginCallback() {
                        @Override
                        public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                            if (callback != null) {
                                callback.onResult(errorCode, "express error:" + extendedData.toString());
                            }
                        }
                    });
                } else {
                    if (callback != null) {
                        callback.onResult(errorInfo.code.value(), "zim error:" + errorInfo.message);
                    }
                }
            }
        });
    }

    public void switchRoom(String fromRoomID, String toRoomID, ZEGOSDKCallBack callback) {
        logoutRoom(new ZEGOSDKCallBack() {
            @Override
            public void onResult(int errorCode, String message) {
                zimService.loginRoom(toRoomID, new ZIMRoomEnteredCallback() {
                    @Override
                    public void onRoomEntered(ZIMRoomFullInfo roomInfo, ZIMError errorInfo) {
                        if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                            expressService.loginRoom(toRoomID, token, new IZegoRoomLoginCallback() {
                                @Override
                                public void onRoomLoginResult(int errorCode, JSONObject extendedData) {
                                    if (callback != null) {
                                        callback.onResult(errorCode, "express error:" + extendedData.toString());
                                    }
                                }
                            });
                        } else {
                            if (callback != null) {
                                callback.onResult(errorInfo.code.value(), "zim error:" + errorInfo.message);
                            }
                        }
                    }
                });
            }
        });
    }

    public void logoutRoom(ZEGOSDKCallBack callBack) {
        Timber.d("logoutRoom() called with: callBack = [" + callBack + "]");
        MergeCallBack<Integer, ZIMError> mergeCallBack = new MergeCallBack<Integer, ZIMError>() {
            @Override
            public void onResult(Integer integer, ZIMError zimError) {
                int errorCode;
                String errorMessage = "";
                if (integer == 0) {
                    errorMessage = zimError.message;
                    errorCode = zimError.code.value();
                } else {
                    errorCode = integer;
                }
                if (callBack != null) {
                    callBack.onResult(errorCode, errorMessage);
                }
            }
        };

        expressService.logoutRoom(new IZegoRoomLogoutCallback() {
            @Override
            public void onRoomLogoutResult(int errorCode, JSONObject extendedData) {
                Timber.d(
                    "onRoomLogoutResult() called with: errorCode = [" + errorCode + "], extendedData = [" + extendedData
                        + "]");
                mergeCallBack.setResult1(errorCode);
            }
        });
        zimService.logoutRoom(new ZIMRoomLeftCallback() {
            @Override
            public void onRoomLeft(String roomID, ZIMError errorInfo) {
                Timber.d("onRoomLeft() called with: roomID = [" + roomID + "], errorInfo = [" + errorInfo + "]");
                mergeCallBack.setResult2(errorInfo);
            }
        });
    }

    public void uploadLog(ZEGOSDKCallBack callBack) {

        MergeCallBack<Integer, ZIMError> mergeCallBack = new MergeCallBack<Integer, ZIMError>() {
            @Override
            public void onResult(Integer integer, ZIMError zimError) {
                int errorCode;
                String errorMessage = "";
                if (integer == 0) {
                    errorMessage = zimError.message;
                    errorCode = zimError.code.value();
                } else {
                    errorCode = integer;
                }
                if (callBack != null) {
                    callBack.onResult(errorCode, errorMessage);
                }
            }
        };
        expressService.uploadLog(new IZegoUploadLogResultCallback() {

            @Override
            public void onUploadLogResult(int errorCode) {
                mergeCallBack.setResult1(errorCode);
            }
        });

        zimService.uploadLog(new ZIMLogUploadedCallback() {

            @Override
            public void onLogUploaded(ZIMError errorInfo) {
                mergeCallBack.setResult2(errorInfo);
            }
        });
    }

    public void renewToken(String token) {
        if (!Objects.equals(token, this.token)) {
            String currentRoomID = expressService.getCurrentRoomID();
            if (!TextUtils.isEmpty(currentRoomID)) {
                expressService.renewToken(currentRoomID, token);
            }
        }
        zimService.renewToken(token, null);
        this.token = token;
    }

    public void setTokenWillExpireListener(ZegoTokenExpireListener listener) {
        this.tokenExpireListener = listener;
    }

    public void notifyTokenWillExpire(int seconds) {
        if (System.currentTimeMillis() - lastNotifyTokenTime > 5 * 60 * 1000) {
            if (tokenExpireListener != null) {
                tokenExpireListener.onTokenWillExpire(seconds);
            }
        }
        this.lastNotifyTokenTime = System.currentTimeMillis();
    }
}
