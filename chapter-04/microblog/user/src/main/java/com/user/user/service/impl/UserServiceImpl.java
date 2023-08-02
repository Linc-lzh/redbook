package com.user.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.result.Result;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.jd.platform.hotkey.client.ClientStarter;
import com.jd.platform.hotkey.client.Context;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.jd.platform.hotkey.client.core.rule.KeyRuleHolder;
import com.jd.platform.hotkey.client.etcd.EtcdConfigFactory;
import com.jd.platform.hotkey.common.configcenter.ConfigConstant;
import com.jd.platform.hotkey.common.configcenter.IConfigCenter;
import com.jd.platform.hotkey.common.rule.KeyRule;
import com.jd.platform.hotkey.common.tool.FastJsonUtils;
import com.user.user.feign.BlurCounterClient;
import com.user.user.mapper.UserMapper;
import com.user.user.model.Counter;
import com.user.user.model.Follower;
import com.user.user.model.User;
import com.user.user.service.UserService;
import com.user.user.utils.RedisLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * user实现
 */
@Service
public class UserServiceImpl implements UserService {

    private final String FOLLOWER = "follower:";

    private final String ATTENTION = "attention:";

    private final String USER = "user_";

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserMapper userMapper;

    @Resource
    private BlurCounterClient blurCounterClient;

    @PostConstruct
    public void initHotkey() {
        ClientStarter.Builder builder = new ClientStarter.Builder();
        // 注意，setAppName很重要，它和dashboard中相关规则是关联的。
        ClientStarter starter = builder.setAppName("user-client")
                .setEtcdServer("http://127.0.0.1:2379")
                .build();
        starter.startPipeline();
    }

    Cache<String, Object> cache = CacheBuilder.newBuilder()
            //设置写缓存后8秒钟过期
            .expireAfterWrite(2, TimeUnit.SECONDS)
            //设置缓存移除通知
            //build方法可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build();

    @Resource
    private RedisLock lock;

