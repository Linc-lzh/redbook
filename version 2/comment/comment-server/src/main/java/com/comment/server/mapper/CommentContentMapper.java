package com.comment.server.mapper;

import com.comment.server.model.CommentContent;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentContentMapper {

    // 插入评论内容
    int insertCommentContent(CommentContent commentContent);

    // 根据commentId更新内容
    int updateMessageByCommentId(Long commentId, String message);

    // 查询评论内容
    CommentContent selectByCommentId(Long commentId);
}
