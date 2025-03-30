package com.message.consumer.listener;

import com.message.consumer.client.CommentServiceClient;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@RocketMQMessageListener(topic = "CommentIndexUpdateTopic", consumerGroup = "commentServiceProdGroup", consumeMode = ConsumeMode.ORDERLY)
public class CommentMessageConsumer implements RocketMQListener<String> {

    @Resource
    private CommentServiceClient commentServiceClient; // Feign客户端

    @Override
    public void onMessage(String message) {
        // 解析消息内容
        String[] parts = message.split(",");
        Long objId = Long.parseLong(parts[0]);
        Integer parent = Integer.parseInt(parts[1]);

        // 通过Feign客户端调用服务提供者的方法
        commentServiceClient.cacheCommentIdsIfNeeded(objId, parent);

    }
}
