package com.post.post.mapper;

import com.post.post.model.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface SensitiveWordMapper {

    void insertSensitiveWord(SensitiveWord sensitiveWord);

    List<SensitiveWord> getAllSensitiveWords();

}
