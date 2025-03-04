package com.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.user.dto.UserDTO;
import com.user.mapper.UserMapper;
import com.user.model.User;
import com.user.service.UserFollowService;
import com.user.service.UserService;
import com.user.utils.PasswordHasher;
import com.user.utils.SaltGenerator;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserServiceImpl implements UserService {

    // 获取Logger实例，用于日志记录
    private static final Logger logger = Logger.getLogger(UserServiceImpl.class.getName());

    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserFollowService userFollowService;

    private static final String FANS_COUNT_KEY = "fansCount";

    private static final String FOLLOW_COUNT_KEY = "followCount";

    @Override
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.findByUsername(user.getUsername());
            if (existingUser != null) {
                // 用户名已存在，注册失败
                logger.warning("Registration failed. Username: " + user.getUsername() + " already exists.");
                return false;
            }

            // 生成盐值
            String salt = SaltGenerator.generateSalt();

            // 使用密码和盐值生成哈希值
            String passwordHash = PasswordHasher.hashPassword(user.getPasswordHash(), salt);

            // 将盐值和哈希密码存储在用户实体中
            user.setSalt(salt);
            user.setPasswordHash(passwordHash);

            // 设置其他属性，例如状态和注册日期
            user.setStatus(1); // 假设1表示正常状态
            user.setDateRegistered(new Date());

            // 向数据库插入新用户
            int rows = userMapper.register(user);
            if (rows > 0) {
                logger.info("User registered successfully. Username: " + user.getUsername());
                return true;
            } else {
                logger.severe("User registration failed. Unable to insert user into the database.");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Error occurred during registration: " + e.getMessage());
            // 在实际环境中，可能还需要把异常抛出，让调用者来处理
            // throw new ServiceException("Error occurred during registration", e);
            return false;
        }
    }


    @Override
    public User login(String username, String password) {
        try {
            // 通过用户名查找用户
            User user = userMapper.findByUsername(username);
            if (user == null) {
                // 用户名不存在
                logger.warning("Login failed. Username: " + username + " does not exist.");
                return null;
            }

            // 检查密码是否匹配
            String hashedInputPassword = PasswordHasher.hashPassword(password, user.getSalt());
            boolean isPasswordMatch = hashedInputPassword.equals(user.getPasswordHash());

            if (!isPasswordMatch) {
                // 密码不匹配
                logger.warning("Login failed. Incorrect password for username: " + username);
                return null;
            }

            // 登录成功
            logger.info("User logged in successfully. Username: " + username);
            return user;
        } catch (Exception e) {
            logger.severe("Error occurred during login: " + e.getMessage());
            // 在实际环境中，可能还需要把异常抛出，让调用者来处理
            // throw new ServiceException("Error occurred during login", e);
            return null;
        }
    }

    @Override
    public List<UserDTO> searchUsersByUsername(String username) {
        List<UserDTO> userDTOS = userMapper.searchUsersByUsername(username);
        if (userDTOS==null){
            return new ArrayList<>();
        }
        return userDTOS;
    }

    @Override
    public List<UserDTO> getUsersInfoByFollowerIds(List<Integer> followerIds , Integer userId) {
        List<String> keysWithPrefix = followerIds.stream()
                .map(id -> "user:" + id)
                .collect(Collectors.toList());

        // 使用带有前缀的键来查询缓存
        List<String> userDataFromCache = redisTemplate.opsForValue().multiGet(keysWithPrefix);

        // 找到缓存中缺失的ID
        List<Integer> missingIds = IntStream.range(0, keysWithPrefix.size())
                .filter(i -> userDataFromCache.get(i) == null)
                .mapToObj(followerIds::get)
                .collect(Collectors.toList());

        List<UserDTO> users = new ArrayList<>();

        // 如果有缓存中不存在的用户 IDs，从数据库中查询这些用户的信息
        if (!missingIds.isEmpty()) {
            // 获取 Redisson 锁
            RLock lock = redissonClient.getLock("userDBLock:" + userId);

            try {
                // 尝试获取锁，等待最多100秒，锁定时间为10秒
                if (lock.tryLock(10, 1, TimeUnit.SECONDS)) {
                    // Double-Check: 再次检查缓存是否已被其他线程更新
                    List<String> stillMissingData = redisTemplate.opsForValue().multiGet(missingIds.stream().map(id -> "user:" + id).collect(Collectors.toList()));
                    List<Integer> stillMissingIds = new ArrayList<>();
                    for (int i = 0; i < stillMissingData.size(); i++) {
                        if (stillMissingData.get(i) == null) {
                            stillMissingIds.add(missingIds.get(i));
                        }
                    }

                    // 如果仍有缺失的ID，从数据库中查询这些用户的信息
                    if (!stillMissingIds.isEmpty()) {
                        List<UserDTO> usersFromDB = userMapper.selectUsersByIds(stillMissingIds);
                        users.addAll(usersFromDB);

                        // 将从数据库中查询到的用户信息存回 Redis
                        Map<String, String> usersMap = usersFromDB.stream()
                                .collect(Collectors.toMap(
                                        user -> "user:" + user.getId(),
                                        this::convertUserToJson // 假设您已经实现了 convertUserToJson 方法
                                ));
                        redisTemplate.opsForValue().multiSet(usersMap);
                    }
                } else {
                    // 获取锁失败，可以选择记录日志、抛异常或其他处理
                    logger.warning("Failed to acquire lock for userDBLock.");
                }
            } catch (InterruptedException e) {
                // 处理异常
                logger.severe("Error acquiring lock for userDBLock");
                e.printStackTrace();
            } finally {
                // 释放锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return users;
    }

    @Override
    public User getUserInfo(Long userId) {
        String key = "user_info:" + userId;
        final long expireTime = 8 * 60 * 60; // 设置过期时间为8小时
        User user = null;
        boolean isHotKey = false;
        try {
            // 首先使用京东HotKey检查是否是热点数据
            if (JdHotKeyStore.isHotKey(key)) {
                // 如果是热点数据，则尝试从本地缓存获取用户信息
                user = (User) JdHotKeyStore.get(key);
                if (user != null) {
                    logger.info("从本地缓存获取到热点用户信息，用户ID: " + userId);
                    return user;
                }
                isHotKey = true;
            }

            // 如果不是热点数据或本地缓存中没有，则继续原有的Redis查询逻辑
            user = (User) redisTemplate.opsForValue().get(key);
            if (user == null) {
                RLock lock = redissonClient.getLock("user_info_lock:" + userId);
                lock.lock();
                try {
                    user = (User) redisTemplate.opsForValue().get(key);
                    if (user == null) {
                        logger.info("从数据库获取用户信息，用户ID: " + userId);
                        user = userMapper.findById(userId);
                        if (user != null) {
                            redisTemplate.opsForValue().set(key, user, expireTime, TimeUnit.SECONDS);
                            logger.info("用户信息已存入Redis，用户ID: " + userId);
                            if (isHotKey){
                                JdHotKeyStore.smartSet(key , user);
                            }
                        } else {
                            logger.warning("数据库中未找到用户，用户ID: " + userId);
                        }
                    }
                } finally {
                    lock.unlock();
                }
            } else {
                if (isHotKey){
                    JdHotKeyStore.smartSet(key , user);
                }
                logger.info("用户信息从Redis获取，用户ID: " + userId);
            }
        } catch (Exception e) {
            logger.severe("获取用户信息时发生异常，用户ID: " + userId + "，异常信息: " + e.getMessage());
        }

        // 获取用户的关注数据
        Map<String, Integer> userFollowData = userFollowService.getUserFollowData(userId);
        if (userFollowData != null) {
            user.setFollowCount(userFollowData.getOrDefault("followCount", 0));
            user.setAttentionCount(userFollowData.getOrDefault("attentionCount", 0));
        }
        return user;
    }

    /**
     * 获取关注数
     * @param userId
     * @return
     */
    public int getAttentionCount(Long userId) {
        Integer count = (Integer) redisTemplate.opsForValue().get(getCacheKey(userId, FANS_COUNT_KEY));
        if (count == null) {
            // 缓存未命中，发送一个消息到消息队列
            count = 0; // 设置默认值
            sendMessageWhenNoDataFound(userId);
        }
        return count;
    }

    private void sendMessageWhenNoDataFound(Long userId) {
        // 消息内容是userId
        String message = String.valueOf(userId);

        // 您的目标主题是"UserAttentionCountMissing"
        String destination = "UserAttentionCountMissing";

        // 发送消息
//        rocketMQTemplate.convertAndSend(destination, message);
    }

    /**
     * 获取粉丝数
     * @param userId
     * @return
     */
    public int getFollowCount(Long userId) {
        Integer count = (Integer) redisTemplate.opsForValue().get(getCacheKey(userId, FOLLOW_COUNT_KEY));
        // 如果没有找到数据，返回0
        return count != null ? count : 0;
    }

    private String getCacheKey(Long userId, String key) {
        // 创建缓存键，可以根据需要调整格式
        return key + "::" + userId;
    }

    private UserDTO convertJsonToUser(String json) {
        // 使用 Fastjson 将 JSON 字符串转换为 User 对象
        return JSON.parseObject(json, UserDTO.class);
    }

    private String convertUserToJson(UserDTO user) {
        // 使用 Fastjson 将 User 对象转换为 JSON 字符串
        return JSON.toJSONString(user);
    }

}
