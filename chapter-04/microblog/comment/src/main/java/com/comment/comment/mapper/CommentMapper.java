package com.comment.comment.mapper;

import com.comment.comment.model.CommentContent;
import com.comment.comment.model.CommentIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    int updateCommentIndex(CommentIndex commentIndex);

    int insertCommentIndex(CommentIndex commentIndex);

    int insertCommentContent(CommentContent commentContent);

    List<CommentIndex> pageRootComment(int objId, int start , int end);

    List<CommentIndex> pageSecondComment(@Param("id") int id , @Param("start") int start , @Param("end") int end);

    List<CommentIndex> contents(@Param("commentIds") List<Long> commentIds);

    List<CommentIndex> rootComment(int objId);
}
