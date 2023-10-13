package com.zegocloud.demo.quickstart;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class FileUtil {

    private static OkHttpClient client = new OkHttpClient();

    public static void downloadFile(String url, Callback callback) {
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
