package com.post.server.mapper;

import com.post.server.model.PostIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostIndexMapper {

    /**
     * 新增文章
     * @param postIndex
     * @return
     */
    int insertPostIndex(PostIndex postIndex);

    /**
     * 更新文章
     * @param postIndex
     * @return
     */
    int updatePostIndex(PostIndex postIndex);

    /**
     * 删除文章（实际是更新is_delete字段）
     * @param id
     * @return
     */
    int softDeletePostIndex(@Param("id") long id);

    /**
     * 根据 uid 查询帖子列表
     * @param uid
     * @return
     */
    List<PostIndex> selectByUserId(@Param("uid") Integer uid);

    /**
     * 根据 id 查询单个帖子
     * @param id
     * @return
     */
    PostIndex selectById(@Param("id") Long id);

    /**
     * 根据id批量查询index
     * @param ids
     * @return
     */
    List<PostIndex> selectPostsByIds(@Param("ids") List<Long> ids);

}
