package com.github.hcsp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * @author SuyuZhuang
 * @date 2019/10/15 10:38 下午
 */
public class MockDataGenerator {

    private static final int TARGET_ROW_COUNT = 100_0000;

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory =
                    new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            List<News> news = session.selectList(
                    "com.github.hcsp.MockMapper.selectNews");
            int count = TARGET_ROW_COUNT - news.size();
            Random random = new Random();
            try {
                // 先判断>0再--
                while (count-- > 0) {
                    int index = random.nextInt(news.size());
                    News newsToBeInsert = news.get(index);
                    Instant currentTime = newsToBeInsert.getCreatedAt();
                    currentTime = currentTime.minusSeconds(random.nextInt(3600 * 24 * 365));
                    newsToBeInsert.setCreatedAt(currentTime);
                    newsToBeInsert.setModifiedAt(currentTime);
                    session.insert("com.github.hcsp.MockMapper.insertNews",
                            newsToBeInsert);
                    if (count % 2000 == 0) {
                        session.flushStatements();
                        System.out.println(count);
                    }
                }
            } catch (Exception e) {
                session.rollback();
            }

            session.commit();
        }
    }
}
