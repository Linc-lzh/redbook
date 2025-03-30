package com.comment.server.controller;

import com.comment.server.dto.CommentDTO;
import com.comment.server.request.CommentIndicesRequest;
import com.comment.server.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Resource
    private CommentService commentService;

    /**
     * 根据对象ID和父评论ID查询所有评论ID集合
     * @param objId 对象ID
     * @param parent 父评论ID
     * @return 评论ID集合的响应实体
     */
    @GetMapping("/findAllCommentIdsByObjId")
    public ResponseEntity<Set<Long>> findAllCommentIdsByObjId(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent) {
        try {
            Set<Long> commentIds = commentService.findAllCommentIdsByObjId(objId, parent);
            return ResponseEntity.ok(commentIds);
        } catch (Exception e) {
            // 异常处理，可以根据不同异常类型返回不同的状态码或错误信息
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 如果需要，将评论ID集合缓存到Redis中
     * @param objId 对象ID
     * @param parent 父评论ID
     * @return 状态200 OK的空响应实体
     */
    @GetMapping("/cacheCommentIdsIfNeeded")
    public ResponseEntity<Void> cacheCommentIdsIfNeeded(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent) {
        try {
            commentService.cacheCommentIdsIfNeeded(objId, parent);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 根据父评论ID查询所有子评论ID
     * @param parentId 父评论ID
     * @return 子评论ID列表的响应实体
     */
    @GetMapping("/findAllChildCommentIdsByParentId/{parentId}")
    public ResponseEntity<List<Long>> findAllChildCommentIdsByParentId(@PathVariable("parentId") Long parentId) {
        try {
            List<Long> childCommentIds = commentService.findAllChildCommentIdsByParentId(parentId);
            return ResponseEntity.ok(childCommentIds);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据评论ID列表查询评论索引
     * @param request 评论ID列表
     * @return 评论索引列表的响应实体
     */
    @PostMapping("/findCommentIndicesByCommentIds")
    public ResponseEntity<List<CommentDTO>> findCommentIndicesByCommentIds(@RequestBody CommentIndicesRequest request) {
        try {
            List<CommentDTO> commentIndices = commentService.findCommentIndicesByCommentIds(
                    request.getCommentIds(), request.getObjId(), request.getParent());
            return ResponseEntity.ok(commentIndices);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 根据评论ID列表查询评论索引和内容
     * @param ids 评论ID列表
     * @return 一个简单的字符串响应
     */
    @PostMapping("/findCommentsByIds")
    public ResponseEntity<String> findCommentsByIds(@RequestBody List<Long> ids) {
        try {
            commentService.findCommentsByIds(ids);
            // 即使没有找到数据，也返回一个简单的成功消息
            return ResponseEntity.ok("查询成功");
        } catch (Exception e) {
            // 发生异常时，返回一个错误消息
            return ResponseEntity.internalServerError().body("查询失败");
        }
    }

    /**
     * 根据对象ID(objId)和父评论ID(parent)查询所有相关评论索引，并更新Redis缓存。
     *
     * @param objId 对象ID，通过查询参数传递
     * @param parent 父评论ID，通过查询参数传递
     * @return ResponseEntity表示操作结果，实际应用中应根据需要返回适当的响应体或状态码
     */
    @GetMapping("/findByObjIdAndParent")
    public ResponseEntity<?> findByObjIdAndParent(
            @RequestParam("objId") Long objId,
            @RequestParam("parent") Integer parent) {

        // 执行查询并缓存结果的操作
        commentService.findByObjIdAndParent(objId, parent);

        // 这里示例中直接返回成功的响应，实际应用中可能会根据操作结果返回更具体的数据或状态
        return ResponseEntity.ok("数据已更新并缓存");
    }
}
