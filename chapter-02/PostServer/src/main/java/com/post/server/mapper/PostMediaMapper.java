package com.post.server.mapper;

import com.post.server.model.PostMedia;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface PostMediaMapper {

    /**
     * 插入帖子媒体信息
     * @param postMedia 帖子媒体对象
     * @return 插入的行数
     */
    @Insert("INSERT INTO post_media (post_id, media_type, media_url, create_time, update_time) VALUES (#{postId}, #{mediaType}, #{mediaUrl}, #{createTime}, #{updateTime})")
    int insertPostMedia(PostMedia postMedia);

    /**
     * 更新帖子媒体信息
     * @param postMedia 更新后的帖子媒体对象
     * @return 更新的行数
     */
    @Update("UPDATE post_media SET media_type = #{mediaType}, media_url = #{mediaUrl}, create_time = #{createTime}, update_time = #{updateTime} WHERE post_id = #{postId}")
    int updatePostMedia(PostMedia postMedia);

    /**
     * 根据帖子ID查询媒体信息
     * @param postId 帖子ID
     * @return 查询到的帖子媒体对象
     */
    @Select("SELECT * FROM post_media WHERE post_id = #{postId}")
    PostMedia getPostMedia(Long postId);

    /**
     * 批量插入帖子媒体信息
     * @param postMediaList 帖子媒体信息列表
     * @return 插入的行数
     */
    @Insert({
            "<script>",
            "INSERT INTO post_media (post_id, media_type, media_url, create_time, update_time) VALUES",
            "<foreach collection='postMediaList' item='postMedia' separator=','>",
            "(#{postMedia.postId}, #{postMedia.mediaType}, #{postMedia.mediaUrl}, #{postMedia.createTime}, #{postMedia.updateTime})",
            "</foreach>",
            "</script>"
    })
    int batchInsertPostMedia(@Param("postMediaList") List<PostMedia> postMediaList);
}
