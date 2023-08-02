package com.counter.service.impl;

import com.common.result.Result;
import com.common.utils.NullOrZeroUtils;
import com.counter.mapper.CounterMapper;
import com.counter.model.Counter;
import com.counter.service.BlurCounterService;
import com.counter.utils.RedisLock;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.jd.platform.hotkey.client.ClientStarter;
import com.jd.platform.hotkey.client.Context;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.jd.platform.hotkey.client.core.rule.KeyRuleHolder;
import com.jd.platform.hotkey.client.etcd.EtcdConfigFactory;
import com.jd.platform.hotkey.common.configcenter.ConfigConstant;
import com.jd.platform.hotkey.common.configcenter.IConfigCenter;
import com.jd.platform.hotkey.common.rule.KeyRule;
import com.jd.platform.hotkey.common.tool.FastJsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 模糊计数
 */
@Service
public class BlurCounterServiceImpl implements BlurCounterService {

    //冷数据添加到缓存过期时间
    private final Long DATABASE_EXPIRE_TIME = 2L;

    //热数据缓存过期时间
    private final Long CACHE_EXPIRE_TIME = 8L;

    //缓冲区阈值
    private final Integer BUFF_COUNT = 50;

    private final String COUNT_PREFIX = "counter_";

    @Resource
    private RedisLock lock;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CounterMapper counterMapper;


    //创建缓冲区,为了方便，使用Caffeine当缓冲区
    Cache<String, Object> cache = Caffeine.newBuilder()
            //设置写缓存后8秒钟过期
            .expireAfterWrite(2, TimeUnit.SECONDS)
            //设置缓存移除通知
            .removalListener((String key, Object value, RemovalCause cause) -> {
                //缓存移除，直接修改数据库
                String[] parts = key.split(":"); // 拆分成数组
                key = parts[0];
                Integer objType = Integer.parseInt(parts[1]);
                Integer objId = Integer.parseInt(parts[2]);
                //调用mapper
                counterMapper.setCounter(objId , objType , key , (Integer) value);

                //异步发送kafka
                // 发送消息到 Kafka 消息队列中
//                String message = key+":"+value;
//                kafkaTemplate.send("counter-topic", message);
            })
            //build方法可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build();

    @Value("${spring.application.name}")
    private String appName;

    @PostConstruct
    public void initHotkey() {
        ClientStarter.Builder builder = new ClientStarter.Builder();
        // 注意，setAppName很重要，它和dashboard中相关规则是关联的。
        ClientStarter starter = builder.setAppName(appName)
                .setEtcdServer("http://127.0.0.1:2379")
                .setCaffeineSize(10)
                .build();
        starter.startPipeline();
        // 添加以下代码
        IConfigCenter configCenter = EtcdConfigFactory.configCenter();
        String rules = configCenter.get(ConfigConstant.rulePath + Context.APP_NAME);
        List<KeyRule> ruleList = FastJsonUtils.toList(rules, KeyRule.class);
        KeyRuleHolder.putRules(ruleList);
    }

