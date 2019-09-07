package com.github.hcsp;

import java.sql.SQLException;

public interface ICrawlerDAO {
    String getNextLink(String sql) throws SQLException;

    String getNextLinkThenDelete() throws SQLException;

    void updateTable(String link, String sql) throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;
}
