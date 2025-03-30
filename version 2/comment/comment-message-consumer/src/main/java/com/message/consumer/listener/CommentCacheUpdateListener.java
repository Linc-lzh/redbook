package com.message.consumer.listener;

import com.message.consumer.client.CommentServiceClient;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RocketMQMessageListener(topic = "CommentCacheUpdateTopic", consumerGroup = "commentCacheConsumerGroup")
public class CommentCacheUpdateListener implements RocketMQListener<List<Long>> {

    private static final Logger log = LoggerFactory.getLogger(CommentCacheUpdateListener.class);

    @Resource
    private CommentServiceClient commentServiceClient;

    @Override
    public void onMessage(List<Long> missingCommentIds) {
        if (missingCommentIds.isEmpty()) {
            log.info("接收到的缺失评论ID列表为空，无需处理。");
            return;
        }

        log.info("正在处理缺失的评论ID列表: {}", missingCommentIds);
        ResponseEntity<String> response = commentServiceClient.findCommentsByIds(missingCommentIds);

        if (response.getStatusCode().is2xxSuccessful()) {
            log.info("远程调用成功，响应消息：{}", response.getBody());
        } else {
            log.error("远程调用失败，状态码：{}", response.getStatusCode());
        }
    }
}


