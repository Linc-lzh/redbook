package com.post.post.service;

import java.util.List;

public interface SensitiveWordService {

    /**
     * 返回敏感词匹配
     * @param text
     * @return
     */
    List<String> match(String text);

}
