package com.counter.service;

import com.common.result.Result;
import com.counter.model.Counter;

import java.util.List;
import java.util.Map;

/**
 * 计数接口用来实现计数的
 */
public interface BlurCounterService {

    /**
     * 获取单个计数
     * @param objId
     * @param key
     * @return
     */
    Result<Counter> getCounter(Integer objId , Integer objType , String key);

    /**
     * 写入单个计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    Result<Counter> setCounter(Integer objId , Integer objType , String key , Integer value);

    /**
     * 批量获取counter
     * @param objId
     * @param keys
     * @return
     */
    Result<List<Counter>> getCounters(Integer objId , Integer objType , List<String> keys);

    /**
     * 批量写入数据
     * @param objId
     * @param kv
     * @return
     */
    Result<Counter> setCounters(Integer objId , Integer objType , Map<String , Integer> kv);

    /**
     * 写入数据库
     * @param objId
     * @param key
     * @param value
     * @return
     */
    Result<Counter> setCounterDB(Integer objId , String key , Integer value);
}
