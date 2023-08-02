package com.user.user.feign;

import com.common.result.Result;
import com.user.user.model.Counter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient("counter-client")
public interface BlurCounterClient {

    /**
     * 获取单个计数
     * @param objId
     * @param key
     * @return
     */
    @GetMapping("/blur/counter/get")
    Result<Counter> getCounter(Counter counter);

    /**
     * 写入单个计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    @PostMapping("/blur/counter/set")
    Result<Counter> setCounter(Counter counter);

    /**
     * 批量获取counter
     * @param objId
     * @param keys
     * @return
     */
    @PostMapping("/blur/counter/gets")
    Result<List<Counter>> getCounters(@RequestBody Counter counter);

    /**
     * 批量写入数据
     * @param objId
     * @param kv
     * @return
     */
    @PostMapping("/blur/counter/sets")
    Result<Counter> setCounters(Counter counter);

}
