package com.comment.comment.consumer;

import com.comment.comment.feign.MatcherClient;
import com.comment.comment.mapper.CommentMapper;
import com.comment.comment.model.CommentContent;
import com.comment.comment.model.CommentIndex;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class CommentConsumer {

    @Resource
    private MatcherClient matcherClient;

    @Resource
    private CommentMapper commentMapper;

    // 指定监听的Kafka主题
    @KafkaListener(topics = "comment-topic")
    public void receiveComment(CommentIndex commentIndex) {
        // 先判断评论内容是否符合要求
        // 先判断评论内容是否符合要求
        List<String> matcher = matcherClient.matcher(commentIndex.getMessage());
        if (matcher==null || matcher.size()<=0){
            // 在此处进行保存评论到数据库的操作
            int id = commentMapper.insertCommentIndex(commentIndex);
            CommentContent commentContent = new CommentContent();
            commentContent.setCommentId(id);
            commentContent.setMessage(commentContent.getMessage());
            commentMapper.insertCommentContent(commentContent);
        }
    }
}

