package com.comment.web.client;

import com.comment.web.request.CommentIndicesRequest;
import com.comment.web.vo.CommentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import java.util.Set;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "comment-service")
public interface CommentServiceClient {
    /**
     * 查询指定对象ID和父评论ID下的所有评论ID集合
     * @param objId 对象ID
     * @param parent 父评论ID
     * @return ResponseEntity封装的评论ID集合
     */
    @GetMapping("/api/comments/findAllCommentIdsByObjId")
    ResponseEntity<Set<Long>> findAllCommentIdsByObjId(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent);

    /**
     * 根据需要将评论ID集合缓存到Redis中
     * @param objId 对象ID
     * @param parent 父评论ID
     * @return ResponseEntity对象，表示操作的响应状态
     */
    @GetMapping("/api/comments/cacheCommentIdsIfNeeded")
    ResponseEntity<Void> cacheCommentIdsIfNeeded(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent);

    /**
     * 根据父评论ID查询所有子评论ID
     * @param parentId 父评论ID
     * @return ResponseEntity封装的子评论ID列表
     */
    @GetMapping("/api/comments/findAllChildCommentIdsByParentId/{parentId}")
    ResponseEntity<List<Long>> findAllChildCommentIdsByParentId(@PathVariable("parentId") Long parentId);

    /**
     * 根据评论ID列表以及其他参数查询评论索引和内容
     *
     * @param request 包含评论ID列表、objId和parent的请求对象
     * @return 封装的评论索引列表的响应实体
     */
    @PostMapping("/api/comments/findCommentIndicesByCommentIds")
    ResponseEntity<List<CommentVO>> findCommentIndicesByCommentIds(@RequestBody CommentIndicesRequest request);
}