    /**
     * 获取用户的信息
     *
     * @param id
     * @return
     */
    @Override
    public Result<User> getUserInfo(Integer id) {
        User user;
        //先判断是否是hotkey
        try {
            boolean hotKey = JdHotKeyStore.isHotKey(USER + id);
            if (hotKey) {
                //如果是热点数据就从本地缓存获取
                user = (User) JdHotKeyStore.get(USER + id);
                if (user == null) {
                    String userJson = (String) redisTemplate.opsForValue().get(USER + id);

                    if (userJson != null && userJson.equals("")) {
                        user = JSONObject.parseObject(userJson, User.class);
                    }
                    if (userJson == null || userJson.equals("")) {
                        //加锁
                        if (lock.lock("lock:" + USER + id, "1")) {
                            user = userMapper.getUserInfo(id);
                            if (user == null) {
                                user = new User();
                                redisTemplate.opsForValue().set(USER + id, JSONObject.toJSONString(user), 30, TimeUnit.SECONDS);
                            } else {
                                redisTemplate.opsForValue().set(USER + id, JSONObject.toJSONString(user));
                            }
                            lock.unlock("lock:" + USER + id, "1");
                        } else {
                            while (true) {
                                userJson = (String) redisTemplate.opsForValue().get(USER + id);
                                if (userJson != null && !userJson.equals("")) {
                                    user = JSONObject.parseObject(userJson, User.class);
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                //获取用户基本信息
                String userJson = (String) redisTemplate.opsForValue().get(USER + id);
                if (userJson != null) {
                    user = JSONObject.parseObject(userJson, User.class);
                } else {
                    user = userMapper.getUserInfo(id);
                    if (user == null) {
                        user = new User();
                        redisTemplate.opsForValue().set(USER + id, JSONObject.toJSONString(user), 30, TimeUnit.SECONDS);
                    } else {
                        //这里直接写入到缓存中，为什么直接写入到缓存中呢。而不是判断是否是热点用户的
                        //第一，因为user是哪里都可能会用到的，这个人点开你主页看可能也会点开到视频，帖子
                        //这些都需要去获取你的user来进行拼装
                        //第二，因为在现在的系统当中，比如微博，小红书，这种基本上系统中会一直存放用户相关信息
                        redisTemplate.opsForValue().set(USER + id, JSONObject.toJSONString(user));
                    }
                }
            }
//            //获取用户粉丝数和关注数
//            if (hotKey){
//                //查询本地count
//                user = hotCount(user);
//                return new Result<>(200 , "success" , user);
//            }
//            //查询远程count
//            user = notHotCount(user);
            return new Result<>(200, "success", user);
        } catch (Exception e) {
            lock.unlock("lock:" + USER + id, "1");
            e.printStackTrace();
            return new Result<>(500, "失败", null);
        }
    }

    /**
     * 热key查询本地count
     * @param user
     * @return
     */
    private User hotCount(User user){
        Set<String> keys = new HashSet<>();
        keys.add("attentionCount");
        keys.add("followerCount");
        Map<String, Object> allPresent = cache.getAllPresent(keys);
        if (allPresent!=null){
            user = notHotCount(user);
            cache.put("attentionCount" , user.getAttentionCount());
            cache.put("followerCount" , user.getFollowerCount());
        }
        return user;
    }

    /**
     * 非热key查询远程count
     * @param user
     * @return
     */
    private User notHotCount(User user){
        List<String> keys = new ArrayList<>();
        keys.add("attentionCount");
        keys.add("followerCount");
        //获取用户的粉丝数和关注数
        Counter counter = new Counter();
        counter.setObjId(user.getId());
        counter.setObjType(1);
        counter.setKeys(keys);
        Result<List<Counter>> counters = blurCounterClient.getCounters(counter);
        if (counters.getData() == null) {
            user.setAttentionCount(0);
            user.setFollowerCount(0);
            cache.put("attentionCount" , 0);
            cache.put("followerCount" , 0);
        } else {
            //说明获取到粉丝数了
            user.setAttentionCount(counters.getData().get(0).getCountValue());
            user.setFollowerCount(counters.getData().get(1).getCountValue());
        }
        return user;
    }

    @Override
    /**
     * 获取用户的关注列表
     * @param uid 用户ID
     * @return 返回用户的关注列表
     */
    public Result<List<Integer>> getFollowers(Integer uid) {

        boolean hotKey = JdHotKeyStore.isHotKey(FOLLOWER + uid);
        // 先判断是否是热点数据
        if (hotKey) {
            // 如果是热点数据就从本地缓存中获取
            List<Integer> followers = getFollowersFromLocalCache(uid);
            if (followers != null) {
                return new Result<>(200, "获取成功", followers);
            }
        }

        // 从 Redis 中获取 follower 列表
        List<Integer> followers = getFollowersFromRedis(uid);
        if (followers == null || followers.size() == 0) {
            // 如果 Redis 中没有数据，则从数据库中获取
            followers = getFollowersFromDB(uid);
        }

        if (followers == null || followers.size() == 0) {
            // 如果数据库中也没有数据，则返回一个空的列表
            followers = new ArrayList<>();
        }

        return new Result<>(200, "获取成功", followers);
    }

    /**
     * 从本地缓存中获取 follower 列表
     *
     * @param uid 用户ID
     * @return 返回 follower 列表
     */
    private List<Integer> getFollowersFromLocalCache(Integer uid) {
        List<Integer> followers = (List<Integer>) JdHotKeyStore.get(FOLLOWER + uid);
        if (followers != null) {
            return followers;
        }

        // 如果本地缓存中没有数据，则从 Redis 中获取
        followers = getFollowersFromRedis(uid);
        if (followers == null) {
            followers = getFollowersFromDB(uid);
        }
        if (followers != null) {
            // 将数据写入本地缓存
            JdHotKeyStore.smartSet(FOLLOWER + uid, followers);
        }

        return followers;
    }

    /**
     * 从 Redis 中获取 follower 列表
     *
     * @param uid 用户ID
     * @return 返回 follower 列表
     */
    private List<Integer> getFollowersFromRedis(Integer uid) {
        return redisTemplate.opsForList().range(FOLLOWER + uid, 0, -1);
    }

    /**
     * 从数据库中获取 follower 列表
     *
     * @param uid 用户ID
     * @return 返回 follower 列表
     */
    private List<Integer> getFollowersFromDB(Integer uid) {
        List<Integer> followers = userMapper.getFollowers(uid);
        if (followers != null && !followers.isEmpty()) {
            // 如果数据库中有数据，则将数据写入 Redis 中
            redisTemplate.opsForList().rightPushAll(FOLLOWER + uid, followers);
        }
        return followers;
    }

    /**
     * 获取用户的关注列表
     *
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
        if (attentions == null || attentions.size() == 0) {
            // 如果 Redis 中没有数据，则从数据库中获取
            attentions = getAttentionFromDB(uid);
        }

        if (attentions == null || attentions.size() == 0) {
            // 如果数据库中也没有数据，则返回一个空的列表
            attentions = new ArrayList<>();
        }

        return new Result<>(200, "获取成功", attentions);
    }

    @Resource
    private KafkaTemplate<String , String> kafkaTemplate;

    /**
     * 用户粉丝列表添加数据
     * @param follower
     * @return
     */
    @Override
    public Result<List<Integer>> setFollowers(Follower follower) {
        //这里分取关还是添加关注
        //通过follower的is_delete判断是否是取关
        if (follower.getIsDelete()==0){
            //新增关注
            //直接添加到redis最左侧
            //这里需要原子操作
            //2. 使用lua脚本执行原子操作
            List<String> keys = new ArrayList<>();
            keys.add(FOLLOWER + follower.getUserId());
            keys.add(follower.getFollowerId().toString());
            keys.add("1");
            kafkaTemplate.send("follower-topic", JSON.toJSONString(keys));

            keys = new ArrayList<>();
            keys.add(ATTENTION + follower.getFollowerId());
            keys.add(follower.getUserId().toString());
            keys.add("1");
            kafkaTemplate.send("attention-topic", JSON.toJSONString(keys));

            //新增粉丝和关注数
            //新增粉丝数
            sendMsg(follower.getUserId() , "followerCount" , 1 , "follower-count-topic");

            //新增关注数
            sendMsg(follower.getFollowerId() , "attentionCount" , 1 , "attention-count-topic");
        }else{
            //取关
            //取关和关注同理，先判断用户是否在redis当中
            //如果在redis当中，就删除redis，如果不在，直接删除mysql
            List<String> keys = new ArrayList<>();
            keys.add(FOLLOWER + follower.getUserId());
            keys.add(follower.getFollowerId().toString());
            keys.add("-1");
            kafkaTemplate.send("follower-topic", JSON.toJSONString(keys));

            keys = new ArrayList<>();
            keys.add(ATTENTION + follower.getFollowerId());
            keys.add(follower.getUserId().toString());
            keys.add("-1");
            kafkaTemplate.send("attention-topic", JSON.toJSONString(keys));

            //取关，减少粉丝数
            sendMsg(follower.getUserId() , "followerCount" , -1 , "follower-count-topic");

            //取关减少关注数
            sendMsg(follower.getFollowerId() , "attentionCount" , 1 , "attention-count-topic");
        }
        return null;
    }

    /**
     * 发送消息到consumer，包括（新增关注数，新增粉丝数，减少粉丝数...）
     * @param uid
     * @param key
     * @param isDelete
     * @param topic
     */
    private void sendMsg(Integer uid , String key , Integer isDelete , String topic){
        Counter counter = new Counter();
        counter.setObjId(uid);
        counter.setObjType(1);
        counter.setCountKey(key);
        counter.setCountValue(isDelete);
        kafkaTemplate.send(topic, JSON.toJSONString(counter));
    }


    //用户关注列表添加数据
    @Override
    public Result<List<Integer>> setAttentions(Follower follower) {
        return null;
    }

    /**
     * 从本地缓存中获取 attention 列表
     *
     * @param uid 用户ID
     * @return 返回 attention 列表
     */
    private List<Integer> getAttentionFromLocalCache(Integer uid) {
        List<Integer> attentions = (List<Integer>) JdHotKeyStore.get(ATTENTION + uid);
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
     *
     * @param uid 用户ID
     * @return 返回 attention 列表
     */
    private List<Integer> getAttentionFromRedis(Integer uid) {
        return redisTemplate.opsForList().range(ATTENTION + uid, 0, -1);
    }

    /**
     * 从数据库中获取 attention 列表
     *
     * @param uid 用户ID
     * @return 返回 attention 列表
     */
    private List<Integer> getAttentionFromDB(Integer uid) {
        List<Integer> attentions = userMapper.getAttentions(uid);
        if (attentions != null && !attentions.isEmpty()) {
            // 如果数据库中有数据，则将数据写入 Redis 中
            redisTemplate.opsForList().rightPushAll(ATTENTION + uid, attentions);
        }
        return attentions;
    }

}
