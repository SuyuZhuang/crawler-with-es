package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";
    private static final String JDBC_URL = "jdbc:h2:file:E:\\study\\xiedaimala\\gitpractice\\crawler-with-es\\news";


    private static String getNextLink(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    private static String getNextLinkThenDelete(Connection connection) throws SQLException {
        String nextLink = getNextLink(connection, "select link from LINKS_TO_BE_PROCESSED");
        if (nextLink != null) {
            updateTable(connection, nextLink, "delete from LINKS_TO_BE_PROCESSED where LINK=?");
        }
        return nextLink;
    }

    @SuppressFBWarnings(value = "DMI_CONSTANT_DB_PASSWORD")
    public static void main(String[] args) throws SQLException {

        Connection connection = DriverManager.getConnection(JDBC_URL, USER_NAME, PASSWORD);

        String nextLink;
        while ((nextLink = getNextLinkThenDelete(connection)) != null) {

            if (isLinkProcessed(connection, nextLink)) {
                continue;
            }

            if (isInterestingLink(nextLink)) {
                Document doc = getAndParseHtml(nextLink);
                parseUrlsFromPageAndStoreIntoDatabase(connection, doc);
                storeInToDatabaseIfItIsNewsPage(doc);
                updateTable(connection, nextLink, "insert into LINKS_ALREADY_PROCESSED(link) values (?)");
            }
        }
    }

    private static void parseUrlsFromPageAndStoreIntoDatabase(Connection connection, Document doc) throws SQLException {
        for (Element aTag : doc.select("a")) {
            String href = aTag.attr("href");
            if (href.startsWith("//")) {
                href = "https:" + href;
            }
            updateTable(connection, href, "insert into LINKS_TO_BE_PROCESSED(link) values (?)");
        }
    }

    private static boolean isLinkProcessed(Connection connection, String link) throws SQLException {
        ResultSet resultSet = null;
        try (PreparedStatement statement = connection.prepareStatement("select link from LINKS_ALREADY_PROCESSED where LINK=?")) {
            statement.setString(1, link);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                return true;
            }
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
        return false;
    }

    private static void updateTable(Connection connection, String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
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

    private static Document getAndParseHtml(String link) {
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
