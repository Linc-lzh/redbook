package com.post.post.mapper;

import com.post.post.model.PostIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostIndexMapper {

    // 根据id查询帖子
    PostIndex findById(@Param("id") int id);

    // 创建帖子
    void createPostIndex(PostIndex postIndex);

    // 更新帖子
    void updatePostIndex(PostIndex postIndex);

    // 删除帖子
    void deletePostIndex(@Param("id") int id);

    // 查询所有帖子
    List<PostIndex> getAllPostIndexes();

    //分页查询
    List<PostIndex> getPostIndexByUserId(@Param("userId") int userId, @Param("start") int start, @Param("pageSize") int pageSize);

    //查询user帖子的count
    int getPostIndexCountByUserId(@Param("userId") int userId);

    List<PostIndex> selectByIdList(List<Integer> idList);
}

