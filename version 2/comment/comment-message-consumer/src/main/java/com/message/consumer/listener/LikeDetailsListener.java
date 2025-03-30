package com.message.consumer.listener;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = "likesUpdateTopic", consumerGroup = "likeDetailsGroup", consumeMode = ConsumeMode.CONCURRENTLY)
public class LikeDetailsListener implements RocketMQListener<Long> {

    @Override
    public void onMessage(Long userId) {
        // 在这里处理接收到的userId消息，例如查询和更新用户的点赞详情
        System.out.println("Received a message for updating like details for userId: " + userId);

        // 示例：根据userId查询和更新点赞详情
        // queryAndUpdateLikeDetails(userId);
    }

    // 示例方法，根据实际业务逻辑实现
    private void queryAndUpdateLikeDetails(Long userId) {
        // 实现查询和更新点赞详情的逻辑
    }
}

