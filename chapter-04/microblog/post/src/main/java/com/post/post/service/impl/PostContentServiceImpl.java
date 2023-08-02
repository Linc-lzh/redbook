package com.post.post.service.impl;

import com.common.result.Result;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.post.post.feign.BlurCounterClient;
import com.post.post.mapper.PostContentMapper;
import com.post.post.model.Counter;
import com.post.post.model.PostContent;
import com.post.post.model.PostStatistics;
import com.post.post.service.PostContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class PostContentServiceImpl implements PostContentService {

    @Resource
    private PostContentMapper postContentMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private BlurCounterClient blurCounterClient;

    /**
     * 获取帖子具体内容
     * @param id
     * @return
     */
    @Override
    public PostContent getPostContentById(int id) {
        // 为 Redis 缓存生成唯一键
        String redisKey = "postId:" + id;
        //先判断是否是hotkey
        boolean hotKey = JdHotKeyStore.isHotKey(redisKey);
        if (hotKey) {
            // 通过 JdHotKeyStore.getValue 获取数据并返回
            PostContent postContent = (PostContent)JdHotKeyStore.getValue(redisKey);
            if (postContent!=null){
                return postContent;
            }
        }
        // 从 Redis 中获取数据
        ValueOperations<String, PostContent> valueOperations = redisTemplate.opsForValue();
        PostContent postContent = valueOperations.get(redisKey);
        if (postContent == null) {
            // Redis 中没有数据，从数据库中获取数据
            postContent = postContentMapper.findById(id);
        }
        if (postContent != null) {
            if (postContent.getPostStatistics()==null){
                //从计数服务中获取计数信息
                PostStatistics counter = getCounter(id);
                postContent.setPostStatistics(counter);
            }
            // 将数据存入 Redis 缓存
            valueOperations.set(redisKey, postContent);
            if (hotKey){
                JdHotKeyStore.smartSet(redisKey , postContent);
            }
        }
        return postContent;
    }

    /**
     * 获取各种计数信息
     * @return
     */
    private PostStatistics getCounter(int id){
        PostStatistics postStatistics = new PostStatistics();
        Counter counter = new Counter();
        counter.setObjId(id);
        counter.setObjType(2);
        List<String> keys = new ArrayList();
        keys.add("like");
        keys.add("view");
        keys.add("comment");
        counter.setKeys(keys);
        Result<List<Counter>> counters = blurCounterClient.getCounters(counter);
        for (Counter value : counters.getData()){
            if (value.getCountKey().equals("like")){
                postStatistics.setLikeCount(value.getCountValue());
            }
            if (value.getCountKey().equals("view")){
                postStatistics.setViewCount(value.getCountValue());
            }
            if (value.getCountKey().equals("comment")){
                postStatistics.setCommentCount(value.getCountValue());
            }
        }
        return postStatistics;
    }

    @Override
    public void createPostContent(PostContent postContent) {
        postContentMapper.createPostContent(postContent);
    }

    @Override
    public void updatePostContent(PostContent postContent) {
        postContentMapper.updatePostContent(postContent);
    }

    @Override
    public void deletePostContent(int id) {
        postContentMapper.deletePostContent(id);
    }

    // 其他自定义的业务方法的实现...
}

