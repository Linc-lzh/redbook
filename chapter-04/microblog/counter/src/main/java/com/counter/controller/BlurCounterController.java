package com.counter.controller;

import com.common.result.Result;
import com.counter.model.Counter;
import com.counter.service.BlurCounterService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 模糊计数接口
 */
@RestController
@RequestMapping("/blur/counter")
public class BlurCounterController {

    @Resource
    private BlurCounterService blurCounterService;

    @GetMapping("/get")
    public Result<Counter> getCounter(Counter counter) {
        return blurCounterService.getCounter(counter.getObjId(), counter.getObjType(), counter.getCountKey());
    }

    @PostMapping("/set")
    public Result<Counter> setCounter(Counter counter) {
        return blurCounterService.setCounter(counter.getObjId(), counter.getObjType(), counter.getCountKey(), counter.getCountValue());
    }

    @PostMapping("/gets")
    public Result<List<Counter>> getCounters(@RequestBody Counter counter) {
        return blurCounterService.getCounters(counter.getObjId(), counter.getObjType(), counter.getKeys());
    }

    @PostMapping("/sets")
    public Result<Counter> setCounters(Counter counter) {
        return blurCounterService.setCounters(counter.getObjId(), counter.getObjType(), counter.getKv());
    }
}
