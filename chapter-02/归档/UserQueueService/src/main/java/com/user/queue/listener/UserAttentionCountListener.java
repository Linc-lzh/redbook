package com.user.queue.listener;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;


@Service
@RocketMQMessageListener(topic = "UserAttentionCountTopic", consumerGroup = "userAttentionCountConsumerGroup")
public class UserAttentionCountListener implements RocketMQListener<Long> {


    @Override
    public void onMessage(Long userId) {
        // 当收到消息时的处理逻辑
        // userId 是消息内容
        // 这里可以添加根据userId获取关注数的逻辑，例如查询数据库等

        System.out.println("Received user attention count request for userId: " + userId);
        // 逻辑处理...
    }
}
