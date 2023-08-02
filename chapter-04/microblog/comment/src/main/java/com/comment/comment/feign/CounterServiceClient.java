package com.comment.comment.feign;

import com.comment.comment.model.Counter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "counter-service")
public interface CounterServiceClient {

    @GetMapping("/counter/{objId}")
    Counter getById(@PathVariable("objId") Integer objId);

}
