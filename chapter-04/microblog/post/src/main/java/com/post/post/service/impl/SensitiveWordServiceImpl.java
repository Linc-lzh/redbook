package com.post.post.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.post.post.ac.SensitiveWordMatcher;
import com.post.post.mapper.SensitiveWordMapper;
import com.post.post.model.SensitiveWord;
import com.post.post.service.SensitiveWordService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    @Resource
    SensitiveWordMatcher sensitiveWordMatcher;

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    //初始化方法
    @PostConstruct
    public void init() {
        //初始化把所有的数据加载到ac自动机中
        List<SensitiveWord> allSensitiveWords = sensitiveWordMapper.getAllSensitiveWords();
        //加载出所有的组合敏感词，添加到ac自动机中
        for (SensitiveWord word : allSensitiveWords){

            JSONArray jsonArray = JSONArray.parseArray(word.getWord());

            // 转换为字符串数组
            String[] stringArray = jsonArray.toArray(new String[0]);
            sensitiveWordMatcher.
                    addSensitiveWords(word.getCategory()+":"+word.getId() , stringArray);
        }
        //初始化完成
        sensitiveWordMatcher.build();
    }

    @Override
    public List<String> match(String text) {
        //如果为空说明没有数据
        return sensitiveWordMatcher.match(text);
    }
}
