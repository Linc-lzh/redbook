package com.message.consumer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "comment-service")
public interface CommentServiceClient {

    @PostMapping("/api/comments/cacheCommentIdsIfNeeded")
    void cacheCommentIdsIfNeeded(@RequestParam("objId") Long objId, @RequestParam("parent") Integer parent);

    /**
     * 根据评论ID列表查询评论索引
     *
     * @param commentIds 评论ID列表
     * @return 评论索引列表的响应实体
     */
    @GetMapping("/api/comments/findCommentIndicesByCommentIds")
    void findCommentIndicesByCommentIds(@RequestBody List<Long> commentIds);

    /**
     * 调用远程服务以根据评论ID列表查询评论索引和内容
     * @param ids 评论ID列表
     * @return 一个简单的字符串响应
     */
    @PostMapping("/api/comments/findCommentsByIds")
    ResponseEntity<String> findCommentsByIds(@RequestBody List<Long> ids);

    /**
     * 调用远程服务以根据对象ID和父评论ID查询所有相关评论索引，并更新Redis缓存。
     *
     * @param objId 对象ID
     * @param parent 父评论ID
     * @return 调用结果
     */
    @GetMapping("/api/comments/findByObjIdAndParent")
    ResponseEntity<?> findByObjIdAndParent(
            @RequestParam("objId") Long objId,
            @RequestParam("parent") Integer parent);

}

