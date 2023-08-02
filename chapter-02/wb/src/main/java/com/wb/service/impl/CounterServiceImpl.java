package com.wb.service.impl;

import com.github.phantomthief.collection.BufferTrigger;
import com.wb.entity.Counter;
import com.wb.entity.Praise;
import com.wb.entity.Re;
import com.wb.mapper.CounterMapper;
import com.wb.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CounterServiceImpl implements CounterService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CounterMapper counterMapper;

    private Map<String , Integer> countMap = new HashMap<>();

    BufferTrigger<Map<String , String>> bufferTrigger = BufferTrigger.<Map<String , String>>batchBlocking()
            .bufferSize(50)
            .batchSize(10)
            .linger(Duration.ofSeconds(1))
            //.setConsumerEx(this::consume)
            .build();


    /**
     * 更新计数在Redis当中进行更新
     * @param id
     * @return
     */
    @Override
    public int updateReadCount(int id) {
        //字符串形式来进行存储，你也可以使用hash来进行存储
        //Redis 使用kv进行存储 key：counters:xx  value：0:0:0:0
        String str = (String)redisTemplate.opsForValue().get("counters:" + id);

        String[] split = str.split(":");
        Integer readCount = Integer.parseInt(split[0]);
        readCount = readCount+1;
        str = "";
        for (int i=0; i<split.length ;i++){
            if(i==0){
                str= str+readCount;
            }else{
                str = str+","+split[i];
            }
        }

        redisTemplate.opsForValue().set("counters:" + id ,str);
        //这里缓冲区其实就是一个带过期时间的队列
        //先判断一下map当中是否有当前数据
        //bufferTrigger.enqueue(hashmap);
        return 0;
    }

    /**
     * 更新计数在Redis当中进行更新
     * @param id
     * @param type
     * @return
     */
    @Override
    public int updatePraiseCount(int id, int type) {
        //说明是要点赞的
        if (type==1){

        }else{
            //取消点赞
        }
        return 0;
    }

    @Override
    public int updateTransmitCount(int id, int type) {
        return 0;
    }

    @Override
    public int updateCommentCount(int id, int type) {
        return 0;
    }

    @Override
    public int insert(Counter counter) {
        return 0;
    }

    /**
     * 查询帖子的各种计数信息
     * @param id
     * @return
     */
    @Override
    public Re<Counter> getById(int id) {
        //可以使用redis的hash进行存储，也可以使用字符串进行存储，这里我就使用字符串进行存储了
        String str = (String)redisTemplate.opsForValue().get("counters:" + id);
        //key：counters:xx  value：0:0:0:0
        if(!str.equals("") && str!=null){
            String[] split = str.split(":");
            Counter counter = new Counter();
            counter.setPostId(id);
            counter.setReadCount(Integer.parseInt(split[0]));
            counter.setPraiseCount(Integer.parseInt(split[1]));
            counter.setTransmitCount(Integer.parseInt(split[2]));
            counter.setCommentCount(Integer.parseInt(split[3]));
            return Re.ok(counter);
        }
        return Re.error("查询失败");
        //往缓冲区中写入数据，异步进行写入mysql

    }

    /**
     * 批量查询帖子各种计数
     * @param ids
     * @return
     */
    @Override
    public List<Counter> gets(List<Integer> ids) {
        //查询帖子计数，从redis进行查询
        List<String> list = redisTemplate.opsForValue().multiGet(ids);
        List<Counter> counters = new ArrayList<>();
        int i=0;
        for (String s:list){

            String[] counts = s.split(":");
            Counter counter = new Counter();
            counter.setPostId(ids.get(i));

            for (int n=0; n<counts.length ;n++){
                if (n==0){
                    counter.setReadCount(Integer.parseInt(counts[n]));
                }
                if (n==1){
                    counter.setPraiseCount(Integer.parseInt(counts[n]));
                }
                if (n==2){
                    counter.setTransmitCount(Integer.parseInt(counts[n]));
                }
                if (n==3){
                    counter.setCommentCount(Integer.parseInt(counts[n]));
                }
            }
            counters.add(counter);
            i++;
        }
        return counters;
    }

    /**
     * 查看点赞详情
     * @param cids
     * @param uid
     * @return
     */
    @Override
    public Re<HashMap<Long, Integer>> getPraises(List<Object> cids, int uid) {
        HashMap<Long , Integer> returnMap = new HashMap<>();
        //这里通过前端传入也行，那object就可以改成String类型，这里怕大家忘记给大家去添加一下
        //一次批量查询四条动态
        cids.add("ttl");
        cids.add("mincid");

        //使用hmget查看数据
        List<Long> praises = redisTemplate.opsForHash().multiGet("praise:" + uid, cids);

        //判断缓存是否存在
        if (praises!=null && praises.size()>0){
            //说明缓存存在
            //判断缓存是否需要更新
            Long ttl = praises.get(4);

            //获取当前时间戳
            Long now = System.currentTimeMillis();

            //这里假设我们设置的是6个小时，算时间戳，小于6小时三分之一就是小于2小时
            if ((ttl-now)/3 < 3600000*2){
                //更新时间
                redisTemplate.expire("praise:" + uid , 6 , TimeUnit.HOURS);
                ttl = ttl - (ttl-now) + 3600000*6;
                //更新hash中ttl的过期时间戳
                redisTemplate.opsForHash().put("praise:" + uid , "ttl" , ttl);
            }

            //循环判断cid是否都在结果集当中，-2是因为ttl和mincid不需要进行判断
            for (int i=0 ; i<praises.size()-2 ; i++){
                //如果不=1就判断是否小于mincid
                if (praises.get(i)!=1){

                    //如果大于说明没点赞过
                    if (Long.parseLong((String) cids.get(i))> praises.get(praises.size()-1)){
                        //没点赞过
                        returnMap.put((Long) cids.get(i), 0);
                    }else{
                        //如果小于mincid，就说明是非常老的数据了，就到数据库中进行查询
                        Praise praise = counterMapper.getPraise((Integer) cids.get(i));
                        //说明是历史点赞过的数据
                        if (praise!=null){
                            returnMap.put((Long) cids.get(i), 1);
                        }else{
                            //反之就没点赞过
                            returnMap.put((Long) cids.get(i), 0);
                        }
                    }
                }else{
                    //说明点过赞
                    returnMap.put((Long) cids.get(i), 1);
                }
            }

        }else{
            //如果缓存不存在，需要查询一定时间一定数量的点赞用户信息，时间
            //根据一定规则，这里我直接就查询最近1000条点赞数据，因为一般我刷这种app的时候，平均一天点赞10条左右
            List<String> getPraises = counterMapper.getPraises(uid);
            getPraises.add("ttl");
            getPraises.add("mincid");
            Long now = System.currentTimeMillis();
            Map<String , Long> setMap = new HashMap<>();
            for (String str : getPraises){
                setMap.put(str , 1l);
                if (str.equals("ttl")){
                    setMap.put(str , now+3600000*6);
                }
                if (str.equals("mincid")){
                    setMap.put(str , Long.parseLong(getPraises.get(0)));
                }
            }
            //两步是原子操作，需要注意一下
            redisTemplate.opsForHash().putAll("praise:" + uid , setMap);
            redisTemplate.expire("praise:" + uid , 6 , TimeUnit.HOURS);
        }
        return null;
    }


}
