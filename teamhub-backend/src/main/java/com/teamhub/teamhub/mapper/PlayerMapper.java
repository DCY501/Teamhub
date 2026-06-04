package com.teamhub.teamhub.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamhub.teamhub.entity.Player;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlayerMapper extends BaseMapper<Player> {
}