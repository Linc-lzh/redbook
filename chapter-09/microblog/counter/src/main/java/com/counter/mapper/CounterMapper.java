package com.counter.mapper;

import com.counter.model.Counter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 查询counter数据库
 */
@Mapper
public interface CounterMapper {

    //单个查询数据库
    Integer getCounter(@Param("objId") Integer objId,@Param("objType") Integer objType, @Param("key") String key);

    void setCounter(@Param("objId") Integer objId,@Param("objType") Integer objType, @Param("key") String key , @Param("value") Integer value);

    //批量查询数据库
    List<Counter> getCounters(@Param("objId") Integer objId, @Param("objType") Integer objType, @Param("keys") List<String> keys);

    List<Counter> setCounters(@Param("objType") Integer objType,@Param("list") List<Counter> list);

}
