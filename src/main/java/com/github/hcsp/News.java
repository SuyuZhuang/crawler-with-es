package com.github.hcsp;

import java.time.Instant;

public class News {
    private Integer id;
    private String url;
    private String title;
    private String content;
    private Instant createdAt;
    private Instant modifiedAt;
    private String uuid;
    private Long snow;

    public News() {
    }

    public News(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
    }

    public News(String url, String title, String content, String uuid, Long snow) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.uuid = uuid;
        this.snow = snow;
    }

    public News(News old) {
        this.id = old.id;
        this.url = old.url;
        this.title = old.title;
        this.content = old.content;
        this.uuid = old.uuid;
        this.snow = old.snow;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getSnow() {
        return snow;
    }

    public void setSnow(Long snow) {
        this.snow = snow;
    }
}
