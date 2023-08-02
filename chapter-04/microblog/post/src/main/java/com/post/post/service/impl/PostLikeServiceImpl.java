package com.post.post.service.impl;

import com.post.post.mapper.PostIndexMapper;
import com.post.post.mapper.PostLikeMapper;
import com.post.post.model.PostIndex;
import com.post.post.model.PostLike;
import com.post.post.service.PostLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostLikeServiceImpl implements PostLikeService {

    @Resource
    private PostLikeMapper postLikeMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private PostIndexMapper postIndexMapper;

    @Resource
    private KafkaTemplate<String, PostLike> kafkaTemplate;

    private static final int MAX_ZSET_SIZE = 10000; // 当一个post的likes超过这个值时,开始分片
    private static final int SHARD_COUNT = 10; // 总共的分片数

    private static final String POST_LIKES_PREFIX = "post_likes:";


    @Override
    public PostLike getPostLikeById(int id) {
        return postLikeMapper.findById(id);
    }

    @Override
    public void createPostLike(PostLike postLike) {
        Integer postId = postLike.getTid();
        Integer userId = postLike.getUid();
        Long timestamp = System.currentTimeMillis();

        // 获取key的最小分片编号
        int minShardId = getMinShardId(postId);

        for (int shardId = minShardId; shardId < minShardId + SHARD_COUNT; shardId++) {
            String shardKey = getShardKey(postId, shardId);

            if (!redisTemplate.hasKey(shardKey) || redisTemplate.opsForZSet().size(shardKey) < MAX_ZSET_SIZE) {
                // 如果分片key不存在，或者分片未满，则添加到该分片中
                redisTemplate.opsForZSet().add(shardKey, userId, timestamp);

                // 设置过期时间为七天
                redisTemplate.expire(shardKey, 7, TimeUnit.DAYS);
                return;
            }
        }

        // 分片都满了，则添加到最小分片的ZSet中
        String minShardKey = getShardKey(postId, minShardId);
        redisTemplate.opsForZSet().add(minShardKey, userId, timestamp);

        // 设置过期时间为七天
        redisTemplate.expire(minShardKey, 7, TimeUnit.DAYS);

        //添加到用户的点赞列表中
        String cacheKey = "userPosts:" + userId; // 设置缓存键名
        String hashKey = String.valueOf(postLike.getId());
        String value = String.valueOf(System.currentTimeMillis());

        //保存在redis hash中，user的inbox
        redisTemplate.opsForHash().put(cacheKey, hashKey, value);

        // 异步任务中发送到Kafka
        sendToKafkaAsync(postLike);
    }

    private int getMinShardId(Integer postId) {
        return Math.abs(postId.hashCode()) % SHARD_COUNT;
    }

    private String getShardKey(Integer postId, int shardId) {
        return POST_LIKES_PREFIX + postId + ":" + shardId;
    }


    @Override
    public void updatePostLike(PostLike postLike) {
        postLikeMapper.updatePostLike(postLike);
    }

    @Override
    public void deletePostLike(int id) {
        postLikeMapper.deletePostLike(id);
    }

    /**
     * 根据帖子id查询所有点赞的用户
     * @param postId
     * @return
     */
    @Override
    public Set<Integer> selectUserIdsByPostId(Integer postId , long start, long end) {
        //这里的话，如果redis中查询不到就可以不用查询数据库了。所以直接查询数据库就行了
        //循环redis分片
        String baseKey = POST_LIKES_PREFIX + postId;

        // 先查询主键对应的有序集合
        Set<Integer> likes = (Set<Integer>)redisTemplate.opsForZSet().range(baseKey, start, end);
        if (likes != null && !likes.isEmpty()) {
            return likes;
        }

        // 判断是否需要查询分片数据
        if (start > MAX_ZSET_SIZE) {
            //循环分片
            for (int i = 0; i < SHARD_COUNT; i++) {
                long shardStart = start - MAX_ZSET_SIZE*(i+1);
                long shardEnd = end - MAX_ZSET_SIZE*(i+1);

                //循环判断分片
                String shardKey = baseKey + ":" + i;
                Set<Integer> shardLikes = redisTemplate.opsForZSet().range(shardKey, shardStart, shardEnd);
                if (shardLikes != null && !shardLikes.isEmpty()) {
                    return shardLikes;
                }
            }
        }

        return new HashSet<>();
    }

    /**
     * 根据用户id查询所有点赞的帖子
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> selectPostIdsByUserId(Integer userId) {
        String cacheKey = "userPosts:" + userId; // 设置缓存键名
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();

        // 尝试从缓存中获取数据
        Map<String, Object> cachedData = hashOperations.entries(cacheKey);
        if (!cachedData.isEmpty()) {
            // 检查时间戳是否小于一个小时
            Long ttl = (Long) cachedData.get("ttl");
            long currentTime = System.currentTimeMillis();
            if (ttl != null && currentTime - ttl < 1 * 60 * 60 * 1000) {
                // 更新缓存过期时间
                redisTemplate.expire(cacheKey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);

                // 更新时间戳
                cachedData.put("ttl", currentTime + 7 * 24 * 60 * 60);
                hashOperations.putAll(cacheKey, cachedData);
            }

            return cachedData; // 如果数据存在于缓存中，直接返回
        }


        // 如果缓存中不存在数据，则从数据库中查询数据
        List<PostLike> postLikes = postLikeMapper.selectByUserId(userId , 1000); // 替换为从数据库中查询帖子id的方法

        // 将查询到的数据存入缓存
        Map<String, Object> cacheData = new HashMap<>();
        for (PostLike postLike : postLikes) {
            cacheData.put(String.valueOf(postLike.getTid()), postLike.getCreateTime());
        }
        cacheData.put("minCid" , postLikes.get(postLikes.size()-1));
        long currentTime = System.currentTimeMillis();
        cachedData.put("ttl", currentTime + 7 * 24 * 60 * 60);
        //存入缓存当中
        hashOperations.putAll(cacheKey, cacheData);

        // 设置过期时间（单位：秒）
        redisTemplate.expire(cacheKey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);

        return cacheData;
    }

    /**
     * 查询所有帖子详情
     * @param postId
     * @return
     */
    @Override
    public List<PostIndex> selectPostIndex(List<Integer> postId) {
        //mGet查询所有postIndex
        List<String> postIdStrings = postId.stream()
                .map(id -> "postIndex:" + id)
                .collect(Collectors.toList());

        //批量查询
        //[postIndex:1,postIndex:2,postIndex:3,postIndex:4,postIndex:5]
        //[1,2,null,4,5]
        List<PostIndex> postIndexValues = redisTemplate.opsForValue().multiGet(postIdStrings);

        //删除postIndexValues中存在的id，剩下的id就是在redis中没有查询到的
        postId.removeIf(id -> postIndexValues.stream().anyMatch(postIndex -> postIndex != null && postIndex.getId() == id));

        //判断postId中是否还有数据
        if (postId==null || postId.size()<=0){
            //说明都在缓存中，直接返回
            return postIndexValues;
        }

        //否则说明还存在数据
        //查询数据库
        List<PostIndex> postIndices = postIndexMapper.selectByIdList(postId);
        //如果为空，说明所有数据都不在缓存中，直接返回数据库查询的内容
        if (postIndexValues==null || postIndexValues.size()<=0){
            return postIndices;
        }
        //循环找到空值，把最左侧弹出去，放入到空值
        for (int i = 0; i < postIndexValues.size(); i++) {
            if (postIndexValues.get(i) == null) {
                if (!postIndices.isEmpty()) {
                    postIndexValues.set(i, postIndices.remove(0));
                }
            }
        }
        return postIndexValues;
    }

    @Async
    void sendToKafkaAsync(PostLike postLike) {
        // 异步任务中发送到Kafka
        kafkaTemplate.send("post_likes_topic", postLike);
    }
}

