package com.message.consumer.listener;

import com.message.consumer.client.CommentServiceClient;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RocketMQMessageListener(topic = "CommentLikesMissingTopic", consumerGroup = "commentLikesMissingGroup")
public class CommentLikesMissingListener implements RocketMQListener<String> {

    @Resource
    private CommentServiceClient commentServiceClient;

    @Override
    public void onMessage(String message) {
        // 解析消息内容
        String[] parts = message.split(":");
        Long objId = Long.parseLong(parts[0]);
        Integer parent = Integer.parseInt(parts[1]);

        // 使用Feign客户端调用远程服务
        commentServiceClient.findByObjIdAndParent(objId, parent);
    }
}
