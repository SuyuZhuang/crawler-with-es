package com.github.hcsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

public class MyBatisCrawlerDAOImpl implements ICrawlerDAO {
    private SqlSessionFactory sqlSessionFactory;

    @SuppressFBWarnings(value = "DMI_CONSTANT_DB_PASSWORD")
    public MyBatisCrawlerDAOImpl() {
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory =
                    new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String getNextLinkThenDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String url = session.selectOne(
                    "com.github.hcsp.NewsMapper.getNextLink");
            if (url != null) {
                session.delete("com.github.hcsp.NewsMapper.deleteOneLink", url);
            }
            return url;
        }
    }

    @Override
    public void insertNewsIntoDatabase(String link, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            News newsvo = new News(link, title, content);
            session.insert("com.github.hcsp.NewsMapper.insertNews", newsvo);
        }
    }

    @Override
    public boolean isLinkProcessed(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int count = session.selectOne("com.github.hcsp.NewsMapper.countProcessedLink", link);
            return count > 0;
        }
    }

    @Override
    public void insertLinkIntoProcessedLinkTable(String nextLink) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.NewsMapper.insertLinkIntoProcessedLinkTable", nextLink);
        }
    }

    @Override
    public void insertLinkIntoToBeProcessedTable(String href) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.hcsp.NewsMapper.insertLinkIntoToBeProcessedTable", href);
        }
    }
}
