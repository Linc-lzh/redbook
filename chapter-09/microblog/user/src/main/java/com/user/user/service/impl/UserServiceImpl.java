package com.user.user.service.impl;

import com.common.result.Result;
import com.jd.platform.hotkey.client.ClientStarter;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.user.user.mapper.UserMapper;
import com.user.user.model.Follower;
import com.user.user.model.User;
import com.user.user.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * user实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final String FOLLOWER = "follower:";

    private final String ATTENTION = "attention:";

    private final String USER = "user:";

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserMapper userMapper;

    @PostConstruct
    public void initHotkey() {
        ClientStarter.Builder builder = new ClientStarter.Builder();
        // 注意，setAppName很重要，它和dashboard中相关规则是关联的。
        ClientStarter starter = builder.setAppName("user-client")
                .setEtcdServer("http://127.0.0.1:2379")
                .setCaffeineSize(10)
                .build();
        starter.startPipeline();
    }

    /**
     * 获取用户的信息
     * @param id
     * @return
     */
    @Override
    public Result<User> getUserInfo(Integer id) {
        User user;
        //先判断是否是hotkey
        if (JdHotKeyStore.isHotKey(FOLLOWER + id)) {
            //如果是热点数据就从本地缓存获取
            user = (User)JdHotKeyStore.get(FOLLOWER + id);
            if (user==null){
                user = (User)redisTemplate.opsForValue().get(USER + id);
                if (user==null){
                    user = userMapper.getUserInfo(id);
                    if (user==null){
                        user = new User();
                    }
                }
            }
        }
        //获取用户基本信息
        user = (User)redisTemplate.opsForValue().get(USER + id);
        if (user==null){
            user = userMapper.getUserInfo(id);
            if (user==null){
                user = new User();
            }
        }
        return new Result<>(200 , "success" , user);
    }

    @Override
    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @return 返回用户的关注列表
     */
    public Result<List<Integer>> getFollowers(Integer uid) {

        // 先判断是否是热点数据
        if (JdHotKeyStore.isHotKey(FOLLOWER + uid)) {
            // 如果是热点数据就从本地缓存中获取
            List<Integer> followers = getFollowersFromLocalCache(uid);
            return new Result<>(200, "获取成功", followers);
        }

        // 从 Redis 中获取 follower 列表
        List<Integer> followers = getFollowersFromRedis(uid);
        if (followers == null) {
            // 如果 Redis 中没有数据，则从数据库中获取
            followers = getFollowersFromDB(uid);
        }

        if (followers == null) {
            // 如果数据库中也没有数据，则返回一个空的列表
            followers = new ArrayList<>();
        }

        return new Result<>(200, "获取成功", followers);
    }

    /**
     * 从本地缓存中获取 follower 列表
     * @param uid 用户ID
     * @return 返回 follower 列表
     */
    private List<Integer> getFollowersFromLocalCache(Integer uid) {
        List<Integer> followers = (List<Integer>)JdHotKeyStore.get(FOLLOWER + uid);
        if (followers != null) {
            return followers;
        }

        // 如果本地缓存中没有数据，则从 Redis 中获取
        followers = getFollowersFromRedis(uid);
        if (followers != null) {
            // 将数据写入本地缓存
            JdHotKeyStore.smartSet(FOLLOWER + uid, followers);
        }

        return followers;
    }

    /**
     * 从 Redis 中获取 follower 列表
     * @param uid 用户ID
     * @return 返回 follower 列表
     */
    private List<Integer> getFollowersFromRedis(Integer uid) {
        return redisTemplate.opsForList().range(FOLLOWER+uid, 0, -1);
    }

    /**
     * 从数据库中获取 follower 列表
     * @param uid 用户ID
     * @return 返回 follower 列表
     */
    private List<Integer> getFollowersFromDB(Integer uid) {
        List<Integer> followers = userMapper.getFollowers(uid);
        if (followers != null && !followers.isEmpty()) {
            // 如果数据库中有数据，则将数据写入 Redis 中
            redisTemplate.opsForList().rightPushAll(FOLLOWER+uid, followers);
        }
        return followers;
    }

    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @return 返回用户的关注列表
     */
    @Override
    public Result<List<Integer>> getAttentions(Integer uid) {
        // 先判断是否是热点数据
        if (JdHotKeyStore.isHotKey(ATTENTION + uid)) {
            // 如果是热点数据就从本地缓存中获取
            List<Integer> attentions = getAttentionFromLocalCache(uid);
            return new Result<>(200, "获取成功", attentions);
        }

        // 从 Redis 中获取 attention 列表
        List<Integer> attentions = getAttentionFromRedis(uid);
        if (attentions == null) {
            // 如果 Redis 中没有数据，则从数据库中获取
            attentions = getAttentionFromDB(uid);
        }

        if (attentions == null) {
            // 如果数据库中也没有数据，则返回一个空的列表
            attentions = new ArrayList<>();
        }

        return new Result<>(200, "获取成功", attentions);
    }

    /**
     * 从本地缓存中获取 attention 列表
     * @param uid 用户ID
     * @return 返回 attention 列表
     */
    private List<Integer> getAttentionFromLocalCache(Integer uid) {
        List<Integer> attentions = (List<Integer>)JdHotKeyStore.get(ATTENTION + uid);
        if (attentions != null) {
            return attentions;
        }

        // 如果本地缓存中没有数据，则从 Redis 中获取
        attentions = getAttentionFromRedis(uid);
        if (attentions != null) {
            // 将数据写入本地缓存
            JdHotKeyStore.smartSet(ATTENTION + uid, attentions);
        }

        return attentions;
    }

    /**
     * 从 Redis 中获取 attention 列表
     * @param uid 用户ID
     * @return 返回 attention 列表
     */
    private List<Integer> getAttentionFromRedis(Integer uid) {
        return redisTemplate.opsForList().range(ATTENTION+uid, 0, -1);
    }

    /**
     * 从数据库中获取 attention 列表
     * @param uid 用户ID
     * @return 返回 attention 列表
     */
    private List<Integer> getAttentionFromDB(Integer uid) {
        List<Integer> attentions = userMapper.getAttentions(uid);
        if (attentions != null && !attentions.isEmpty()) {
            // 如果数据库中有数据，则将数据写入 Redis 中
            redisTemplate.opsForList().rightPushAll(ATTENTION+uid, attentions);
        }
        return attentions;
    }

}
