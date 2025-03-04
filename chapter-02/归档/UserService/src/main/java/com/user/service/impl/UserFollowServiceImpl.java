package com.user.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.user.mapper.UserMapper;
import com.user.model.User;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import com.user.mapper.UserFollowMapper;
import com.user.model.UserFollow;
import com.user.service.UserFollowService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Resource
    private UserFollowMapper userFollowMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private static final int MAX_RETRY_COUNT = 3;

    private static final Logger logger = Logger.getLogger(UserFollowServiceImpl.class.getName());

    @Override
    public Map<String, Object> getFollowingsByUserId(Integer userId, int pageNum, int pageSize) {
        logger.info("Fetching followings for user ID: " + userId + " Page Number: " + pageNum + " Page Size: " + pageSize);
        try {
            Map<String, Object> data = new HashMap<>();
            Page page = PageHelper.startPage(pageNum, pageSize, true);
            List<UserFollow> followings = userFollowMapper.selectFollowingsByUserId(userId);
            data.put("total", page.getTotal());
            data.put("nowPage", pageNum);
            data.put("data", followings);
            logger.info("Followings fetched successfully.");
            return data;
        } catch (Exception e) {
            logger.severe("Error occurred while fetching followings: " + e.getMessage());
//            throw new RuntimeException(e);
            return null;
        }
    }

