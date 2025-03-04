package com.comment.server.mapper;

import com.comment.server.model.CommentLikeDetails;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentLikeDetailsMapper {

    // 插入新的点赞详情
    int insertCommentLikeDetails(CommentLikeDetails commentLikeDetails);

    // 根据commentId和memberId更新点赞时间，仅当记录存在时
    int updateLikeTimeByCommentIdAndMemberId(@Param("commentId") Long commentId, @Param("memberId") Long memberId);

    // 根据commentId查询点赞详情
    CommentLikeDetails selectByCommentId(Long commentId);

    //根据用户id查询用户所有点赞的评论id
    List<Long> findCommentIdsByMemberId(@Param("memberId") Long memberId);
}
