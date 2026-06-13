package com.example.emission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.emission.model.Announcement;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
