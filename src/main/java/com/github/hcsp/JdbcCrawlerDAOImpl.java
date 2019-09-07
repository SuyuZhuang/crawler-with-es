package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCrawlerDAOImpl implements ICrawlerDAO {
    private static final String USER_NAME = "root";
    private static final String PASSWORD = "root";
    private static final String JDBC_URL = "jdbc:h2:file:E:\\study\\xiedaimala\\gitpractice\\crawler-with-es\\news";

    private final Connection connection;

    @SuppressFBWarnings(value = "DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDAOImpl() {
        try {
            connection = DriverManager.getConnection(JDBC_URL, USER_NAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private String getNextLink(String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                return resultSet.getString(1);
            }
        }
        return null;
    }

    @Override
    public String getNextLinkThenDelete() throws SQLException {
        String nextLink = getNextLink("select link from LINKS_TO_BE_PROCESSED LIMIT 1");
        if (nextLink != null) {
            updateTable(nextLink, "delete from LINKS_TO_BE_PROCESSED where LINK=?");
        }
        return nextLink;
    }

    private void updateTable(String link, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public void insertNewsIntoDatabase(String link, String title, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS(URL,TITLE,CONTENT,CREATED_AT,MODIFIED_AT) VALUES (?,?,?,NOW(),NOW())")) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
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

    @Override
    public void insertLinkIntoProcessedLinkTable(String nextLink) throws SQLException {
        updateTable(nextLink, "insert into LINKS_ALREADY_PROCESSED(link) values (?)");
    }

    @Override
    public void insertLinkIntoToBeProcessedTable(String href) throws SQLException {
        updateTable(href, "insert into LINKS_TO_BE_PROCESSED(link) values (?)");
    }
}
