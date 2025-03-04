package com.user.mapper;


import com.user.model.Attention;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AttentionMapper {

    int follow(int userId, int attentionId);

    int unfollow(int userId, int attentionId);
}
