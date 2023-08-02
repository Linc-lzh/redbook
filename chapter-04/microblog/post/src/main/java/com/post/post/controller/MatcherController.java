package com.post.post.controller;


import com.post.post.ac.AhoCorasickAutomata;
import com.post.post.ac.SensitiveWordMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class MatcherController {

    @Resource
    private SensitiveWordMatcher sensitiveWordMatcher;

    @RequestMapping("/matcher")
    public List<String> matcher(String text){
        return sensitiveWordMatcher.match(text);
    }
}
