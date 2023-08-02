package com.post.post.service.impl;

import com.common.result.Result;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.post.post.feign.BlurCounterClient;
import com.post.post.mapper.PostIndexMapper;
import com.post.post.mapper.PostLikeMapper;
import com.post.post.model.Counter;
import com.post.post.model.PostIndex;
import com.post.post.model.PostLike;
import com.post.post.service.PostIndexService;
import com.post.post.service.SensitiveWordService;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PostIndexServiceImpl implements PostIndexService {

    @Resource
    private PostIndexMapper postIndexMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private BlurCounterClient blurCounterClient;

    @Resource
    private PostLikeMapper postLikeMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;



    /**
     * 查询单个帖子索引
     * @param id
     * @return
     */
    @Override
    public PostIndex getPostIndexById(int id) {
        return postIndexMapper.findById(id);
    }

    @Override
    public void createPostIndex(PostIndex postIndex) {
        //通过ac自动机来判断帖子的标题和内容是否有敏感词
        List<String> titles = sensitiveWordService.match(postIndex.getTitle());
        boolean titleBool = true;
        boolean contentBool = true;
        if (titles!=null && titles.size()>0){
            //说明标题是包含敏感词的
            titleBool = false;
        }
        List<String> contents = sensitiveWordService.match(postIndex.getContent());
        if (contents!=null && contents.size()>0){
            contentBool = false;
        }
        //创建帖子
        if (titleBool && contentBool){
            postIndexMapper.createPostIndex(postIndex);
            //发送到自己的发件箱同时发送到其他用户的收件箱

        }
    }

    @Override
    public void updatePostIndex(PostIndex postIndex) {
        postIndexMapper.updatePostIndex(postIndex);
    }

    @Override
    public void deletePostIndex(int id) {
        postIndexMapper.deletePostIndex(id);
    }

    /**
     * 分页查询
     * @param userId
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public List<PostIndex> getPostIndexByUserId(int userId, int page, int pageSize) {
        // 为 Redis 缓存生成唯一键
        String redisKey = "postIndex:userId:" + userId;

        //判断是否是hotkey，如果是hotkey就直接从本地读取
        boolean hotKey = JdHotKeyStore.isHotKey(redisKey);

        if (hotKey){
            //从本地获取
            List<PostIndex> value = (List<PostIndex>)JdHotKeyStore.getValue(redisKey+page+pageSize);
            if (value!=null){
                return value;
            }
        }
        // 检查 Redis 中的数据是否存在
        boolean existsInRedis = redisTemplate.hasKey(redisKey);
        if (existsInRedis) {
            // Redis 中存在数据，直接返回缓存的结果
            int start = (page - 1) * pageSize;
            int end = start + pageSize - 1;
            Set<PostIndex> cachedData = redisTemplate.opsForZSet().reverseRange(redisKey, start, end);
            ArrayList<PostIndex> postIndices = new ArrayList<>(cachedData);
            //循环查询点赞数
            for (PostIndex postIndex : postIndices){
                Counter counter = new Counter();
                counter.setObjId(postIndex.getId());
                counter.setObjType(2);
                counter.setCountKey("like");
                Result<Counter> result = blurCounterClient.getCounter(counter);
                if (result.getData().getCountValue() != null) {
                    postIndex.getPostStatistics().setLikeCount(result.getData().getCountValue());
                } else {
                    postIndex.getPostStatistics().setLikeCount(0);
                }
            }
            if (hotKey){
                JdHotKeyStore.smartSet(redisKey+page+pageSize , postIndices);
            }
            return postIndices;
        } else {
            // Redis 中不存在数据，从 MySQL 中获取
            int start = (page - 1) * pageSize;
            List<PostIndex> postIndices = postIndexMapper.getPostIndexByUserId(userId, start, pageSize);

            // 将获取的数据存储到 Redis 中以备将来使用
            // 把帖子按照是否置顶+时间进行排序。所以这里我们就需要 是否置顶+创建时间
            Set<ZSetOperations.TypedTuple<Object>> tuples = new HashSet<>();
            for (PostIndex postIndex : postIndices) {
                double createTime = (double) postIndex.getCreateTime().getTime();
                createTime = postIndex.getFlag()+createTime;
                DefaultTypedTuple<Object> typedTuple = new DefaultTypedTuple<>(postIndex, createTime);
                tuples.add(typedTuple);
            }

            //循环查询点赞数
            for (PostIndex postIndex : postIndices){
                Counter counter = new Counter();
                counter.setObjId(postIndex.getId());
                counter.setObjType(2);
                counter.setCountKey("like");
                Result<Counter> result = blurCounterClient.getCounter(counter);
                if (result.getData().getCountValue() != null) {
                    postIndex.getPostStatistics().setLikeCount(result.getData().getCountValue());
                } else {
                    postIndex.getPostStatistics().setLikeCount(0);
                }
            }

            // 设置 Redis 缓存的过期时间（例如，1小时）
            redisTemplate.opsForZSet().add(redisKey, tuples);
            redisTemplate.expire(redisKey, 1, TimeUnit.HOURS);
            if (hotKey){
                JdHotKeyStore.smartSet(redisKey+page+pageSize , postIndices);
            }
            return postIndices;
        }
    }



    /**
     * 分页查询count计算
     * @param userId
     * @return
     */
    @Override
    public int getPostIndexCountByUserId(int userId) {
        return postIndexMapper.getPostIndexCountByUserId(userId);
    }

    /**
     * 判断用户是否给一个帖子点赞过
     * 抖音刷视频可以这么做，当然抖音也是一口气返回10个，20个视频，进行分页查询也不是一个一个查询
     * @param userId
     * @param postId
     * @return
     */
    @Override
    public boolean isPostLikedByUser(int userId, int postId, int likeCount) {
        //查询user点赞列表
        String cacheKey = "userPosts:" + userId;
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();

        // 查询ttl和minCid的值
        Long ttl = (long)hashOperations.get(cacheKey, "ttl");
        String minCid = (String) hashOperations.get(cacheKey, "minCid");
        String cachedPostId = (String) hashOperations.get(cacheKey, String.valueOf(postId));
        //判断ttl是否小于一个小时，如果小于一个小时就更新时间戳
        if (ttl != null && ttl > 0 && System.currentTimeMillis() - ttl < 1 * 60 * 60 * 1000){
            // 更新时间戳
            hashOperations.put(cacheKey,
                    "ttl", System.currentTimeMillis()+ 7 * 24 * 60 * 60);
        }

        //对于ttl没有的话需要保存到缓存中一份。
        if (ttl==null){
            List<PostLike> postLikes = postLikeMapper.selectByUserId(userId , 1000); // 替换为从数据库中查询帖子id的方法
            // 将查询到的数据存入缓存
            Map<String, Object> cacheData = new HashMap<>();
            for (PostLike postLike : postLikes) {
                cacheData.put(String.valueOf(postLike.getTid()), postLike.getCreateTime());
            }
            minCid = postLikes.get(postLikes.size()-1).toString();
            cacheData.put("minCid" , minCid);
            long currentTime = System.currentTimeMillis();
            cacheData.put("ttl", currentTime + 7 * 24 * 60 * 60);
            //存入缓存当中
            hashOperations.putAll(cacheKey, cacheData);
            // 设置过期时间（单位：秒）
            redisTemplate.expire(cacheKey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
        }


        //如果大于minCid，并且在缓存中没有这一了
        if (postId>Integer.parseInt(minCid) && cachedPostId==null){
            //说明没点赞过，直接返回
            return false;
        }

        //说明帖子是老数据需要查询数据库
        int count = postLikeMapper.countByUserIdAndPostId(userId, postId);
        if (count<=0){
            //说明没点赞过
            return false;
        }

        return true;
    }

    /**
     * 小红书，微博等批量查询批量判断，使用这个方法
     * @param userId
     * @param postId
     * @param likeCount
     * @return
     */
    @Override
    public List<Integer> isPostLikedByUser(int userId, List<Object> postId, int likeCount) {
        String cacheKey = "userPosts:" + userId;
        if (postId!=null){
            postId.add(0 ,"ttl");
            postId.add(1, "minCid");
        }
        HashOperations<String, Object, Object> hashOperations = redisTemplate.opsForHash();
        List<Object> hashValues = hashOperations.multiGet(cacheKey, postId);
        if (hashValues!=null){
            //说明value不是空，那就判断ttl是否小于一个小时
            long ttl = (long)hashValues.get(0);
            if (System.currentTimeMillis() - ttl < 1 * 60 * 60 * 1000){
                // 设置过期时间（单位：秒）
                redisTemplate.expire(cacheKey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);
                // 更新时间戳
                hashOperations.put(cacheKey,
                        "ttl", System.currentTimeMillis()+ 7 * 24 * 60 * 60);
            }
            //获取minCid
            int minCid = (int) hashValues.get(1);
            //定义一个值来获取看是否都小于mincid
            List<Integer> surplus = new ArrayList<>();
            for (int i = 2; i < hashValues.size(); i++) {
                int postIdValue = (int) hashValues.get(i);
                if (postIdValue < minCid ) {
                    // 说明帖子是老数据
                    surplus.add(postIdValue);
                }
            }
            //判断surplus中是否还有数据
            if (surplus.size()==0){
                //说明全部都是新数据且都没点赞过
                return surplus;
            }
            //反之就是有老数据，那就查询数据库
            surplus = postLikeMapper.selectPostIdsByUserIdAndPostIds(userId , surplus);
            if (surplus==null || surplus.size()<=0){
                //说明都没点赞过
                return new ArrayList<>();
            }
            //反之返回surplus
            return surplus;

        }
        //如果为空就需要查询数据库并添加到redis hash中
        List<PostLike> postLikes = postLikeMapper.selectByUserId(userId , 1000); // 替换为从数据库中查询帖子id的方法
        // 将查询到的数据存入缓存
        Map<String, Object> cacheData = new HashMap<>();
        for (PostLike postLike : postLikes) {
            cacheData.put(String.valueOf(postLike.getTid()), postLike.getCreateTime());
        }
        cacheData.put("minCid" , postLikes.get(postLikes.size()-1).toString());
        long currentTime = System.currentTimeMillis();
        cacheData.put("ttl", currentTime + 7 * 24 * 60 * 60);
        //存入缓存当中
        hashOperations.putAll(cacheKey, cacheData);
        // 设置过期时间（单位：秒）
        redisTemplate.expire(cacheKey, 7 * 24 * 60 * 60, TimeUnit.SECONDS);

        //循环判断
        List<Integer> databasePostIds =
                postLikes.stream().map(PostLike::getTid).collect(Collectors.toList());

        // 比较postId和缓存中的帖子ID列表
        postId.remove(1);
        postId.remove(0 );
        postId.removeAll(databasePostIds);
        //删除之后判断postId中是否还有数据，如果有数据就是老数据
        if (postId==null || postId.size()<=0){
            return new ArrayList<>();
        }

        //转换成int集合
        List<Integer> postIdIntegers = postId.stream()
                .map(object -> Integer.parseInt(object.toString()))
                .collect(Collectors.toList());

        return postIdIntegers;
    }




}

