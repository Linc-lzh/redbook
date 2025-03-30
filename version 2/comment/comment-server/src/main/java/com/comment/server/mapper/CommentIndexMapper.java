package com.comment.server.mapper;

import com.comment.server.dto.CommentDTO;
import com.comment.server.model.CommentIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface CommentIndexMapper {

    // 插入评论索引
    int insertCommentIndex(CommentIndex commentIndex);

    // 更新评论点赞数
    int updateLikeCountById(@Param("id") Long id, @Param("likeCount") Integer likeCount);

    // 查询评论索引
    CommentIndex selectById(Long id);

    /**
     * 根据对象ID和父评论ID查询所有评论ID集合
     * @param objId 对象ID
     * @param parent 父评论ID
     * @return 评论ID集合
     */
    Set<Long> selectCommentIdsByObjIdAndParent(@Param("objId") Long objId, @Param("parent") Integer parent);

    /**
     * 根据ids查询评论索引和内容
     * @param ids
     * @return
     */
    List<CommentDTO> findCommentsByIds(@Param("ids") List<Long> ids);

    /**
     * 根据ids查询评论索引
     * @param ids
     * @return
     */
    List<CommentIndex> selectCommentsByIds(@Param("ids") List<Long> ids);

    /**
     * 根据objId和parent查询评论索引
     * @param objId
     * @param parent
     * @return
     */
    List<CommentIndex> findByObjIdAndParent(@Param("objId") Long objId, @Param("parent") Integer parent);
}
