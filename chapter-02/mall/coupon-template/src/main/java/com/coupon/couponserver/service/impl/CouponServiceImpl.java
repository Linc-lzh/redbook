package com.coupon.couponserver.service.impl;

import com.alibaba.fastjson.JSON;
import com.coupon.couponserver.consumer.DelayQueueConsumer;
import com.coupon.couponserver.exception.LeafServerException;
import com.coupon.couponserver.exception.NoKeyException;
import com.coupon.couponserver.generator.CouponCodeGenerator;
import com.coupon.couponserver.mapper.ActivityMapper;
import com.coupon.couponserver.mapper.CouponMapper;
import com.coupon.couponserver.mapper.CouponTemplateMapper;
import com.coupon.couponserver.model.Activity;
import com.coupon.couponserver.model.Coupon;
import com.coupon.couponserver.model.CouponTemplate;
import com.coupon.couponserver.model.DelayedCouponTemplate;
import com.coupon.couponserver.service.CouponService;
import com.coupon.couponserver.service.SnowflakeService;
import com.coupon.couponserver.snowflake.SnowflakeIdGenerator;
import com.sankuai.inf.leaf.common.Result;
import com.sankuai.inf.leaf.common.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CouponServiceImpl implements CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponServiceImpl.class);

    @Resource
    private CouponTemplateMapper couponTemplateMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private CouponMapper couponMapper;

    @Resource
    private KafkaTemplate kafkaTemplate;

    private CouponCodeGenerator couponCodeGenerator;

    @Resource
    private SnowflakeService snowflakeService;

    /**
     * 库存map
     */
    private Map<String , Integer> inventory = new HashMap<>();

    /**
     * 库存分片id列表
     */
    private Map<String , List<String>> shardIds = new HashMap<>();

    //雪花算法
    SnowflakeIdGenerator idGenerator = new SnowflakeIdGenerator(1, 1);

    /**
     * 后台添加优惠券模板
     * @param couponTemplate
     * @return
     */
    @Override
    public int insert(CouponTemplate couponTemplate) {
        try {
            //判断活动是否存在
//            Activity activity = activityMapper.getById(couponTemplate.getActivityId());
//            if (activity==null){
//                //说明活动不存在不允许创建
//                return 0;
//            }

            //后台添加优惠券模板
            couponTemplateMapper.insertCouponTemplate(couponTemplate);
            //查询最大的id，因为是后台，所以没关系
            couponTemplate = couponTemplateMapper.findMaxCouponId();
            System.out.println(couponTemplate.getId());
            //把库存和优惠券信息放入到缓存当中，库存单独存放

            // 设置缓存键值对
            // 假设以优惠券模板的ID作为缓存的键
            String key = "couponTemplate:" + couponTemplate.getId();
            // 将优惠券模板对象转换为JSON字符串
            String value = JSON.toJSONString(couponTemplate);
            // 计算过期时间
            long expiration = calculateExpiration(couponTemplate.getEndTime());
            //加入到redis当中
            redisTemplate.opsForValue().set(key, value, expiration, TimeUnit.MILLISECONDS);
            //把库存单独添加到库存的redis中，进行分片存储
            int result = couponTemplate.getCouponCount() % 1000 == 0 ? 0 : 1;
            initStock(couponTemplate.getId()+"",
                    couponTemplate.getCouponCount()/1000+result, couponTemplate.getCouponCount());
            //有一个后台工具来生成优惠券码，根据couponTemplate的count来生成
            //先生成1/4的优惠券码
            generateCoupons(couponTemplate.getId(), couponTemplate.getCouponCount()/4);
            //直接生成优惠券，根据优惠券模板的结束时间来设置优惠券领取的过期时间
            //异步发送到一个程序上，让这个程序动态生成优惠券码
            return 1;
        }catch (Exception e){
            return 0;
        }

    }

    // 计算过期时间
    private long calculateExpiration(Date endTime) {
        long currentTime = System.currentTimeMillis();
        long expirationTime = endTime.getTime() - currentTime;
        return expirationTime;
    }

    //根据实际需要调整线程池大小
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    /**
     * 预生成优惠券并存储到Redis
     * @param couponTemplateId 优惠券模板id
     * @param count 生成数量
     */
    @Async
    public void generateCoupons(int couponTemplateId, int count) {
        int numThreads = 5; // 可以根据实际需要调整线程数
        int countPerThread = count / numThreads;
        for (int i = 0; i < numThreads; i++) {
            int start = i * countPerThread;
            int end = (i == numThreads - 1) ? count : start + countPerThread;
            executorService.submit(() -> generateAndStoreCoupons(couponTemplateId, start, end));
        }
    }

    /**
     * 获取优惠券码并保存在redis
     * @param couponTemplateId
     * @param start
     * @param end
     */
    private void generateAndStoreCoupons(int couponTemplateId, int start, int end) {
        List<String> coupons = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            // 这里只是一个示例，实际情况下你可能需要使用更复杂的算法来生成优惠券码
            String coupon = get("couponCode" , snowflakeService.getId("couponCode"));
            coupons.add(coupon);
        }

        // 存储到Redis
        redisTemplate.opsForList().rightPushAll("couponCode:"+couponTemplateId, coupons);
    }

    private String get(String key, Result id) {
        Result result;
        if (key == null || key.isEmpty()) {
            throw new NoKeyException();
        }
        result = id;
        if (result.getStatus().equals(Status.EXCEPTION)) {
            throw new LeafServerException(result.toString());
        }
        return String.valueOf(result.getId());
    }

    /**
     * 初始化库存，并进行分片存储
     * @param couponTemplateId 优惠券模板id
     * @param shardNum 分片数量
     * @param totalStock 总库存数量
     */
    @Async
    public void initStock(String couponTemplateId, int shardNum, int totalStock) {
        // 计算每个分片的库存数量
        int shardStock = totalStock / shardNum;
        int remaining = totalStock % shardNum;

        // 创建存储分片ID的列表
        List<String> shardIds = new ArrayList<>();

        // 初始化分片
        for (int i = 0; i < shardNum; i++) {
            // 计算这个分片的库存数量
            int stock = shardStock + (i < remaining ? 1 : 0);

            // 构造这个分片的键
            String shardKey = "couponInventory"+couponTemplateId + ":shard:" + i;

            // 设置库存
            redisTemplate.opsForValue().set(shardKey, String.valueOf(stock));

            // 将分片ID添加到列表
            shardIds.add(shardKey);
        }

        // 将分片ID列表存储到 Redis 列表中
        redisTemplate.opsForList().rightPushAll("couponInventory"+couponTemplateId+":shardIds", shardIds);
    }

    /**
     * 追加库存，并进行分片存储
     * @param couponTemplateId 优惠券模板id
     * @param shardNum 分片数量
     * @param totalStock 总库存数量
     */
    @Async
    public void appendStock(String couponTemplateId, int shardNum, int totalStock) {
        // 计算每个分片的库存数量
        int shardStock = totalStock / shardNum;
        int remaining = totalStock % shardNum;

        // 读取分片ID列表的长度
        Long shardIdsLength = redisTemplate.opsForList().size("couponInventory" + couponTemplateId + ":shardIds");

        // 创建存储分片ID的列表
        List<String> shardIds = new ArrayList<>();

        // 初始化分片
        for (int i = shardIdsLength.intValue(); i < shardNum + shardIdsLength.intValue(); i++) {
            // 计算这个分片的库存数量
            int stock = shardStock + (i < remaining + shardIdsLength.intValue() ? 1 : 0);

            // 构造这个分片的键
            String shardKey = "couponInventory" + couponTemplateId + ":shard:" + i;

            // 设置库存
            redisTemplate.opsForValue().set(shardKey, String.valueOf(stock));

            // 将分片ID添加到列表
            shardIds.add(shardKey);
        }

        // 将分片ID列表存储到 Redis 列表中
        redisTemplate.opsForList().rightPushAll("couponInventory" + couponTemplateId + ":shardIds", shardIds);
    }


    /**
     * 修改优惠券模板
     * @param couponTemplate
     * @return
     */
    @Override
    public int update(CouponTemplate couponTemplate) {

        couponTemplateMapper.update(couponTemplate);

        //判断是修改还是删除优惠券
        if (couponTemplate.getAvailable()==1){
            //说明是删除

            // 假设以优惠券模板的ID作为缓存的键
            String key = "couponTemplate:" + couponTemplate.getId();
            //删除缓存
            redisTemplate.delete(key);
            //同时删除redis中的优惠券码
            redisTemplate.delete("couponCode:"+couponTemplate.getId());
            //删除库存
            //查询所有id列表
            List<String> range = redisTemplate.opsForList().
                    range("couponInventory" + couponTemplate.getId() + ":shardIds"
                            , 0, -1);
            //批量删除缓存分片
            redisTemplate.delete(range);
            //删除缓存列表
            redisTemplate.delete("couponInventory" + couponTemplate.getId() + ":shardIds");
            return 1;
        }
        //增加优惠券数量
        if (couponTemplate.getCouponCount()>0){
            //说明是增加优惠券数量
            appendStock(couponTemplate.getId()+"",
                    couponTemplate.getCouponCount()/1000, couponTemplate.getCouponCount());
            return 1;
        }
        //一个活动绑定了3个优惠券模板
        //查询3个模板，通过优惠券金额*优惠券的数量 查询活动跟活动的总金额进行比较
        //如果大于活动金额了，不允许追加库存，如果小于并且+追加的库存不大于活动的金额
        //允许你生成

        return 0;
    }

    @Override
    public int delete(int id) {
        return 0;
    }

    @Override
    public CouponTemplate findById(int id) {
        return null;
    }

    @Override
    public List<CouponTemplate> findAll() {
        return null;
    }

    /**
     * 查询所有用户的优惠券
     * @param userId
     * @return
     */
    @Override
    public List<Coupon> findAll(int userId , int type) {
        //查询用户所有优惠券
        if (type==0){
            //查询所有可用的优惠券
            Set<Coupon> set = redisTemplate.opsForZSet().rangeWithScores("user_coupon:" + userId, 0, -1);
            if (set==null || set.size()<=0){
                Coupon coupon = new Coupon();
                coupon.setUserId(userId);
                coupon.setStatus(0);
                //查询数据库
                List<Coupon> all = couponMapper.all(coupon);
                Set<ZSetOperations.TypedTuple<Coupon>> tuples = new HashSet<>();
                for (Coupon coupon1 : all) {
                    double score = coupon.getAssignTime().getTime();  // 使用 assignTime 字段作为分数
                    tuples.add(new DefaultTypedTuple<>(coupon1, score));
                }

                redisTemplate.opsForZSet().add("user_coupon:" + userId, tuples);
            }
            //返回list
            List<Coupon> couponList = new ArrayList<>(set);
            return couponList;
        }else {
            //查询所有不可用的优惠券
            //包括已使用，过期，未使用
            Set<Coupon> set = redisTemplate.opsForZSet().rangeWithScores("user_un_coupon:" + userId, 0, -1);
            if (set==null || set.size()<=0){
                Coupon coupon = new Coupon();
                coupon.setUserId(userId);
                coupon.setStatus(1);
                //查询数据库
                List<Coupon> all = couponMapper.all(coupon);
                Set<ZSetOperations.TypedTuple<Coupon>> tuples = new HashSet<>();
                for (Coupon coupon1 : all) {
                    double score = coupon.getAssignTime().getTime();  // 使用 assignTime 字段作为分数
                    tuples.add(new DefaultTypedTuple<>(coupon1, score));
                }

                redisTemplate.opsForZSet().add("user_un_coupon:" + userId, tuples);
            }
            //返回list
            List<Coupon> couponList = new ArrayList<>(set);
            return couponList;
        }
    }

    /**
     * 查询匹配的优惠券
     * @param userId
     * @param type
     * @param amount
     * @param brandId
     * @param merchantId
     * @param commodityId
     * @return
     */
    @Override
    public List<Coupon> findAll(int userId , int type , BigDecimal amount
            , int brandId , int merchantId , int commodityId) {
        Set<Coupon> set = redisTemplate.opsForZSet().rangeWithScores("user_coupon:" + userId, 0, -1);
        List<Coupon> all;
        if (set==null || set.size()<=0){
            Coupon coupon = new Coupon();
            coupon.setUserId(userId);
            coupon.setStatus(0);
            //查询数据库
            all = couponMapper.all(coupon);
            Set<ZSetOperations.TypedTuple<Coupon>> tuples = new HashSet<>();
            for (Coupon coupon1 : all) {
                double score = coupon.getAssignTime().getTime();  // 使用 assignTime 字段作为分数
                tuples.add(new DefaultTypedTuple<>(coupon1, score));
            }
            redisTemplate.opsForZSet().add("user_coupon:" + userId, tuples);
        }else{
            //set不为空
            all = new ArrayList<>(set);
        }
        //循环判断和brandId，merchantId，commodityId相同的
        List<Coupon> matchedCoupons = new ArrayList<>();
        for (Coupon coupon : all) {
            int objId = coupon.getCouponTemplate().getObjId();
            //比较objId
            if (objId == brandId || objId == merchantId || objId == commodityId) {

                //如果objId匹配上了，就需要匹配额度是否可以用
                if (amount.compareTo(coupon.getCouponTemplate().getMinAmount())>=0){
                    //判断maxAmount是否有值
                    if (coupon.getCouponTemplate().
                            getMaxAmount().compareTo(BigDecimal.ZERO) == 0){
                        //如果为0直接保存
                        matchedCoupons.add(coupon);
                    }else{
                        //反之就判断是否小于最大值
                        if (amount.compareTo(coupon.getCouponTemplate().getMaxAmount())<=0){
                            matchedCoupons.add(coupon);
                        }
                    }
                }

            }
        }
        return matchedCoupons;
    }

    /**
     * 领取优惠券方法
     * @param coupon 要领取的优惠券对象
     */
    @Override
    public void receive(Coupon coupon) {
        // 构造关键的Redis键名
        final String inventoryKey = "couponInventory" + coupon.getTemplateId() + ":shardIds";
        final String templateKey = "couponTemplate:" + coupon.getTemplateId();
        final String couponCodeKey = "couponCode:" + coupon.getTemplateId();
        final String userCouponKey = "user_coupon:" + coupon.getUserId();

        // 获取分片ID列表
        List<String> shardIdList = getShardIdList(inventoryKey);
        // 对列表进行随机排序，以确保公平性
        Collections.shuffle(shardIdList);

        boolean isCouponReceived = false;
        for (String shardId : shardIdList) {
            // 尝试减少库存
            Long remainingStock = decrementStock(shardId);
            if (remainingStock == null || remainingStock < 0) {
                // 处理库存不足的情况
                handleOutOfStock(inventoryKey, shardId);
                continue;
            }

            // 检查是否需要启动令牌桶算法
            if (shouldStartTokenBucket(remainingStock, shardIdList, templateKey)) {
                couponCodeGenerator = new CouponCodeGenerator(100);
            }

            isCouponReceived = true;
            break;
        }

        if (isCouponReceived) {
            // 处理优惠券领取逻辑
            processCouponReceiving(coupon, couponCodeKey, templateKey, userCouponKey);
        }
    }

    /**
     * 从Redis获取分片ID列表
     * @param inventoryKey Redis中存储分片ID的键名
     * @return 分片ID列表
     */
    private List<String> getShardIdList(String inventoryKey) {
        List<String> list = shardIds.get(inventoryKey);
        // 如果本地没有缓存，则从Redis中获取
        if (list == null) {
            list = redisTemplate.opsForList().range(inventoryKey, 0, -1);
            shardIds.put(inventoryKey, list);
            log.info("从Redis获取分片ID列表，键名：{}", inventoryKey);
        }
        return list;
    }

    /**
     * 减少指定分片ID的库存
     * @param shardId 分片ID
     * @return 减少后的库存数量
     */
    private Long decrementStock(String shardId) {
        try {
            return redisTemplate.opsForValue().decrement(shardId, 1);
        } catch (Exception e) {
            log.error("减少库存时出错，分片ID：{}", shardId, e);
            return null;
        }
    }

    /**
     * 处理库存不足的情况
     * @param inventoryKey Redis中存储分片ID的键名
     * @param shardId 分片ID
     */
    private void handleOutOfStock(String inventoryKey, String shardId) {
        shardIds.remove(inventoryKey, shardId);
        redisTemplate.opsForList().remove(inventoryKey, 0, shardId);
        log.warn("库存不足，移除分片ID：{}，键名：{}", shardId, inventoryKey);
    }

    /**
     * 判断是否需要启动令牌桶算法
     * @param remainingStock 剩余库存
     * @param shardIdList 分片ID列表
     * @param templateKey 优惠券模板键名
     * @return 是否启动令牌桶算法
     */
    private boolean shouldStartTokenBucket(Long remainingStock, List<String> shardIdList, String templateKey) {
        Integer couponCount = getCouponCount(templateKey);
        // 根据库存量和优惠券总量判断是否启动令牌桶
        return couponCount != null && remainingStock * shardIdList.size() < couponCount / 4;
    }

    /**
     * 获取优惠券的总数
     * @param templateKey 优惠券模板键名
     * @return 优惠券总数
     */
    private Integer getCouponCount(String templateKey) {
        CouponTemplate couponTemplate = (CouponTemplate) redisTemplate.opsForValue().get(templateKey);
        return couponTemplate != null ? couponTemplate.getCouponCount() : null;
    }

    /**
     * 处理优惠券领取的具体逻辑
     * @param coupon 优惠券对象
     * @param couponCodeKey 优惠券码的键名
     * @param templateKey 优惠券模板键名
     * @param userCouponKey 用户优惠券的键名
     */
    private void processCouponReceiving(Coupon coupon, String couponCodeKey, String templateKey, String userCouponKey) {
        String code = (String) redisTemplate.opsForList().leftPop(couponCodeKey);
        // 如果能够获取到优惠券码
        if (code != null) {
            // 设置优惠券详情
            setCouponDetails(coupon, code, templateKey);
            // 将优惠券添加到用户的优惠券集合中
            redisTemplate.opsForZSet().add(userCouponKey, coupon, System.currentTimeMillis());
            // 异步发送到Kafka
            kafkaTemplate.send("coupon-topic", coupon);
            log.info("用户{}领取优惠券成功", coupon.getUserId());
        } else {
            log.warn("优惠券代码不可用，模板键：{}", templateKey);
        }
    }

    /**
     * 设置优惠券的详细信息
     * @param coupon 优惠券对象
     * @param code 优惠券码
     * @param templateKey 优惠券模板键名
     */
    private void setCouponDetails(Coupon coupon, String code, String templateKey) {
        coupon.setCouponCode(code);
        coupon.setStatus(0);
        CouponTemplate couponTemplate = (CouponTemplate) redisTemplate.opsForValue().get(templateKey);
        coupon.setCouponTemplate(couponTemplate);
    }

    // 消费者监听方法
    @KafkaListener(topics = "coupon-topic" , groupId = "my-consumer-group")
    public void receiveCoupon(Coupon coupon) {
        try {
            // 调用数据库操作的方法，将数据添加到数据库中
            List<Coupon> coupons = new ArrayList<>();
            coupons.add(coupon);
            couponMapper.receive(coupons);
        } catch (Exception e) {
            System.out.println("Failed to receive coupon: " + e.getMessage());
        }
    }


    /**
     * 领取优惠券
     * @param coupons
     */
    @Override
    public void receives(List<Coupon> coupons) {
        //批量领取优惠券需要给每个优惠券都加上锁
        for (Coupon coupon : coupons){
            //查询本地
            List<String> list = shardIds.get("couponInventory"+coupon.getTemplateId()+":shardIds");
            if (list==null){
                //先扣减库存
                list = redisTemplate.opsForList().range("couponInventory"+coupon.getTemplateId()+":shardIds", 0, -1);
                shardIds.put("couponInventory"+coupon.getTemplateId()+":shardIds" , list);
            }
            //随机排序
            Collections.shuffle(list);
            //循环遍历优惠券列表
            boolean b = false;
            for (String randomItem : list){
                Long remainingStock = redisTemplate.opsForValue().decrement(randomItem, 1);
                if (remainingStock<0){
                    //从本地删除
                    shardIds.remove("couponInventory"+coupon.getTemplateId()+":shardIds" , randomItem);
                    //说明库存已经没有了，从redis中删除这个数据继续循环就行了
                    redisTemplate.opsForList().remove("couponInventory"+coupon.getTemplateId()+":shardIds", 0, randomItem);
                    continue;
                }else{
                    //说明还有库存直接直接跳出循环就行
                    //先查询本地库存
                    Integer couponCount = inventory.get("couponTemplate:" + coupon.getTemplateId());
                    if (couponCount==null){
                        CouponTemplate couponTemplate = (CouponTemplate)
                                redisTemplate.opsForValue().get("couponTemplate:" + coupon.getTemplateId());
                        couponCount = couponTemplate.getCouponCount();
                        //保存到本地
                        inventory.put("couponTemplate:" + coupon.getTemplateId()
                                , couponTemplate.getCouponCount());
                    }
                    //判断库存
                    if (remainingStock*list.size() < couponCount / 4){
                        //判断库存总数是否小于总数的四分之一，如果小于了直接开启令牌桶
                        couponCodeGenerator = new CouponCodeGenerator(100);
                    }
                    b = true;
                    break;
                }

            }
            //扣减库存之后领取优惠券码
            if (b){
                //领取优惠券需要从redis中先领取优惠券码
                String code = (String)redisTemplate.opsForList()
                        .leftPop("couponCode:" + coupon.getTemplateId());
                if (code!=null){
                    long timestamp = System.currentTimeMillis();
                    coupon.setCouponCode(code);
                    //添加到redis中
                    redisTemplate.opsForZSet().add("user_coupon:"+coupon.getUserId(), coupon, timestamp);
                    //异步添加到数据库当中
                    kafkaTemplate.send("coupon-topic", coupon);
                }
            }
        }
    }

    @Override
    public List<CouponTemplate> getExpiringTemplates() {
        // 查询所有的优惠券模板
        List<CouponTemplate> allTemplates = couponTemplateMapper.findAll();// 查询所有优惠券模板的逻辑

        // 获取当前时间
        Date now = new Date();

        // 筛选出即将过期的模板
        List<CouponTemplate> expiringTemplates = allTemplates.stream()
                .filter(template -> template.getEndTime().getTime() - now.getTime() <= 24 * 60 * 60 * 1000) // 过期时间距离当前时间不到一天
                .collect(Collectors.toList());

        return expiringTemplates;
    }

    @Autowired
    private DelayQueueConsumer delayQueueConsumer;

    @Resource
    private DelayQueue<DelayedCouponTemplate> delayQueue;

    @PostConstruct
    public void init() {
        Thread consumerThread = new Thread(() -> {
            while (true) {
                try {
                    DelayedCouponTemplate delayedTemplate = delayQueue.take(); // 阻塞等待获取到期的模板
                    delayQueueConsumer.consume(delayedTemplate); // 执行消费逻辑
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    // 处理中断异常
                }
            }
        });
        consumerThread.start();
    }

}
