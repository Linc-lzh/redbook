package com.post.server.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "user-service")
public interface FollowersClient {

    @GetMapping("/followers/all/{userId}")
    List<Integer> getAllFollowers(@PathVariable("userId") int userId);
}
