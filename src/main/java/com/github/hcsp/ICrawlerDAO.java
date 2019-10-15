package com.github.hcsp;

import java.sql.SQLException;

public interface ICrawlerDAO {
    String getNextLinkThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;

    void insertNewsVOIntoDatabase(News vo) throws SQLException;

    boolean isLinkProcessed(String link) throws SQLException;

    void insertLinkIntoProcessedLinkTable(String nextLink) throws SQLException;

    void insertLinkIntoToBeProcessedTable(String href) throws SQLException;
}
