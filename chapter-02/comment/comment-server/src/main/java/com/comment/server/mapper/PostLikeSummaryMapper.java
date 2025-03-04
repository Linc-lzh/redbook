package com.comment.server.mapper;

import com.comment.server.model.PostLikeSummary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostLikeSummaryMapper {

    // 插入帖子点赞汇总
    int insertPostLikeSummary(PostLikeSummary postLikeSummary);

    // 更新点赞总数
    int updateLikeCountByObjId(Long objId, Long likeCount);

    // 查询帖子点赞汇总
    PostLikeSummary selectByObjId(Long objId);
}
