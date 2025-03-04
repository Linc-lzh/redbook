package com.comment.web.service;

import com.comment.web.request.CommentIndicesRequest;
import com.comment.web.vo.CommentVO;

import java.util.List;
import java.util.Set;

public interface CommentService {

    Set<Long> findAllCommentIdsByObjId(Long objId, Integer parent);

    void cacheCommentIdsIfNeeded(Long objId, Integer parent);

    List<Long> findAllChildCommentIdsByParentId(Long parentId);

    List<CommentVO> findCommentIndicesByCommentIds(CommentIndicesRequest indicesRequest);
}
