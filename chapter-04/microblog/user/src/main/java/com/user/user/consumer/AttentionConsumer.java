package com.user.user.consumer;

import com.alibaba.fastjson.JSON;
import com.user.user.mapper.UserMapper;
import com.user.user.model.Attention;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Component
public class AttentionConsumer {

    @Resource
    private RedisTemplate redisTemplate;

    private DefaultRedisScript<Long> script;

    @Resource
    private UserMapper userMapper;

    @PostConstruct
    public void init(){
        script = new DefaultRedisScript<Long>();
        //返回值为Long
        script.setResultType(Long.class);
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/Attentions.lua")));
    }

    @KafkaListener(topics = "attention-topic")
    public void onMessage(ConsumerRecord<String,String> record){
        List<String> value = JSON.parseArray(record.value(), String.class);

        List<String> keys = Arrays.asList(value.get(0));
        List<String> args = Arrays.asList(value.get(1));
        int index = keys.get(0).indexOf(":");
        String result = keys.get(0).substring(index + 1);

        //说明是点关注
        if (value.get(2).equals("1")){
            redisTemplate.execute(script,keys,args);
            //判断数据库中是否存在
            Attention attention = userMapper.getAttention(Integer.valueOf(result), Integer.valueOf(value.get(1)));
            if (attention!=null){
                //修改
                userMapper.delAttention(Integer.valueOf(result) , Integer.valueOf(value.get(1)) , 0);
            }else{
                //新增
                userMapper.setAttention(Integer.valueOf(result) , Integer.valueOf(value.get(1)));
            }
        }else{
            //说明是取关
            //不管redis有没有直接在redis中删除
            redisTemplate.boundListOps(value.get(0)).remove(0 , value.get(1));
            //从数据库中删除
            userMapper.delAttention(Integer.valueOf(result) , Integer.valueOf(value.get(1)) , 1);
        }

    }

}
