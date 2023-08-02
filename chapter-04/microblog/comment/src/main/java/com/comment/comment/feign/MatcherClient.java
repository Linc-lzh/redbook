package com.comment.comment.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("post-client")
public interface MatcherClient {

    @GetMapping("/matcher")
    List<String> matcher(String text);
}