//    @Override
//    public Map<String, Object>  getFollowersByUserId(Integer userId, int pageNum, int pageSize) {
//        logger.info("Fetching followers for user ID: " + userId + " Page Number: " + pageNum + " Page Size: " + pageSize);
//        try {
//            Map<String, Object> data = new HashMap<>();
//            Page page = PageHelper.startPage(pageNum, pageSize, true);
//            List<UserFollow> followers = userFollowMapper.selectFollowersByUserId(userId);
//            data.put("total", page.getTotal());
//            data.put("nowPage", pageNum);
//            data.put("data", followers);
//            logger.info("Followers fetched successfully.");
//            return data;
//        } catch (Exception e) {
//            logger.severe("Error occurred while fetching followers: " + e.getMessage());
////            throw new RuntimeException(e);
//            return null;
//        }
//    }

    @Override
    public Map<String, Object> getFollowersByUserId(Integer userId, int pageNum, int pageSize) {
        logger.info("Fetching followers for user ID: " + userId + " Page Number: " + pageNum + " Page Size: " + pageSize);

        // 判断查询长度是否超过最大限制200
        if (pageNum * pageSize > 200) {
            logger.warning("Query length exceeds the maximum limit of 200. Returning null.");
            return null;
        }

        // 构建 Redis 中存储关注者数据的 key
        String redisKey = "followers:" + userId;
        Map<String, Object> data = new HashMap<>();

        try {
            // 尝试从 Redis 中获取数据
            Set<ZSetOperations.TypedTuple<String>> followersWithScores = redisTemplate.opsForZSet().rangeWithScores(redisKey, (pageNum - 1) * pageSize, pageNum * pageSize - 1);

            if (followersWithScores != null && !followersWithScores.isEmpty()) {
                List<String> followers = followersWithScores.stream()
                        .map(ZSetOperations.TypedTuple::getValue)
                        .collect(Collectors.toList());

                data.put("total", redisTemplate.opsForZSet().zCard(redisKey));
                data.put("nowPage", pageNum);
                data.put("data", followers);
                logger.info("Followers fetched successfully from Redis.");
            } else {
                // 获取 Redisson 锁
                RLock lock = redissonClient.getLock("followersLock");
                lock.lock();
                try {
                    // Double-Check Locking
                    followersWithScores = redisTemplate.opsForZSet().rangeWithScores(redisKey, (pageNum - 1) * pageSize, pageNum * pageSize - 1);
                    if (followersWithScores != null && !followersWithScores.isEmpty()) {
                        List<String> followers = followersWithScores.stream()
                                .map(ZSetOperations.TypedTuple::getValue)
                                .collect(Collectors.toList());
                        data.put("total", redisTemplate.opsForZSet().zCard(redisKey));
                        data.put("nowPage", pageNum);
                        data.put("data", followers);
                        logger.info("Followers fetched successfully from Redis.");
                    } else {
                        List<UserFollow> followers = userFollowMapper.selectFollowersByUserId(userId);
                        if (!followers.isEmpty()) {
                            ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
                            Set<ZSetOperations.TypedTuple<String>> followersSet = followers.stream()
                                    .map(follower -> new DefaultTypedTuple<>(follower.getFollowerId().toString(), (double) follower.getFollowDate().getTime()))
                                    .collect(Collectors.toSet());
                            zSetOps.add(redisKey, followersSet);
                            // 如果需要，设置 Redis 数据的过期时间
                            // zSetOps.expire(redisKey, expirationTimeInSeconds, TimeUnit.SECONDS);
                            data.put("total", followers.size());
                            data.put("nowPage", pageNum);
                            data.put("data", followers);
                            logger.info("Followers fetched successfully from MySQL and cached in Redis.");
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }

            return data;
        } catch (Exception e) {
            logger.severe("Error occurred while fetching followers: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Integer> getFollowerIds(Integer userId) {
        String redisKey = "followers:" + userId;

        logger.info("Fetching follower IDs for user ID: " + userId);

        // 尝试从 Redis 中获取数据
        Set<String> followerIdsSet = redisTemplate.opsForZSet().range(redisKey, 0, -1);

        if (followerIdsSet != null && !followerIdsSet.isEmpty()) {
            logger.info("Follower IDs fetched successfully from Redis.");
            return followerIdsSet.stream().map(Integer::parseInt).collect(Collectors.toList());
        } else {
            // 获取 Redisson 锁
            RLock lock = redissonClient.getLock("followerIdsLock:"+userId);
            lock.lock();
            try {
                // Double-Check Locking
                followerIdsSet = redisTemplate.opsForZSet().range(redisKey, 0, -1);
                if (followerIdsSet != null && !followerIdsSet.isEmpty()) {
                    logger.info("Follower IDs fetched successfully from Redis.");
                    return followerIdsSet.stream().map(Integer::parseInt).collect(Collectors.toList());
                } else {
                    List<UserFollow> followers = userFollowMapper.selectFollowersByUserId(userId);
                    if (!followers.isEmpty()) {
                        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
                        Set<ZSetOperations.TypedTuple<String>> followersSet = followers.stream()
                                .map(follower -> new DefaultTypedTuple<>(follower.getFollowerId().toString(), (double) follower.getFollowDate().getTime()))
                                .collect(Collectors.toSet());
                        zSetOps.add(redisKey, followersSet);
                        //redis的key还有redis过期时间是应该单独写类或者配置文件维护的，这里我直接定死了
                        // 设置过期时间，例如，设置为8小时后过期
                        redisTemplate.expire(redisKey, 8, TimeUnit.HOURS);
                        logger.info("Follower IDs fetched successfully from MySQL and cached in Redis.");
                        return followers.stream().map(UserFollow::getFollowerId).collect(Collectors.toList());
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
        logger.warning("No followers found for user ID: " + userId);
        return new ArrayList<>();
    }

    @Override
    public Map<Long, User> fetchUsersData(List<Long> userIds) {
        List<Long> nullUserIds = new ArrayList<>();
        Map<Long, User> usersData = new HashMap<>();

        // 将长整型用户ID列表转换为字符串列表，以便用于Redis查询
        List<String> userIdsAsString = new ArrayList<>();
        for (Long userId : userIds) {
            userIdsAsString.add("user:" + userId);
        }

        // 使用 Redis MGET 命令批量获取数据
        List<User> redisData = redisTemplate.opsForValue().multiGet(userIdsAsString);

        for (int i = 0; i < redisData.size(); i++) {
            User user = redisData.get(i);
            if (user == null) {
                // 如果用户数据为空，将该用户ID添加到nullUserIds列表
                nullUserIds.add(userIds.get(i));
            } else {
                // 如果用户数据非空，将其添加到返回结果中
                usersData.put(userIds.get(i), user);
            }
        }

        // 如果存在空数据的用户ID，从数据库中获取这些用户的数据
        if (!nullUserIds.isEmpty()) {
            Map<Long, User> dbData = fetchFromDatabase(nullUserIds);
            usersData.putAll(dbData);
        }

        return usersData;
    }

    @Override
    public Map<String, Integer> getUserFollowData(Long userId) {
        String followCountKey = "user_follow_count:" + userId;
        String attentionCountKey = "user_attention_count:" + userId;
        final long expireTime = 8 * 60 * 60; // 8 小时

        List<String> keys = Arrays.asList(followCountKey, attentionCountKey);
        List<Integer> values = redisTemplate.opsForValue().multiGet(keys);

        boolean allFieldsPresent = values.stream().noneMatch(Objects::isNull);

        if (!allFieldsPresent) {
            // 加锁
            RLock lock = redissonClient.getLock("updateUserFollowDataLock:" + userId);
            lock.lock();
            try {
                User user = userMapper.findById(userId);
                if (user == null) {
                    throw new RuntimeException("User not found");
                }
                Integer followCount = user.getFollowCount();
                Integer attentionCount = user.getAttentionCount();

                // 更新 Redis 缓存并设置过期时间
                redisTemplate.opsForValue().set(followCountKey, followCount, expireTime, TimeUnit.SECONDS);
                redisTemplate.opsForValue().set(attentionCountKey, attentionCount, expireTime, TimeUnit.SECONDS);

                values = Arrays.asList(followCount, attentionCount);
            } finally {
                // 释放锁
                lock.unlock();
            }
        }

        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(keys::get, values::get));
    }



    private Map<Long, User> fetchFromDatabase(List<Long> userIds) {
        Map<Long, User> dataFromDb = new HashMap<>();

        // 对每个用户ID进行加锁，避免缓存击穿
        for (Long userId : userIds) {
            String lockKey = "userLock:" + userId;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                // 尝试获取锁，带有等待时间和自动释放时间
                boolean isLocked = lock.tryLock(10, 2, TimeUnit.SECONDS);

                if (isLocked) {
                    try {
                        // 使用 MyBatis mapper 根据ID列表批量获取用户数据
                        List<User> users = userMapper.findByIds(userIds);

                        // 转换并缓存用户数据
                        for (User user : users) {
                            Long id = user.getId();
                            dataFromDb.put(id, user);

                            // 将用户数据缓存到Redis
                            String redisKey = "user:" + id;
                            redisTemplate.opsForValue().set(redisKey, user);
                        }
                    } finally {
                        // 确保释放锁
                        lock.unlock();
                    }
                }
            } catch (InterruptedException e) {
                // 处理异常，例如记录日志
                e.printStackTrace();
            }
        }

        return dataFromDb;
    }

    @Override
    public void addFollower(int userId, int followerId) {
        addSendMessage("followerAddTopic", userId, followerId, 0);
    }

    public void addSendMessage(String topic, int userId, int followerId, int retryCount) {
        JSONObject messageObj = new JSONObject();
        messageObj.put("userId", userId);
        messageObj.put("followerId", followerId);
        String messageContent = messageObj.toString();

        // 生成唯一标识符
        String uniqueId = UUID.randomUUID().toString();
        messageObj.put("uniqueId", uniqueId);

        // Redisson 锁的 key
        String lockKey = "sendMessageLock:" + uniqueId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁，最多等待10秒，锁自动释放时间为1分钟
            if (lock.tryLock(10, 60, TimeUnit.SECONDS)) {
                try {
                    rocketMQTemplate.asyncSend(topic, messageContent, new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                logger.info("Message sent successfully: " + sendResult);
                            } else {
                                logger.warning("Message sent, but status is not SEND_OK: " + sendResult);
                                if (retryCount < MAX_RETRY_COUNT) {
                                    addSendMessage(topic, userId, followerId, retryCount + 1);
                                } else {
                                    logger.severe("Message failed to send after " + MAX_RETRY_COUNT + " retries");
                                }
                            }
                        }

                        @Override
                        public void onException(Throwable e) {
                            logger.severe("Failed to send message");
                            if (retryCount < MAX_RETRY_COUNT) {
                                addSendMessage(topic, userId, followerId, retryCount + 1);
                            } else {
                                logger.severe("Message failed to send after " + MAX_RETRY_COUNT + " retries");
                            }
                        }
                    });
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            } else {
                logger.info("Did not acquire lock, not sending message.");
            }
        } catch (InterruptedException e) {
            logger.severe("Failed to acquire lock due to interruption");
        }
    }

    @Override
    public void unfollow(int userId, int followerId) {
        unSendMessage("followerUnfollowTopic", userId, followerId, 0);
    }

    private void unSendMessage(String topic, int userId, int followerId, int retryCount) {
        JSONObject messageObj = new JSONObject();
        messageObj.put("userId", userId);
        messageObj.put("followerId", followerId);
        String messageContent = messageObj.toString();

        // 生成唯一标识符
        String uniqueId = UUID.randomUUID().toString();
        messageObj.put("uniqueId", uniqueId);

        // Redisson 锁的 key
        String lockKey = "unSendMessageLock:" + uniqueId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试加锁，最多等待10秒，锁自动释放时间为1分钟
            if (lock.tryLock(10, 60, TimeUnit.SECONDS)) {
                try {
                    rocketMQTemplate.asyncSend(topic, messageContent, new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                logger.info("Unfollow message sent successfully: " + sendResult);
                            } else {
                                logger.warning("Unfollow message sent, but status is not SEND_OK: " + sendResult);
                                if (retryCount < MAX_RETRY_COUNT) {
                                    unSendMessage(topic, userId, followerId, retryCount + 1);
                                } else {
                                    logger.severe("Unfollow message failed to send after " + MAX_RETRY_COUNT + " retries");
                                }
                            }
                        }

                        @Override
                        public void onException(Throwable e) {
                            logger.severe("Failed to send unfollow message: ");
                            if (retryCount < MAX_RETRY_COUNT) {
                                unSendMessage(topic, userId, followerId, retryCount + 1);
                            } else {
                                logger.severe("Unfollow message failed to send after " + MAX_RETRY_COUNT + " retries");
                            }
                        }
                    });
                } finally {
                    // 释放锁
                    lock.unlock();
                }
            } else {
                logger.info("Did not acquire lock, not sending unfollow message.");
            }
        } catch (InterruptedException e) {
            logger.severe("Failed to acquire lock due to interruption");
        }
    }

    @Override
    public List<Integer> getAllFollowers(int userId) {
        return userFollowMapper.findAllFollowersByUserId(userId);
    }

}

