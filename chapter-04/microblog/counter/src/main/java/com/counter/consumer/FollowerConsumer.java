package com.counter.consumer;

import com.alibaba.fastjson.JSON;
import com.counter.model.Counter;
import com.counter.service.BlurCounterService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FollowerConsumer {

    @Resource
    private BlurCounterService blurCounterService;

    @KafkaListener(topics = "follower-count-topic")
    public void onMessage(ConsumerRecord<String,String> record){
        Counter counter = JSON.parseObject(record.value() , Counter.class);
        //set数据
        blurCounterService.setCounter(
                counter.getObjId(), counter.getObjType(),
                counter.getCountKey() , counter.getCountValue());
    }

}
