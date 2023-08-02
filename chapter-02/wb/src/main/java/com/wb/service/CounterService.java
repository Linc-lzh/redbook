package com.wb.service;

import com.wb.entity.Counter;
import com.wb.entity.Re;

import java.util.HashMap;
import java.util.List;

public interface CounterService {

    int updateReadCount(int id);

    int updatePraiseCount(int id , int type);

    int updateTransmitCount(int id , int type);

    int updateCommentCount(int id , int type);

    int insert(Counter counter);

    Re<Counter> getById(int id);

    List<Counter> gets(List<Integer> ids);

    Re<HashMap<Long , Integer>> getPraises(List<Object> cids , int uid);

}
