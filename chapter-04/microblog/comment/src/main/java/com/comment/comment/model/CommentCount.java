package com.comment.comment.model;

import lombok.Data;

/**
 * 评论计数
 */
@Data
public class CommentCount {

    private Long id;

    private Integer rootCommentCount;

    private Integer commentCount;
}
