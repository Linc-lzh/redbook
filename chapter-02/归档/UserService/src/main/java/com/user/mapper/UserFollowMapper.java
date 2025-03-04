package com.user.mapper;

import com.github.pagehelper.Page;
import com.user.dto.FollowerInfoDTO;
import com.user.model.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFollowMapper {

    /**
     * 添加一条用户关注记录
     * @param userFollow 用户关注关系对象
     * @return 影响的行数，应为1
     */
    int follow(UserFollow userFollow);

    /**
     * 取消用户的关注关系
     * @param followerId 关注者的用户ID
     * @param followingId 被关注者的用户ID
     * @return 影响的行数，应为1
     */
    int unfollow(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    /**
     * 统计用户关注的人数
     * @param userId 用户ID
     * @return 用户关注的人数
     */
    int countFollowing(@Param("userId") Integer userId);

    /**
     * 统计用户的粉丝数
     * @param userId 用户ID
     * @return 用户的粉丝数
     */
    int countFollowers(@Param("userId") Integer userId);

    /**
     * 判断一个用户是否关注了另一个用户
     * @param followerId 关注者的用户ID
     * @param followingId 被关注者的用户ID
     * @return 如果关注了返回true，否则返回false
     */
    boolean isFollowing(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);

    /**
     * 根据用户ID查询其关注的用户列表（分页）
     *
     * @param userId 用户ID
     * @return 关注的用户列表（分页）
     */
    List<UserFollow> selectFollowingsByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID查询其粉丝列表（分页）
     *
     * @param userId 用户ID
     * @return 粉丝列表（分页）
     */
    List<UserFollow> selectFollowersByUserId(@Param("userId") Integer userId);

    /**
     * 添加关注
     * @param userId
     * @param followerId
     * @return
     */
    int addFollower(@Param("userId") int userId, @Param("followerId") int followerId);

    /**
     * 取消关注
     * @param userId
     * @param followerId
     * @return
     */
    int unfollower(@Param("userId") int userId, @Param("followerId") int followerId);

    /**
     * 查询用户的所有粉丝id
     * @param userId
     * @return
     */
    List<Integer> findAllFollowersByUserId(int userId);

}
