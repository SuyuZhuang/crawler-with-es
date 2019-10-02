package com.github.hcsp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler extends Thread {
    private OkHttpClient client;

    private ICrawlerDAO dao;

    public Crawler(OkHttpClient client, ICrawlerDAO dao) {
        this.client = client;
        this.dao = dao;
    }

    @Override
    public void run() {
        String nextLink;
        try {
            while ((nextLink = dao.getNextLinkThenDelete()) != null) {
                if (dao.isLinkProcessed(nextLink)) {
                    continue;
                }
                if (isInterestingLink(nextLink)) {
                    Document doc = getAndParseHtml(nextLink);
                    parseUrlsFromPageAndStoreIntoDatabase(doc);
                    storeInToDatabaseIfItIsNewsPage(doc, nextLink);
                    dao.insertLinkIntoProcessedLinkTable(nextLink);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void parseUrlsFromPageAndStoreIntoDatabase(Document doc) throws SQLException {
        if (doc == null) {
            return;
        }
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            if (!href.toLowerCase().startsWith("javascript")) {
                dao.insertLinkIntoToBeProcessedTable(href);
            }

        }
    }

    private void storeInToDatabaseIfItIsNewsPage(Document doc, String link) throws SQLException {
        if (doc == null) {
            return;
        }
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleElement : articleTags) {
                String title = articleElement.child(0).text();
                String content = articleElement.select("p").stream()
                        .map(Element::text).collect(Collectors.joining("\n"));
                System.out.println(title);
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }


    }


    private Document getAndParseHtml(String link) {
        Document doc = null;
        Request request = new Request.Builder()
                .url(link)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0")
                .build();

        try (Response response = this.client.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body != null) {
                doc = Jsoup.parse(body.string());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private static boolean isInterestingLink(String link) {
        return isNewsPageLink(link) || isIndexLink(link) && isNotLoginPage(link);
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }

    private static boolean isIndexLink(String link) {
        return "https://sina.cn".equals(link);
    }

    private static boolean isNewsPageLink(String link) {
        return link.contains("news.sina.cn");
    }
}
