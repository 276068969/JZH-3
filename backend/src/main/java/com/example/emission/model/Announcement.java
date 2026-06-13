package com.example.emission.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Announcement {
    private Long id;
    private String title;
    private String content;
    private String type;
    private String publishStatus;
    private String publisher;
    private String publishTime;
    private String createTime;
    private String updateTime;

    public Announcement() {}

    public Announcement(Long id, String title, String content, String type,
                        String publishStatus, String publisher, String publishTime,
                        String createTime, String updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.type = type;
        this.publishStatus = publishStatus;
        this.publisher = publisher;
        this.publishTime = publishTime;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    @JsonProperty("id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("title")
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    @JsonProperty("content")
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @JsonProperty("type")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @JsonProperty("publishStatus")
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String publishStatus) { this.publishStatus = publishStatus; }

    @JsonProperty("publisher")
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    @JsonProperty("publishTime")
    public String getPublishTime() { return publishTime; }
    public void setPublishTime(String publishTime) { this.publishTime = publishTime; }

    @JsonProperty("createTime")
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    @JsonProperty("updateTime")
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
}
