package com.github.hcsp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static final OkHttpClient client = new OkHttpClient();

    public static void main(String[] args) throws IOException {
        // 待处理的链接池
        List<String> linkPool = new ArrayList<>();
        // 已处理的链接池
        Set<String> processedLinks = new HashSet<>();

        linkPool.add("https://sina.cn");
        while (!linkPool.isEmpty()) {
            String link = linkPool.remove(linkPool.size() - 1);

            if (processedLinks.contains(link)) {
                continue;
            }

            if (link.startsWith("//")) {
                link = "https:" + link;
            }

            if (isInterestingLink(link)) {
                Document doc = getAndParceHtml(link);
                doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(linkPool::add);
                storeInToDatabaseIfItIsNewsPage(doc);
                processedLinks.add(link);
            }
        }
    }

    private static void storeInToDatabaseIfItIsNewsPage(Document doc) {
        ArrayList<Element> articleTags = doc.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleElement : articleTags) {
                System.out.println(articleElement.child(0).text());
            }
        }
    }

    private static Document getAndParceHtml(String link) {
        Document doc = null;
        Request request = new Request.Builder()
                .url(link)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:68.0) Gecko/20100101 Firefox/68.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
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
