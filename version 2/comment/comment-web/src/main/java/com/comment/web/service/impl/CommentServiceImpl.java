package com.comment.web.service.impl;

import com.comment.web.client.CommentServiceClient;
import com.comment.web.request.CommentIndicesRequest;
import com.comment.web.service.CommentService;
import com.comment.web.vo.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentServiceClient commentServiceClient;

    @Override
    public Set<Long> findAllCommentIdsByObjId(Long objId, Integer parent) {
        ResponseEntity<Set<Long>> response = commentServiceClient.findAllCommentIdsByObjId(objId, parent);
        return response.getBody();
    }

    @Override
    public void cacheCommentIdsIfNeeded(Long objId, Integer parent) {
        commentServiceClient.cacheCommentIdsIfNeeded(objId, parent);
    }

    @Override
    public List<Long> findAllChildCommentIdsByParentId(Long parentId) {
        ResponseEntity<List<Long>> response = commentServiceClient.findAllChildCommentIdsByParentId(parentId);
        return response.getBody();
    }

    @Override
    public List<CommentVO> findCommentIndicesByCommentIds(CommentIndicesRequest indicesRequest) {
        ResponseEntity<List<CommentVO>> response = commentServiceClient.findCommentIndicesByCommentIds(indicesRequest);
        return response.getBody();
    }
}
