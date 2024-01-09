package com.zegocloud.demo.bestpractice.custom;

import android.text.TextUtils;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.HttpLoggingInterceptor.Level;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;

public class GameTestApi {

    private static final String miniGameHostUrl = "https://mini-game-test-server.zego.im";
    // final miniGameHostUrl = "http://192.168.38.62:3020";
    private static final String apiToken = "api/token";
    private static final String apiGetUserCurrency = "api/getUserCurrency";
    private static final String apiExchangeUserCurrency = "api/exchangeUserCurrency";
    private static HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    private static final MediaType JSON = MediaType.get("application/json");
    private static OkHttpClient client;


    public static void getToken(long appID, String userID, Callback callback) {
        if (client == null) {
            logging.setLevel(Level.BODY);
            client = new OkHttpClient.Builder().addInterceptor(logging).build();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("app_id", appID);
        jsonObject.put("user_id", userID);
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(miniGameHostUrl + "/" + apiToken).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    public static void getUserCurrency(long appID, String userID, String gameID) {
        if (client == null) {
            logging.setLevel(Level.BASIC);
            client = new OkHttpClient.Builder().addInterceptor(logging).build();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AppId", appID);
        jsonObject.put("UserId", userID);
        jsonObject.put("MiniGameId", gameID);
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(miniGameHostUrl + "/" + apiGetUserCurrency).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                // ...
            }

            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public static void exchangeUserCurrency(long appID, String userID, String gameID, String outOrderId,
        int exchangeValue, Callback callback) {
        if (client == null) {
            logging.setLevel(Level.BASIC);
            client = new OkHttpClient.Builder().addInterceptor(logging).build();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("AppId", appID);
        jsonObject.put("UserId", userID);
        jsonObject.put("MiniGameId", gameID);
        jsonObject.put("OutOrderId", outOrderId);
        jsonObject.put("CurrencyDiff", exchangeValue);
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder().url(miniGameHostUrl + "/" + apiExchangeUserCurrency).post(body).build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            public void onResponse(Call call, Response response) throws IOException {
                if (callback != null) {
                    callback.onResponse(call, response);
                }
            }

            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFailure(call, e);
                }
            }
        });
    }
}
