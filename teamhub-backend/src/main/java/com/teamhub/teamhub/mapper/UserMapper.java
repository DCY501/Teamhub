package com.teamhub.teamhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamhub.teamhub.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}