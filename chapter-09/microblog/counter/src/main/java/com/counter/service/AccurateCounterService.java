package com.counter.service;

import com.common.result.Result;
import com.counter.model.Counter;

/**
 * 精准计数实现
 */
public interface AccurateCounterService {

    /**
     * 获取计数
     * @param objId
     * @param key
     * @return
     */
    Result<Counter> getCounter(Integer objId , Integer objType , String key);

    /**
     * 写入计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    Result<Counter> setCounter(Integer objId, Integer objType  , String key , Integer value);

    /**
     * 删除计数
     * @param objId
     * @param key
     * @return
     */
    Result<Counter> delCounter(Integer objId, Integer objType  , String key);
}
