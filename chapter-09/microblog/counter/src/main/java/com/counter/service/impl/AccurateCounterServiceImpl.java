package com.counter.service.impl;

import com.common.result.Result;
import com.common.utils.NullOrZeroUtils;
import com.counter.mapper.CounterMapper;
import com.counter.model.Counter;
import com.counter.service.AccurateCounterService;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 精准计数
 * 精准计数实现起来就非常简单了，因为精准计数都是业务表中count计算，计数这边只需要在redis当中存储count就行了
 */
@Service
public class AccurateCounterServiceImpl implements AccurateCounterService {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CounterMapper counterMapper;

    //热数据缓存过期时间
    private final Long CACHE_EXPIRE_TIME = 12L;

    //缓存前缀
    private final String COUNT_PREFIX = "accurate_counter:";

    @Override
    public Result<Counter> getCounter(Integer objId, Integer objType , String key) {
        Integer counterValue = (Integer)redisTemplate.opsForValue().get(COUNT_PREFIX + key +":"+objType+":" + objId);
        if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)){
            //如果不为空直接返回就ok了
            return new Result<>(200 , "success" , new Counter(objId , key , counterValue));
        }
        counterValue = counterMapper.getCounter(objId, objType, key);
        if (!NullOrZeroUtils.isNullOrEmptyOrZero(counterValue)){
            //如果是热点数据，就保存到redis当中
            if(JdHotKeyStore.isHotKey("counter:"+key + ":" + objId)){
                redisTemplate.opsForValue().set(COUNT_PREFIX + key +":"+objType+":" + objId , counterValue);
            }
            //如果不为空直接返回就ok了
            return new Result<>(200 , "success" , new Counter(objId , key , counterValue));
        }
        //如果不为空直接返回就ok了
        return new Result<>(500 , "获取失败" , null);
    }

    /**
     * 修改缓存
     * @param objId
     * @param key
     * @param value
     * @return
     */
    @Override
    public Result<Counter> setCounter(Integer objId, Integer objType , String key, Integer value) {
        try {
            redisTemplate.opsForValue().set(COUNT_PREFIX + key +":"+objType+":" + objId , value);
            return new Result<>(200 , "success" , null);
        }catch (Exception e){
            return new Result<>(500 , "删除" , null);
        }

    }

    /**
     * 删除缓存
     * @param objId
     * @param key
     * @return
     */
    @Override
    public Result<Counter> delCounter(Integer objId , Integer objType , String key) {
        try {
            redisTemplate.delete(COUNT_PREFIX + key +":"+objType+":" + objId);
            return new Result<>(200 , "success" , null);
        }catch (Exception e){
            return new Result<>(500 , "删除" , null);
        }
    }
}