    //单个值查询，比如查询浏览量
    @Override
    public Result<Counter> getCounter(Integer objId , Integer objType, String key) {

        try {
            boolean hotKey = JdHotKeyStore.isHotKey("counter:"+key + ":"+objType+":" + objId);

            //查询缓存
            Integer countValue = (Integer)redisTemplate.opsForValue().get("counter:"+key + ":"+objType+":" + objId);
            if (!NullOrZeroUtils.isNullOrEmptyOrZero(countValue)) {
                //获取key过期时间
                Long expire = redisTemplate.opsForValue().getOperations().getExpire("counter:"+key + ":"+objType+":" + objId);
                //判断过期时间是否小于1小时
                if (expire<60*60){
                    //增加过期时间，不过这里QPS*2了，其实效率是比较低的了
                    redisTemplate.expire(key + ":" + objId , CACHE_EXPIRE_TIME , TimeUnit.HOURS);
                }
                //如果不为空直接返回就ok了
                return new Result<>(200 , "success" , new Counter(objId , key , countValue));
            }
            //如果countValue为空就说明缓存中是没有数据的，所以这里就需要去查询数据库
            //判断是否是热点数据，如果是热点数据，为了防止突发流量打到数据库，需要加一个分布式锁
            if (hotKey){
                if (lock.lock("lock:"+key + ":"+objType+":" + objId , "1")){
                    //查询数据库
                    countValue = counterMapper.getCounter(objId,objType, key);
                    redisTemplate.opsForValue().set("counter:"+key + ":"+objType+":" + objId , countValue , DATABASE_EXPIRE_TIME , TimeUnit.HOURS);
                }else{
                    //获取锁失败的，可以直接返回获取失败稍后再试，或者while循环让他访问数据库
                    //这里使用while获取数据
                    while (true){
                        countValue = (Integer)redisTemplate.opsForValue().get("counter:"+key + ":"+objType+":" + objId);
                        if (countValue!=null){
                            break;
                        }
                    }
                }
            }else{
                //查询数据库
                countValue = counterMapper.getCounter(objId, objType, key);
            }
            return new Result<>(200 , "success" , new Counter(objId , key , countValue));
        }catch (Exception e){

            return new Result<>(500 , "获取失败" , null);
        }finally {
            lock.unlock("lock:"+key + ":"+objType+":" + objId , "1");
        }
    }

    /**
     * 写入单个计数
     * @param objId
     * @param key
     * @param value
     * @return
     */
    @Override
    public Result<Counter> setCounter(Integer objId , Integer objType, String key, Integer value) {
        Boolean bool = redisTemplate.hasKey(COUNT_PREFIX + objType +":" + objId);
        if (bool){
            redisTemplate.opsForHash().increment(COUNT_PREFIX + objType +":" + objId, key, value);
            //这里需要一个缓冲区来更新到数据库当中

            //添加到缓冲区中，批量更新数据库
            putBuffer(objId , objType , key , value);
        }else{
            //添加到数据库当中
            counterMapper.setCounter(objId, objType , key , value);
        }
        return new Result<>(200 ,"success" , null);
    }

    /**
     * 添加缓冲区
     * @param objId
     * @param key
     * @param value
     */
    private void putBuffer(Integer objId , Integer objType, String key, Integer value){
        Integer countValue = (Integer)cache.getIfPresent(key + ":"+objType+":" + objId);
        //判断
        if (NullOrZeroUtils.isNullOrEmptyOrZero(countValue)){
            cache.put(key + ":"+objType+":" + objId , countValue+value);
            if (countValue+value>=BUFF_COUNT){
                cache.invalidate(key + ":"+objType+":" + objId);
            }
        }else{
            cache.put(key + ":"+objType+":" + objId , value);
        }
    }

