package com.post.post.service;

import com.post.post.mapper.PostLikeMapper;
import com.post.post.model.PostLike;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class KafkaReceiver {

    @Resource
    private PostLikeMapper postLikeMapper;

    @KafkaListener(topics = "post_likes_topic")
    public void receivePostLike(PostLike postLike) {
        // 在这里处理接收到的点赞消息
        System.out.println("Received post like: " + postLike);

        // 执行相应的操作，例如更新数据库
        postLikeMapper.updatePostLike(postLike);
    }
}

