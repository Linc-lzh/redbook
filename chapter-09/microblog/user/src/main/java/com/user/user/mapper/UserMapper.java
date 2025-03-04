package com.user.user.mapper;

import com.user.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    //获取用户基本信息
    User getUserInfo(@Param("uid")int uid);

    //获取followers列表
    List<Integer> getFollowers(@Param("uid")int uid);

    //获取followers列表
    List<Integer> getAttentions(@Param("uid")int uid);
}
