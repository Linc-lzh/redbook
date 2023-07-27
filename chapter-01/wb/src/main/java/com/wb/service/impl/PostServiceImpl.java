package com.wb.service.impl;

import com.alibaba.fastjson.JSON;
import com.wb.entity.Counter;
import com.wb.entity.PostIndex;
import com.wb.entity.Re;
import com.wb.mapper.CounterMapper;
import com.wb.mapper.PostMapper;
import com.wb.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;

public class PostServiceImpl implements PostService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private CounterMapper counterMapper;


    /**
     * 查询用户帖子列表
     * @param uid
     * @return
     */
    @Override
    public List<PostIndex> userPosts(int uid,int start,int stop) {
        //先分页查询redis，如果redis有直接返回
        List<String> posts = redisTemplate.opsForList().range("posts:" + uid, start, stop);
        List<String> ids = new ArrayList<>();
        List<PostIndex> postIndexList = new ArrayList<>();
        //然后循环查询拼装
        for (int i=0 ; i<posts.size(); i++){
            String postStr = posts.get(i);
            if (postStr!=null && !postStr.equals("")){
                //把string转换成对象
                PostIndex post = JSON.parseObject(postStr, PostIndex.class);
                ids.add("counters:" + post.getId());
                postIndexList.add(post);
            }
        }
        //mget进行查询点赞信息
        List<String> counterStr = redisTemplate.opsForValue().multiGet(ids);
        if (counterStr!=null){
            for (int i=0 ; i<counterStr.size() ; i++){
                String str = counterStr.get(i);
                if (str!=null && str.equals("")){
                    //阅读数：0:点赞数：0:转发数：0:0
                    String[] split = str.split(":");
                    //获取第二个计数数据也就是点赞计数
                    PostIndex postIndex = postIndexList.get(i);
                    postIndex.setPraiseCount(Integer.parseInt(split[1]));
                }else{
                    //如果点赞计数为空就需要查询mysql了呗
                    //这里和上面一样，查询出来然后进行拼装就ok了。
                }
            }

        }else {
            //所有的点赞都未空的话，说明这个用户他可能是一个比较冷的用户对吧
            //直接批量查询mysql把这些点赞进行查询出来进行拼装就ok了
            //点赞我们再存入到redis当中呢？
            //我们会有热点探测
        }
        return postIndexList;
    }
}
