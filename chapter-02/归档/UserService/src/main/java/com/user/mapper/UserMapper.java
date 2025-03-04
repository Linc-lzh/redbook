package com.user.mapper;

import com.user.dto.UserDTO;
import com.user.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户信息表Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户实体
     */
    User findById(@Param("id") Long id);

    /**
     * 通过用户名查找用户
     * @param username 用户名
     * @return 用户实体
     */
    User findByUsername(@Param("username") String username);

    /**
     * 注册新用户
     * @param user 用户实体
     * @return 影响的行数
     */
    int register(User user);

    /**
     * 模糊搜索
     * @param username
     * @return
     */
    List<UserDTO> searchUsersByUsername(@Param("username") String username);

    /**
     * 批量查询用户信息
     * @param userIds
     * @return
     */
    List<UserDTO> selectUsersByIds(@Param("userIds") List<Integer> userIds);

    /**
     * 根据用户ID列表批量查询用户信息
     * @param userIds
     * @return
     */
    List<User> findByIds(List<Long> userIds);

    // 增加关注数
    void increaseFollowCount(@Param("userId") int userId);

    // 减少关注数
    void decreaseFollowCount(@Param("userId") int userId);

    // 增加粉丝数
    void increaseAttentionCount(@Param("userId") int userId);

    // 减少粉丝数
    void decreaseAttentionCount(@Param("userId") int userId);
}

