package com.github.hcsp;

import org.apache.http.HttpHost;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author SuyuZhuang
 * @date 2019/11/26 11:53 下午
 */
public class ElasticsearchDataGenerator {
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

        List<News> newsFromMysql = getNewsFromMysql(sqlSessionFactory);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> runSingleThread(newsFromMysql)).start();
        }


    }

    private static void runSingleThread(List<News> newsFromMysql) {
        long begin = System.currentTimeMillis();
        try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost("localhost", 9200, "http")))) {
            // 批处理
            BulkRequest bulkRequest = new BulkRequest();

            for (News news : newsFromMysql) {
                IndexRequest request = new IndexRequest("news");

                Map<String, Object> data = new HashMap<>();
                data.put("content", news.getContent());
                data.put("title", news.getTitle());
                data.put("url", news.getUrl());
                data.put("createdAt", news.getCreatedAt());
                data.put("modifiedAt", news.getModifiedAt());
                request.source(data);

                bulkRequest.add(request);

//                IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
//                System.out.println(indexResponse.status().getStatus());
            }
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse.status().getStatus());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long end = System.currentTimeMillis();
        long duration = end - begin;
        System.out.println("单线程耗时：" + duration);
    }

    private static List<News> getNewsFromMysql(SqlSessionFactory sqlSessionFactory) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return session.selectList(
                    "com.github.hcsp.MockMapper.selectNews");
        }
    }
}
