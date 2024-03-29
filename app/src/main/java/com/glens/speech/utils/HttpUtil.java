package com.glens.speech.utils;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendRequestWithOkhttp(String address, okhttp3.Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).get().build();
        client.newCall(request).enqueue(callback);
    }
}
