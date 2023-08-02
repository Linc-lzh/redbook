package com.wb.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.phantomthief.collection.BufferTrigger;
import com.wb.entity.Attention;
import com.wb.entity.Follower;
import com.wb.entity.Re;
import com.wb.entity.User;
import com.wb.mapper.UserMapper;
import com.wb.service.RedisService;
import com.wb.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisService redisService;

    private Map<String, User> userMap = new HashMap<>();

    //热点数据列表
    private Map<String, User> map = new HashMap<>();

    //操作redis
    private RedisTemplate redisTemplate;




    @Override
    public Re<User> getUser(int id) {
        //有一个热点数据的列表

        //这里首先需要考虑一个热点问题，所以需要二级缓存，当然如果不考虑到热点问题是不需要二级缓存的
        //这里先直接用map来代替
        //本地缓存这一块，热点数据
        User user;
        user = userMap.get("user:" + id);
        if (user!=null){
            return Re.ok(user);
        }

        //通过缓存获取user信息
        String userJson = redisService.get("user:" + id);
        if (!userJson.equals("") || userJson != null){
            user = JSON.parseObject(userJson , User.class);
            return Re.ok(user);
        }

        //如果说你想为了修改比较简单的话，那这里可以建议把attationcount和followercount
        //单独的存储到redis当中进行查询和修改

        //查询数据库
        user = userMapper.getById(id);
        //需要设置一个过期时间
        redisService.set("user:"+id, JSON.toJSONString(user));
        return Re.ok(user);
    }

    @Override
    public Re<String> update(User user) {
        //点关注需要发送一个消息到mq中，进行实现
        return null;
    }

    @Override
    public Re<List<User>> getFollower(int userId,int start) {
        //从缓存中获取粉丝列表
        List<String> followers = redisTemplate.opsForList().range("follower:" + userId, start, start+6);
        List<User> users = null;
        //从缓存中获取用户信息
        if (followers != null){
            //这里需要mget获取用户信息，不过mget有缺点，这里课程里面会讲到
            users = redisTemplate.opsForValue().multiGet(followers);
        }
        if (users!=null){
            return Re.ok(users);
        }
        return Re.error();
    }

    @Override
    public Re<List<User>> getAttention(int userId,int start) {
        //从缓存中获取粉丝列表
        List<String> followers = redisTemplate.opsForList().range("attention:" + userId, start, start+6);
        List<User> users = null;
        //从缓存中获取用户信息
        if (followers != null){
            //这里需要mget获取用户信息，不过mget有缺点，这里课程里面会讲到
            users = redisTemplate.opsForValue().multiGet(followers);
        }
        if (users!=null){
            return Re.ok(users);
        }
        return Re.error();
    }

    @Override
    public Re<String> insertAttention(Attention attention) {
        //直接从前端获取到用户信息然后进行修改
        //理论上这一步是放在MQ当中，这里为了方便演示就直接修改了
        int i = userMapper.insertAttention(attention);
        //进行修改user后面的attention数据
        userMapper.updateUserAttentionCount(attention.getUserId());
        return null;
    }

    /**
     * 用户点关注的方法
     * @param follower
     * @return
     */
    @Override
    public Re<String> insertFollower(Follower follower) {
        //获取user信息从redis当中
        String userJson = redisService.get("user:" + follower.getUserId());
        User user = null;
        if (!userJson.equals("") || userJson != null){
            user = JSON.parseObject(userJson , User.class);
        }
        if (user!=null){
            user.setFollowerCount(user.getFollowerCount()+1);
        }
        //可以使用缓冲区，或者说，我们可以使用快手开源的一个产品啊
        //关注到一定数量，一起进行修改，或者一定时间直接去修改了

        //需要设置一个过期时间
        redisService.set("user:"+follower.getUserId(), JSON.toJSONString(user));

        //后面会给大家去讲到，我们粉丝列表是需要进行限制

        //同时，在Redis当中，我们需要进行修改Redis的粉丝列表
        Long size = redisTemplate.opsForList().size("followers:" + follower.getUserId());

        //如果规定要是比较强的话，实际上是一个原子操作
        //从最左边加入元素
        redisTemplate.opsForList().leftPush("followers:" + follower.getUserId(), user.getId());
        redisTemplate.opsForList().leftPush("attentions:" + follower.getFollowerId(), follower.getUserId());
        if (size>=200){
            //删除最右边的元素
            //我们为什么要删除最右边的元素呢？
            redisTemplate.opsForList().rightPop("followers:" + follower.getUserId());
        }
        //就需要使用到lua脚本进行原子操作
        //list列表，还是会被删除掉，所以说这里的话，我们就不太需要原子操作


        //理论上这一步是放在MQ当中，这里为了方便演示就直接修改了
        //follower表当中添加关系记录
        userMapper.insertFollower(follower);
        //进行修改user后面的follower数据，这里可以做一个优化就是使用缓冲区
        // 但是这里先不给大家讲，因为后面讲计数服务的时候，会给大家讲到缓冲区
        //修改粉丝数

        userMapper.updateUserFollowerCount(follower.getUserId());
        return null;
    }

    @Override
    public Re<String> deleteFollower(Follower follower) {

        //直接从Redis中进行删除，不管是否存在Redis当中
        //从当前用户关注列表删除
        redisTemplate.opsForList().remove("attentions:" + follower.getFollowerId() , 1 , follower.getUserId());

        //从粉丝列表里面删除，这步也可以异步来进行操作
        redisTemplate.opsForList().remove("followers:" + follower.getUserId() , 1 , follower.getFollowerId());
        //异步进行操作
        userMapper.insertFollower(follower);
        //异步进行操作
        userMapper.updateUserFollowerCount(follower.getUserId());
        return null;
    }


}
