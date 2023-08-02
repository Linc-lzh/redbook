package com.user.user.mapper;

import com.user.user.model.Attention;
import com.user.user.model.Follower;
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

    //获取attentions列表
    List<Integer> getAttentions(@Param("uid")int uid);

    Follower getFollower(@Param("uid")int uid , @Param("followerId") int followerId);

    Attention getAttention(@Param("uid")int uid , @Param("attentionId") int attentionId);

    //新增follower
    void setFollower(@Param("uid")int uid , @Param("followerId") int followerId);

    //新增attention
    void setAttention(@Param("uid")int uid , @Param("attentionId") int attentionId);

    //删除follower
    void delFollower(@Param("uid")int uid , @Param("followerId") int followerId , @Param("isDelete") int isDelete);

    //删除attention
    void delAttention(@Param("uid")int uid , @Param("attentionId") int attentionId , @Param("isDelete") int isDelete);
}
