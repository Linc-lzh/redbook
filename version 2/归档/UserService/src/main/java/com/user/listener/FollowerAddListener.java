package com.user.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.user.mapper.AttentionMapper;
import com.user.mapper.UserFollowMapper;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RocketMQMessageListener(topic = "followerAddTopic", consumerGroup = "followerConsumerGroup")
public class FollowerAddListener implements RocketMQListener<String> {

    @Resource
    private UserFollowMapper userFollowMapper;

    @Resource
    private AttentionMapper attentionMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Transactional
    @Override
    public void onMessage(String message) {
        JSONObject messageObj = JSON.parseObject(message);
        String uniqueId = messageObj.getString("uniqueId");
        int userId = messageObj.getIntValue("userId");
        int followerId = messageObj.getIntValue("followerId");

        // 检查并设置消息处理标记
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        Boolean isAbsent = valueOps.setIfAbsent(uniqueId, "processed");

        if (Boolean.TRUE.equals(isAbsent)) {
            redisTemplate.expire(uniqueId, 10, TimeUnit.MINUTES);
            // 消息未处理，进行处理
            int result1 = userFollowMapper.addFollower(userId, followerId);
            //另一个表也需要操作
            int result2 = attentionMapper.follow(followerId, userId);

            if (result1 > 0 && result2 > 0) {
                // 准备脚本资源和参数
                DefaultRedisScript<Long> script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/AddFollower.lua")));
                script.setResultType(Long.class); // 根据 Lua 脚本返回类型调整

                String followersKey = "followers:" + userId;
                String followingKey = "following:" + followerId;
                List<String> keys = Arrays.asList(followersKey, followingKey);
                String userIdStr = String.valueOf(userId);
                String followerIdStr = String.valueOf(followerId);
                String score = String.valueOf(System.currentTimeMillis());

                // 执行 Lua 脚本
                redisTemplate.execute(script, keys, userIdStr, followerIdStr, score);

                //粉丝数和关注数+1
                //缓冲区进行count操作

            } else {
                // 数据库操作失败，记录错误或采取恢复措施，调用钉钉或者企业微信什么的，报警，通知
            }
        } else {
            // 消息已处理，不执行任何操作
        }
    }
}

