package com.comment.web.controller;

import com.comment.web.client.CommentServiceClient;
import com.comment.web.request.CommentIndicesRequest;
import com.comment.web.vo.CommentVO;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Resource
    private CommentServiceClient commentServiceClient;

    @GetMapping("/findAllCommentIdsByObjId")
    public ResponseEntity<Set<Long>> findAllCommentIdsByObjId(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent) {
        return commentServiceClient.findAllCommentIdsByObjId(objId, parent);
    }

    @GetMapping("/cacheCommentIdsIfNeeded")
    public ResponseEntity<Void> cacheCommentIdsIfNeeded(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent) {
        return commentServiceClient.cacheCommentIdsIfNeeded(objId, parent);
    }

    @GetMapping("/findAllChildCommentIdsByParentId/{parentId}")
    public ResponseEntity<List<Long>> findAllChildCommentIdsByParentId(@PathVariable("parentId") Long parentId) {
        return commentServiceClient.findAllChildCommentIdsByParentId(parentId);
    }

    @PostMapping("/findCommentIndicesByCommentIds")
    public ResponseEntity<List<CommentVO>> findCommentIndicesByCommentIds(@RequestBody CommentIndicesRequest request) {
        // 直接调用Feign客户端，传递整个请求对象
        return commentServiceClient.findCommentIndicesByCommentIds(request);
    }
}
