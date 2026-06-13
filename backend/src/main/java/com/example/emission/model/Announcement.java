package com.example.emission.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;

@TableName("announcements")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("title")
    private String title;
    @TableField("content")
    private String content;
    @TableField("type")
    private String type;
    @TableField("publish_status")
    private String publishStatus;
    @TableField("publisher")
    private String publisher;
    @TableField("publish_time")
    private String publishTime;
    @TableField("create_time")
    private String createTime;
    @TableField("update_time")
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
