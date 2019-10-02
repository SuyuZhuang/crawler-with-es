# 多线程爬虫和ES数据分析

## 1 简介和运行方法

**进度**：当前项目实现了多线程的爬虫，但是还没有开始引入ElasticSearch做数据分析。


**内容**：爬取的内容是[手机新浪网](https://sina.cn)的所有新闻。



**数据库**：当前所有数据是存储在H2数据库中。DAO层有两个实现，一个是MyBatisCrawlerDAOImpl.java，一个是JdbcCrawlerDAOImpl.java
- JdbcCrawlerDAOImpl.java 最原始的实现，配置直接写进了Java类中
- MyBatisCrawlerDAOImpl.java，当前实际使用的实现
- 注意：由于jdbc链接是以绝对路径写的，所以下载代码后需要修改调整[config.xml](/src/main/resources/db/mybatis/config.xml)中URL的值。

一共使用了三个数据库表
- LINKS_ALREADY_PROCESSED 用来储存已经处理过了的链接，避免重复爬取
- LINKS_TO_BE_PROCESSED 用来存储待处理的链接
- NEWS 用来存储爬取下来的新闻的相关信息（题目、内容、链接）

```sql
create table LINKS_ALREADY_PROCESSED
(
	LINK VARCHAR(2000)
);

create table LINKS_TO_BE_PROCESSED
(
	LINK VARCHAR(2000)
);

create table NEWS
(
	ID BIGINT AUTO_INCREMENT primary key,
	TITLE TEXT,
	CONTENT TEXT,
	URL VARCHAR(2000),
	CREATED_AT TIMESTAMP,
	MODIFIED_AT TIMESTAMP
);
```

**运行方法**：
1. 下载或clone代码
2. 导入pom.xml中的依赖包
3. 修改[config.xml](/src/main/resources/db/mybatis/config.xml)文件中url的值
4. 修改[pom.xml]()文件中flyway-maven-plugin配置的jdbc_url值
4. 根据[config.xml](/pom.xml)文件中的配置创建数据库
5. 使用flyway命令`mvn flyway:migrate`初始化数据库


## 2 算法
1. 从待处理链接池中拿一个链接
2. 如果已经处理过了，拿下一个
3. 如果没有处理过，检查是否是我们想要的链接
4. 如果是我们想要的，根据链接拿到page中所有其他链接，放入待处理池子中
5. 该页面如果是新闻链接，将新闻相关的标题、内容存储下来
6. 回到第1步


## 3 实现步骤(括号内为完成日期)
0. （9.5）创项目，搭建骨架
1. （9.6）添加okhttp依赖，进行冒烟测试
2. （9.6）修改CI配置文件，保证circleCI正常运行
3. （9.6）使用两个ArrayList实现了基本的算法，能打印出新闻标题，做了小部分重构
4. （9.6）添加spotbugs插件，修复潜在bug
5. （9.7）使用H2数据库替换掉ArrayList实现，并进行小部分重构
6. （9.7）引入flyway
7. （9.7）完善算法，将标题、链接、文章内容放入数据库
8. （9.7）抽取出DAO层，专门处理数据库链接和增删改查工作
9. （9.7）为DAO层添加MyBatis实现
10. （9.7）改造为多线程
