package com.meetingroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meetingroom.entity.MeetingBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<MeetingBook> {
}
