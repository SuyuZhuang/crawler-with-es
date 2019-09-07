package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import okhttp3.OkHttpClient;

public class Main {
    public static void main(String[] args) {
        OkHttpClient client = new OkHttpClient();
        ICrawlerDAO dao = new MyBatisCrawlerDAOImpl();

        for (int i = 0; i < 4; i++) {
            new Crawler(client, dao).start();
        }
    }
}
