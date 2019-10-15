package com.github.hcsp;

import okhttp3.OkHttpClient;

public class Main {
    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();
        ICrawlerDAO dao = new MyBatisCrawlerDAOImpl();

        for (int i = 0; i < 10; i++) {
            new Crawler(client, dao).start();
        }
    }
}