    /**
     * 批量获取count
     * @param objId
     * @param keys
     * @return
     */
    @Override
    public Result<List<Counter>> getCounters(Integer objId , Integer objType, List<String> keys) {
        try {
            //添加ttl，查询的时候QPS少一倍
            keys.add(0 ,"ttl");
            List<Object> result = redisTemplate.opsForHash().multiGet(COUNT_PREFIX + objType +":" + objId, keys);
            System.out.println("result:"+result.toString());
            if (result.get(0)!=null){
                //说明缓存是存在的
                //判断ttl来判断是否需要去更新缓存
                System.out.println("ttl:"+result.get(0));
                if ((Long)result.get(0) - System.currentTimeMillis() < 60 * 60 * 1000){
                    //如果小于一个小时就更新时间
                    redisTemplate.expire(COUNT_PREFIX + objType +":" + objId , CACHE_EXPIRE_TIME , TimeUnit.HOURS);
                    //同时更新ttl
                    long ttl = addHoursToTimestamp(System.currentTimeMillis(), CACHE_EXPIRE_TIME);
                    redisTemplate.opsForHash().put(COUNT_PREFIX + objType +":" + objId, "ttl" , ttl);
                }

                return new Result<List<Counter>>(200 , "success" , resultCounters(result , objId , keys));
            }
            boolean hotKey = JdHotKeyStore.isHotKey(COUNT_PREFIX + objType +"_" + objId);
            System.out.println("是否是热key："+hotKey);
            if (hotKey){
                System.out.println("执行到这里了！！！！");
                if (lock.lock("lock:"+COUNT_PREFIX + objType +":" + objId , "1")){
                    //查询数据库
                    List<Counter> counters = counterMapper.getCounters(objId , objType, keys);

                    Map<String, Object> map = new HashMap<>();
                    map.put("ttl" , System.currentTimeMillis()+DATABASE_EXPIRE_TIME*60 * 60 * 1000);
                    for (Counter counter : counters){
                        map.put(counter.getCountKey() , counter.getCountValue());
                    }
                    System.out.println(map.toString());
                    //批量在hash中添加数据
                    redisTemplate.opsForHash().putAll(COUNT_PREFIX + objType +":" + objId , map);
                    List list = redisTemplate.opsForHash().multiGet(COUNT_PREFIX + objType + ":" + objId, keys);
                    System.out.println(list.toString());
                    lock.unlock("lock:"+COUNT_PREFIX + objType +":" + objId , "1");
                }else{
                    //获取锁失败的，可以直接返回获取失败稍后再试，或者while循环让他访问数据库
                    //这里使用while获取数据
                    while (true){
                        result = redisTemplate.opsForHash().multiGet(COUNT_PREFIX + objType +":" + objId, keys);
                        if (result!=null){
                            break;
                        }
                    }
                    //返回
                    return new Result<>(200 , "success" , resultCounters(result , objId , keys));
                }
            }else{
                //如果缓存中不存在,查询数据库
                System.out.println("执行到数据库了");
                List<Counter> counters = counterMapper.getCounters(objId , objType, keys);
                if (counters!=null){
                    return new Result<>(200 , "success" , counters);
                }
            }
            return new Result<>(500 , "获取失败" , null);
        }catch (Exception e){
            e.printStackTrace();
            return new Result<>(500 , "获取失败" , null);
        }
    }

    /**
     * 批量写入数据
     * @param objId
     * @param kv
     * @return
     */
    @Override
    public Result<Counter> setCounters(Integer objId , Integer objType, Map<String , Integer> kv) {
        List<String> keys = kv.keySet().stream().collect(Collectors.toList());
        //先查询redis当中是否存在
        List<Integer> results = redisTemplate.opsForHash().multiGet(COUNT_PREFIX + objType +":" + objId, keys);
        if (results!=null){
            //说明存在直接修改
            int i = 0;
            for (Map.Entry<String, Integer> entry : kv.entrySet()) {
                entry.setValue(entry.getValue() + results.get(i));
                i++;
            }
            //修改
            redisTemplate.opsForHash().putAll(COUNT_PREFIX + objType +":" + objId , kv);
            //放入缓冲区
            for (String key : kv.keySet()){
                putBuffer(objId , objType , key , kv.get(key));
            }
        }else{
            //不存在说明需要修改数据库
            List<Counter> list = new ArrayList<>();
            for (String key : keys){
                Counter counter = new Counter();
                counter.setObjId(objId);
                counter.setObjType(objType);
                counter.setCountKey(key);
                counter.setCountValue(kv.get(key));
                list.add(counter);
            }
            counterMapper.setCounters(objType , list);
        }
        return new Result<>(200 ,"success" , null);
    }


    //返回一个新的时间戳
    public long addHoursToTimestamp(long timestamp, long hours) {
        long hoursInMs = hours * 60 * 60 * 1000;
        return timestamp + hoursInMs;
    }

    //拼装数据
    private List<Counter> resultCounters(List<Object> result , Integer objId, List<String> keys){
        List<Counter> counters = new ArrayList<>();
        int i = 0;
        for (Object obj : result){
            if (i==0){
                i++;
                continue;
            }
            Counter counter = new Counter();
            counter.setCountKey(keys.get(i));
            counter.setObjId(objId);
            counter.setCountValue((Integer)obj);
            counters.add(counter);
            i++;
        }
        return counters;
    }


    @Override
    public Result<Counter> setCounterDB(Integer objId, String key, Integer value) {
        return null;
    }
}
