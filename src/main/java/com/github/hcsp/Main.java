package com.github.hcsp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;

public class Main {
    public static OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) {
        String url = "https://sina.cn";
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println(Objects.requireNonNull(response.body()).string());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
